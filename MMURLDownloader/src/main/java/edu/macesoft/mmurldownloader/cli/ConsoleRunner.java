package edu.macesoft.mmurldownloader.cli;

import edu.macesoft.mmurldownloader.engine.DownloadRequest;
import edu.macesoft.mmurldownloader.engine.DownloadResult;
import edu.macesoft.mmurldownloader.engine.Downloader;
import edu.macesoft.mmurldownloader.http.DirectoryParser;
import edu.macesoft.mmurldownloader.http.RemoteFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Interactive console interface for MMURLDownloader v2.0.
 *
 * <p>Interaction flow:
 * <ol>
 *   <li>Prompt for source URL</li>
 *   <li>Prompt for destination directory</li>
 *   <li>Fetch URL and attempt to parse as HTTP directory listing</li>
 *   <li>If listing found: display files sorted by extension then name, accept selection</li>
 *   <li>Confirm and download with a live progress bar per file</li>
 *   <li>Print session summary</li>
 * </ol>
 */
public class ConsoleRunner {

    private static final String SEPARATOR = "─".repeat(60);

    private final HttpClient httpClient;
    private final Downloader downloader;
    private final DirectoryParser directoryParser;
    private final FileSelector fileSelector;
    private final BufferedReader console;

    private enum OverwritePolicy { ASK, OVERWRITE_ALL, SKIP_ALL }
    private OverwritePolicy overwritePolicy = OverwritePolicy.ASK;

    public ConsoleRunner() {
        this.httpClient = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.NORMAL)
            .connectTimeout(Duration.ofSeconds(15))
            .build();
        this.downloader      = new Downloader(httpClient);
        this.directoryParser = new DirectoryParser(httpClient);
        this.console         = new BufferedReader(new InputStreamReader(System.in));
        this.fileSelector    = new FileSelector(console);
    }

    public void run() {
        printHeader();
        try {
            URI sourceUri  = promptSourceUri();
            Path destDir   = promptDestinationDirectory();

            Optional<List<RemoteFile>> listing = directoryParser.parse(sourceUri);

            List<DownloadRequest> requests;

            if (listing.isEmpty()) {
                // Non-HTML response: direct file download
                String filename = extractFilename(sourceUri);
                if (filename.isEmpty()) {
                    System.err.println("\n  Cannot determine filename from URL.");
                    System.err.println("  Please provide a direct file URL (e.g. http://host/path/file.zip).");
                    return;
                }
                requests = List.of(new DownloadRequest(sourceUri, destDir.resolve(filename)));
            } else {
                List<RemoteFile> availableFiles = listing.get();
                if (availableFiles.isEmpty()) {
                    System.err.println("\n  No downloadable files found at this URL.");
                    System.err.println("  The URL returned an HTML page but no directory listing was detected.");
                    return;
                }
                System.out.printf("%n  Found %d file(s) available for download.%n%n",
                    availableFiles.size());
                List<RemoteFile> sorted   = sortByExtensionThenName(availableFiles);
                List<RemoteFile> selected = fileSelector.selectFrom(sorted);
                if (selected.isEmpty()) {
                    System.out.println("\n  No files selected. Exiting.");
                    return;
                }
                requests = buildRequests(selected, destDir);
            }

            if (!confirmDownload(requests)) {
                System.out.println("\n  Download cancelled.");
                return;
            }

            System.out.println();
            List<DownloadResult> results = downloadAll(requests);
            printSummary(results);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("\n  Download interrupted.");
        } catch (Exception e) {
            System.err.println("\n  Error: " + e.getMessage());
        }
    }

    // ─── Sorting ──────────────────────────────────────────────────────────────

    private List<RemoteFile> sortByExtensionThenName(List<RemoteFile> files) {
        return files.stream()
            .sorted(Comparator
                .comparing((RemoteFile f) -> extension(f.name()).toLowerCase())
                .thenComparing(f -> f.name().toLowerCase()))
            .toList();
    }

    private String extension(String filename) {
        int dot = filename.lastIndexOf('.');
        return dot >= 0 ? filename.substring(dot + 1) : "";
    }

    // ─── Request builder ──────────────────────────────────────────────────────

    private List<DownloadRequest> buildRequests(List<RemoteFile> files, Path destDir) {
        return files.stream()
            .map(f -> new DownloadRequest(f.uri(), destDir.resolve(f.name())))
            .toList();
    }

    // ─── Download loop ────────────────────────────────────────────────────────

    private List<DownloadResult> downloadAll(List<DownloadRequest> requests) throws InterruptedException {
        List<DownloadResult> results = new ArrayList<>();

        for (DownloadRequest request : requests) {
            String filename = request.destinationFile().getFileName().toString();

            if (Files.exists(request.destinationFile()) && !resolveOverwrite(filename)) {
                System.out.printf("  Skipped: %s%n", filename);
                results.add(DownloadResult.skipped(filename));
                continue;
            }

            ProgressBar bar = new ProgressBar(filename);
            try {
                DownloadResult result = downloader.download(request, bar::update);
                bar.complete();
                results.add(result);
            } catch (IOException e) {
                bar.complete();
                System.err.printf("  Failed : %s — %s%n", filename, e.getMessage());
                results.add(DownloadResult.failure(filename, e.getMessage()));
            }
        }

        return results;
    }

    // ─── Overwrite policy ─────────────────────────────────────────────────────

    private boolean resolveOverwrite(String filename) {
        return switch (overwritePolicy) {
            case OVERWRITE_ALL -> true;
            case SKIP_ALL      -> false;
            case ASK           -> askOverwrite(filename);
        };
    }

    private boolean askOverwrite(String filename) {
        System.out.printf("%n  '%s' already exists.%n", filename);
        System.out.print("  [O]verwrite  [S]kip  [A]ll overwrite  s[K]ip all : ");
        try {
            return switch (console.readLine().trim().toUpperCase()) {
                case "A" -> { overwritePolicy = OverwritePolicy.OVERWRITE_ALL; yield true; }
                case "K" -> { overwritePolicy = OverwritePolicy.SKIP_ALL;      yield false; }
                case "O" -> true;
                default  -> false;
            };
        } catch (IOException e) {
            return false;
        }
    }

    // ─── Prompts ─────────────────────────────────────────────────────────────

    private URI promptSourceUri() throws IOException {
        System.out.print("  Source URL        : ");
        return URI.create(console.readLine().trim());
    }

    private Path promptDestinationDirectory() throws IOException {
        System.out.print("  Destination dir   : ");
        return Path.of(console.readLine().trim());
    }

    private boolean confirmDownload(List<DownloadRequest> requests) throws IOException {
        System.out.println();
        System.out.println(SEPARATOR);
        if (requests.size() == 1) {
            System.out.printf("  From : %s%n", requests.get(0).sourceUri());
            System.out.printf("  To   : %s%n", requests.get(0).destinationFile());
        } else {
            System.out.printf("  %d file(s)  →  %s%n",
                requests.size(),
                requests.get(0).destinationFile().getParent());
        }
        System.out.println(SEPARATOR);
        System.out.print("  Proceed? (Y/N)    : ");
        return console.readLine().trim().equalsIgnoreCase("Y");
    }

    // ─── Summary ─────────────────────────────────────────────────────────────

    private void printSummary(List<DownloadResult> results) {
        long succeeded  = results.stream().filter(DownloadResult::success).count();
        long skipped    = results.stream().filter(DownloadResult::skipped).count();
        long failed     = results.stream().filter(r -> !r.success() && !r.skipped()).count();
        long totalBytes = results.stream().mapToLong(DownloadResult::bytesWritten).sum();
        long totalTime  = results.stream().mapToLong(DownloadResult::durationMillis).sum();

        System.out.println();
        System.out.println(SEPARATOR);
        System.out.printf("  Downloaded : %d file(s)%n", succeeded);
        if (skipped > 0) System.out.printf("  Skipped    : %d file(s)%n", skipped);
        if (failed  > 0) System.out.printf("  Failed     : %d file(s)%n", failed);
        System.out.printf("  Total size : %s%n", ProgressBar.formatBytes(totalBytes));
        System.out.printf("  Time       : %.1f s%n", totalTime / 1000.0);
        System.out.println(SEPARATOR);

        if (failed > 0) {
            System.out.println();
            System.out.println("  Failed files:");
            results.stream()
                .filter(r -> !r.success() && !r.skipped())
                .forEach(r -> System.out.printf("    - %s: %s%n", r.filename(), r.errorMessage()));
        }
        System.out.println();
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private String extractFilename(URI uri) {
        String path = uri.getPath();
        int lastSlash = path.lastIndexOf('/');
        return lastSlash >= 0 ? path.substring(lastSlash + 1) : path;
    }

    private void printHeader() {
        System.out.println();
        System.out.println("  MMURLDownloader v2.0");
        System.out.println(SEPARATOR);
        System.out.println();
    }
}

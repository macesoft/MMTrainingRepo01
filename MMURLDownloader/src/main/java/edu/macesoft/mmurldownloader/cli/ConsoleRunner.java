package edu.macesoft.mmurldownloader.cli;

import edu.macesoft.mmurldownloader.engine.DownloadRequest;
import edu.macesoft.mmurldownloader.engine.Downloader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.nio.file.Path;

/**
 * Interactive console interface for MMURLDownloader.
 * Prompts the user for a source URL and destination directory,
 * confirms the operation, and delegates to {@link Downloader}.
 */
public class ConsoleRunner {

    private static final String SEPARATOR = "─".repeat(40);

    private final Downloader downloader;
    private final BufferedReader console;

    public ConsoleRunner() {
        this.downloader = new Downloader();
        this.console = new BufferedReader(new InputStreamReader(System.in));
    }

    public void run() {
        printHeader();
        try {
            URI sourceUri = promptSourceUri();
            Path destinationFile = promptDestinationDirectory(sourceUri);

            if (!confirmDownload(sourceUri, destinationFile)) {
                System.out.println("\nDownload cancelled.");
                return;
            }

            System.out.println("\nDownloading...");
            long bytesWritten = downloader.download(new DownloadRequest(sourceUri, destinationFile));
            System.out.printf("Done. %,d bytes written to: %s%n", bytesWritten, destinationFile);

        } catch (IllegalArgumentException e) {
            System.err.println("\nInvalid input: " + e.getMessage());
        } catch (IOException e) {
            System.err.println("\nDownload failed: " + e.getMessage());
        }
    }

    private URI promptSourceUri() throws IOException {
        System.out.print("  Source URL        : ");
        return URI.create(console.readLine().trim());
    }

    private Path promptDestinationDirectory(URI sourceUri) throws IOException {
        String fileName = Path.of(sourceUri.getPath()).getFileName().toString();
        System.out.print("  Destination dir   : ");
        String directory = console.readLine().trim();
        return Path.of(directory).resolve(fileName);
    }

    private boolean confirmDownload(URI sourceUri, Path destinationFile) throws IOException {
        System.out.println("\n" + SEPARATOR);
        System.out.println("  From : " + sourceUri);
        System.out.println("  To   : " + destinationFile);
        System.out.println(SEPARATOR);
        System.out.print("  Proceed? (Y/N)    : ");
        return console.readLine().trim().equalsIgnoreCase("Y");
    }

    private void printHeader() {
        System.out.println();
        System.out.println("  MMURLDownloader v2.0");
        System.out.println(SEPARATOR);
        System.out.println();
    }
}

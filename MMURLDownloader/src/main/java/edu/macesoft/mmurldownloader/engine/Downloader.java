package edu.macesoft.mmurldownloader.engine;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.function.BiConsumer;

/**
 * Downloads a single file from a URI to a local path.
 *
 * <p>Uses {@link HttpClient} for full HTTP support (redirects, timeouts).
 * Reports progress through a callback receiving (bytesRead, totalBytes),
 * where totalBytes is -1 if the server did not provide a Content-Length header.
 */
public class Downloader {

    private static final int BUFFER_SIZE = 16 * 1024; // 16 KB

    private final HttpClient httpClient;

    public Downloader(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Downloads the resource described by {@code request}.
     *
     * @param onProgress callback invoked after each chunk: (bytesRead, totalBytes)
     */
    public DownloadResult download(DownloadRequest request, BiConsumer<Long, Long> onProgress)
            throws IOException, InterruptedException {

        long startTime = System.currentTimeMillis();
        String filename = request.destinationFile().getFileName().toString();

        HttpRequest httpRequest = HttpRequest.newBuilder(request.sourceUri()).GET().build();
        HttpResponse<InputStream> response = httpClient.send(
            httpRequest, HttpResponse.BodyHandlers.ofInputStream());

        long totalBytes = response.headers().firstValueAsLong("content-length").orElse(-1);

        try (InputStream input = response.body();
             BufferedOutputStream output = new BufferedOutputStream(
                 Files.newOutputStream(request.destinationFile(),
                     StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING))) {

            byte[] buffer = new byte[BUFFER_SIZE];
            long totalRead = 0;
            int bytesRead;

            while ((bytesRead = input.read(buffer)) != -1) {
                output.write(buffer, 0, bytesRead);
                totalRead += bytesRead;
                onProgress.accept(totalRead, totalBytes);
            }

            long duration = System.currentTimeMillis() - startTime;
            return DownloadResult.success(filename, totalRead, duration);
        }
    }
}

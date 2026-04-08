package edu.macesoft.mmurldownloader.engine;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Downloads the resource at a given URI to a local file.
 * Stream lifecycle is managed internally via try-with-resources.
 */
public class Downloader {

    public long download(DownloadRequest request) throws IOException {
        try (InputStream inputStream = request.sourceUri().toURL().openStream()) {
            return Files.copy(inputStream, request.destinationFile(), StandardCopyOption.REPLACE_EXISTING);
        }
    }
}

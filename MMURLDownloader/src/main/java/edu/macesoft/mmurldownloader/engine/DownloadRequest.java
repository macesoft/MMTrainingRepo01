package edu.macesoft.mmurldownloader.engine;

import java.net.URI;
import java.nio.file.Path;

/**
 * Immutable value object representing a download request:
 * a source URI and a destination file path.
 */
public record DownloadRequest(URI sourceUri, Path destinationFile) {

    public DownloadRequest {
        if (sourceUri == null) {
            throw new IllegalArgumentException("Source URI must not be null");
        }
        if (destinationFile == null) {
            throw new IllegalArgumentException("Destination file must not be null");
        }
    }
}

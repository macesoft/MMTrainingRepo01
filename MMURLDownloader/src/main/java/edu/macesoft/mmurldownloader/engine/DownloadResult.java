package edu.macesoft.mmurldownloader.engine;

/**
 * The outcome of a single file download operation.
 * Covers three states: success, failure, and skipped (user chose not to overwrite).
 */
public record DownloadResult(
    String filename,
    boolean success,
    boolean skipped,
    long bytesWritten,
    long durationMillis,
    String errorMessage
) {

    public static DownloadResult success(String filename, long bytesWritten, long durationMillis) {
        return new DownloadResult(filename, true, false, bytesWritten, durationMillis, null);
    }

    public static DownloadResult skipped(String filename) {
        return new DownloadResult(filename, false, true, 0, 0, null);
    }

    public static DownloadResult failure(String filename, String errorMessage) {
        return new DownloadResult(filename, false, false, 0, 0, errorMessage);
    }
}

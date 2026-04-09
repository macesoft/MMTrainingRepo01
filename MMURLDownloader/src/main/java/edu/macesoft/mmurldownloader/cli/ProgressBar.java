package edu.macesoft.mmurldownloader.cli;

/**
 * Renders a single-line download progress bar to stdout.
 *
 * <p>Uses carriage return (\r) to overwrite the current line on each update,
 * giving the appearance of an animated progress indicator.
 *
 * <p>When Content-Length is known (totalBytes > 0), shows a filled bar with
 * percentage and total size. Otherwise, shows bytes downloaded and speed only.
 */
public class ProgressBar {

    private static final int BAR_WIDTH = 25;
    private static final int NAME_WIDTH = 22;

    private final String displayName;
    private final long startTimeMs;

    public ProgressBar(String filename) {
        this.displayName = truncate(filename, NAME_WIDTH);
        this.startTimeMs = System.currentTimeMillis();
    }

    public void update(long bytesRead, long totalBytes) {
        long elapsed = System.currentTimeMillis() - startTimeMs;
        long bytesPerSecond = elapsed > 0 ? bytesRead * 1000L / elapsed : 0;

        if (totalBytes > 0) {
            int percent = (int) Math.min(bytesRead * 100L / totalBytes, 100);
            System.out.printf("\r  %-" + NAME_WIDTH + "s %s %3d%% | %-9s / %-9s | %s/s   ",
                displayName,
                buildBar(bytesRead, totalBytes),
                percent,
                formatBytes(bytesRead),
                formatBytes(totalBytes),
                formatBytes(bytesPerSecond));
        } else {
            System.out.printf("\r  %-" + NAME_WIDTH + "s  %-9s | %s/s   ",
                displayName,
                formatBytes(bytesRead),
                formatBytes(bytesPerSecond));
        }
        System.out.flush();
    }

    public void complete() {
        System.out.println();
    }

    private String buildBar(long bytesRead, long totalBytes) {
        int filled = (int) Math.min(bytesRead * BAR_WIDTH / totalBytes, BAR_WIDTH);
        return "[" + "█".repeat(filled) + "░".repeat(BAR_WIDTH - filled) + "]";
    }

    private static String truncate(String name, int maxLength) {
        if (name.length() <= maxLength) return name;
        return name.substring(0, maxLength - 3) + "...";
    }

    public static String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024L * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
    }
}

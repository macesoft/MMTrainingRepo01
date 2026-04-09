package edu.macesoft.mmurldownloader.cli;

import edu.macesoft.mmurldownloader.http.RemoteFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Displays a numbered list of remote files and parses the user's selection.
 *
 * <p>Supported selection formats:
 * <ul>
 *   <li>{@code *}     — all files</li>
 *   <li>{@code 1}     — single file</li>
 *   <li>{@code 1,3,5} — multiple files</li>
 *   <li>{@code 2-5}   — range (inclusive)</li>
 *   <li>{@code 1,3-5} — combined</li>
 * </ul>
 */
public class FileSelector {

    private static final String SEPARATOR = "─".repeat(60);
    private static final int INDEX_WIDTH = 3;
    private static final int NAME_WIDTH  = 48;

    private final BufferedReader console;

    public FileSelector(BufferedReader console) {
        this.console = console;
    }

    public List<RemoteFile> selectFrom(List<RemoteFile> files) throws IOException {
        printListing(files);
        return promptSelection(files);
    }

    private void printListing(List<RemoteFile> files) {
        System.out.println(SEPARATOR);
        System.out.printf("  %-" + INDEX_WIDTH + "s  %-" + NAME_WIDTH + "s%n", "#", "Name");
        System.out.println(SEPARATOR);
        for (int i = 0; i < files.size(); i++) {
            System.out.printf("  %-" + INDEX_WIDTH + "d  %s%n", i + 1, files.get(i).name());
        }
        System.out.println(SEPARATOR);
        System.out.println();
    }

    private List<RemoteFile> promptSelection(List<RemoteFile> files) throws IOException {
        System.out.println("  Select: [*] all  [1] single  [1,3] multiple  [2-5] range");
        System.out.print("  > ");
        String input = console.readLine().trim();

        if (input.equals("*")) {
            return new ArrayList<>(files);
        }

        Set<Integer> indices = parseIndices(input, files.size());
        List<RemoteFile> selected = new ArrayList<>();
        for (int index : indices) {
            selected.add(files.get(index - 1));
        }
        return selected;
    }

    private Set<Integer> parseIndices(String input, int maxIndex) {
        Set<Integer> result = new LinkedHashSet<>();
        for (String token : input.split(",")) {
            token = token.trim();
            if (token.contains("-")) {
                String[] parts = token.split("-", 2);
                int from = Integer.parseInt(parts[0].trim());
                int to   = Integer.parseInt(parts[1].trim());
                for (int i = from; i <= to; i++) {
                    if (i >= 1 && i <= maxIndex) result.add(i);
                }
            } else if (!token.isEmpty()) {
                int idx = Integer.parseInt(token);
                if (idx >= 1 && idx <= maxIndex) result.add(idx);
            }
        }
        return result;
    }
}

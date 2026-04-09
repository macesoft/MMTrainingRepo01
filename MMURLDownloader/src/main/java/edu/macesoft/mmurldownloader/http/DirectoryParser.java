package edu.macesoft.mmurldownloader.http;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parses HTTP directory index pages (Apache/Nginx autoindex format)
 * to extract a list of downloadable files.
 *
 * <p>Returns an {@code Optional} to distinguish two cases:
 * <ul>
 *   <li>{@code Optional.empty()} — the response is not HTML; treat the URL as a direct file download</li>
 *   <li>{@code Optional.of(files)} — the response is HTML; {@code files} may be empty if no
 *       downloadable links were found (e.g. a regular webpage, not a directory listing)</li>
 * </ul>
 */
public class DirectoryParser {

    private static final Pattern LINK_PATTERN = Pattern.compile(
        "<a\\s[^>]*href=\"([^\"]+)\"[^>]*>([^<]+)</a>",
        Pattern.CASE_INSENSITIVE
    );

    private final HttpClient httpClient;

    public DirectoryParser(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * Fetches the URI and attempts to parse it as an HTTP directory listing.
     *
     * @return {@code Optional.empty()} if the URL points to a direct (non-HTML) file;
     *         {@code Optional.of(files)} if the URL returned HTML, with the list of
     *         discovered downloadable files (possibly empty)
     */
    public Optional<List<RemoteFile>> parse(URI uri) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder(uri).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        String contentType = response.headers().firstValue("content-type").orElse("");
        if (!contentType.contains("text/html")) {
            return Optional.empty();
        }

        return Optional.of(extractFileLinks(uri, response.body()));
    }

    private List<RemoteFile> extractFileLinks(URI base, String html) {
        List<RemoteFile> files = new ArrayList<>();
        Matcher matcher = LINK_PATTERN.matcher(html);

        while (matcher.find()) {
            String href = matcher.group(1).trim();
            String label = matcher.group(2).trim();

            if (shouldSkip(href, label)) {
                continue;
            }

            URI fileUri = base.resolve(href);
            String fileName = href.contains("/")
                ? href.substring(href.lastIndexOf('/') + 1)
                : href;

            files.add(new RemoteFile(fileName, fileUri));
        }

        return files;
    }

    private boolean shouldSkip(String href, String label) {
        return href.startsWith("?")
            || href.startsWith("/")
            || href.startsWith("..")
            || href.contains("://")
            || href.endsWith("/")
            || label.equalsIgnoreCase("Parent Directory")
            || label.equalsIgnoreCase("Name")
            || label.equalsIgnoreCase("Last modified")
            || label.equalsIgnoreCase("Size")
            || label.equalsIgnoreCase("Description");
    }
}

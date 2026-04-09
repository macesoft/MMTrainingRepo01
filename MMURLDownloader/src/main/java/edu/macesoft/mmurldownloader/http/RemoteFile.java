package edu.macesoft.mmurldownloader.http;

import java.net.URI;

/**
 * Represents a file available for download from a remote HTTP directory listing.
 */
public record RemoteFile(String name, URI uri) {}

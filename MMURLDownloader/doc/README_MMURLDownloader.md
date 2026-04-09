# MMURLDownloader

## Purpose

MMURLDownloader is a command-line tool that downloads files from HTTP URLs. It supports two modes:

- **Directory mode**: if the URL points to an HTTP directory listing (Apache/Nginx autoindex), it lists available files, lets the user select which ones to download, and downloads them in batch with a live progress bar per file.
- **Single file mode**: if the URL points directly to a file, it downloads it with a progress bar.

## Functional Overview

1. Prompt for a source URL
2. Fetch the URL and detect if it is a directory listing or a direct file
3. **If directory**: display a numbered file list and accept a selection (`*`, `1`, `1,3`, `2-5`, or combined)
4. Prompt for a local destination directory
5. Confirm the download before proceeding
6. Download each file with a live progress bar showing:
   - Filled bar with percentage (when Content-Length is available)
   - Bytes downloaded and transfer speed (always)
7. Handle pre-existing destination files: Overwrite / Skip / All / Skip all
8. Continue to the next file if one fails (error resilience)
9. Print a session summary: files downloaded, skipped, failed, total size, elapsed time

## Project Structure

```
MMURLDownloader/
├── doc/
│   └── README_MMURLDownloader.md
├── pom.xml
└── src/
    └── main/
        └── java/
            └── edu/macesoft/mmurldownloader/
                ├── Main.java
                ├── cli/
                │   ├── ConsoleRunner.java      ← orchestrates the full flow
                │   ├── FileSelector.java       ← interactive file selection UI
                │   └── ProgressBar.java        ← live terminal progress bar
                ├── engine/
                │   ├── DownloadRequest.java    ← value object: URI + destination path
                │   ├── DownloadResult.java     ← outcome: success / skipped / failure
                │   └── Downloader.java         ← HTTP download with progress callback
                └── http/
                    ├── DirectoryParser.java    ← parses HTTP directory index pages
                    └── RemoteFile.java         ← value object: name + URI
```

## Dependencies

- Java 17 or higher
- Apache Maven 3.6 or higher
- No external runtime dependencies (uses Java standard library only)

## Installation

```bash
git clone https://github.com/macesoft/MMTrainingRepo01.git
cd MMTrainingRepo01/MMURLDownloader
```

## Compilation

```bash
mvn package
```

The compiled JAR will be at:

```
target/MMURLDownloader.jar
```

## Execution

```bash
java -jar target/MMURLDownloader.jar
```

## Example Sessions

### Directory listing mode

```
  MMURLDownloader v2.0
────────────────────────────────────────────────────────────

  Source URL        : http://example.com/files/

  Found 3 file(s) available for download.

────────────────────────────────────────────────────────────
  #    Name
────────────────────────────────────────────────────────────
  1    report.pdf
  2    dataset.csv
  3    archive.zip
────────────────────────────────────────────────────────────

  Select: [*] all  [1] single  [1,3] multiple  [2-5] range
  > 1,3

  Destination dir   : /home/user/downloads

────────────────────────────────────────────────────────────
  2 file(s) → /home/user/downloads
────────────────────────────────────────────────────────────
  Proceed? (Y/N)    : Y

  report.pdf             [█████████████████████████]  100% | 2.3 MB    / 2.3 MB    | 1.1 MB/s
  archive.zip            [████████████░░░░░░░░░░░░░]   49% | 512.0 KB  / 1.0 MB    | 487.2 KB/s

────────────────────────────────────────────────────────────
  Downloaded : 2 file(s)
  Total size : 3.3 MB
  Time       : 4.2 s
────────────────────────────────────────────────────────────
```

### Single file mode

```
  MMURLDownloader v2.0
────────────────────────────────────────────────────────────

  Source URL        : https://example.com/files/image.png
  Destination dir   : /home/user/downloads

────────────────────────────────────────────────────────────
  From : https://example.com/files/image.png
  To   : /home/user/downloads/image.png
────────────────────────────────────────────────────────────
  Proceed? (Y/N)    : Y

  image.png              [█████████████████████████]  100% | 456.0 KB  / 456.0 KB  | 892.1 KB/s

────────────────────────────────────────────────────────────
  Downloaded : 1 file(s)
  Total size : 456.0 KB
  Time       : 0.5 s
────────────────────────────────────────────────────────────
```

## Technical Notes

- Directory detection is based on the `Content-Type: text/html` response header. The parser targets Apache/Nginx autoindex format.
- Progress bar percentage is shown only when the server provides a `Content-Length` header.
- Files are downloaded with a 16 KB buffer. Streams are managed via try-with-resources.
- HTTP redirects are followed automatically.
- If a download fails mid-batch, the error is logged and the next file proceeds.
- Destination files are created or overwritten atomically via `StandardOpenOption.TRUNCATE_EXISTING`.

## Limitations

- Directory browsing is single-level (no recursive descent into subdirectories).
- No download resume for partial files.
- No retry logic on network failure.
- The destination directory must exist before running.
- Directory parsing relies on standard autoindex HTML; non-standard listing formats may not be recognized.

# MMURLDownloader

## Purpose

MMURLDownloader is a command-line tool that downloads a file from a given URL to a local directory. It prompts the user interactively for a source URL and a destination directory, confirms the operation, and reports the number of bytes written.

## Functional Overview

1. Prompts for a source URL
2. Prompts for a local destination directory
3. Derives the output filename from the URL path
4. Asks for confirmation before proceeding
5. Downloads the file using Java NIO (`Files.copy`)
6. Reports bytes written on success, or an error message on failure

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
                │   └── ConsoleRunner.java
                └── engine/
                    ├── DownloadRequest.java
                    └── Downloader.java
```

## Dependencies

- Java 17 or higher
- Apache Maven 3.6 or higher
- No external runtime dependencies

## Installation

Clone the repository and navigate to the project directory:

```bash
git clone https://github.com/macesoft/MMTrainingRepo01.git
cd MMTrainingRepo01/MMURLDownloader
```

## Compilation

```bash
mvn package
```

The compiled JAR will be generated at:

```
target/MMURLDownloader.jar
```

## Execution

```bash
java -jar target/MMURLDownloader.jar
```

### Example Session

```
  MMURLDownloader v2.0
────────────────────────────────────────

  Source URL        : https://example.com/files/report.pdf
  Destination dir   : /home/user/downloads

────────────────────────────────────────
  From : https://example.com/files/report.pdf
  To   : /home/user/downloads/report.pdf
────────────────────────────────────────
  Proceed? (Y/N)    : Y

Downloading...
Done. 284,712 bytes written to: /home/user/downloads/report.pdf
```

## Technical Notes

- The destination filename is derived automatically from the URL path.
- If the destination file already exists, it will be overwritten.
- The download uses `java.nio.file.Files.copy()` for efficient stream transfer.
- Streams are managed internally via try-with-resources — no resource leaks.

## Limitations

- No progress bar for large downloads.
- No retry logic on network failure.
- Requires the destination directory to exist before running.

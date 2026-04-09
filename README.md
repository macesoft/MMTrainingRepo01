# MMTrainingRepo01

Java projects repository — macesoft training and experimentation.

## Projects

### MMURLDownloader

A command-line tool for downloading files from HTTP URLs.

**Features:**
- Detects HTTP directory listings (Apache/Nginx autoindex) and lists available files
- Interactive file selection: single, multiple, range, or all (`*`)
- Files listed sorted alphabetically by extension, then by name
- Live progress bar per file with speed and size indicators
- Handles existing files: overwrite / skip / apply to all
- Error resilience: continues with next file if one fails
- Session summary: files downloaded, skipped, failed, total size and time

**Build:**
```bash
cd MMURLDownloader
mvn package
```

**Run:**
```bash
java -jar MMURLDownloader/target/MMURLDownloader.jar
```

See [`MMURLDownloader/doc/README_MMURLDownloader.md`](MMURLDownloader/doc/README_MMURLDownloader.md) for full documentation.

## Requirements

- Java 17+
- Apache Maven 3.6+

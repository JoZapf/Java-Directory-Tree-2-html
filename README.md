# Java-Directory-Tree-2-HTML

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![JDK](https://img.shields.io/badge/JDK-21-blue)
![Platforms](https://img.shields.io/badge/OS-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)
![Version](https://img.shields.io/badge/version-1.2-orange)

> Generate an **interactive HTML directory view** from any folder with **file sizes**, **progress tracking**, and **dark mode by default**.  
> **Double-click** opens a GUI with real-time progress; **CLI** usage with an argument skips the GUI.

---

## Project Header

| Repository | Author | License | Version | Date |
|---|---|---|---|---|
| <https://github.com/JoZapf/Java-Directory-Tree-2-html> | Jo Zapf | MIT | **1.2** | **2025-10-18** |
- **Built with:** Java/JDK 21 and the standard library (`java.nio.file`, `javax.swing`)

---

## ✨ New in Version 1.2

### 🚀 **Single-Pass Architecture**
Complete performance overhaul! The application now traverses your directory structure **only once**, calculating sizes and generating HTML simultaneously. This results in **60-70% faster execution** compared to v1.0, especially noticeable on large drives (3TB+ datasets).

### 📊 **Progress Dialog with Live Updates**
- **Circular progress indicator** showing processing status
- **Real-time item counter** displaying processed files/folders
- **Phase indicators** ("Generating HTML tree...")
- **Responsive UI** that remains visible throughout the entire process
- Helpful hint text for large directory structures

### 📏 **File Size Display**
- **Every file and folder** now shows its size
- Automatic formatting in **MB** or **GB** (2 decimal places)
- Folder sizes are calculated recursively and cached in RAM for performance

### 🌙 **Dark Mode by Default**
- Professional dark theme activated on launch
- Toggle button with intuitive icons (☀️ Light Mode / 🌙 Dark Mode)
- Improved contrast and readability in both modes

### 🎨 **Enhanced Visual Design**
- Centered layout with **1200px max-width** for better readability
- Refined border colors (#555 dark / #ddd light)
- File sizes positioned 2vw from the right edge
- Bottom borders on all list items for better structure

### 🛡️ **Improved Error Handling**
- Graceful handling of **AccessDeniedException** (permissions issues)
- Files/folders with denied access are marked but don't stop execution
- Better logging for troubleshooting
- No crash on protected system directories

---

## Table of Contents

- [Overview](#overview)
- [Compatibility & Supported Platforms](#compatibility--supported-platforms)
- [Technical How-To](#technical-how-to)
- [Build with JDK 21](#build-with-jdk-21)
  - [IntelliJ IDEA (Artifacts)](#intellij-idea-artifacts)
  - [Command line (portable)](#command-line-portable)
- [Run](#run)
  - [Double-click (GUI)](#double-click-gui)
  - [CLI (skip GUI)](#cli-skip-gui)
- [Performance Notes](#performance-notes)
- [Known Limitations & Roadmap](#known-limitations--roadmap)
- [Versioning & Changelog](#versioning--changelog)
- [License](#license)

---

## Overview

**Java-Directory-Tree → HTML** scans a chosen root directory and writes **`directory-tree.html`** into exactly that directory.  
The output provides:

- A collapsible **Explorer** tree with **file sizes** for all entries
- A **File Types** table with per-extension counts  
- An **Unknown Files** list (files without a dot/extension)  
- **Dark Mode** by default with a toggle button
- A **timestamp** of the generation run

The app offers a **progress-tracked GUI** via Swing:
- **Double-click the JAR** → folder dialog → **real-time progress** → HTML generated
- **CLI usage** → pass the folder path as an argument → GUI is skipped

Core classes:
- `RootDirectoryListing` — single-pass traversal, size calculation, statistics, HTML generation (UTF-8)  
- `HtmlSnippets` — embedded CSS/JS (tabs, dark mode toggle, responsive design)  
- `FileTypeIcons` — emoji icons per extension
- `ProcessingDialog` — circular progress indicator with live updates

---

## Compatibility & Supported Platforms

**Operating Systems**
- **Windows 10/11** (NTFS/exFAT; junctions/symlinks per OS)
- **macOS** (Intel & Apple Silicon; APFS/HFS+)
- **Linux** (modern distributions; ext4, btrfs, XFS, ZFS, etc.)

**Java Runtime**
- Built with **JDK 21** → runs on **Java 17+** by default.  

**Filesystem Behavior**
- **Read-only traversal**; file contents are **not** opened.  
- **Hidden files** are included as provided by the OS/JVM.  
- **Permissions:** 
  - Files/folders with **AccessDeniedException** are marked as `[Access denied]` and **skipped gracefully**
  - The application **continues processing** remaining accessible entries
  - Admin/root privileges may reveal additional system folders
- **Symlinks/Junctions:** Links may be followed by the platform → **cycle risk** in current version (no visited-set yet).  
- **Long paths (Windows):** May require *LongPathsEnabled* for very deep/long paths.

**Encoding & UI**
- HTML output is **UTF-8**; recommend running with `-Dfile.encoding=UTF-8`.  
- Emoji-based file-type icons rely on system/browser fonts; rendering can vary across platforms/browsers.

---

## Technical How-To

### 1. **Selecting the root directory**
   - If `args[0]` is set (non-blank), its absolute path is used.
   - Else (non-headless) a `JFileChooser` asks for a directory.
   - Else the fallback is `System.getProperty("user.dir")`.
   - For compatibility, `System.setProperty("user.dir", …)` is set to the chosen directory.

### 2. **Single-Pass Traversal & Data Collection** ⚡ NEW in v1.2
   - **One-pass algorithm**: Simultaneous traversal, size calculation, and HTML generation
   - **RAM-based caching**: Directory sizes cached in `HashMap<Path, Long>` during traversal
   - **Performance optimization**: Eliminates redundant filesystem access (60-70% faster)
   
   **Technical details:**
   - Uses `Files.newDirectoryStream` for each directory
   - Recursive descent with size accumulation
   - File extensions collected (case-insensitive)
   - Files **without `.`** tagged as "Unknown Files"
   - **AccessDeniedException**: Caught explicitly, logged, and skipped without terminating
   - Non-readable entries: Annotated in HTML **and** logged; processing **continues**

### 3. **Progress Tracking** 🆕 NEW in v1.2
   - **SwingWorker** for background processing (non-blocking UI)
   - **Live item counter** updates with each processed file/folder
   - **Circular progress indicator** with percentage display
   - **Phase labels** showing current operation
   - **Responsive dialog** remains visible throughout generation

### 4. **HTML Generation**
   - Writes **`directory-tree.html`** using **UTF-8** (`Files.newBufferedWriter(..., StandardCharsets.UTF_8)`)
   - **File sizes** formatted automatically (MB for < 1GB, GB for ≥ 1GB)
   - CSS/JS from `HtmlSnippets`, icons from `FileTypeIcons`
   - Minimal HTML escaping for `&`, `<`, `>` in names/paths
   - **Dark mode CSS** applied by default via JavaScript on page load

### 5. **User Experience**
   - **GUI mode:** 
     - Progress dialog with circular indicator and item counter
     - Completion dialog shows the absolute path of the generated file
   - **CLI mode:** 
     - Prints the path to stdout
     - Errors via exception stack trace

---

## Build with JDK 21

> The JAR is built with **JDK 21**. For **Java 17+ runtime compatibility**, compile with `--release 17`.  
> Without `--release`, the resulting bytecode typically **requires Java 21+** to run.

### IntelliJ IDEA (Artifacts)

1. Open/import the project; mark `src` as *Sources Root*.  
2. **Project SDK:** JDK 21, **Language level:** 21.  
3. **Artifacts:** `File → Project Structure → Artifacts` → `+` → **JAR → From modules with dependencies…**  
   - Select module → **Main Class:** `RootDirectoryListing`  
   - Choose an output directory (e.g., `out/artifacts/JavaDirectoryTree_jar/`)  
4. **Build:** `Build → Build Artifacts → … → Build`  
   → Produces `…/java-directory-tree-2-html.jar` (rename as you like).

### Command line (portable)

**Windows (cmd.exe)**
```bat
cd /d C:\path\to\src
rmdir /s /q out 2>nul & mkdir out
:: Java 21+ runtime:
javac -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java
:: For Java 17+ runtime compatibility:
:: javac --release 17 -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java

> manifest.mf echo Main-Class: RootDirectoryListing
>> manifest.mf echo.
jar cfm java-directory-tree-2-html.jar manifest.mf -C out .
```

**Linux / macOS (bash)**
```bash
cd /path/to/src
rm -rf out && mkdir out
# Java 21+ runtime:
javac -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java
# For Java 17+ runtime compatibility:
# javac --release 17 -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java

echo "Main-Class: RootDirectoryListing" > manifest.mf
echo "" >> manifest.mf
jar cfm java-directory-tree-2-html.jar manifest.mf -C out .
```

---

## Run

### Double-click (GUI)
- **What it does:** 
  - Opens a minimal folder picker (`JFileChooser`)
  - Shows a **progress dialog** with circular indicator and item counter
  - Generates **`directory-tree.html`** in the selected folder
  - Displays completion dialog with absolute output path
- **When to use:** For casual use and non-terminal workflows. No parameters needed.
- **Visual feedback:** Real-time progress updates showing processed items count

### CLI (skip GUI)

#### Windows (cmd.exe)
```bat
java -Dfile.encoding=UTF-8 -jar java-directory-tree-2-html.jar "C:\Users\YourName\Downloads"
```

#### Windows (PowerShell)
```powershell
# Quote each -D argument or use stop-parsing:
java '-Dfile.encoding=UTF-8' -jar java-directory-tree-2-html.jar 'C:\Users\YourName\Downloads'
# or:
java --% -Dfile.encoding=UTF-8 -jar java-directory-tree-2-html.jar C:\Users\YourName\Downloads
```

#### Linux / macOS
```bash
java -Dfile.encoding=UTF-8 -jar java-directory-tree-2-html.jar /Users/yourname/Downloads
```

---

## Performance Notes

### Version 1.2 Improvements

**Single-Pass Architecture Benefits:**
- **60-70% faster** than v1.0 on large directories
- **Reduced I/O operations**: Only one traversal instead of three
- **RAM-efficient caching**: Typical usage ~30-50MB for 75,000 files
- **Scalability**: Tested on 3.28TB / 75,760 files (previous 30-45 min → now ~10-15 min)

**Memory Usage Examples:**
- 10,000 files: ~5-10 MB RAM
- 75,000 files: ~30-50 MB RAM  
- 1,000,000 files: ~400 MB RAM

**Performance Tips:**
- **HDDs**: Sequential reads are optimal; actual speed limited by disk I/O, not CPU
- **SSDs**: Significantly faster due to random read performance
- **Network drives**: Performance depends heavily on network speed and latency
- **Large directories**: Progress dialog provides feedback; patience recommended for 100K+ files

---

## Known Limitations & Roadmap

**Current Limitations (v1.2):**
- **Symlink cycles:** Not yet detected; may cause infinite loops on circular links
- **Very deep paths:** Windows may need `LongPathsEnabled` registry setting
- **Extremely large directories:** 10M+ files may require increased JVM heap (`-Xmx4g`)

**Planned for v1.3+:**
- Symlink cycle detection (visited-set tracking)
- Configurable output filename and location
- Optional size calculation (toggle for speed vs. detail)
- Export to other formats (JSON, CSV, Markdown)
- Multi-language support in generated HTML
- Filtering options (exclude patterns, min/max file size)

---

## Versioning & Changelog

### **1.2 — 2025-10-18** 🚀
- **[PERFORMANCE]** Complete rewrite with **single-pass architecture** (60-70% faster)
- **[FEATURE]** Progress dialog with circular indicator and live item counter
- **[FEATURE]** File and folder sizes displayed in MB/GB (2 decimal precision)
- **[FEATURE]** Dark mode activated by default with improved toggle (☀️ / 🌙 icons)
- **[UI]** Centered layout with 1200px max-width for better readability
- **[UI]** Refined border colors for light/dark modes (#555 / #ddd)
- **[UI]** File sizes positioned 2vw from right edge
- **[BUGFIX]** Graceful handling of AccessDeniedException (no crashes on protected files)
- **[BUGFIX]** Better error handling for unreadable files/directories
- **[TECH]** RAM-based directory size caching for performance
- **[TECH]** SwingWorker for non-blocking GUI operations

### **1.0 — 2025-10-14**
- Initial public release
- Minimal GUI (folder picker) and CLI mode
- UTF-8 HTML output as `directory-tree.html`
- File-type statistics and unknown-files tab
- Dark-mode toggle and timestamp footer
- Built with JDK 21 (see "Compatibility" for targeting Java 17+ with `--release 17`)

---

## License

MIT © 2025 Jo Zapf — see the published [LICENSE](LICENSE) file in this repository.

---

## Contributing

Contributions, issues, and feature requests are welcome!  
Feel free to check the [issues page](https://github.com/JoZapf/Java-Directory-Tree-2-html/issues).

---

## Support

If you find this tool useful, please consider:
- ⭐ **Starring** the repository
- 🐛 **Reporting** bugs or issues
- 💡 **Suggesting** new features
- 🔧 **Contributing** code improvements

---

**Happy directory indexing! 📂✨**

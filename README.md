# Java-Directory-Tree-2-HTML

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![JDK](https://img.shields.io/badge/JDK-21-blue)
![Platforms](https://img.shields.io/badge/OS-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)
![Version](https://img.shields.io/badge/version-1.0-orange)

> Generate an **interactive HTML directory view** from any folder, including a file-type stats table and a list of files without extensions.  
> **Double-click** opens a minimal GUI (folder picker); **CLI** usage with an argument skips the GUI.

---

## Project Header

| Repository | Author | License | Version | Date |
|---|---|---|---|---|
| <https://github.com/JoZapf/Java-Directory-Tree-2-html> | Jo Zapf | MIT | **1.0** | **2025-10-14** |
- **Built with:** Java/JDK 21 and the standard library (`java.nio.file`, `javax.swing`)

---

## Table of Contents

- [Overview](#overview)
- [Compatibility & Supported Platforms (Overview)](#compatibility--supported-platforms-overview)
- [Technical How-To](#technical-how-to)
- [Build with JDK 21](#build-with-jdk-21)
  - [IntelliJ IDEA (Artifacts)](#intellij-idea-artifacts)
  - [Command line (portable)](#command-line-portable)
- [Run](#run)
  - [Double-click (GUI)](#double-click-gui)
  - [CLI (skip GUI)](#cli-skip-gui)
- [Security](#security)
- [Known Limitations & Roadmap](#known-limitations--roadmap)
- [Versioning & Changelog](#versioning--changelog)
- [License](#license)

---

## Overview

**Java-Directory-Tree → HTML** scans a chosen root directory and writes **`directory-tree.html`** into exactly that directory.  
The output provides:

- A collapsible **Explorer** tree  
- A **File Types** table with per-extension counts  
- An **Unknown Files** list (files without a dot/extension)  
- A **Dark Mode** toggle and a **timestamp** of the run

The app offers a minimal GUI via `JFileChooser`:
- **Double-click the JAR** → a folder dialog appears → output is generated there.
- **CLI usage** → pass the folder path as an argument → GUI is skipped.

Core classes:
- `RootDirectoryListing` — traversal, statistics, HTML generation (UTF-8)  
- `HtmlSnippets` — embedded CSS/JS (tabs, dark mode, toggle)  
- `FileTypeIcons` — emoji icons per extension

> Note: UI strings in the generated HTML are currently German.

---

## Compatibility & Supported Platforms (Overview)

**Operating Systems**
- **Windows 10/11** (NTFS/exFAT; junctions/symlinks per OS)
- **macOS** (Intel & Apple Silicon; APFS/HFS+)
- **Linux** (modern distributions; ext4, btrfs, XFS, ZFS, etc.)

**Java Runtime**
- Built with **JDK 21** → runs on **Java 21+** by default.  
- If you recompile with `javac --release 17`, the JAR runs on **Java 17+**.

**Filesystem Behavior**
- **Read-only traversal**; file contents are **not** opened.  
- **Hidden files** are included as provided by the OS/JVM.  
- **Permissions:** Non-readable entries are skipped and annotated; admin/root may be required on system folders.  
- **Symlinks/Junctions:** Links may be followed by the platform → **cycle risk** in v1.0 (no visited-set yet).  
- **Long paths (Windows):** May require *LongPathsEnabled* for very deep/long paths.

**Encoding & UI**
- HTML output is **UTF-8**; recommend running with `-Dfile.encoding=UTF-8`.  
- Emoji-based file-type icons rely on system/browser fonts; rendering can vary across platforms/browsers.

---

## Technical How-To

1. **Selecting the root directory**
   - If `args[0]` is set (non-blank), its absolute path is used.
   - Else (non-headless) a `JFileChooser` asks for a directory.
   - Else the fallback is `System.getProperty("user.dir")`.
   - For compatibility, `System.setProperty("user.dir", …)` is set to the chosen directory.

2. **Traversal & data collection**
   - Iteration with `Files.newDirectoryStream` for each directory.
   - Recursive descent; counts by file extension (case-insensitive).
   - Files **without `.`** are collected as “Unknown Files”.
   - Non-readable entries: noted in HTML **and** logged to console; the run **continues**.

3. **HTML generation**
   - Writes **`directory-tree.html`** using **UTF-8** (`Files.newBufferedWriter(..., StandardCharsets.UTF_8)`).
   - CSS/JS from `HtmlSnippets`, icons from `FileTypeIcons`.
   - Minimal HTML escaping for `&`, `<`, `>` in names/paths.

4. **User experience**
   - **GUI mode:** completion dialog shows the absolute path of the generated file.
   - **CLI mode:** prints the path to stdout; errors via exception stack trace.

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
   → Produces `…/Java-Directory-Tree.jar` (rename as you like).

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
jar cfm Java-Directory-Tree.jar manifest.mf -C out .

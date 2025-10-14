# README_BUILD — How to Build the JAR (Java-Directory-Tree → HTML)

[![License: MIT](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)
![JDK](https://img.shields.io/badge/JDK-21-blue)
![Platforms](https://img.shields.io/badge/OS-Windows%20%7C%20macOS%20%7C%20Linux-lightgrey)
![Version](https://img.shields.io/badge/version-1.0-orange)

---

## Build Header

| Repository | Author | License | Version | Date |
|---|---|---|---|---|
| <https://github.com/JoZapf/Java-Directory-Tree-2-html> | Jo Zapf | MIT | **1.0** | **2025-10-14** |

---

## Goal

Produce an executable **JAR** (e.g., `Java-Directory-Tree.jar`) for the app that generates an interactive HTML file **`directory-tree.html`** from a chosen directory.  
The JAR should run by **double-click** (GUI folder picker) and via **CLI** with an optional directory argument (which skips the GUI).

---

## Prerequisites

- **JDK 21** installed (JDK 17 works if you compile with `--release 17` for broader runtime compatibility).
- `java`, `javac`, and `jar` available on your PATH (or use full paths, e.g., `"%ProgramFiles%\Java\jdk-21\bin\javac.exe"`).
- Source files in the `src` directory (default package):
  - `RootDirectoryListing.java` (main class with `public static void main`)
  - `HtmlSnippets.java` (returns CSS & JS for the HTML output)
  - `FileTypeIcons.java` (returns emoji icons per extension)

> **Why JDK 21?** We target a current LTS. If you want the JAR to run on Java 17+ runtimes, compile with `--release 17` (see below).

---

## Source Layout & Main Class

- No explicit package declarations (default package).  
- **Main class**: `RootDirectoryListing`. The manifest must reference this exact name:
  ```text
  Main-Class: RootDirectoryListing
  ```
  > If you **add a package**, change the manifest to the fully qualified name, e.g., `Main-Class: com.example.RootDirectoryListing`.

---

## Build Path Options

You can build the JAR either from **IntelliJ IDEA (Artifacts)** or from the **command line**. Both produce identical runtime behavior.

### Option A — IntelliJ IDEA (Artifacts)

1. **Open/Import** the project. Mark `src` as *Sources Root* (Right-click → *Mark Directory as → Sources Root*).
2. **Project SDK / Language Level**  
   - *Project SDK*: **JDK 21** (or JDK 17 if you explicitly target `--release 17`)  
   - *Language level*: **21**
3. **Create Artifact**  
   - *File → Project Structure → Artifacts*  
   - Click **`+` → JAR → From modules with dependencies…**  
   - Choose your module → set **Main Class** to `RootDirectoryListing`.  
   - Choose an output directory (e.g., `out/artifacts/JavaDirectoryTree_jar/`).  
4. **Build**  
   - *Build → Build Artifacts… → JavaDirectoryTree:jar → Build*  
   - Result: a JAR in your chosen output directory. You can rename it to `Java-Directory-Tree.jar`.

> *Notes:*  
> - This project has **no external libraries**, so “with dependencies” simply bundles your classes.  
> - If “Main class not found” appears, set the correct Main Class in the artifact settings and rebuild.

---

### Option B — Command Line (portable, reproducible)

The following commands demonstrate **exactly** how we produced the JAR.

#### Windows (cmd.exe)

```bat
cd /d C:\path\to\src
rmdir /s /q out 2>nul & mkdir out

:: Compile all sources to 'out' using UTF-8
:: If you want the JAR to run on Java 21+ only:
javac -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java

:: If you prefer Java 17+ runtime compatibility instead (recommended for portability):
:: javac --release 17 -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java

:: Create a manifest file with the Main-Class entry.
> manifest.mf echo Main-Class: RootDirectoryListing
>> manifest.mf echo.

:: Package classes from 'out' into the JAR, applying the manifest.
jar cfm Java-Directory-Tree.jar manifest.mf -C out .

:: Verify contents (optional)
jar tf Java-Directory-Tree.jar
```

#### Windows (PowerShell)

> PowerShell handles redirection differently; use `Out-File` for the manifest or run the **cmd.exe** block above.

```powershell
Set-Location "C:\path\to\src"
Remove-Item -Recurse -Force out -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path out | Out-Null

# Java 21+ runtime target:
javac -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java
# For Java 17+ runtime compatibility:
# javac --release 17 -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java

# Create manifest with a trailing blank line
"Main-Class: RootDirectoryListing`n" | Out-File -Encoding ascii manifest.mf

jar cfm Java-Directory-Tree.jar manifest.mf -C out .

# Verify
jar tf Java-Directory-Tree.jar
```

#### macOS / Linux (bash/zsh)

```sh
cd /path/to/src
rm -rf out && mkdir -p out

# Java 21+ runtime target:
javac -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java
# For Java 17+ runtime compatibility:
# javac --release 17 -encoding UTF-8 -d out RootDirectoryListing.java HtmlSnippets.java FileTypeIcons.java

# Create manifest (note the required trailing blank line)
printf "Main-Class: RootDirectoryListing\n\n" > manifest.mf

jar cfm Java-Directory-Tree.jar manifest.mf -C out .

# Verify
jar tf Java-Directory-Tree.jar
```

---

## What Each Step Does (Didactic Notes)

- `javac -encoding UTF-8 -d out …`  
  Compiles `.java` sources using **UTF-8** (avoids platform-dependent encoding issues) and writes `.class` files into the `out` directory (clean separation of sources and artifacts).

- `javac --release 17 …`  
  Produces **Java 17**-compatible bytecode and links against Java 17 APIs, ensuring the JAR runs on **Java 17+**. Without this flag and compiling on JDK 21, the output typically **requires Java 21+** to run.

- `manifest.mf` with a **blank line** at the end  
  The JAR manifest requires a trailing newline to be valid. Missing it can cause “no main manifest attribute” errors.

- `jar cfm Java-Directory-Tree.jar manifest.mf -C out .`  
  Creates (`c`) a JAR, uses the provided manifest (`m`), and reads files from the `out` directory (`-C out .` sets `out` as the working directory for adding files).

- `jar tf …`  
  Lists the JAR’s contents for a quick sanity check.

---

## Test the JAR

- **Double-click** the JAR: a folder picker appears; on completion, a dialog shows the path to the generated **`directory-tree.html`** in the chosen directory.
- **Command line** (example):
  - Windows (cmd.exe):
    ```bat
    java -Dfile.encoding=UTF-8 -jar Java-Directory-Tree.jar "C:\Users\YourName\Downloads"
    ```
  - macOS/Linux:
    ```sh
    java -Dfile.encoding=UTF-8 -jar Java-Directory-Tree.jar "/Users/yourname/Downloads"
    ```

> PowerShell parsing tip: either quote each `-D` argument (`'-Dfile.encoding=UTF-8'`) or use `--%` stop-parsing mode.

---

## Troubleshooting

- **`no main manifest attribute, in …`**  
  The manifest is missing or incorrect. Ensure `manifest.mf` contains `Main-Class: RootDirectoryListing` **and** ends with a blank line. Rebuild the JAR.

- **`Error: Could not find or load main class RootDirectoryListing`**  
  The manifest’s Main-Class is wrong or the class was compiled into a package. If you add a package, use the **fully qualified** name in the manifest.

- **`Unsupported class file major version …`**  
  You compiled with a newer JDK than the runtime supports (e.g., built on 21, running on 17). Recompile with `--release 17` or use a newer runtime.

- **Emoji not showing / odd glyphs in HTML**  
  This depends on the OS/browser font. The generated HTML is UTF-8; rendering relies on available emoji fonts.

- **Access denied errors during traversal**  
  Expected on system folders or protected paths. Run with appropriate permissions or test on user folders first.

---

## Reproducible Build Scripts (Optional)

**Windows (cmd.exe) — build_jar.cmd**
```bat
@echo off
setlocal
set "SRC=%~dp0src"
set "OUT=%~dp0out"
set "JAR=%~dp0Java-Directory-Tree.jar"

rmdir /s /q "%OUT%" 2>nul
mkdir "%OUT%"

rem Compile (choose one)
rem javac --release 17 -encoding UTF-8 -d "%OUT%" "%SRC%\RootDirectoryListing.java" "%SRC%\HtmlSnippets.java" "%SRC%\FileTypeIcons.java"
javac -encoding UTF-8 -d "%OUT%" "%SRC%\RootDirectoryListing.java" "%SRC%\HtmlSnippets.java" "%SRC%\FileTypeIcons.java"

> "%~dp0manifest.mf" echo Main-Class: RootDirectoryListing
>> "%~dp0manifest.mf" echo.

jar cfm "%JAR%" "%~dp0manifest.mf" -C "%OUT%" .
jar tf "%JAR%"
echo Built: %JAR%
endlocal
```

**macOS/Linux — build_jar.sh**
```sh
#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
SRC="$SCRIPT_DIR/src"
OUT="$SCRIPT_DIR/out"
JAR="$SCRIPT_DIR/Java-Directory-Tree.jar"

rm -rf "$OUT" && mkdir -p "$OUT"

# Compile (choose one)
# javac --release 17 -encoding UTF-8 -d "$OUT" "$SRC/RootDirectoryListing.java" "$SRC/HtmlSnippets.java" "$SRC/FileTypeIcons.java"
javac -encoding UTF-8 -d "$OUT" "$SRC/RootDirectoryListing.java" "$SRC/HtmlSnippets.java" "$SRC/FileTypeIcons.java"

printf "Main-Class: RootDirectoryListing\n\n" > "$SCRIPT_DIR/manifest.mf"

jar cfm "$JAR" "$SCRIPT_DIR/manifest.mf" -C "$OUT" .
jar tf "$JAR"
echo "Built: $JAR"
```

---

## License & Credits

- **License:** MIT — see [LICENSE](LICENSE).  
- **Author:** Jo Zapf  
- **Repository:** <https://github.com/JoZapf/Java-Directory-Tree-2-html>

**Build doc version:** 1.0 (2025-10-14)

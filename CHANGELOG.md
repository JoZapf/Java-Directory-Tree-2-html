# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).


## [1.4] - 2025-02-04

### Added
- Comprehensive JavaDoc documentation for all classes and methods
- Generated HTML JavaDoc documentation in `docs/javadoc/`
- Private constructors to utility classes (FileTypeIcons, HtmlSnippets, RootDirectoryListing)
- AssertionError on instantiation attempts for utility classes (best practice)
- Complete `@param`, `@return`, `@see`, and `@throws` tags for all public APIs
- Detailed class-level documentation with usage examples
- Documentation for inner classes (ProcessingDialog, CircularProgressPanel, ProgressCallback, ProgressUpdate)
- Enhanced MANIFEST.MF with Implementation and Specification metadata

### Changed
- Updated .gitignore to exclude `docs/*` but include `docs/javadoc/`
- Improved code maintainability through comprehensive inline documentation

### Documentation
- Full API reference available in `docs/javadoc/`
- Professional-grade documentation for developers and contributors
- Cross-linked classes and methods in generated JavaDoc HTML

## [1.3] - 2025-10-19

### Added
- Comprehensive tree statistics in HTML page header
- Internationalized number formatting (space as thousand separator, dot as decimal)
- Automatic unit selection (GB for < 1TB, TB for ≥ 1TB) with 2 decimal precision
- Complete statistics display in GUI success state

### Changed
- **[BREAKING]** Complete rewrite to StringBuilder-based HTML generation (eliminates redundant file I/O)
- Optimized memory management with single-pass HTML building
- Single write operation for final HTML output (instead of streaming during traversal)

### Performance
- Additional 5-15 seconds saved on large directories (especially on HDDs)
- Total memory footprint: ~40-80 MB for 75,000 files
- StringBuilder-based HTML generation: ~10-30 MB for typical use cases
- RAM-cached directory sizes: ~30-50 MB for 75,000 files
- No temporary files or disk-based caching required

## [1.2] - 2025-10-18

### Added
- Real-time progress tracking with circular indicator
- Live item counter showing processed files and folders
- File and folder sizes displayed in MB/GB (2 decimal precision)
- Dark mode activated by default with improved toggle (☀️ / 🌙 icons)
- RAM-based directory size caching for performance
- Graceful handling of AccessDeniedException (no crashes on protected files)
- Better error handling for unreadable files and directories
- SwingWorker for non-blocking GUI operations

### Changed
- **[BREAKING]** Complete rewrite with single-pass architecture (60-70% faster)
- Centered layout with 1200px max-width for better readability
- Refined border colors for light/dark modes (#555 / #ddd)
- File sizes positioned 2vw from right edge

### Performance
- 60-70% faster processing on large directories
- Single filesystem traversal (no redundant reads)
- Efficient memory usage with size caching

### Fixed
- Crash when encountering files without read permissions
- Incomplete error handling for system-protected directories
- Progress feedback missing during long operations

## [1.0] - 2025-10-14

### Added
- Initial public release
- Minimal GUI with folder picker (JFileChooser)
- CLI mode with directory argument support
- UTF-8 HTML output as `directory-tree.html`
- Collapsible directory tree with emoji file type icons
- File type statistics table with extension counts
- Unknown files tab (files without extensions)
- Dark mode toggle (Light/Dark theme switching)
- Timestamp footer showing generation time
- Three-tab interface (Explorer, File Types, Unknown Files)

### Technical
- Built with JDK 21
- Pure Java standard library (no external dependencies)
- Cross-platform support (Windows, macOS, Linux)
- Uses `java.nio.file` for filesystem operations
- Uses `javax.swing` for GUI components

---

## Version Comparison

| Version | Release Date | Key Highlight | Performance Impact |
|---------|--------------|---------------|-------------------|
| **1.4** | 2025-02-04 | JavaDoc Documentation | No performance change |
| **1.3** | 2025-10-19 | StringBuilder Optimization | +5-15s faster |
| **1.2** | 2025-10-18 | Single-Pass Architecture | 60-70% faster |
| **1.0** | 2025-10-14 | Initial Release | Baseline |

## Migration Notes

### Upgrading from 1.3 to 1.4
- No breaking changes
- No configuration changes required
- Generated HTML output format unchanged
- JAR can be replaced directly

### Upgrading from 1.2 to 1.3
- No breaking changes
- HTML output now includes statistics in header
- Memory usage slightly reduced
- JAR can be replaced directly

### Upgrading from 1.0 to 1.2
- No breaking changes
- Significant performance improvement (60-70% faster)
- Dark mode now default (was light mode in 1.0)
- HTML output includes file/folder sizes
- JAR can be replaced directly

---

## Links

- [Repository](https://github.com/JoZapf/Java-Directory-Tree-2-html)
- [Issues](https://github.com/JoZapf/Java-Directory-Tree-2-html/issues)
- [License](LICENSE)
- [JavaDoc Documentation](docs/javadoc/index.html)

---

**Changelog Format:** [Keep a Changelog](https://keepachangelog.com/)  
**Versioning:** [Semantic Versioning](https://semver.org/)  
**Author:** Jo Zapf  
**License:** MIT

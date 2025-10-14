import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Generates an interactive HTML directory tree for a chosen root directory.
 * - Double-click JAR: shows a minimal GUI (JFileChooser).
 * - CLI: pass a directory argument to skip the GUI.
 * Output file: directory-tree.html in the selected root directory.
 */
public class RootDirectoryListing {

    // Application metadata
    private static final String APP_NAME = "Java-Directory-Tree";
    private static final String VERSION = "1.0";
    private static final String AUTHOR = "Jo Zapf";
    private static final String LICENSE = "MIT";
    private static final String REPO_URL = "https://github.com/JoZapf/Java-Directory-Tree-2-html";
    private static final String LICENSE_URL = "https://opensource.org/licenses/MIT";

    private static final Logger LOGGER = Logger.getLogger(RootDirectoryListing.class.getName());

    public static void main(String[] args) {
        try {
            String startDir = resolveStartDir(args);
            if (startDir == null) {
                // User cancelled the dialog; exit gracefully
                return;
            }

            // Keep compatibility with earlier logic relying on System.getProperty("user.dir")
            System.setProperty("user.dir", startDir);

            Map<String, Integer> extensionStats = new TreeMap<>();
            List<Path> unknownFiles = new ArrayList<>();
            Path rootDir = Paths.get(startDir);
            Path outputHtml = rootDir.resolve("directory-tree.html");

            try (BufferedWriter writer = Files.newBufferedWriter(outputHtml, StandardCharsets.UTF_8)) {
                // Timestamp for head + footer
                LocalDateTime now = LocalDateTime.now();
                String isoGenerated = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                // HTML head
                writer.write("<!DOCTYPE html>\n<html lang='en'>\n<head>\n<meta charset='UTF-8'>\n");
                writer.write("<title>" + APP_NAME + "</title>\n");

                // Metadata & links
                writer.write("<meta name='author' content='" + escapeHtml(AUTHOR) + "'>\n");
                writer.write("<meta name='license' content='" + escapeHtml(LICENSE) + "'>\n");
                writer.write("<meta name='version' content='" + escapeHtml(VERSION) + "'>\n");
                writer.write("<meta name='repository' content='" + escapeHtml(REPO_URL) + "'>\n");
                writer.write("<meta name='generated' content='" + escapeHtml(isoGenerated) + "'>\n");
                writer.write("<link rel='license' href='" + escapeHtml(LICENSE_URL) + "'>\n");
                writer.write("<link rel='author' href='https://github.com/JoZapf'>\n");

                writer.write(HtmlSnippets.getCss());
                writer.write(HtmlSnippets.getJavaScript());
                writer.write("</head><body>\n");

                // Header / Controls
                writer.write("<div class='dark-toggle' onclick='toggleDarkMode()'>🌙 Dark Mode</div>\n");
                writer.write("<h2>Tree:&nbsp;" + escapeHtml(rootDir.toString()) + "</h2>\n");

                // Tabs skeleton
                writer.write("""
                    <div class="tabs">
                      <ul class="tab-header">
                        <li class="active" onclick="showTab('explorer')">Explorer</li>
                        <li onclick="showTab('filetypes')">File Types</li>
                        <li onclick="showTab('unknownfiles')">Unknown Files</li>
                      </ul>
                      <div class="tab-content">
                    """);

                // Explorer tab
                writer.write("<div id='explorer' class='tab-pane active'>\n<ul>\n");
                writeDirectoryRecursive(writer, rootDir, new int[]{0}, extensionStats, unknownFiles);
                writer.write("</ul>\n</div>\n");

                // File types tab
                writer.write("<div id='filetypes' class='tab-pane'>\n<h3>File Type Overview</h3>\n");
                writer.write("<table border='1' cellpadding='5' cellspacing='0'>\n<tr><th>File type</th><th>Count</th></tr>\n");
                for (Map.Entry<String, Integer> entry : extensionStats.entrySet()) {
                    writer.write("<tr><td>" + escapeHtml(entry.getKey()) + "</td><td>" + entry.getValue() + "</td></tr>\n");
                }
                writer.write("</table>\n</div>\n");

                // Unknown files tab
                writer.write("<div id='unknownfiles' class='tab-pane'>\n<h3>Unknown file types (files without extension)</h3>\n<ul>\n");
                for (Path p : unknownFiles) {
                    writer.write("<li>" + escapeHtml(p.toString().replace("\\", "/")) + "</li>\n");
                }
                writer.write("</ul>\n</div>\n</div>\n</div>\n"); // close tab-content and tabs

                // Footer timestamp
                DateTimeFormatter footerFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                writer.write("<div style='text-align: right; margin-top: 1em; font-size: 0.9em; color: gray;'>");
                writer.write("Tree index timestamp: " + now.format(footerFmt));
                writer.write("</div>\n</body></html>");
            }

            // Success notification (GUI if available)
            String successMsg = "HTML file created:\n" + outputHtml.toAbsolutePath();
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        successMsg,
                        APP_NAME,
                        JOptionPane.INFORMATION_MESSAGE
                );
            } else {
                LOGGER.info(successMsg);
            }

        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unhandled error while generating directory tree", e);
            if (!GraphicsEnvironment.isHeadless()) {
                JOptionPane.showMessageDialog(
                        null,
                        "Error: " + e.getMessage(),
                        APP_NAME + " – Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }

    /**
     * Resolves the start directory in this order:
     * 1) Use args[0] if present and non-blank.
     * 2) If not headless, show a directory chooser dialog.
     * 3) Fallback: current System property "user.dir".
     */
    private static String resolveStartDir(String[] args) {
        if (args != null && args.length > 0 && args[0] != null && !args[0].isBlank()) {
            return Paths.get(args[0]).toAbsolutePath().toString();
        }
        if (!GraphicsEnvironment.isHeadless()) {
            JFileChooser fc = new JFileChooser();
            fc.setDialogTitle("Choose folder to index");
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fc.setAcceptAllFileFilterUsed(false);
            int res = fc.showOpenDialog(null);
            if (res == JFileChooser.APPROVE_OPTION) {
                return fc.getSelectedFile().getAbsolutePath();
            } else {
                JOptionPane.showMessageDialog(null, "Cancelled.", APP_NAME, JOptionPane.INFORMATION_MESSAGE);
                return null;
            }
        }
        return System.getProperty("user.dir");
    }

    /**
     * Recursively writes the directory tree and collects stats and unknown files.
     */
    private static void writeDirectoryRecursive(BufferedWriter writer,
                                                Path dir,
                                                int[] idCounter,
                                                Map<String, Integer> stats,
                                                List<Path> unknownFiles) throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                // Skip unreadable entries; annotate both console and HTML
                if (!Files.isReadable(entry)) {
                    LOGGER.warning("Not readable: " + entry.toAbsolutePath());
                    writer.write("<li><span style='color:orange;'>[Not readable: " +
                            escapeHtml(entry.getFileName().toString()) + "]</span></li>\n");
                    continue;
                }

                String name = entry.getFileName().toString();
                String id = "node" + idCounter[0]++;

                if (Files.isDirectory(entry)) {
                    LOGGER.info("Folder: " + entry.toAbsolutePath());
                    writer.write("<li class='folder'><span onclick=\"toggle(event, '" + id + "')\">📁 " +
                            escapeHtml(name) + "</span>\n");
                    writer.write("<ul class='nested' id='" + id + "'>\n");
                    writeDirectoryRecursive(writer, entry, idCounter, stats, unknownFiles);
                    writer.write("</ul></li>\n");
                } else {
                    LOGGER.info("  File: " + entry.toAbsolutePath());
                    String ext = getExtension(name).toLowerCase(Locale.ROOT);
                    if (ext.equals("unknown")) {
                        unknownFiles.add(entry.toAbsolutePath());
                    }
                    stats.put(ext, stats.getOrDefault(ext, 0) + 1);
                    String icon = FileTypeIcons.getIcon(ext);
                    writer.write("<li>" + icon + " " + escapeHtml(name) +
                            " <small>(" + escapeHtml(ext.toUpperCase(Locale.ROOT)) + " file)</small></li>\n");
                }
            }
        } catch (IOException e) {
            // Report directory access problems
            LOGGER.log(Level.WARNING, "Error accessing: " + dir.toAbsolutePath(), e);
            Path last = dir.getFileName();
            String label = (last == null) ? dir.toString() : last.toString();
            writer.write("<li><span style='color:red;'>[Error accessing " +
                    escapeHtml(label) + "]</span></li>\n");
        }
    }

    /**
     * Returns the file extension or "unknown" if none is present.
     */
    private static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "unknown";
    }

    /**
     * Minimal HTML escaping for text nodes: &, <, >.
     */
    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

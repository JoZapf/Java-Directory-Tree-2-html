import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.List;
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
    private static final String VERSION = "1.2";
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

            Path rootDir = Paths.get(startDir);
            Path outputHtml = rootDir.resolve("directory-tree.html");

            // Show progress dialog if GUI is available
            if (!GraphicsEnvironment.isHeadless()) {
                SwingUtilities.invokeLater(() -> {
                    ProcessingDialog dialog = new ProcessingDialog(rootDir, outputHtml);
                    dialog.start();
                });
            } else {
                // CLI mode: process without GUI
                processDirectory(rootDir, outputHtml, null);
                LOGGER.info("HTML file created: " + outputHtml.toAbsolutePath());
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
     * Main processing logic with optional progress callback - SINGLE PASS
     */
    private static void processDirectory(Path rootDir, Path outputHtml, ProgressCallback callback) throws IOException {
        Map<String, Integer> extensionStats = new TreeMap<>();
        List<Path> unknownFiles = new ArrayList<>();
        Map<Path, Long> sizeCache = new HashMap<>(); // RAM cache for directory sizes
        
        if (callback != null) callback.updatePhase("Generating HTML tree...");
        
        try (BufferedWriter writer = Files.newBufferedWriter(outputHtml, StandardCharsets.UTF_8)) {
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
            writer.write("<div class='mode-toggle' onclick='toggleMode()'>☀️ Light Mode</div>\n");
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

            // Explorer tab - SINGLE PASS
            writer.write("<div id='explorer' class='tab-pane active'>\n<ul>\n");
            int[] processedCounter = new int[]{0};
            writeDirectoryRecursiveSinglePass(writer, rootDir, new int[]{0}, extensionStats, unknownFiles, 
                                             sizeCache, processedCounter, callback);
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
    }

    /**
     * SINGLE PASS: Recursively writes directory tree AND calculates sizes in one pass
     */
    private static void writeDirectoryRecursiveSinglePass(BufferedWriter writer,
                                                          Path dir,
                                                          int[] idCounter,
                                                          Map<String, Integer> stats,
                                                          List<Path> unknownFiles,
                                                          Map<Path, Long> sizeCache,
                                                          int[] processedCounter,
                                                          ProgressCallback callback) throws IOException {
        List<Path> entries = new ArrayList<>();
        long totalDirSize = 0;
        
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                entries.add(entry);
            }
        } catch (java.nio.file.AccessDeniedException e) {
            LOGGER.log(Level.WARNING, "Access denied: " + dir.toAbsolutePath(), e);
            Path last = dir.getFileName();
            String label = (last == null) ? dir.toString() : last.toString();
            writer.write("<li><span style='color:orange;'>[Access denied: " +
                    escapeHtml(label) + "]</span></li>\n");
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error accessing: " + dir.toAbsolutePath(), e);
            Path last = dir.getFileName();
            String label = (last == null) ? dir.toString() : last.toString();
            writer.write("<li><span style='color:red;'>[Error accessing " +
                    escapeHtml(label) + "]</span></li>\n");
            return;
        }

        for (Path entry : entries) {
            // Update progress
            if (callback != null) {
                processedCounter[0]++;
                callback.updateProgress(processedCounter[0]);
            }
            
            // Skip unreadable entries
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
                
                // Calculate size recursively and cache it
                long dirSize = calculateDirectorySizeAndCache(entry, sizeCache);
                totalDirSize += dirSize;
                
                String sizeStr = formatFileSize(dirSize);
                writer.write("<li class='folder'><span onclick=\"toggle(event, '" + id + "')\">📁 " +
                        escapeHtml(name) + "</span><span class='file-size'>" + sizeStr + "</span>\n");
                writer.write("<ul class='nested' id='" + id + "'>\n");
                writeDirectoryRecursiveSinglePass(writer, entry, idCounter, stats, unknownFiles, 
                                                 sizeCache, processedCounter, callback);
                writer.write("</ul></li>\n");
            } else {
                LOGGER.info("  File: " + entry.toAbsolutePath());
                String ext = getExtension(name).toLowerCase(Locale.ROOT);
                if (ext.equals("unknown")) {
                    unknownFiles.add(entry.toAbsolutePath());
                }
                stats.put(ext, stats.getOrDefault(ext, 0) + 1);
                String icon = FileTypeIcons.getIcon(ext);
                
                // Handle potential AccessDeniedException for file size
                long fileSize = 0;
                try {
                    fileSize = Files.size(entry);
                } catch (java.nio.file.AccessDeniedException e) {
                    LOGGER.warning("Access denied for size: " + entry.toAbsolutePath());
                    writer.write("<li>" + icon + " " + escapeHtml(name) +
                            " <small>" + escapeHtml(ext.toUpperCase(Locale.ROOT)) + " file</small>" +
                            "<span class='file-size'>[Access Denied]</span></li>\n");
                    continue;
                } catch (IOException e) {
                    LOGGER.warning("Error reading size: " + entry.toAbsolutePath());
                    continue;
                }
                
                totalDirSize += fileSize;
                String sizeStr = formatFileSize(fileSize);
                writer.write("<li>" + icon + " " + escapeHtml(name) +
                        " <small>(" + escapeHtml(ext.toUpperCase(Locale.ROOT)) + " file)</small>" +
                        "<span class='file-size'>" + sizeStr + "</span></li>\n");
            }
        }
        
        // Cache the total size of this directory
        sizeCache.put(dir, totalDirSize);
    }

    /**
     * Calculate directory size recursively using cache
     */
    private static long calculateDirectorySizeAndCache(Path dir, Map<Path, Long> sizeCache) {
        // Check cache first
        if (sizeCache.containsKey(dir)) {
            return sizeCache.get(dir);
        }
        
        long size = 0;
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(dir)) {
            for (Path entry : stream) {
                if (!Files.isReadable(entry)) continue;
                if (Files.isDirectory(entry)) {
                    size += calculateDirectorySizeAndCache(entry, sizeCache);
                } else {
                    try {
                        size += Files.size(entry);
                    } catch (java.nio.file.AccessDeniedException e) {
                        // Skip files we can't access
                        LOGGER.fine("Access denied for size calculation: " + entry);
                    } catch (IOException e) {
                        // Skip files we can't read
                    }
                }
            }
        } catch (java.nio.file.AccessDeniedException e) {
            // Can't access this directory at all
            LOGGER.fine("Access denied to directory: " + dir);
        } catch (IOException e) {
            // Return size calculated so far
        }
        
        // Cache the result
        sizeCache.put(dir, size);
        return size;
    }

    /**
     * Format file size in MB or GB with 2 decimal places
     */
    private static String formatFileSize(long bytes) {
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);
        if (gb >= 1.0) {
            return String.format("%.2f GB", gb);
        } else {
            double mb = bytes / (1024.0 * 1024.0);
            return String.format("%.2f MB", mb);
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

    /**
     * Interface for progress callbacks
     */
    interface ProgressCallback {
        void updateProgress(int current);
        void updatePhase(String phase);
    }

    /**
     * Progress dialog with circular progress indicator
     */
    static class ProcessingDialog extends SwingWorker<Void, ProgressUpdate> {
        private static final int DIAMETER = 200; // Same as CircularProgressPanel
        private final Path rootDir;
        private final Path outputHtml;
        private final JDialog dialog;
        private final CircularProgressPanel progressPanel;
        private final JLabel phaseLabel;
        private final JLabel counterLabel;
        private final JLabel hintLabel;

        public ProcessingDialog(Path rootDir, Path outputHtml) {
            this.rootDir = rootDir;
            this.outputHtml = outputHtml;
            
            // Create modal dialog
            dialog = new JDialog((Frame) null, APP_NAME, true);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setSize(500, 550);
            dialog.setLocationRelativeTo(null);
            dialog.setLayout(new BorderLayout());

            // Create progress panel with dark gray background
            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setBackground(new Color(64, 64, 64));
            
            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.CENTER;
            
            // Phase label ABOVE circle
            phaseLabel = new JLabel("Initializing...", SwingConstants.CENTER);
            phaseLabel.setForeground(Color.WHITE);
            phaseLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            centerPanel.add(phaseLabel, gbc);
            
            // Progress circle
            gbc.gridy = 1;
            progressPanel = new CircularProgressPanel();
            centerPanel.add(progressPanel, gbc);
            
            // Counter label in styled container BELOW circle
            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0; // No stretch
            
            JPanel counterContainer = new JPanel(new BorderLayout());
            counterContainer.setPreferredSize(new Dimension(DIAMETER, 60));
            counterContainer.setBackground(new Color(64, 64, 64));
            counterContainer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(85, 85, 85), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));
            
            counterLabel = new JLabel("<html><center>Processing: 0 items</center></html>", SwingConstants.CENTER);
            counterLabel.setForeground(Color.WHITE);
            counterLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            counterContainer.add(counterLabel, BorderLayout.CENTER);
            
            centerPanel.add(counterContainer, gbc);
            
            // Hint label BELOW counter with fixed width
            gbc.gridy = 3;
            gbc.insets = new Insets(5, 10, 10, 10);
            
            JPanel hintContainer = new JPanel(new BorderLayout());
            hintContainer.setPreferredSize(new Dimension(DIAMETER, 50));
            hintContainer.setBackground(new Color(64, 64, 64));
            
            hintLabel = new JLabel("<html><center>Depending on the number of files,<br>this may take some time.</center></html>", 
                                  SwingConstants.CENTER);
            hintLabel.setForeground(new Color(180, 180, 180));
            hintLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            hintContainer.add(hintLabel, BorderLayout.CENTER);
            centerPanel.add(hintContainer, gbc);
            
            dialog.add(centerPanel, BorderLayout.CENTER);
        }

        @Override
        protected Void doInBackground() throws Exception {
            ProgressCallback callback = new ProgressCallback() {
                @Override
                public void updateProgress(int current) {
                    publish(new ProgressUpdate(current, null));
                }

                @Override
                public void updatePhase(String phase) {
                    publish(new ProgressUpdate(-1, phase));
                }
            };
            
            processDirectory(rootDir, outputHtml, callback);
            return null;
        }

        @Override
        protected void process(List<ProgressUpdate> chunks) {
            if (!chunks.isEmpty()) {
                ProgressUpdate latest = chunks.get(chunks.size() - 1);
                if (latest.phase != null) {
                    phaseLabel.setText(latest.phase);
                }
                if (latest.count >= 0) {
                    counterLabel.setText("<html><center>Processing: " + String.format("%,d", latest.count) + " items</center></html>");
                    // Since we don't have a total anymore, we can't show percentage
                    // But we can show a pulsing animation or just keep counting
                    int pseudoProgress = Math.min(99, (latest.count / 1000) % 100); // Pseudo progress
                    progressPanel.setProgress(pseudoProgress);
                }
            }
        }

        @Override
        protected void done() {
            dialog.dispose();
            try {
                get(); // Check for exceptions
                JOptionPane.showMessageDialog(
                        null,
                        "HTML file created:\n" + outputHtml.toAbsolutePath(),
                        APP_NAME,
                        JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during processing", e);
                JOptionPane.showMessageDialog(
                        null,
                        "Error: " + e.getMessage(),
                        APP_NAME + " – Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        }

        public void start() {
            execute();
            dialog.setVisible(true);
        }
    }

    /**
     * Progress update container
     */
    static class ProgressUpdate {
        final int count;
        final String phase;
        
        ProgressUpdate(int count, String phase) {
            this.count = count;
            this.phase = phase;
        }
    }

    /**
     * Custom panel for circular progress indicator
     */
    static class CircularProgressPanel extends JPanel {
        private int progress = 0;
        private static final int DIAMETER = 200;

        public CircularProgressPanel() {
            setPreferredSize(new Dimension(DIAMETER + 20, DIAMETER + 20));
            setOpaque(false);
        }

        public void setProgress(int progress) {
            this.progress = Math.min(100, Math.max(0, progress));
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int x = (getWidth() - DIAMETER) / 2;
            int y = (getHeight() - DIAMETER) / 2;

            // Draw background circle (gray)
            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(15));
            g2d.drawOval(x, y, DIAMETER, DIAMETER);

            // Draw progress arc (green)
            g2d.setColor(new Color(76, 175, 80)); // Green
            double angle = (progress / 100.0) * 360.0;
            Arc2D.Double arc = new Arc2D.Double(x, y, DIAMETER, DIAMETER, 90, -angle, Arc2D.OPEN);
            g2d.draw(arc);

            // Draw percentage text
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("SansSerif", Font.BOLD, 36));
            String percentText = progress + "%";
            FontMetrics fm = g2d.getFontMetrics();
            int textX = (getWidth() - fm.stringWidth(percentText)) / 2;
            int textY = (getHeight() + fm.getAscent()) / 2 - 5;
            g2d.drawString(percentText, textX, textY);
        }
    }
}

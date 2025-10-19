import javax.swing.*;
import java.awt.*;
import java.awt.geom.Arc2D;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
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
    private static final String VERSION = "1.3";
    private static final String AUTHOR = "Jo Zapf";
    private static final String LICENSE = "MIT";
    private static final String REPO_URL = "https://github.com/JoZapf/Java-Directory-Tree-2-html";
    private static final String LICENSE_URL = "https://opensource.org/licenses/MIT";

    private static final Logger LOGGER = Logger.getLogger(RootDirectoryListing.class.getName());

    /**
     * Statistics container for traversal results
     */
    static class TreeStats {
        long totalSize = 0;
        int folderCount = 0;
        int fileCount = 0;
    }

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
     * Main processing logic with optional progress callback - SINGLE PASS with StringBuilder
     * Returns the collected TreeStats
     */
    private static TreeStats processDirectory(Path rootDir, Path outputHtml, ProgressCallback callback) throws IOException {
        Map<String, Integer> extensionStats = new TreeMap<>();
        List<Path> unknownFiles = new ArrayList<>();
        Map<Path, Long> sizeCache = new HashMap<>();
        TreeStats stats = new TreeStats();
        StringBuilder htmlBuilder = new StringBuilder();

        if (callback != null) callback.updatePhase("Generating HTML tree...");

        LocalDateTime now = LocalDateTime.now();
        String isoGenerated = now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);

        // HTML head
        htmlBuilder.append("<!DOCTYPE html>\n<html lang='en'>\n<head>\n<meta charset='UTF-8'>\n");
        htmlBuilder.append("<title>").append(APP_NAME).append("</title>\n");

        // Metadata & links
        htmlBuilder.append("<meta name='author' content='").append(escapeHtml(AUTHOR)).append("'>\n");
        htmlBuilder.append("<meta name='license' content='").append(escapeHtml(LICENSE)).append("'>\n");
        htmlBuilder.append("<meta name='version' content='").append(escapeHtml(VERSION)).append("'>\n");
        htmlBuilder.append("<meta name='repository' content='").append(escapeHtml(REPO_URL)).append("'>\n");
        htmlBuilder.append("<meta name='generated' content='").append(escapeHtml(isoGenerated)).append("'>\n");
        htmlBuilder.append("<link rel='license' href='").append(escapeHtml(LICENSE_URL)).append("'>\n");
        htmlBuilder.append("<link rel='author' href='https://github.com/JoZapf'>\n");

        htmlBuilder.append(HtmlSnippets.getCss());
        htmlBuilder.append(HtmlSnippets.getJavaScript());
        htmlBuilder.append("</head><body>\n");

        // Header / Controls
        htmlBuilder.append("<div class='mode-toggle' onclick='toggleMode()'>☀️ Light Mode</div>\n");

        // H2 with stats - we'll build this after traversal
        String statsHeader = ""; // Placeholder

        // Tabs skeleton
        htmlBuilder.append("""
            <div class="tabs">
              <ul class="tab-header">
                <li class="active" onclick="showTab('explorer')">Explorer</li>
                <li onclick="showTab('filetypes')">File Types</li>
                <li onclick="showTab('unknownfiles')">Unknown Files</li>
              </ul>
              <div class="tab-content">
            """);

        // Explorer tab - SINGLE PASS
        htmlBuilder.append("<div id='explorer' class='tab-pane active'>\n<ul>\n");
        int[] processedCounter = new int[]{0};
        writeDirectoryRecursiveSinglePass(htmlBuilder, rootDir, new int[]{0}, extensionStats, unknownFiles,
                sizeCache, processedCounter, callback, stats);
        htmlBuilder.append("</ul>\n</div>\n");

        // File types tab
        htmlBuilder.append("<div id='filetypes' class='tab-pane'>\n<h3>File Type Overview</h3>\n");
        htmlBuilder.append("<table border='1' cellpadding='5' cellspacing='0'>\n<tr><th>File type</th><th>Count</th></tr>\n");
        for (Map.Entry<String, Integer> entry : extensionStats.entrySet()) {
            htmlBuilder.append("<tr><td>").append(escapeHtml(entry.getKey())).append("</td><td>").append(entry.getValue()).append("</td></tr>\n");
        }
        htmlBuilder.append("</table>\n</div>\n");

        // Unknown files tab
        htmlBuilder.append("<div id='unknownfiles' class='tab-pane'>\n<h3>Unknown file types (files without extension)</h3>\n<ul>\n");
        for (Path p : unknownFiles) {
            htmlBuilder.append("<li>").append(escapeHtml(p.toString().replace("\\", "/"))).append("</li>\n");
        }
        htmlBuilder.append("</ul>\n</div>\n</div>\n</div>\n");

        // Footer timestamp
        DateTimeFormatter footerFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        htmlBuilder.append("<div style='text-align: right; margin-top: 1em; font-size: 0.9em; color: gray;'>");
        htmlBuilder.append("Tree index timestamp: ").append(now.format(footerFmt));
        htmlBuilder.append("</div>\n</body></html>");

        // Now build the complete HTML with stats header
        statsHeader = buildStatsHeader(rootDir, stats);
        String completeHtml = htmlBuilder.toString();
        String marker = "</div>\n<div class=\"tabs\">";
        completeHtml = completeHtml.replace(marker, "</div>\n" + statsHeader + "\n<div class=\"tabs\">");

        // Write to file ONCE
        Files.writeString(outputHtml, completeHtml, StandardCharsets.UTF_8);
        
        // Return stats for GUI display
        return stats;
    }

    /**
     * Build the h2 header with internationalized statistics
     */
    private static String buildStatsHeader(Path rootDir, TreeStats stats) {
        String sizeStr = formatSizeInternational(stats.totalSize);
        String foldersStr = formatNumberInternational(stats.folderCount);
        String filesStr = formatNumberInternational(stats.fileCount);

        return "<h2>Tree: " + escapeHtml(rootDir.toString()) + " | " +
                sizeStr + " Total | " +
                foldersStr + " Folders | " +
                filesStr + " Files</h2>";
    }

    /**
     * Format size in GB/TB with space as thousand separator and dot as decimal
     */
    private static String formatSizeInternational(long bytes) {
        double tb = bytes / (1024.0 * 1024.0 * 1024.0 * 1024.0);
        double gb = bytes / (1024.0 * 1024.0 * 1024.0);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator('.');

        DecimalFormat df = new DecimalFormat("#,##0.00", symbols);

        if (tb >= 1.0) {
            return df.format(tb) + " TB";
        } else {
            return df.format(gb) + " GB";
        }
    }

    /**
     * Format number with space as thousand separator
     */
    private static String formatNumberInternational(int number) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.US);
        symbols.setGroupingSeparator(' ');

        DecimalFormat df = new DecimalFormat("#,##0", symbols);
        return df.format(number);
    }

    /**
     * SINGLE PASS: Recursively writes directory tree AND calculates sizes in one pass
     */
    private static void writeDirectoryRecursiveSinglePass(StringBuilder html,
                                                          Path dir,
                                                          int[] idCounter,
                                                          Map<String, Integer> extStats,
                                                          List<Path> unknownFiles,
                                                          Map<Path, Long> sizeCache,
                                                          int[] processedCounter,
                                                          ProgressCallback callback,
                                                          TreeStats stats) throws IOException {
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
            html.append("<li><span style='color:orange;'>[Access denied: ")
                    .append(escapeHtml(label)).append("]</span></li>\n");
            return;
        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Error accessing: " + dir.toAbsolutePath(), e);
            Path last = dir.getFileName();
            String label = (last == null) ? dir.toString() : last.toString();
            html.append("<li><span style='color:red;'>[Error accessing ")
                    .append(escapeHtml(label)).append("]</span></li>\n");
            return;
        }

        for (Path entry : entries) {
            if (callback != null) {
                processedCounter[0]++;
                callback.updateProgress(processedCounter[0]);
            }

            if (!Files.isReadable(entry)) {
                LOGGER.warning("Not readable: " + entry.toAbsolutePath());
                html.append("<li><span style='color:orange;'>[Not readable: ")
                        .append(escapeHtml(entry.getFileName().toString())).append("]</span></li>\n");
                continue;
            }

            String name = entry.getFileName().toString();
            String id = "node" + idCounter[0]++;

            if (Files.isDirectory(entry)) {
                LOGGER.info("Folder: " + entry.toAbsolutePath());
                stats.folderCount++;

                long dirSize = calculateDirectorySizeAndCache(entry, sizeCache);
                totalDirSize += dirSize;

                String sizeStr = formatFileSize(dirSize);
                html.append("<li class='folder'><span onclick=\"toggle(event, '").append(id).append("')\">📁 ")
                        .append(escapeHtml(name)).append("</span><span class='file-size'>").append(sizeStr).append("</span>\n");
                html.append("<ul class='nested' id='").append(id).append("'>\n");
                writeDirectoryRecursiveSinglePass(html, entry, idCounter, extStats, unknownFiles,
                        sizeCache, processedCounter, callback, stats);
                html.append("</ul></li>\n");
            } else {
                LOGGER.info("  File: " + entry.toAbsolutePath());
                stats.fileCount++;

                String ext = getExtension(name).toLowerCase(Locale.ROOT);
                if (ext.equals("unknown")) {
                    unknownFiles.add(entry.toAbsolutePath());
                }
                extStats.put(ext, extStats.getOrDefault(ext, 0) + 1);
                String icon = FileTypeIcons.getIcon(ext);

                long fileSize = 0;
                try {
                    fileSize = Files.size(entry);
                } catch (java.nio.file.AccessDeniedException e) {
                    LOGGER.warning("Access denied for size: " + entry.toAbsolutePath());
                    html.append("<li>").append(icon).append(" ").append(escapeHtml(name))
                            .append(" <small>").append(escapeHtml(ext.toUpperCase(Locale.ROOT))).append(" file</small>")
                            .append("<span class='file-size'>[Access Denied]</span></li>\n");
                    continue;
                } catch (IOException e) {
                    LOGGER.warning("Error reading size: " + entry.toAbsolutePath());
                    continue;
                }

                totalDirSize += fileSize;
                String sizeStr = formatFileSize(fileSize);
                html.append("<li>").append(icon).append(" ").append(escapeHtml(name))
                        .append(" <small>(").append(escapeHtml(ext.toUpperCase(Locale.ROOT))).append(" file)</small>")
                        .append("<span class='file-size'>").append(sizeStr).append("</span></li>\n");
            }
        }

        stats.totalSize += totalDirSize;
        sizeCache.put(dir, totalDirSize);
    }

    /**
     * Calculate directory size recursively using cache
     */
    private static long calculateDirectorySizeAndCache(Path dir, Map<Path, Long> sizeCache) {
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
                        LOGGER.fine("Access denied for size calculation: " + entry);
                    } catch (IOException e) {
                        // Skip files we can't read
                    }
                }
            }
        } catch (java.nio.file.AccessDeniedException e) {
            LOGGER.fine("Access denied to directory: " + dir);
        } catch (IOException e) {
            // Return size calculated so far
        }

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

    private static String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "unknown";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }

    interface ProgressCallback {
        void updateProgress(int current);
        void updatePhase(String phase);
    }

    static class ProcessingDialog extends SwingWorker<Void, ProgressUpdate> {
        private static final int DIAMETER = 200;
        private final Path rootDir;
        private final Path outputHtml;
        private final JDialog dialog;
        private final CircularProgressPanel progressPanel;
        private final JLabel phaseLabel;
        private final JLabel counterLabel;
        private final JLabel hintLabel;
        private TreeStats finalStats = null;

        public ProcessingDialog(Path rootDir, Path outputHtml) {
            this.rootDir = rootDir;
            this.outputHtml = outputHtml;

            dialog = new JDialog((Frame) null, APP_NAME, true);
            dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
            dialog.setSize(500, 570); // Adjusted for taller counter container
            dialog.setLocationRelativeTo(null);
            dialog.setLayout(new BorderLayout());

            JPanel centerPanel = new JPanel(new GridBagLayout());
            centerPanel.setBackground(new Color(64, 64, 64));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.gridx = 0;
            gbc.gridy = 0;
            gbc.insets = new Insets(10, 10, 10, 10);
            gbc.anchor = GridBagConstraints.CENTER;

            phaseLabel = new JLabel("Initializing...", SwingConstants.CENTER);
            phaseLabel.setForeground(Color.WHITE);
            phaseLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
            centerPanel.add(phaseLabel, gbc);

            gbc.gridy = 1;
            progressPanel = new CircularProgressPanel();
            centerPanel.add(progressPanel, gbc);

            gbc.gridy = 2;
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.weightx = 0;

            JPanel counterContainer = new JPanel(new BorderLayout());
            counterContainer.setPreferredSize(new Dimension(DIAMETER, 80)); // Increased for multi-line stats
            counterContainer.setBackground(new Color(64, 64, 64));
            counterContainer.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(85, 85, 85), 1),
                    BorderFactory.createEmptyBorder(10, 15, 10, 15)
            ));

            counterLabel = new JLabel("<html><center>Processing: 0 items total</center></html>", SwingConstants.CENTER);
            counterLabel.setForeground(Color.WHITE);
            counterLabel.setFont(new Font("SansSerif", Font.PLAIN, 14));
            counterContainer.add(counterLabel, BorderLayout.CENTER);

            centerPanel.add(counterContainer, gbc);

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

            // Process and capture stats
            finalStats = processDirectory(rootDir, outputHtml, callback);
            
            // Update to completed
            publish(new ProgressUpdate(-1, "Completed!"));
            
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
                    int pseudoProgress = Math.min(99, (latest.count / 1000) % 100);
                    progressPanel.setProgress(pseudoProgress);
                }
            }
        }

        @Override
        protected void done() {
            try {
                get(); // Check for exceptions
                
                // Display final stats in counter label (replaces counter)
                if (finalStats != null) {
                    String sizeStr = formatSizeInternational(finalStats.totalSize);
                    String foldersStr = formatNumberInternational(finalStats.folderCount);
                    String filesStr = formatNumberInternational(finalStats.fileCount);
                    
                    counterLabel.setText("<html><center>" +
                        rootDir.toString() + "<br>" +
                        sizeStr + " Total | " +
                        foldersStr + " Folders | " +
                        filesStr + " Files</center></html>");
                }
                
                // Update UI to show completion
                progressPanel.setProgress(100);
                phaseLabel.setText("✅ Success!");
                hintLabel.setVisible(false);
                
                // Calculate position for success popup (above the progress dialog)
                Point dialogLocation = dialog.getLocationOnScreen();
                int popupX = dialogLocation.x + 150;
                int popupY = dialogLocation.y - 150; // 100px above
                
                // Show success message in positioned dialog
                JDialog successDialog = new JDialog(dialog, "Success", true);
                successDialog.setLayout(new BorderLayout());
                successDialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
                
                JPanel messagePanel = new JPanel(new BorderLayout());
                messagePanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                
                JLabel messageLabel = new JLabel("<html><center>HTML file created:<br>" + 
                    outputHtml.toAbsolutePath() + "</center></html>", SwingConstants.CENTER);
                messagePanel.add(messageLabel, BorderLayout.CENTER);
                
                JButton okButton = new JButton("OK");
                okButton.addActionListener(e -> {
                    successDialog.dispose();
                    dialog.dispose(); // Close progress dialog after OK
                });
                
                JPanel buttonPanel = new JPanel();
                buttonPanel.add(okButton);
                messagePanel.add(buttonPanel, BorderLayout.SOUTH);
                
                successDialog.add(messagePanel);
                successDialog.pack();
                successDialog.setLocation(popupX, popupY);
                successDialog.setVisible(true); // Blocks until OK is clicked
                
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Error during processing", e);
                dialog.dispose();
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

    static class ProgressUpdate {
        final int count;
        final String phase;

        ProgressUpdate(int count, String phase) {
            this.count = count;
            this.phase = phase;
        }
    }

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

            g2d.setColor(new Color(100, 100, 100));
            g2d.setStroke(new BasicStroke(15));
            g2d.drawOval(x, y, DIAMETER, DIAMETER);

            g2d.setColor(new Color(76, 175, 80));
            double angle = (progress / 100.0) * 360.0;
            Arc2D.Double arc = new Arc2D.Double(x, y, DIAMETER, DIAMETER, 90, -angle, Arc2D.OPEN);
            g2d.draw(arc);

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
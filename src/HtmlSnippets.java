/**
 * Provides HTML, CSS, and JavaScript code snippets for the generated directory tree.
 * <p>
 * This class contains the complete styling and interactive functionality
 * required for the HTML output. All methods return text blocks with embedded
 * code that is injected into the generated HTML file.
 * </p>
 * <p>
 * Features included:
 * </p>
 * <ul>
 *   <li>Dark/Light mode toggle with persistent styling</li>
 *   <li>Collapsible tree structure with smooth transitions</li>
 *   <li>Tab-based navigation (Explorer, File Types, Unknown Files)</li>
 *   <li>Responsive layout with file size display</li>
 *   <li>Interactive folder expansion/collapse</li>
 * </ul>
 *
 * @author Jo Zapf
 * @version 1.3
 * @since 1.0
 */
public class HtmlSnippets {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private HtmlSnippets() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Returns the complete CSS stylesheet for the directory tree HTML.
     * <p>
     * The stylesheet includes:
     * </p>
     * <ul>
     *   <li>Base layout and typography</li>
     *   <li>Dark and light mode color schemes</li>
     *   <li>Collapsible tree structure styling</li>
     *   <li>Tab navigation interface</li>
     *   <li>File size display formatting</li>
     *   <li>Mode toggle button styling</li>
     * </ul>
     *
     * @return CSS code as a text block string ready for injection into HTML
     * @see #getJavaScript()
     */
    public static String getCss() {
        return """
        <style>
        body {
            font-family: sans-serif;
            max-width: 1200px;
            margin: 0 auto;
            padding: 20px;
            background-color: #1e1e1e;
            color: #ddd;
        }

        ul { list-style-type: none; padding-left: 1em; }
        li {
            border-bottom: 1px solid #ddd;
            padding: 4px 0;
            position: relative;
        }
        body.dark li {
            border-bottom: 1px solid #555;
        }
        li.folder > span { cursor: pointer; font-weight: bold; }
        .file-size {
            position: absolute;
            right: 2vw;
            color: #666;
            font-size: 0.9em;
        }
        body.dark .file-size {
            color: #aaa;
        }
        .nested { display: none; }
        .nested.visible { display: block; }

        .tab-pane { display: none; }
        .tab-pane.active { display: block; }

        .tabs { margin-top: 2em; }
        .tab-header {
            display: flex;
            list-style-type: none;
            padding: 0;
            margin: 0 0 10px 0;
            border-bottom: 2px solid #ccc;
        }
        .tab-header li {
            padding: 10px 20px;
            cursor: pointer;
            background: #eee;
            margin-right: 5px;
            border-top-left-radius: 5px;
            border-top-right-radius: 5px;
        }
        .tab-header li.active {
            background: #fff;
            border: 1px solid #ccc;
            border-bottom: none;
            font-weight: bold;
        }
        .tab-pane {
            display: none;
            border: 1px solid #ccc;
            padding: 15px;
            background: #fff;
        }
        .tab-pane.active {
            display: block;
        }

        body.light {
            background-color: #fff;
            color: #333;
        }
        body.light li {
            border-bottom: 1px solid #ddd;
        }

        body.light .tab-header li {
            background: #eee;
            border-color: #ccc;
            color: #333;
        }
        body.dark .tab-header li {
            background: #333;
            border-color: #555;
            color: #eee;
        }

        body.light .tab-header li.active {
            background: #fff;
            border-color: #ccc;
        }
        body.dark .tab-header li.active {
            background: #1e1e1e;
            border-color: #888;
        }

        body.light .tab-pane {
            background: #fff;
            border-color: #ccc;
            color: #333;
        }
        body.dark .tab-pane {
            background: #2c2c2c;
            border-color: #555;
            color: #ddd;
        }

        .mode-toggle {
            position: absolute;
            top: 1em;
            right: 2em;
            padding: 5px 10px;
            background: #444;
            color: #ddd;
            border-radius: 5px;
            font-size: 0.9em;
            cursor: pointer;
            user-select: none;
            z-index: 1000;
        }

        body.light .mode-toggle {
            background: #ccc;
            color: #333;
        }
        </style>
        """;
    }

    /**
     * Returns the JavaScript code for interactive tree functionality.
     * <p>
     * The JavaScript provides:
     * </p>
     * <ul>
     *   <li>Folder expand/collapse toggle functionality</li>
     *   <li>Tab switching between Explorer, File Types, and Unknown Files</li>
     *   <li>Dark/Light mode toggle with visual feedback</li>
     *   <li>DOM manipulation for dynamic UI updates</li>
     *   <li>Event handling for user interactions</li>
     * </ul>
     * <p>
     * The code includes:
     * </p>
     * <ul>
     *   <li><code>toggle(event, id)</code> - Expands/collapses folder contents</li>
     *   <li><code>showTab(id)</code> - Switches between different tab views</li>
     *   <li><code>toggleMode()</code> - Switches between dark and light themes</li>
     * </ul>
     *
     * @return JavaScript code as a text block string ready for injection into HTML
     * @see #getCss()
     */
    public static String getJavaScript() {
        return """
        <script>
        function toggle(event, id) {
            const target = document.getElementById(id);
            if (target) {
                target.classList.toggle("visible");
            }
            event.stopPropagation();
        }

        function showTab(id) {
            document.querySelectorAll('.tab-pane').forEach(el => el.classList.remove('active'));
            document.querySelectorAll('.tab-header li').forEach(el => el.classList.remove('active'));
            document.getElementById(id).classList.add('active');

            const ids = ['explorer', 'filetypes', 'unknownfiles'];
            const index = ids.indexOf(id);
            if (index !== -1) {
                document.querySelectorAll('.tab-header li')[index].classList.add('active');
            }
        }

        function toggleMode() {
            const body = document.body;
            const toggle = document.querySelector('.mode-toggle');
            if (body.classList.contains('dark')) {
                body.classList.remove('dark');
                body.classList.add('light');
                toggle.textContent = '🌙 Dark Mode';
            } else {
                body.classList.remove('light');
                body.classList.add('dark');
                toggle.textContent = '☀️ Light Mode';
            }
        }
        
        // Set dark mode as default on load
        document.addEventListener('DOMContentLoaded', function() {
            document.body.classList.add('dark');
            const toggle = document.querySelector('.mode-toggle');
            if (toggle) {
                toggle.textContent = '☀️ Light Mode';
            }
        });
        </script>
        """;
    }
}

public class HtmlSnippets {

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

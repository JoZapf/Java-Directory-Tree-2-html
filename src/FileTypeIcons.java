import java.util.HashMap;
import java.util.Map;
public class FileTypeIcons {

    private static final Map<String, String> fileIcons = new HashMap<>();

    static {
        fileIcons.put("txt", "📄");
        fileIcons.put("pdf", "📕");
        fileIcons.put("doc", "📘");
        fileIcons.put("docx", "📘");
        fileIcons.put("xls", "📊");
        fileIcons.put("xlsx", "📊");
        fileIcons.put("csv", "📈");
        fileIcons.put("png", "🖼️");
        fileIcons.put("jpg", "🖼️");
        fileIcons.put("jpeg", "🖼️");
        fileIcons.put("gif", "🖼️");
        fileIcons.put("svg", "🖌️");
        fileIcons.put("bmp", "🖼️");
        fileIcons.put("mp3", "🎵");
        fileIcons.put("wav", "🎶");
        fileIcons.put("ogg", "🎶");
        fileIcons.put("mp4", "🎞️");
        fileIcons.put("mkv", "📽️");
        fileIcons.put("avi", "📽️");
        fileIcons.put("mov", "🎬");
        fileIcons.put("zip", "🗜️");
        fileIcons.put("rar", "🗜️");
        fileIcons.put("7z", "🗜️");
        fileIcons.put("tar", "🗜️");
        fileIcons.put("gz", "🗜️");
        fileIcons.put("exe", "⚙️");
        fileIcons.put("msi", "⚙️");
        fileIcons.put("apk", "📱");
        fileIcons.put("jar", "☕");
        fileIcons.put("java", "📦");
        fileIcons.put("class", "🔧");
        fileIcons.put("cpp", "💻");
        fileIcons.put("c", "💻");
        fileIcons.put("h", "💻");
        fileIcons.put("py", "🐍");
        fileIcons.put("js", "🧩");
        fileIcons.put("ts", "🧩");
        fileIcons.put("html", "🌐");
        fileIcons.put("htm", "🌐");
        fileIcons.put("css", "🎨");
        fileIcons.put("xml", "📄");
        fileIcons.put("json", "🧾");
        fileIcons.put("yml", "⚙️");
        fileIcons.put("yaml", "⚙️");
        fileIcons.put("md", "📝");
        fileIcons.put("log", "📜");
        fileIcons.put("sql", "💾");
        fileIcons.put("db", "💽");
        fileIcons.put("ini", "⚙️");
        fileIcons.put("cfg", "⚙️");
        fileIcons.put("conf", "⚙️");
        fileIcons.put("bat", "📁");
        fileIcons.put("sh", "🐚");
        fileIcons.put("ps1", "🖥️");
        fileIcons.put("ttf", "🔤");
        fileIcons.put("otf", "🔠");
        fileIcons.put("woff", "🔡");
        fileIcons.put("woff2", "🔡");
        fileIcons.put("eot", "🔣");
        fileIcons.put("svgfont", "🔤");
        fileIcons.put("bak", "📦");
        fileIcons.put("tmp", "❄️");
        fileIcons.put("lock", "🔒");
        fileIcons.put("unbekannt", "❓");

        // ... (weitere bis 128+ Einträge möglich)
    }

    public static String getIcon(String extension) {
        return fileIcons.getOrDefault(extension.toLowerCase(), "📄");
    }
}

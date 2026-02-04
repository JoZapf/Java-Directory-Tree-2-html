import java.util.HashMap;
import java.util.Map;

/**
 * Provides file type icon mapping using Unicode emoji characters.
 * <p>
 * This class maintains a static mapping between file extensions and their
 * corresponding Unicode emoji representations for visual display in the
 * generated HTML directory tree.
 * </p>
 * <p>
 * Supported file types include:
 * </p>
 * <ul>
 *   <li>Documents (TXT, PDF, DOC, DOCX)</li>
 *   <li>Spreadsheets (XLS, XLSX, CSV)</li>
 *   <li>Images (PNG, JPG, JPEG, GIF, SVG, BMP)</li>
 *   <li>Audio (MP3, WAV, OGG)</li>
 *   <li>Video (MP4, MKV, AVI, MOV)</li>
 *   <li>Archives (ZIP, RAR, 7Z, TAR, GZ)</li>
 *   <li>Programming files (JAVA, CLASS, CPP, C, PY, JS, TS, HTML, CSS)</li>
 *   <li>Configuration files (XML, JSON, YML, YAML, INI, CFG, CONF)</li>
 *   <li>System files (BAT, SH, PS1, EXE, MSI, APK, JAR)</li>
 *   <li>Fonts (TTF, OTF, WOFF, WOFF2, EOT)</li>
 *   <li>And many more...</li>
 * </ul>
 *
 * @author Jo Zapf
 * @version 1.3
 * @since 1.0
 */
public class FileTypeIcons {

    /**
     * Private constructor to prevent instantiation.
     * This is a utility class with only static methods.
     */
    private FileTypeIcons() {
        throw new AssertionError("Utility class - do not instantiate");
    }

    /**
     * Static map containing file extension to emoji icon mappings.
     * Keys are lowercase file extensions, values are Unicode emoji characters.
     */
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

    /**
     * Returns the appropriate emoji icon for a given file extension.
     * <p>
     * The method performs a case-insensitive lookup in the icon mapping.
     * If the extension is not found, a default document icon (📄) is returned.
     * </p>
     *
     * @param extension the file extension to look up (with or without leading dot)
     * @return the Unicode emoji icon representing the file type,
     *         or "📄" (document icon) as default for unknown extensions
     * @see #fileIcons
     */
    public static String getIcon(String extension) {
        return fileIcons.getOrDefault(extension.toLowerCase(), "📄");
    }
}

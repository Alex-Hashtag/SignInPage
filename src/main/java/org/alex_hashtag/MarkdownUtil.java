package org.alex_hashtag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MarkdownUtil {

    public static String renderMarkdownToHtml(String md) {
        if (md == null || md.isEmpty()) return "";

        md = escapeHtml(md);

        StringBuilder html = new StringBuilder();
        String[] lines = md.split("\n");

        boolean inUl = false;
        boolean inOl = false;
        boolean inBlockquote = false;
        boolean inCodeBlock = false;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            // Code block start/end
            if (line.trim().startsWith("```")) {
                if (inCodeBlock) {
                    html.append("</code></pre>\n");
                } else {
                    html.append("<pre><code>\n");
                }
                inCodeBlock = !inCodeBlock;
                continue;
            }

            if (inCodeBlock) {
                html.append(line).append("\n");
                continue;
            }

            // Horizontal rules
            if (line.matches("^\\s*(\\*\\s*){3,}$") || line.matches("^\\s*(-\\s*){3,}$") || line.matches("^\\s*(_\\s*){3,}$")) {
                html.append("<hr/>\n");
                continue;
            }

            // Headings (###### to #)
            for (int h = 6; h >= 1; h--) {
                String prefix = "#".repeat(h) + " ";
                if (line.startsWith(prefix)) {
                    html.append("<h").append(h).append(">")
                            .append(parseInline(line.substring(h + 1).trim()))
                            .append("</h").append(h).append(">\n");
                    line = null;
                    break;
                }
            }
            if (line == null) continue;

            // Blockquote
            if (line.trim().startsWith(">")) {
                if (!inBlockquote) {
                    html.append("<blockquote>\n");
                    inBlockquote = true;
                }
                html.append(parseInline(line.trim().substring(1).trim())).append("<br/>\n");

                if (i + 1 >= lines.length || !lines[i + 1].trim().startsWith(">")) {
                    html.append("</blockquote>\n");
                    inBlockquote = false;
                }
                continue;
            }

            // Ordered List
            if (line.matches("^\\s*\\d+\\.\\s+.*")) {
                if (!inOl) {
                    html.append("<ol>\n");
                    inOl = true;
                }
                html.append("<li>").append(parseInline(line.trim().replaceFirst("^\\d+\\.\\s+", ""))).append("</li>\n");

                if (i + 1 >= lines.length || !lines[i + 1].matches("^\\s*\\d+\\.\\s+.*")) {
                    html.append("</ol>\n");
                    inOl = false;
                }
                continue;
            }

            // Unordered List
            if (line.matches("^\\s*([-+*])\\s+.*")) {
                if (!inUl) {
                    html.append("<ul>\n");
                    inUl = true;
                }
                html.append("<li>").append(parseInline(line.trim().substring(2).trim())).append("</li>\n");

                if (i + 1 >= lines.length || !lines[i + 1].matches("^\\s*([-+*])\\s+.*")) {
                    html.append("</ul>\n");
                    inUl = false;
                }
                continue;
            }

            // Paragraph
            if (!line.trim().isEmpty()) {
                html.append("<p>").append(parseInline(line.trim())).append("</p>\n");
            }
        }

        // Close unclosed tags
        if (inUl) html.append("</ul>\n");
        if (inOl) html.append("</ol>\n");
        if (inBlockquote) html.append("</blockquote>\n");
        if (inCodeBlock) html.append("</code></pre>\n");

        return html.toString().trim();
    }

    private static String parseInline(String text) {
        // Escape again for extra safety (after code blocks are handled)
        if (text == null || text.isEmpty()) return "";

        // Inline code
        text = text.replaceAll("`([^`]+)`", "<code>$1</code>");

        // Images ![alt](url)
        text = text.replaceAll("!\\[(.*?)\\]\\((.*?)\\)", "<img alt=\"$1\" src=\"$2\"/>");

        // Links [text](url)
        text = text.replaceAll("\\[(.*?)\\]\\((.*?)\\)", "<a href=\"$2\">$1</a>");

        // Bold + Italic ***text***
        text = text.replaceAll("\\*\\*\\*(.*?)\\*\\*\\*", "<b><i>$1</i></b>");

        // Bold **text**
        text = text.replaceAll("\\*\\*(.*?)\\*\\*", "<b>$1</b>");

        // Italic *text*
        text = text.replaceAll("(?<!\\*)\\*(?!\\*)(.*?)\\*(?!\\*)", "<i>$1</i>");

        // Strikethrough ~~text~~
        text = text.replaceAll("~~(.*?)~~", "<del>$1</del>");

        return text;
    }

    public static String escapeHtml(String text) {
        return text == null ? "" : text
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}

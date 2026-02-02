package top.ourisland.invertotimer.runtime;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextRenderer {

    private TextRenderer() {
    }

    public static Component renderToComponent(String input, Function<String, String> placeholderFn) {
        if (input == null) input = "";

        String s = replaceI18n(input);

        if (placeholderFn != null) {
            s = placeholderFn.apply(s);
        }

        return MiniMessage.miniMessage().deserialize(s);
    }

    public static String replaceI18n(String input) {
        if (input == null) return "";
        Matcher m = Pattern.compile("\\{i18n:([a-zA-Z0-9_.-]+)}").matcher(input);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String key = m.group(1);
            String rep;
            try {
                rep = I18n.langStrNP(key);
            } catch (Exception e) {
                rep = "{missing:" + key + "}";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }
}

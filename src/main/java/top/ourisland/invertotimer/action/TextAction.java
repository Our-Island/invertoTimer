package top.ourisland.invertotimer.action;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;

/**
 * An action for text displaying.
 *
 * @author Chiloven945
 */
public class TextAction implements Action {
    private final RuntimeContext ctx;
    private final TextType textType;

    private final String info;

    private final String title;
    private final String subtitle;

    private final Duration fadeIn;
    private final Duration stay;
    private final Duration fadeOut;

    public TextAction(
            @NonNull RuntimeContext ctx,
            @NonNull TextType textType,
            Object infoRaw
    ) {
        this.ctx = ctx;
        this.textType = textType;

        Duration fi = null, st = null, fo = null;

        if (textType == TextType.TITLE || textType == TextType.SUBTITLE) {
            String t = "";
            String s = "";

            if (infoRaw instanceof List<?> list) {
                if (!list.isEmpty()) t = String.valueOf(list.get(0));
                if (list.size() > 1) s = String.valueOf(list.get(1));

                if (list.size() > 2) fi = parseSecondsToDuration(list.get(2));
                if (list.size() > 3) st = parseSecondsToDuration(list.get(3));
                if (list.size() > 4) fo = parseSecondsToDuration(list.get(4));
            } else if (infoRaw instanceof Object[] arr) {
                if (arr.length > 0) t = String.valueOf(arr[0]);
                if (arr.length > 1) s = String.valueOf(arr[1]);

                if (arr.length > 2) fi = parseSecondsToDuration(arr[2]);
                if (arr.length > 3) st = parseSecondsToDuration(arr[3]);
                if (arr.length > 4) fo = parseSecondsToDuration(arr[4]);
            } else if (infoRaw != null) {
                if (textType == TextType.TITLE) {
                    t = String.valueOf(infoRaw);
                    s = "";
                } else {
                    t = "";
                    s = String.valueOf(infoRaw);
                }
            }

            this.title = t == null ? "" : t;
            this.subtitle = s == null ? "" : s;
            this.info = "";

            this.fadeIn = fi;
            this.stay = st;
            this.fadeOut = fo;
        } else {
            this.info = infoRaw == null ? "" : String.valueOf(infoRaw);
            this.title = "";
            this.subtitle = "";

            this.fadeIn = null;
            this.stay = null;
            this.fadeOut = null;
        }
    }

    private static Duration parseSecondsToDuration(Object o) {
        if (o == null) return null;
        try {
            if (o instanceof Number n) {
                long sec = n.longValue();
                if (sec < 0) sec = 0;
                return Duration.ofSeconds(sec);
            }
            String s = String.valueOf(o).trim();
            if (s.isEmpty()) return null;
            long sec = Long.parseLong(s);
            if (sec < 0) sec = 0;
            return Duration.ofSeconds(sec);
        } catch (Exception ignored) {
            return null;
        }
    }

    public static TextType parse(String s) {
        if (s == null) return TextType.MESSAGE;
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "actionbar" -> TextType.ACTIONBAR;
            case "title" -> TextType.TITLE;
            case "subtitle" -> TextType.SUBTITLE;
            default -> TextType.MESSAGE;
        };
    }

    @Override
    public String name() {
        return "text";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.action.text.desc");
    }

    @Override
    public void execute() {
        for (var p : ctx.players()) {
            if (!ctx.allowed(p)) continue;

            switch (textType) {
                case ACTIONBAR -> p.sendActionBar(ctx.render(info));
                case MESSAGE -> p.sendMessage(ctx.render(info));
                case TITLE, SUBTITLE -> {
                    Component t = ctx.render(title);
                    Component s = ctx.render(subtitle);

                    if (fadeIn != null || stay != null || fadeOut != null) {
                        Duration fi = fadeIn != null ? fadeIn : Duration.of(0, ChronoUnit.SECONDS);
                        Duration st = stay != null ? stay : Duration.of(2, ChronoUnit.SECONDS);
                        Duration fo = fadeOut != null ? fadeOut : Duration.of(0, ChronoUnit.SECONDS);

                        p.showTitle(Title.title(t, s, Title.Times.times(fi, st, fo)));
                    } else {
                        p.showTitle(Title.title(t, s));
                    }
                }
            }
        }
    }

    public enum TextType {
        MESSAGE,
        ACTIONBAR,
        TITLE,
        SUBTITLE
    }
}

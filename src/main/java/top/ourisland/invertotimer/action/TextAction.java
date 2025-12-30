package top.ourisland.invertotimer.action;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Locale;
import java.util.Objects;

public class TextAction implements Action {
    private final RuntimeContext ctx;
    private final TextType textType;
    private final String info;

    public TextAction(RuntimeContext ctx, TextType textType, String info) {
        this.ctx = Objects.requireNonNull(ctx);
        this.textType = Objects.requireNonNull(textType);
        this.info = info == null ? "" : info;
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
        final Component msg = ctx.render(info);

        for (var p : ctx.players()) {
            if (!ctx.allowed(p)) continue;
            switch (textType) {
                case ACTIONBAR -> p.sendActionBar(msg);
                case TITLE -> p.showTitle(Title.title(msg, Component.empty()));
                case SUBTITLE -> p.showTitle(Title.title(Component.empty(), msg));
                case MESSAGE -> p.sendMessage(msg);
            }
        }
    }

    public enum TextType {MESSAGE, ACTIONBAR, TITLE, SUBTITLE}
}

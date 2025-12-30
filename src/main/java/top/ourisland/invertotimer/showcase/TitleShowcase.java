package top.ourisland.invertotimer.showcase;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Objects;

public class TitleShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final String title;
    private final String subtitle;

    public TitleShowcase(RuntimeContext ctx, String title, String subtitle) {
        this.ctx = Objects.requireNonNull(ctx);
        this.title = title == null ? "" : title;
        this.subtitle = subtitle == null ? "" : subtitle;
    }

    @Override
    public String name() {
        return "title";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.showcase.title.desc");
    }

    @Override
    public void show() {
        // 两段都走 ctx.render：支持 {i18n:key} + MiniMessage + 你的 placeholders
        final Component t = ctx.render(title);
        final Component s = ctx.render(subtitle);

        for (var p : ctx.players()) {
            if (!ctx.allowed(p)) continue;
            p.showTitle(Title.title(t, s));
        }
    }
}

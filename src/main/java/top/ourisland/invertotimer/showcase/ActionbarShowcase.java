package top.ourisland.invertotimer.showcase;

import net.kyori.adventure.text.Component;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Objects;

public class ActionbarShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final String text;

    public ActionbarShowcase(RuntimeContext ctx, String text) {
        this.ctx = Objects.requireNonNull(ctx);
        this.text = text == null ? "" : text;
    }

    @Override
    public String name() {
        return "actionbar";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.showcase.actionbar.desc");
    }

    @Override
    public void show() {
        final Component msg = ctx.render(text);
        for (var p : ctx.players()) {
            if (ctx.allowed(p)) p.sendActionBar(msg);
        }
    }
}

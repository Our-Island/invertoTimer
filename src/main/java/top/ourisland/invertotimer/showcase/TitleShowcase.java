package top.ourisland.invertotimer.showcase;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
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
        final Component t = ctx.render(title);
        final Component s = ctx.render(subtitle);

        for (var p : ctx.players()) {
            if (!ctx.allowed(p)) continue;
            p.showTitle(Title.title(t, s, Title.Times.times(
                    Duration.of(0, ChronoUnit.SECONDS),
                    Duration.of(2, ChronoUnit.SECONDS),
                    Duration.of(0, ChronoUnit.SECONDS)
            )));
        }
    }
}

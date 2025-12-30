package top.ourisland.invertotimer.showcase;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.function.Supplier;

public class BossbarShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final String text;
    private final BossBar bossBar;
    private final Supplier<Float> progressSupplier;

    public BossbarShowcase(
            RuntimeContext ctx,
            String text,
            Supplier<Float> progressSupplier
    ) {
        this.ctx = ctx;
        this.text = text;
        this.progressSupplier = progressSupplier;
        this.bossBar = BossBar.bossBar(
                Component.empty(),
                1.0f,
                BossBar.Color.BLUE,
                BossBar.Overlay.PROGRESS
        );
    }


    @Override
    public String name() {
        return "bossbar";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.showcase.bossbar.desc");
    }

    @Override
    public void show() {
        bossBar.name(ctx.render(text));

        float p = progressSupplier.get();
        if (p < 0f) p = 0f;
        if (p > 1f) p = 1f;
        bossBar.progress(p);

        for (Player p0 : ctx.players()) {
            if (!ctx.allowed(p0)) {
                p0.hideBossBar(bossBar);
                continue;
            }
            p0.showBossBar(bossBar);
        }
    }

    public void showTo(Player p) {
        if (!ctx.allowed(p)) return;
        bossBar.name(ctx.render(text));
        bossBar.progress(progressSupplier.get());
        p.showBossBar(bossBar);
    }

    public void hideFrom(Player p) {
        p.hideBossBar(bossBar);
    }
}

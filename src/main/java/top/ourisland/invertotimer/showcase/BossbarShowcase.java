package top.ourisland.invertotimer.showcase;

import com.velocitypowered.api.proxy.Player;
import lombok.NonNull;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Locale;
import java.util.function.Supplier;

/**
 * A showcase using a bossbar to display information.
 *
 * @author Chiloven945
 */
public class BossbarShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final Supplier<Object> textSupplier;
    private final BossBar bossBar;
    private final Supplier<Float> progressSupplier;

    public BossbarShowcase(
            RuntimeContext ctx,
            Supplier<Object> textSupplier,
            Supplier<Float> progressSupplier,
            String colorName
    ) {
        this.ctx = ctx;
        this.textSupplier = textSupplier;
        this.progressSupplier = progressSupplier;

        BossBar.Color color = parseColor(colorName);

        this.bossBar = BossBar.bossBar(
                Component.empty(),
                1.0f,
                color,
                BossBar.Overlay.PROGRESS
        );
    }

    private static BossBar.Color parseColor(@NonNull String s) {
        return switch (s.toLowerCase(Locale.ROOT)) {
            case "pink" -> BossBar.Color.PINK;
            case "red" -> BossBar.Color.RED;
            case "green" -> BossBar.Color.GREEN;
            case "yellow" -> BossBar.Color.YELLOW;
            case "purple" -> BossBar.Color.PURPLE;
            case "white" -> BossBar.Color.WHITE;
            default -> BossBar.Color.BLUE;
        };
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
        float prog = progressSupplier.get();
        if (prog < 0f) prog = 0f;
        if (prog > 1f) prog = 1f;
        bossBar.progress(prog);

        final String raw = String.valueOf(textSupplier.get());

        for (Player p : ctx.players()) {
            if (!ctx.allowed(p)) {
                p.hideBossBar(bossBar);
                continue;
            }

            bossBar.name(ctx.render(p, raw));
            p.showBossBar(bossBar);
        }
    }

    public void showTo(Player p) {
        if (!ctx.allowed(p)) return;

        bossBar.name(ctx.render(p, String.valueOf(textSupplier.get())));

        float prog = progressSupplier.get();
        if (prog < 0f) prog = 0f;
        if (prog > 1f) prog = 1f;
        bossBar.progress(prog);

        p.showBossBar(bossBar);
    }

    public void hideFrom(Player p) {
        p.hideBossBar(bossBar);
    }
}

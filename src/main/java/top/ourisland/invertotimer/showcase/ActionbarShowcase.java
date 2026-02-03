package top.ourisland.invertotimer.showcase;

import lombok.NonNull;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.function.Supplier;

/**
 * A showcase using actionbar to display information.
 *
 * @author Chiloven945
 */
public class ActionbarShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final Supplier<Object> textSupplier;

    public ActionbarShowcase(
            @NonNull RuntimeContext ctx,
            @NonNull Supplier<Object> textSupplier
    ) {
        this.ctx = ctx;
        this.textSupplier = textSupplier;
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
        final String raw = String.valueOf(textSupplier.get());
        ctx.players().stream()
                .filter(ctx::allowed)
                .forEach(
                        p -> p.sendActionBar(ctx.render(p, raw))
                );
    }
}

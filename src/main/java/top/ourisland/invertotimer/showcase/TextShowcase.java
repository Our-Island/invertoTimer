package top.ourisland.invertotimer.showcase;

import lombok.NonNull;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.function.Supplier;

/**
 * A showcase using chat message to display information.
 *
 * @author Chiloven945
 */
public class TextShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final Supplier<Object> textSupplier;

    public TextShowcase(
            @NonNull RuntimeContext ctx,
            @NonNull Supplier<Object> textSupplier
    ) {
        this.ctx = ctx;
        this.textSupplier = textSupplier;
    }

    @Override
    public String name() {
        return "text";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.showcase.text.desc");
    }

    @Override
    public void show() {
        final String raw = String.valueOf(textSupplier.get());
        ctx.players().stream()
                .filter(ctx::allowed)
                .forEach(
                        p -> p.sendMessage(ctx.render(raw))
                );
    }
}

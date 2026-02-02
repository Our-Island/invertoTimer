package top.ourisland.invertotimer.showcase;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.time.Duration;
import java.util.List;
import java.util.function.Supplier;

/**
 * A showcase using title and subtitle to display information.
 *
 * @author Chiloven945
 */
public class TitleShowcase implements Showcase {
    private final RuntimeContext ctx;
    private final Supplier<Object> textSupplier;

    public TitleShowcase(
            @NonNull RuntimeContext ctx,
            @NonNull Supplier<Object> textSupplier
    ) {
        this.ctx = ctx;
        this.textSupplier = textSupplier;
    }

    @Override
    public String name() {
        return "title";
    }

    @Override
    public String description() {
        return "Show title";
    }

    @Override
    public void show() {
        Parsed p0 = parse(textSupplier.get());

        Component t = ctx.render(p0.title());
        Component s = ctx.render(p0.subtitle());

        Title.Times times = Title.Times.times(
                Duration.ofSeconds(p0.fadeIn()),
                Duration.ofSeconds(p0.stay()),
                Duration.ofSeconds(p0.fadeOut())
        );

        ctx.players().stream()
                .filter(ctx::allowed)
                .forEach(
                        p -> p.showTitle(Title.title(t, s, times))
                );
    }

    private static Parsed parse(Object raw) {
        String title = "";
        String subtitle = "";
        long fadeIn = 0;
        long stay = 2;
        long fadeOut = 0;

        if (raw instanceof List<?> list) {
            if (!list.isEmpty()) title = String.valueOf(list.get(0));
            if (list.size() > 1) subtitle = String.valueOf(list.get(1));
            if (list.size() > 2) fadeIn = parseSeconds(list.get(2), 0);
            if (list.size() > 3) stay = parseSeconds(list.get(3), 2);
            if (list.size() > 4) fadeOut = parseSeconds(list.get(4), 0);
        } else if (raw instanceof Object[] arr) {
            if (arr.length > 0) title = String.valueOf(arr[0]);
            if (arr.length > 1) subtitle = String.valueOf(arr[1]);
            if (arr.length > 2) fadeIn = parseSeconds(arr[2], 0);
            if (arr.length > 3) stay = parseSeconds(arr[3], 2);
            if (arr.length > 4) fadeOut = parseSeconds(arr[4], 0);
        } else if (raw != null) {
            title = String.valueOf(raw);
            subtitle = "";
        }

        if (title == null) title = "";
        if (subtitle == null) subtitle = "";

        if (fadeIn < 0) fadeIn = 0;
        if (stay < 0) stay = 0;
        if (fadeOut < 0) fadeOut = 0;

        return new Parsed(title, subtitle, fadeIn, stay, fadeOut);
    }

    private static long parseSeconds(Object o, long def) {
        if (o instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(o));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    private record Parsed(
            String title,
            String subtitle,
            long fadeIn,
            long stay,
            long fadeOut
    ) {
    }
}

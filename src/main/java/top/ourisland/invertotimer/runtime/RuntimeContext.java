package top.ourisland.invertotimer.runtime;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * Runtime context shared by showcases and actions.
 * <p>
 * This context provides:
 * <ul>
 *   <li>player selection and limitation checks</li>
 *   <li>ONE unified rendering entry via {@link PlaceholderEngine}</li>
 *   <li>optional MiniPlaceholders support via MiniMessage TagResolver</li>
 * </ul>
 */
public final class RuntimeContext {
    @Getter
    private final ProxyServer proxy;
    private final Predicate<Player> allowed;

    private final PlaceholderEngine engine;
    private final Supplier<PlaceholderEngine.Context> ctxSupplier;

    private final TagResolver mpGlobal;
    private final TagResolver mpAudience;

    @Getter
    private final Logger logger;

    public RuntimeContext(
            @NonNull ProxyServer proxy,
            @NonNull Predicate<Player> allowed,
            @NonNull PlaceholderEngine engine,
            @NonNull Supplier<PlaceholderEngine.Context> ctxSupplier,
            @NonNull Logger logger
    ) {
        this.proxy = proxy;
        this.allowed = allowed;
        this.engine = engine;
        this.ctxSupplier = ctxSupplier;

        TagResolver g = TagResolver.empty();
        TagResolver a = TagResolver.empty();

        if (proxy.getPluginManager().getPlugin("miniplaceholders").isPresent()) {
            try {
                MiniPlaceholdersHook hook = new MiniPlaceholdersHook();
                g = hook.global();
                a = hook.audience();
                logger.info("MiniPlaceholders detected: placeholder tags are enabled.");
            } catch (Throwable t) {
                logger.warn("MiniPlaceholders detected but failed to initialize integration. Continuing without it.", t);
            }
        }

        this.mpGlobal = g;
        this.mpAudience = a;

        this.logger = logger;
    }

    public Collection<Player> players() {
        return proxy.getAllPlayers();
    }

    public boolean allowed(Player p) {
        return allowed.test(p);
    }

    public Component render(String text) {
        return render(null, text);
    }

    /**
     * Render a player-visible MiniMessage text with ALL placeholders applied.
     */
    public Component render(Player player, String text) {
        final PlaceholderEngine.Context ctx = ctxSupplier.get();

        TagResolver resolver = (player == null)
                ? TagResolver.resolver(mpGlobal)
                : TagResolver.resolver(mpGlobal, mpAudience);

        return engine.renderToComponent(text, player, resolver, ctx);
    }

    public String renderString(String text) {
        return renderString(null, text);
    }

    /**
     * Render a plain string (no MiniMessage parsing).
     * <p>
     * Intended for command strings and other non-player-facing texts.
     */
    public String renderString(Player player, String text) {
        final PlaceholderEngine.Context ctx = ctxSupplier.get();
        return engine.apply(text, ctx);
    }
}

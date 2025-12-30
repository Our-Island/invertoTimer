package top.ourisland.invertotimer.runtime;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;

import java.util.Collection;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

public final class RuntimeContext {
    private final ProxyServer proxy;
    private final Predicate<Player> allowed;
    private final Function<String, String> placeholder;

    public RuntimeContext(
            ProxyServer proxy,
            Predicate<Player> allowed,
            Function<String, String> placeholder
    ) {
        this.proxy = Objects.requireNonNull(proxy);
        this.allowed = Objects.requireNonNull(allowed);
        this.placeholder = Objects.requireNonNull(placeholder);
    }

    public ProxyServer proxy() {
        return proxy;
    }

    public Collection<Player> players() {
        return proxy.getAllPlayers();
    }

    public boolean allowed(Player p) {
        return allowed.test(p);
    }


    public Component render(String text) {
        return TextRenderer.renderToComponent(text, placeholder);
    }

    public String renderString(String text) {
        return placeholder.apply(TextRenderer.replaceI18n(text));
    }
}

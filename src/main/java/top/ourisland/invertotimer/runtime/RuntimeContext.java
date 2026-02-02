package top.ourisland.invertotimer.runtime;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import lombok.NonNull;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Predicate;

public final class RuntimeContext {
    @Getter
    private final ProxyServer proxy;
    private final Predicate<Player> allowed;
    private final Function<String, String> placeholder;

    @Getter
    private final Logger logger;

    public RuntimeContext(
            @NonNull ProxyServer proxy,
            @NonNull Predicate<Player> allowed,
            @NonNull Function<String, String> placeholder,
            @NonNull Logger logger
    ) {
        this.proxy = proxy;
        this.allowed = allowed;
        this.placeholder = placeholder;
        this.logger = logger;
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

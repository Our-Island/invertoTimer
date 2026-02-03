package top.ourisland.invertotimer.runtime;

import io.github.miniplaceholders.api.MiniPlaceholders;
import lombok.Getter;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;

/**
 * Optional integration with MiniPlaceholders.
 * <p>
 * This class intentionally contains the only direct references to the MiniPlaceholders API. It must only be loaded/used
 * when the MiniPlaceholders plugin is present at runtime.
 */
@Getter
public final class MiniPlaceholdersHook {

    private final TagResolver global;
    private final TagResolver audience;

    public MiniPlaceholdersHook() {
        this.global = MiniPlaceholders.globalPlaceholders();
        this.audience = MiniPlaceholders.audiencePlaceholders();
    }
}

package top.ourisland.invertotimer.runtime.showcase;

import java.time.Duration;
import java.util.Locale;

public enum ShowcaseType {
    ACTIONBAR(Duration.ofSeconds(1)),
    BOSSBAR(Duration.ofSeconds(1)),
    TEXT(Duration.ofSeconds(10)),
    TITLE(Duration.ofSeconds(1));

    private final Duration defaultInterval;

    ShowcaseType(Duration defaultInterval) {
        this.defaultInterval = defaultInterval;
    }

    public static ShowcaseType fromKey(String key) {
        if (key == null) return null;
        return switch (key.toLowerCase(Locale.ROOT)) {
            case "actionbar" -> ACTIONBAR;
            case "bossbar" -> BOSSBAR;
            case "text" -> TEXT;
            case "title" -> TITLE;
            default -> null;
        };
    }

    public Duration defaultInterval() {
        return defaultInterval;
    }
}

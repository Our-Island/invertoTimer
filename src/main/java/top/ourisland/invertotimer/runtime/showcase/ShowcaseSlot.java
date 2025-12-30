package top.ourisland.invertotimer.runtime.showcase;

import top.ourisland.invertotimer.config.model.ShowcaseConfig;
import top.ourisland.invertotimer.showcase.Showcase;

public final class ShowcaseSlot {
    private final ShowcaseType kind;
    private final ShowcaseConfig config;
    private final Showcase showcase;

    private long lastSentMs = 0;

    public ShowcaseSlot(ShowcaseType kind, ShowcaseConfig config, Showcase showcase) {
        this.kind = kind;
        this.config = config;
        this.showcase = showcase;
    }

    public ShowcaseType kind() {
        return kind;
    }

    public ShowcaseConfig config() {
        return config;
    }

    public Showcase showcase() {
        return showcase;
    }

    public boolean tryAcquire(long nowMs, long intervalMs) {
        if (intervalMs <= 0) return true;
        if (nowMs - lastSentMs < intervalMs) return false;
        lastSentMs = nowMs;
        return true;
    }
}

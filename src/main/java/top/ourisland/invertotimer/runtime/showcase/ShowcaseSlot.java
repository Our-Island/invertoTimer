package top.ourisland.invertotimer.runtime.showcase;

import lombok.Getter;
import top.ourisland.invertotimer.config.model.ShowcaseConfig;
import top.ourisland.invertotimer.showcase.Showcase;

@Getter
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

    public boolean tryAcquire(long nowMs, long intervalMs) {
        if (intervalMs <= 0) return true;
        if (nowMs - lastSentMs < intervalMs) return false;
        lastSentMs = nowMs;
        return true;
    }
}

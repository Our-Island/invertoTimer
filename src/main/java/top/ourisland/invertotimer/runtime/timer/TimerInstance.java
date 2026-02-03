package top.ourisland.invertotimer.runtime.timer;

import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.scheduler.ScheduledTask;
import lombok.NonNull;
import org.slf4j.Logger;
import top.ourisland.invertotimer.InvertoTimer;
import top.ourisland.invertotimer.action.Action;
import top.ourisland.invertotimer.config.ConfigManager;
import top.ourisland.invertotimer.config.model.*;
import top.ourisland.invertotimer.runtime.PlaceholderEngine;
import top.ourisland.invertotimer.runtime.RuntimeContext;
import top.ourisland.invertotimer.runtime.action.ActionFactory;
import top.ourisland.invertotimer.runtime.showcase.ShowcaseFactory;
import top.ourisland.invertotimer.runtime.showcase.ShowcaseSlot;
import top.ourisland.invertotimer.runtime.showcase.ShowcaseType;
import top.ourisland.invertotimer.showcase.BossbarShowcase;
import top.ourisland.invertotimer.showcase.Showcase;
import top.ourisland.invertotimer.util.Cron5;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

final class TimerInstance {
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final InvertoTimer plugin;

    private final ProxyServer proxy;
    private final Logger logger;

    private final ConfigManager configManager;
    private final TimerConfig cfg;
    private final ZoneId zoneId;

    private final List<ScheduledTask> actionTasks = new ArrayList<>();
    private final Map<String, ShowcaseSlot> showcaseSlots = new HashMap<>();
    private final PlaceholderEngine placeholders;
    private Instant expireAt = Instant.EPOCH;
    private Cron5 cron;
    private LocalDateTime oneTime;
    private ZonedDateTime nextTarget;
    private volatile GlobalConfig lastGlobal;
    private volatile Instant lastNow;
    private BossbarShowcase bossbarShowcase;
    private ShowcaseSlot bossbarSlot;
    private ShowcaseConfig bossBarConfig;
    private RuntimeContext ctx;

    TimerInstance(
            @NonNull final InvertoTimer plugin,
            final ProxyServer proxy,
            final Logger logger,
            final ConfigManager configManager,
            final TimerConfig cfg,
            final ZoneId zoneId
    ) {
        this.plugin = plugin;
        this.proxy = proxy;
        this.logger = logger;
        this.configManager = configManager;
        this.placeholders = new PlaceholderEngine(configManager);
        this.cfg = cfg;
        this.zoneId = zoneId;

        parseTimeSpec();
        buildRuntimeContext();
        rebuildForNewTarget();
    }

    private void parseTimeSpec() {
        try {
            if (cfg.cron() != null && !cfg.cron().isBlank()) {
                this.cron = Cron5.parse(cfg.cron());
            }
        } catch (Exception ignored) {
            this.cron = null;
        }

        try {
            if (cfg.time() != null && !cfg.time().isBlank()) {
                this.oneTime = LocalDateTime.parse(cfg.time(), TIME_FMT);
            }
        } catch (Exception ignored) {
            this.oneTime = null;
        }
    }

    private void buildRuntimeContext() {
        this.ctx = new RuntimeContext(
                proxy,
                this::isPlayerAllowedUsingLastGlobal,
                placeholders,
                () -> buildPlaceholderContext(lastNow == null ? Instant.now() : lastNow),
                logger
        );
    }

    private void rebuildForNewTarget() {
        cancelActionTasks();

        showcaseSlots.clear();
        bossbarShowcase = null;
        bossbarSlot = null;
        bossBarConfig = null;

        expireAt = Instant.EPOCH;

        if (nextTarget == null) return;

        final Instant targetInstant = nextTarget.toInstant();
        expireAt = targetInstant;

        for (ActionConfig ac : cfg.actions()) {
            final Instant at = targetInstant.plus(ac.shift());
            final Action action = ActionFactory.create(ac, ctx);
            if (action == null) continue;

            scheduleAction(at, action);
            if (at.isAfter(expireAt)) expireAt = at;
        }

        for (Map.Entry<String, ShowcaseConfig> e : cfg.showcases().entrySet()) {
            final String key = e.getKey();
            final ShowcaseConfig sc = e.getValue();
            if (sc == null || !sc.enabled()) continue;

            ShowcaseType kind = ShowcaseType.fromKey(key);
            if (kind == null) continue;

            Showcase showcase = ShowcaseFactory.create(
                    key,
                    sc,
                    ctx,
                    () -> textFor(sc, lastNow == null ? Instant.now() : lastNow),
                    () -> progressFor(sc, lastNow == null ? Instant.now() : lastNow)
            );
            if (showcase == null) continue;

            ShowcaseSlot slot = new ShowcaseSlot(kind, sc, showcase);
            showcaseSlots.put(key.toLowerCase(Locale.ROOT), slot);

            if (showcase instanceof BossbarShowcase bbs) {
                bossbarShowcase = bbs;
                bossbarSlot = slot;
                bossBarConfig = sc;
            }
        }
    }

    private boolean isPlayerAllowedUsingLastGlobal(final Player p) {
        final GlobalConfig g = lastGlobal;
        if (g == null) return true;
        return isPlayerAllowed(p, g);
    }

    private PlaceholderEngine.Context buildPlaceholderContext(final Instant now) {
        final long remainingSec = nextTarget == null ? 0 :
                Math.max(0, java.time.Duration.between(now, nextTarget.toInstant()).getSeconds());

        final String targetText = nextTarget == null ? "" : TIME_FMT.format(nextTarget);
        return new PlaceholderEngine.Context(
                cfg.id(),
                cfg.description(),
                now,
                nextTarget,
                targetText,
                remainingSec
        );
    }

    private void cancelActionTasks() {
        for (ScheduledTask t : actionTasks) {
            try {
                t.cancel();
            } catch (Exception ignored) {
            }
        }
        actionTasks.clear();
    }

    private void scheduleAction(final Instant at, final Action action) {
        long delayMs = at.toEpochMilli() - System.currentTimeMillis();
        if (delayMs < 0) delayMs = 0;

        ScheduledTask task = proxy.getScheduler()
                .buildTask(plugin, () -> {
                    lastNow = Instant.now();
                    try {
                        action.execute();
                    } catch (Exception e) {
                        logger.error("Failed executing action {} for timer {}", action.name(), cfg.id(), e);
                    }
                })
                .delay(delayMs, TimeUnit.MILLISECONDS)
                .schedule();

        actionTasks.add(task);
    }

    private Object textFor(final ShowcaseConfig sc, final Instant now) {
        if (nextTarget == null) return sc.text();

        ShowcaseConfig.After af = sc.after();
        if (af == null || af.duration() == null || af.duration().isZero() || af.text() == null) return sc.text();

        Instant target = nextTarget.toInstant();
        Instant end = target.plus(af.duration());

        if (!now.isBefore(target) && !now.isAfter(end)) return af.text();
        return sc.text();
    }

    private float progressFor(final ShowcaseConfig sc, final Instant now) {
        if (nextTarget == null) return 1.0f;
        final Instant target = nextTarget.toInstant();
        final Duration startAt = sc.startAt();

        if (startAt != null && !startAt.isZero()) {
            long total = startAt.abs().getSeconds();
            long rem = Math.max(0, Duration.between(now, target).getSeconds());
            float p = total == 0 ? 0f : (rem / (float) total);
            if (p < 0f) p = 0f;
            if (p > 1f) p = 1f;
            return p;
        }

        long rem = Math.max(0, Duration.between(now, target).getSeconds());
        return rem > 0 ? 1.0f : 0.0f;
    }

    private boolean isPlayerAllowed(final Player p, final GlobalConfig global) {
        final String serverName = p.getCurrentServer()
                .map(c -> c.getServerInfo().getName())
                .orElse(null);

        if (!global.limitation().isAllowed(serverName)) return false;
        return cfg.limitation().isAllowed(serverName);
    }

    private String animationFrameText(final String id, final Instant now) {
        if (id == null || id.isBlank()) return "";
        final AnimationConfig anim = configManager.animations().get(id);
        if (anim == null) return "";

        final List<AnimationConfig.Frame> frames = anim.frames();
        if (frames == null || frames.isEmpty()) return "";

        final long total = Math.max(1, anim.totalDurationMs());
        final long pos = Math.floorMod(now.toEpochMilli(), total);

        long acc = 0;
        for (AnimationConfig.Frame f : frames) {
            acc += Math.max(1, f.durationMs());
            if (pos < acc) {
                return f.text() == null ? "" : f.text();
            }
        }

        AnimationConfig.Frame last = frames.getLast();
        return last.text() == null ? "" : last.text();
    }

    void tick(final Instant now, final GlobalConfig global) {
        this.lastNow = now;
        this.lastGlobal = global;

        ensureNextTarget(now);
        updateShowcases(now);
    }

    void ensureNextTarget(final Instant now) {
        if (nextTarget == null) {
            nextTarget = computeNextTarget(now);
            rebuildForNewTarget();
            return;
        }

        if (now.isAfter(getExpireTime())) {
            nextTarget = computeNextTarget(now);
            rebuildForNewTarget();
        }
    }

    private void updateShowcases(final Instant now) {
        if (nextTarget == null) return;

        final long nowMs = now.toEpochMilli();

        for (ShowcaseSlot slot : showcaseSlots.values()) {
            if (!shouldShow(slot, now)) continue;

            Duration interval = slot.config().interval();
            if (interval == null) interval = slot.kind().defaultInterval();

            if (!slot.tryAcquire(nowMs, interval.toMillis())) continue;

            try {
                slot.showcase().show();
            } catch (Exception e) {
                logger.error("Failed showing {} for timer {}", slot.showcase().name(), cfg.id(), e);
            }
        }
    }

    private ZonedDateTime computeNextTarget(final Instant now) {
        final ZonedDateTime zNow = ZonedDateTime.ofInstant(now, zoneId).withSecond(0).withNano(0);
        if (oneTime != null) {
            final ZonedDateTime target = oneTime.atZone(zoneId);
            return target.isAfter(zNow) ? target : null;
        }
        if (cron != null) {
            return cron.nextAfter(zNow);
        }
        return null;
    }

    private Instant getExpireTime() {
        Instant last = expireAt;

        if (nextTarget != null) {
            final Instant target = nextTarget.toInstant();

            for (ShowcaseSlot slot : showcaseSlots.values()) {
                boolean allowAfter = switch (slot.kind()) {
                    case ACTIONBAR, BOSSBAR, TITLE -> true;
                    default -> false;
                };
                if (!allowAfter) continue;

                ShowcaseConfig.After af = slot.config().after();
                if (af == null || af.duration() == null || af.duration().isZero()) continue;

                Instant end = target.plus(af.duration());
                if (end.isAfter(last)) last = end;
            }
        }

        return last.plusSeconds(2);
    }

    private boolean shouldShow(final ShowcaseSlot slot, final Instant now) {
        if (nextTarget == null) return false;

        final ShowcaseConfig sc = slot.config();
        final Instant target = nextTarget.toInstant();

        final Instant begin = (sc.startAt() == null) ? Instant.EPOCH : target.minus(sc.startAt().abs());

        Instant end = target;

        boolean allowAfter = switch (slot.kind()) {
            case ACTIONBAR, BOSSBAR, TITLE -> true;
            default -> false;
        };

        final ShowcaseConfig.After af = sc.after();
        if (allowAfter && af != null && af.duration() != null && !af.duration().isZero()) {
            end = target.plus(af.duration());
        }

        if (now.isBefore(begin)) return false;
        return !now.isAfter(end);
    }

    String peekNext() {
        return nextTarget == null ? null : nextTarget.toString();
    }

    void refreshFor(final Player p) {
        if (bossbarShowcase == null || bossbarSlot == null) return;

        final Instant now = lastNow == null ? Instant.now() : lastNow;

        if (nextTarget == null) return;

        if (!shouldShow(bossbarSlot, now)) {
            bossbarShowcase.hideFrom(p);
            return;
        }

        if (!isPlayerAllowedUsingLastGlobal(p)) {
            bossbarShowcase.hideFrom(p);
            return;
        }

        bossbarShowcase.showTo(p);
    }

    void hideFor(final Player p) {
        if (bossbarShowcase != null) bossbarShowcase.hideFrom(p);
    }

    void dispose() {
        cancelActionTasks();

        if (bossbarShowcase != null) {
            for (Player p : proxy.getAllPlayers()) {
                try {
                    bossbarShowcase.hideFrom(p);
                } catch (Exception ignored) {
                }
            }
        }
    }
}

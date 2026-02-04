package top.ourisland.invertotimer.runtime;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import top.ourisland.invertotimer.config.ConfigManager;
import top.ourisland.invertotimer.config.model.AnimationConfig;
import top.ourisland.invertotimer.util.TimeUtil;

import java.time.Instant;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Unified text pipeline for invertoTimer.
 * <p>
 * This class is the ONLY place that processes:
 * <ul>
 *   <li>{@code {i18n:key}}</li>
 *   <li>plugin curly placeholders: {@code {remaining}}, {@code {days}}, ... (including parameter forms)</li>
 *   <li>{@code {animation:<id>}} (frames defined in animations.yml)</li>
 *   <li>MiniMessage rendering stage (deserialize to Component)</li>
 * </ul>
 *
 * <p>
 * MiniPlaceholders are supported via MiniMessage tag resolvers (passed in from RuntimeContext),
 * not by reflection and not by scattering logic elsewhere.
 *
 * <h3>Remaining format</h3>
 * {@code {remaining:...}} supports a tokenized format where ONLY parts wrapped by {@code %...%} are replaced.
 * <p>
 * Examples:
 * <pre>
 * {remaining:%hh%:%mm%:%ss%}
 * {remaining:%d%Days %hh%:%mm%:%ss%}
 * </pre>
 * Tokens:
 * <ul>
 *   <li>{@code %d%} / {@code %dd%} ... : days</li>
 *   <li>{@code %h%} / {@code %hh%} ... : hours (by default total hours if no days token is used)</li>
 *   <li>{@code %m%} / {@code %mm%} ... : minutes</li>
 *   <li>{@code %s%} / {@code %ss%} ... : seconds</li>
 * </ul>
 *
 * <p>
 * Optional suffix & hide-when-zero inside token:
 * <pre>
 * {remaining:%d:Days % %hh%:%mm%:%ss%}
 * </pre>
 * Token form {@code %d:Days %} will emit {@code "NDays "} when N&gt;0, otherwise emits empty string.
 */
public final class PlaceholderEngine {

    private static final Pattern I18N_TOKEN = Pattern.compile("\\{i18n:([a-zA-Z0-9_.-]+)}");
    private static final Pattern PLACEHOLDER = Pattern.compile("\\{([a-zA-Z0-9_.-]+)(?::([^}]*))?}");
    private static final Pattern ANIMATION = Pattern.compile("\\{animation:([a-zA-Z0-9_.-]+)}");

    private final ConfigManager configManager;

    public PlaceholderEngine(final ConfigManager configManager) {
        this.configManager = configManager;
    }

    /**
     * Render a player-visible MiniMessage text to {@link Component}.
     * <p>
     * Pipeline (single entry point):
     * <ol>
     *   <li>expand {@code {animation:<id>}}</li>
     *   <li>replace {@code {i18n:key}}</li>
     *   <li>replace plugin curly placeholders ({@code {remaining}}, {@code {days}}, ...)</li>
     *   <li>MiniMessage deserialize (with optional TagResolver, e.g. MiniPlaceholders)</li>
     * </ol>
     */
    public Component renderToComponent(
            final String input,
            final Audience audience,
            final TagResolver resolver,
            final Context ctx
    ) {
        String s = apply(input, ctx);

        TagResolver r = (resolver == null) ? TagResolver.empty() : resolver;
        if (audience != null) {
            return MiniMessage.miniMessage().deserialize(s, audience, r);
        }
        return MiniMessage.miniMessage().deserialize(s, r);
    }

    /**
     * Apply all non-MiniMessage transformations (i18n, animations, curly placeholders).
     * <p>
     * Intended for command strings or other non-component outputs.
     */
    public String apply(final String input, final Context ctx) {
        if (input == null) return "";

        // 1) animation first, so inserted frame text can be further processed
        String s = expandAnimations(input, ctx);

        // 2) i18n replacement (works for both original and animation frame text)
        s = replaceI18n(s);

        // 3) core placeholders
        return replaceCore(s, ctx);
    }

    private String expandAnimations(final String input, final Context ctx) {
        Matcher m = ANIMATION.matcher(input);
        if (!m.find()) return input;

        StringBuilder sb = new StringBuilder();
        int last = 0;

        do {
            sb.append(input, last, m.start());
            String id = m.group(1);
            sb.append(animationFrameText(id, ctx));
            last = m.end();
        } while (m.find());

        sb.append(input, last, input.length());
        return sb.toString();
    }

    /**
     * Replace {@code {i18n:key}} tokens in plain text.
     */
    public static String replaceI18n(String input) {
        if (input == null) return "";
        Matcher m = I18N_TOKEN.matcher(input);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String key = m.group(1);
            String rep;
            try {
                rep = I18n.langStrNP(key);
            } catch (Exception e) {
                rep = "{missing:" + key + "}";
            }
            m.appendReplacement(sb, Matcher.quoteReplacement(rep));
        }
        m.appendTail(sb);
        return sb.toString();
    }

    private static String replaceCore(final String input, final Context ctx) {
        Matcher m = PLACEHOLDER.matcher(input);
        if (!m.find()) return input;

        StringBuilder sb = new StringBuilder();
        int last = 0;

        do {
            sb.append(input, last, m.start());

            String key = m.group(1);
            String arg = m.group(2);

            String rep = resolve(key, arg, ctx);
            if (rep == null) {
                sb.append(m.group(0));
            } else {
                sb.append(rep);
            }

            last = m.end();
        } while (m.find());

        sb.append(input, last, input.length());
        return sb.toString();
    }

    private String animationFrameText(final String id, final Context ctx) {
        if (id == null || id.isBlank()) return "";
        Map<String, AnimationConfig> animations = configManager.animations();
        if (animations == null) return "";

        AnimationConfig cfg = animations.get(id);
        if (cfg == null) return "";

        long total = Math.max(1, cfg.totalDurationMs());
        long offset = Math.floorMod(ctx.now().toEpochMilli(), total);

        long acc = 0;
        for (AnimationConfig.Frame f : cfg.frames()) {
            long dur = Math.max(1, f.durationMs());
            acc += dur;
            if (offset < acc) return safe(f.text());
        }
        return safe(cfg.frames().isEmpty() ? "" : cfg.frames().getFirst().text());
    }

    private static String resolve(final String keyRaw, final String arg, final Context ctx) {
        if (keyRaw == null) return null;

        String key = keyRaw.toLowerCase(Locale.ROOT);

        return switch (key) {
            case "id" -> safe(ctx.id());
            case "description" -> safe(ctx.description());
            case "target" -> safe(ctx.targetText());

            case "total_seconds" -> String.valueOf(ctx.remainingSeconds());

            case "days" -> formatUnit(ctx.days(), arg, 'd');
            case "hours" -> formatUnit(ctx.hoursPart(), arg, 'h');
            case "minutes" -> formatUnit(ctx.minutesPart(), arg, 'm');
            case "seconds" -> formatUnit(ctx.secondsPart(), arg, 's');

            case "remaining" -> formatRemaining(ctx, arg);

            // handled elsewhere in this engine:
            case "i18n" -> null;
            case "animation" -> null;

            default -> null;
        };
    }

    private static String safe(String s) {
        return s == null ? "" : s;
    }

    /**
     * Format unit placeholders like:
     * <ul>
     *   <li>{@code {hours}} -> {@code 3}</li>
     *   <li>{@code {hours:hh}} -> {@code 03}</li>
     *   <li>{@code {hours:Hours}} -> {@code 3Hours} (hidden if 0)</li>
     *   <li>{@code {hours:hh:Hours}} -> {@code 03Hours} (hidden if 0)</li>
     * </ul>
     *
     * <p>Rules:
     * <ul>
     *   <li>If suffix is provided (non-empty), value==0 hides the whole placeholder.</li>
     *   <li>Padding is applied when a width token like {@code hh} is provided.</li>
     *   <li>Backward compatible with old {@code {days: 天}} form.</li>
     * </ul>
     */
    private static String formatUnit(final long value, final String arg, final char unitLetter) {
        UnitArg ua = UnitArg.parse(arg, unitLetter);

        if (!ua.suffix.isEmpty() && value <= 0) return "";

        String num = toPaddedNumber(value, ua.width);
        return num + ua.suffix;
    }

    /**
     * Format remaining time.
     * <p>
     * No arg: {@link TimeUtil#formatHMS(long)}
     * <p>
     * With arg: ONLY parts wrapped by {@code %...%} are replaced. Everything else is literal.
     * <p>
     * Token examples:
     * <ul>
     *   <li>{@code %d%}, {@code %dd%}</li>
     *   <li>{@code %h%}, {@code %hh%}</li>
     *   <li>{@code %m%}, {@code %mm%}</li>
     *   <li>{@code %s%}, {@code %ss%}</li>
     * </ul>
     * Optional suffix+hide-when-zero inside token: {@code %d:Days %}.
     */
    private static String formatRemaining(final Context ctx, final String format) {
        long sec = ctx.remainingSeconds();
        if (format == null || format.isBlank()) return TimeUtil.formatHMS(sec);

        RemainingFormat rf = RemainingFormat.parse(format);

        StringBuilder out = new StringBuilder();
        for (Piece p : rf.pieces) {
            if (p.kind == Kind.LITERAL) {
                out.append(p.literal);
                continue;
            }
            out.append(formatRemainingToken(ctx, rf, p));
        }
        return out.toString().trim();
    }

    private static String formatRemainingToken(final Context ctx, final RemainingFormat rf, final Piece p) {
        if (p.kind != Kind.UNIT) return "";

        long v;
        switch (p.unit) {
            case 'd' -> v = ctx.days();
            case 'h' -> v = rf.usesDays ? ctx.hoursPart() : ctx.totalHours();
            case 'm' -> v = (rf.usesDays || rf.usesHours) ? ctx.minutesPart() : ctx.totalMinutes();
            case 's' ->
                    v = (rf.usesDays || rf.usesHours || rf.usesMinutes) ? ctx.secondsPart() : ctx.remainingSeconds();
            default -> {
                return "";
            }
        }

        if (p.hideWhenZero && v <= 0) return "";

        String num = toPaddedNumber(v, p.width);
        return num + p.suffix;
    }

    private static String toPaddedNumber(long v, int width) {
        String s = String.valueOf(v);
        if (width <= 1) return s;
        if (s.length() >= width) return s;
        return "0".repeat(width - s.length()) + s;
    }

    private enum Kind {LITERAL, UNIT}

    /**
     * Runtime data used for placeholder expansion.
     *
     * @param id               timer id
     * @param description      timer description (raw config value; not rendered)
     * @param now              current time
     * @param target           next target time (nullable)
     * @param targetText       formatted target time (nullable/empty if target is null)
     * @param remainingSeconds remaining seconds to target (never negative)
     */
    public record Context(
            String id,
            String description,
            Instant now,
            ZonedDateTime target,
            String targetText,
            long remainingSeconds
    ) {
        public long days() {
            return remainingSeconds / 86400;
        }

        public long hoursPart() {
            return (remainingSeconds % 86400) / 3600;
        }

        public long minutesPart() {
            return (remainingSeconds % 3600) / 60;
        }

        public long secondsPart() {
            return remainingSeconds % 60;
        }

        public long totalHours() {
            return remainingSeconds / 3600;
        }

        public long totalMinutes() {
            return remainingSeconds / 60;
        }
    }

    private record Piece(Kind kind, String literal, char unit, int width, String suffix, boolean hideWhenZero) {
        static Piece literal(String s) {
            return new Piece(Kind.LITERAL, s, '\0', 0, "", false);
        }

        static Piece unit(char unit, int width, String suffix, boolean hideWhenZero) {
            return new Piece(Kind.UNIT, "", unit, width, suffix, hideWhenZero);
        }
    }

    /**
     * Parsed remaining format where tokens are wrapped by %...%. Example: "%d%Days %hh%:%mm%:%ss%"
     */
    private record RemainingFormat(List<Piece> pieces, boolean usesDays, boolean usesHours, boolean usesMinutes) {

        static RemainingFormat parse(final String fmt) {
            String f = safe(fmt);
            List<Piece> pieces = new ArrayList<>();

            boolean usesD = false, usesH = false, usesM = false;

            int i = 0;
            int lastLiteralStart = 0;

            while (i < f.length()) {
                char c = f.charAt(i);
                if (c != '%') {
                    i++;
                    continue;
                }

                if (i > lastLiteralStart) {
                    pieces.add(Piece.literal(f.substring(lastLiteralStart, i)));
                }

                int end = f.indexOf('%', i + 1);
                if (end < 0) {
                    pieces.add(Piece.literal(f.substring(i)));
                    return new RemainingFormat(pieces, usesD, usesH, usesM);
                }

                String token = f.substring(i + 1, end);
                Piece p = parseToken(token);
                pieces.add(p);

                if (p.kind == Kind.UNIT) {
                    if (p.unit == 'd') usesD = true;
                    if (p.unit == 'h') usesH = true;
                    if (p.unit == 'm') usesM = true;
                }

                i = end + 1;
                lastLiteralStart = i;
            }

            if (lastLiteralStart < f.length()) {
                pieces.add(Piece.literal(f.substring(lastLiteralStart)));
            }

            return new RemainingFormat(pieces, usesD, usesH, usesM);
        }

        /**
         * Token forms:
         * <ul>
         *   <li>"hh" -> unit h width 2</li>
         *   <li>"d"  -> unit d width 1</li>
         *   <li>"d:Days " -> unit d width 1 suffix "Days " and hide when zero</li>
         * </ul>
         */
        private static Piece parseToken(final String tokenRaw) {
            String t = safe(tokenRaw);
            if (t.isEmpty()) return Piece.literal("%%"); // defensive

            String left = t;
            String suffix = "";
            int colon = t.indexOf(':');
            if (colon >= 0) {
                left = t.substring(0, colon);
                suffix = t.substring(colon + 1); // may contain spaces etc.
            }

            if (left.isEmpty()) return Piece.literal("%" + t + "%");

            char u = Character.toLowerCase(left.charAt(0));
            if (u != 'd' && u != 'h' && u != 'm' && u != 's') {
                return Piece.literal("%" + t + "%");
            }

            for (int k = 0; k < left.length(); k++) {
                if (Character.toLowerCase(left.charAt(k)) != u) {
                    return Piece.literal("%" + t + "%");
                }
            }

            int width = left.length();
            boolean hideWhenZero = !suffix.isEmpty();
            return Piece.unit(u, width, suffix, hideWhenZero);
        }
    }

    /**
     * Parsed arguments for unit placeholders like:
     * <ul>
     *   <li>{@code {hours}} -> arg = null</li>
     *   <li>{@code {hours:hh}} -> width=2, suffix=""</li>
     *   <li>{@code {hours:Hours}} -> width=0, suffix="Hours"</li>
     *   <li>{@code {hours:hh:Hours}} -> width=2, suffix="Hours"</li>
     * </ul>
     */
    private record UnitArg(int width, String suffix) {
        static UnitArg parse(String a, char unitLetter) {
            if (a == null) return new UnitArg(0, "");
            if (a.isEmpty()) return new UnitArg(0, "");

            int firstColon = a.indexOf(':');
            if (firstColon >= 0) {
                String left = a.substring(0, firstColon);
                String suffix = a.substring(firstColon + 1);

                int width = parseWidthToken(left, unitLetter);
                if (width < 0) {
                    return new UnitArg(0, a);
                }
                return new UnitArg(width, suffix);
            }

            // No ":" case:
            // - if arg looks like "hh" / "mm" etc -> width token only
            // - else treat as suffix only (legacy {days: 天})
            int width = parseWidthToken(a, unitLetter);
            if (width >= 0) return new UnitArg(width, "");
            return new UnitArg(0, a);
        }

        private static int parseWidthToken(String s, char unitLetter) {
            if (s == null) return -1;
            if (s.isEmpty()) return 0;
            char u = Character.toLowerCase(unitLetter);

            // Accept "h", "hh", "hhh"... for hours (etc.)
            for (int i = 0; i < s.length(); i++) {
                if (Character.toLowerCase(s.charAt(i)) != u) return -1;
            }
            return s.length();
        }
    }
}

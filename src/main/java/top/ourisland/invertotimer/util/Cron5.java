package top.ourisland.invertotimer.util;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.*;

/**
 * A 5-field cron expression (minute hour day-of-month month day-of-week).
 * <p>
 * Supports standard Unix/Vixie cron features:
 * <ul>
 *   <li>{@code *} any</li>
 *   <li>{@code ?} no specific value (only for DOM/DOW)</li>
 *   <li>lists: {@code 1,2,3}</li>
 *   <li>ranges: {@code 1-5}</li>
 *   <li>steps: {@code x/5}, {@code 1-10/2}, {@code 3/15}</li>
 *   <li>month names: {@code JAN..DEC}</li>
 *   <li>day-of-week names: {@code MON..SUN} (SUN may be 0 or 7)</li>
 * </ul>
 * <p>
 * DOM and DOW follow OR semantics when both are restricted (not {@code *} or {@code ?}),
 * matching traditional cron behavior.
 */
public class Cron5 {
    private final Field minute;     // 0-59
    private final Field hour;       // 0-23
    private final Field dayOfMonth; // 1-31
    private final Field month;      // 1-12
    private final Field dayOfWeek;  // 0-6

    private Cron5(
            Field minute,
            Field hour,
            Field dayOfMonth,
            Field month,
            Field dayOfWeek
    ) {
        this.minute = minute;
        this.hour = hour;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
    }

    public static Cron5 parse(final String expr) {
        if (expr == null) return null;
        String e = expr.trim();
        if (e.isEmpty()) throw new IllegalArgumentException("Cron expression is empty.");

        if (e.startsWith("@")) {
            e = switch (e.toLowerCase(Locale.ROOT)) {
                case "@yearly", "@annually" -> "0 0 1 1 *";
                case "@monthly" -> "0 0 1 * *";
                case "@weekly" -> "0 0 * * 0";
                case "@daily", "@midnight" -> "0 0 * * *";
                case "@hourly" -> "0 * * * *";
                default -> throw new IllegalArgumentException("Unknown cron macro: " + e);
            };
        }

        final String[] parts = e.split("\\s+");
        if (parts.length != 5) throw new IllegalArgumentException("Cron must have 5 fields.");

        return new Cron5(
                Field.parse(parts[0], Spec.MINUTE),
                Field.parse(parts[1], Spec.HOUR),
                Field.parse(parts[2], Spec.DOM),
                Field.parse(parts[3], Spec.MONTH),
                Field.parse(parts[4], Spec.DOW)
        );
    }

    /**
     * Returns the next time strictly after {@code after} that matches this cron.
     * <p>
     * This implementation is optimized: it jumps across invalid ranges by aligning month/day/hour/minute fields instead
     * of scanning every minute.
     * </p>
     *
     * @param after base time (exclusive)
     * @return next matching time, or {@code null} if not found within 10 years (safety bound)
     */
    public ZonedDateTime nextAfter(final ZonedDateTime after) {
        ZonedDateTime t = after.plusMinutes(1).withSecond(0).withNano(0);
        final ZonedDateTime end = t.plusYears(10);

        while (!t.isAfter(end)) {
            // 1) align month
            int mon = t.getMonthValue();
            int nextMon = month.nextOrSame(mon);
            if (nextMon == -1) {
                int firstMon = month.first();
                if (firstMon == -1) return null;
                t = at(t, t.getYear() + 1, firstMon, 1, 0, 0);
                continue;
            }
            if (nextMon != mon) {
                t = at(t, t.getYear(), nextMon, 1, 0, 0);
                continue;
            }

            // 2) align day (within current month)
            ZonedDateTime dayAligned = findNextValidDayInMonth(t);
            if (dayAligned == null) {
                t = advanceToNextAllowedMonth(t);
                if (t == null) return null;
                continue;
            }
            if (!sameDate(dayAligned, t)) {
                int h0 = hour.firstOrMin();
                int m0 = minute.firstOrMin();
                t = at(dayAligned, dayAligned.getYear(), dayAligned.getMonthValue(), dayAligned.getDayOfMonth(), h0, m0);
                continue;
            }

            // 3) align hour
            int hr = t.getHour();
            int nextHr = hour.nextOrSame(hr);
            if (nextHr == -1) {
                ZonedDateTime nd = t.plusDays(1);
                int h0 = hour.firstOrMin();
                int m0 = minute.firstOrMin();
                t = at(nd, nd.getYear(), nd.getMonthValue(), nd.getDayOfMonth(), h0, m0);
                continue;
            }
            if (nextHr != hr) {
                int m0 = minute.firstOrMin();
                t = at(t, t.getYear(), t.getMonthValue(), t.getDayOfMonth(), nextHr, m0);
                continue;
            }

            // 4) align minute
            int min = t.getMinute();
            int nextMin = minute.nextOrSame(min);
            if (nextMin == -1) {
                ZonedDateTime nh = t.plusHours(1);
                int m0 = minute.firstOrMin();
                t = at(nh, nh.getYear(), nh.getMonthValue(), nh.getDayOfMonth(), nh.getHour(), m0);
                continue;
            }
            if (nextMin != min) {
                t = at(t, t.getYear(), t.getMonthValue(), t.getDayOfMonth(), t.getHour(), nextMin);
            }

            if (matches(t)) return t;
            t = t.plusMinutes(1).withSecond(0).withNano(0);
        }
        return null;
    }

    private static ZonedDateTime at(ZonedDateTime base, int year, int month, int day, int hour, int minute) {
        return ZonedDateTime.ofLocal(
                LocalDateTime.of(year, month, day, hour, minute),
                base.getZone(),
                null
        ).withSecond(0).withNano(0);
    }

    private ZonedDateTime findNextValidDayInMonth(ZonedDateTime t) {
        int year = t.getYear();
        int mon = t.getMonthValue();
        int startDom = t.getDayOfMonth();
        int maxDom = t.toLocalDate().lengthOfMonth();

        for (int dom = startDom; dom <= maxDom; dom++) {
            ZonedDateTime cand = at(t, year, mon, dom, t.getHour(), t.getMinute());
            if (dayMatches(cand)) return cand;
        }
        return null;
    }

    private ZonedDateTime advanceToNextAllowedMonth(ZonedDateTime t) {
        int year = t.getYear();
        int mon = t.getMonthValue();

        int next = month.nextAfter(mon);
        if (next != -1) {
            return at(t, year, next, 1, 0, 0);
        }
        int first = month.first();
        if (first == -1) return null;
        return at(t, year + 1, first, 1, 0, 0);
    }

    private static boolean sameDate(ZonedDateTime a, ZonedDateTime b) {
        return a.getYear() == b.getYear()
                && a.getMonthValue() == b.getMonthValue()
                && a.getDayOfMonth() == b.getDayOfMonth();
    }

    private boolean matches(final ZonedDateTime zdt) {
        int min = zdt.getMinute();
        int hr = zdt.getHour();
        int mon = zdt.getMonthValue();

        if (!minute.matches(min)) return false;
        if (!hour.matches(hr)) return false;
        if (!month.matches(mon)) return false;

        return dayMatches(zdt);
    }

    private boolean dayMatches(final ZonedDateTime zdt) {
        int dom = zdt.getDayOfMonth();
        int dow = zdt.getDayOfWeek().getValue() % 7; // Java: Mon=1..Sun=7 -> Cron: Sun=0..Sat=6

        // DOM/DOW semantics:
        // - if one is "no specific" (?) then only the other is considered
        // - if one is "*" and the other is restricted => other decides
        // - if both restricted => OR (traditional cron)
        boolean domUnspec = dayOfMonth.unspecified;
        boolean dowUnspec = dayOfWeek.unspecified;

        boolean domAny = dayOfMonth.any;
        boolean dowAny = dayOfWeek.any;

        boolean domMatch = dayOfMonth.matches(dom);
        boolean dowMatch = dayOfWeek.matches(dow);

        if (domUnspec && dowUnspec) {
            // not meaningful, but treat as always true here
            return true;
        }
        if (domUnspec) return dowMatch;   // only DOW matters
        if (dowUnspec) return domMatch;   // only DOM matters

        if (domAny && dowAny) return true;
        if (domAny) return dowMatch;
        if (dowAny) return domMatch;

        // both restricted: OR
        return domMatch || dowMatch;
    }

    private enum Spec {
        MINUTE(0, 59, false, null),
        HOUR(0, 23, false, null),
        DOM(1, 31, true, null),
        MONTH(1, 12, false, monthNames()),
        DOW(0, 6, true, dowNames());

        final int min;
        final int max;
        final boolean allowQuestion;
        final Map<String, Integer> names;

        Spec(int min, int max, boolean allowQuestion, Map<String, Integer> names) {
            this.min = min;
            this.max = max;
            this.allowQuestion = allowQuestion;
            this.names = names;
        }

        private static Map<String, Integer> monthNames() {
            Map<String, Integer> m = new HashMap<>();
            m.put("jan", 1);
            m.put("feb", 2);
            m.put("mar", 3);
            m.put("apr", 4);
            m.put("may", 5);
            m.put("jun", 6);
            m.put("jul", 7);
            m.put("aug", 8);
            m.put("sep", 9);
            m.put("oct", 10);
            m.put("nov", 11);
            m.put("dec", 12);
            return m;
        }

        private static Map<String, Integer> dowNames() {
            Map<String, Integer> m = new HashMap<>();
            // 0=SUN..6=SAT
            m.put("sun", 0);
            m.put("mon", 1);
            m.put("tue", 2);
            m.put("wed", 3);
            m.put("thu", 4);
            m.put("fri", 5);
            m.put("sat", 6);
            return m;
        }
    }

    /**
     * @param unspecified '?' only for DOM/DOW
     * @param names       may be null
     */
    private record Field(
            boolean any,
            boolean unspecified,
            BitSet allowed,
            int min,
            int max,
            Map<String, Integer> names) {

        static Field parse(String s, Spec spec) {
            if (s == null) throw new IllegalArgumentException("Cron field is null.");
            String raw = s.trim().toLowerCase(Locale.ROOT);
            switch (raw) {
                case "" -> throw new IllegalArgumentException("Cron field is empty.");
                case "*" -> {
                    return new Field(true, false, null, spec.min, spec.max, spec.names);
                }
                case "?" -> {
                    if (!spec.allowQuestion) {
                        throw new IllegalArgumentException("'?' is only allowed for day-of-month/day-of-week.");
                    }
                    return new Field(true, true, null, spec.min, spec.max, spec.names);
                }
            }

            BitSet bs = new BitSet(spec.max + 1);

            String[] items = raw.split(",");
            Arrays.stream(items)
                    .map(String::trim)
                    .filter(part -> !part.isEmpty())
                    .forEach(part -> addPart(bs, part, spec));

            if (bs.isEmpty()) {
                throw new IllegalArgumentException("Cron field has no valid values: " + s);
            }
            return new Field(false, false, bs, spec.min, spec.max, spec.names);
        }

        private static void addPart(BitSet bs, String part, Spec spec) {
            String base = part;
            int step = 1;
            int slash = part.indexOf('/');
            if (slash >= 0) {
                base = part.substring(0, slash).trim();
                String stepStr = part.substring(slash + 1).trim();
                if (stepStr.isEmpty()) throw new IllegalArgumentException("Missing step in: " + part);
                step = Integer.parseInt(stepStr);
                if (step <= 0) throw new IllegalArgumentException("Step must be > 0 in: " + part);
            }

            int start, end;

            if (base.isEmpty() || "*".equals(base)) {
                start = spec.min;
                end = spec.max;
            } else {
                int dash = base.indexOf('-');
                if (dash >= 0) {
                    String a = base.substring(0, dash).trim();
                    String b = base.substring(dash + 1).trim();
                    if (a.isEmpty() || b.isEmpty()) throw new IllegalArgumentException("Bad range: " + part);
                    start = parseValue(a, spec);
                    end = parseValue(b, spec);
                } else {
                    start = parseValue(base, spec);
                    end = (slash >= 0) ? spec.max : start;
                }
            }

            if (spec == Spec.DOW) {
                if (start == 7) start = 0;
                if (end == 7) end = 0;
            }

            if (start <= end) {
                fill(bs, start, end, step, spec);
            } else {
                fill(bs, start, spec.max, step, spec);
                fill(bs, spec.min, end, step, spec);
            }
        }

        private static int parseValue(String token, Spec spec) {
            String t = token.trim().toLowerCase(Locale.ROOT);
            if (t.isEmpty()) throw new IllegalArgumentException("Empty value token.");

            if (spec.names != null) {
                Integer v = spec.names.get(t);
                if (v != null) return v;
            }

            int v = Integer.parseInt(t);

            if (spec == Spec.DOW && v == 7) return 0;

            return v;
        }

        private static void fill(BitSet bs, int start, int end, int step, Spec spec) {
            if (start < spec.min || start > spec.max)
                throw new IllegalArgumentException("Value out of range: " + start);
            if (end < spec.min || end > spec.max) throw new IllegalArgumentException("Value out of range: " + end);
            for (int v = start; v <= end; v += step) {
                bs.set(v);
            }
        }

        boolean matches(int v) {
            if (any) return true;
            return allowed != null && v >= min && v <= max && allowed.get(v);
        }

        int nextOrSame(int v) {
            if (any) return v;
            if (allowed == null) return -1;
            int n = allowed.nextSetBit(v);
            return (n == -1 || n > max) ? -1 : n;
        }

        int nextAfter(int v) {
            if (any) return (v < max) ? (v + 1) : -1;
            if (allowed == null) return -1;
            int n = allowed.nextSetBit(v + 1);
            return (n == -1 || n > max) ? -1 : n;
        }

        int firstOrMin() {
            int f = first();
            return f == -1 ? min : f;
        }

        int first() {
            if (any) return min;
            if (allowed == null) return -1;
            int n = allowed.nextSetBit(min);
            return (n == -1 || n > max) ? -1 : n;
        }
    }
}

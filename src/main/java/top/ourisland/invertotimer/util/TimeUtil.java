package top.ourisland.invertotimer.util;

import java.time.Duration;
import java.util.Locale;

public final class TimeUtil {
    private TimeUtil() {
    }

    public static Duration parseDurationLoose(final Object obj) {
        if (obj == null) return null;
        final String s0 = String.valueOf(obj).trim();
        if (s0.isEmpty()) return null;

        // Accept: 10h, 8m, 30s, 1d, with optional leading +/-.
        String s = s0.toLowerCase(Locale.ROOT);
        boolean neg = false;
        if (s.startsWith("-")) {
            neg = true;
            s = s.substring(1).trim();
        } else if (s.startsWith("+")) {
            s = s.substring(1).trim();
        }
        if (s.isEmpty()) return null;

        char unit = s.charAt(s.length() - 1);
        String numStr = s.substring(0, s.length() - 1).trim();
        long n;
        try {
            n = Long.parseLong(numStr);
        } catch (NumberFormatException e) {
            return null;
        }

        Duration d = switch (unit) {
            case 'd' -> Duration.ofDays(n);
            case 'h' -> Duration.ofHours(n);
            case 'm' -> Duration.ofMinutes(n);
            case 's' -> Duration.ofSeconds(n);
            default -> null;
        };
        if (d == null) return null;
        return neg ? d.negated() : d;
    }

    public static String formatHMS(long totalSeconds) {
        if (totalSeconds < 0) totalSeconds = 0;
        long days = totalSeconds / 86400;
        long rem = totalSeconds % 86400;
        long hours = rem / 3600;
        rem %= 3600;
        long minutes = rem / 60;
        long seconds = rem % 60;
        if (days > 0) {
            return String.format("%dd %02d:%02d:%02d", days, hours, minutes, seconds);
        }
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}

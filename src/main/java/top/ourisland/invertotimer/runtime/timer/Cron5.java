package top.ourisland.invertotimer.runtime.timer;

import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Locale;

public class Cron5 {
    private final Field minute;
    private final Field hour;
    private final Field dayOfMonth;
    private final Field month;
    private final Field dayOfWeek;

    private Cron5(Field minute, Field hour, Field dayOfMonth, Field month, Field dayOfWeek) {
        this.minute = minute;
        this.hour = hour;
        this.dayOfMonth = dayOfMonth;
        this.month = month;
        this.dayOfWeek = dayOfWeek;
    }

    public static Cron5 parse(final String expr) {
        if (expr == null) return null;
        final String[] parts = expr.trim().split("\\s+");
        if (parts.length != 5) throw new IllegalArgumentException("Cron must have 5 fields.");
        return new Cron5(
                Field.parse(parts[0]),
                Field.parse(parts[1]),
                Field.parse(parts[2]),
                Field.parse(parts[3]),
                Field.parse(parts[4])
        );
    }

    public ZonedDateTime nextAfter(final ZonedDateTime after) {
        ZonedDateTime t = after.plusMinutes(1).withSecond(0).withNano(0);
        // brute-force up to 10 years (enough for yearly jobs)
        for (int i = 0; i < 60 * 24 * 366 * 10; i++) {
            if (matches(t)) return t;
            t = t.plusMinutes(1);
        }
        return null;
    }

    private boolean matches(final ZonedDateTime zdt) {
        int min = zdt.getMinute();
        int hr = zdt.getHour();
        int dom = zdt.getDayOfMonth();
        int mon = zdt.getMonthValue();
        int dow = zdt.getDayOfWeek().getValue(); // 1=Mon..7=Sun
        return minute.matches(min)
                && hour.matches(hr)
                && dayOfMonth.matches(dom)
                && month.matches(mon)
                && dayOfWeek.matches(dow);
    }

    private record Field(boolean any, Integer value) {
        static Field parse(String s) {
            s = s.trim().toLowerCase(Locale.ROOT);
            if (s.equals("*")) return new Field(true, null);
            return new Field(false, Integer.parseInt(s));
        }

        boolean matches(int v) {
            if (any) return true;
            if (value == null) return false;
            if (value == 0 && v == DayOfWeek.SUNDAY.getValue()) return true;
            if (value == 7 && v == DayOfWeek.SUNDAY.getValue()) return true;
            return value == v;
        }
    }
}

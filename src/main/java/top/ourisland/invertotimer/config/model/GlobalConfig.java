package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.config.SimpleYaml;

import java.time.ZoneId;
import java.util.Map;

public record GlobalConfig(
        String lang,
        ZoneId zoneId,
        ServerLimitation limitation
) {
    public static GlobalConfig defaults() {
        return new GlobalConfig("en_us", ZoneId.systemDefault(), ServerLimitation.allowAll());
    }

    public static GlobalConfig fromYaml(final Map<?, ?> m) {
        final String lang = SimpleYaml.getString(m, "lang", "en_us");
        final String tz = SimpleYaml.getString(m, "timezone", ZoneId.systemDefault().getId());

        ZoneId zone;
        try {
            zone = ZoneId.of(tz);
        } catch (Exception e) {
            zone = ZoneId.systemDefault();
        }

        final ServerLimitation lim = ServerLimitation.fromYaml(m.get("limitation"));
        return new GlobalConfig(lang, zone, lim);
    }
}

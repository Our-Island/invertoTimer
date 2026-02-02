package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.util.YamlUtil;

import java.time.ZoneId;
import java.util.Map;

/**
 * Global config record.
 *
 * @param lang the language that the plugin uses
 * @param zoneId timezone that the timer will use
 * @param limitation global whitelist/blacklist of the server
 */
public record GlobalConfig(
        String lang,
        ZoneId zoneId,
        ServerLimitation limitation
) {
    public static GlobalConfig defaults() {
        return new GlobalConfig("en_us", ZoneId.systemDefault(), ServerLimitation.allowAll());
    }

    public static GlobalConfig fromYaml(final Map<?, ?> m) {
        final String lang = YamlUtil.getString(m, "lang", "en_us");
        final String tz = YamlUtil.getString(m, "timezone", ZoneId.systemDefault().getId());

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

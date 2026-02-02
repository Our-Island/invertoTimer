package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.util.YamlUtil;
import top.ourisland.invertotimer.util.TimeUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Config record of actions.
 *
 * @param type the type of the action
 * @param shift time shift
 * @param options options of the action
 */
public record ActionConfig(
        String type,
        Duration shift,
        Map<String, Object> options
) {
    public static ActionConfig fromYaml(final Object obj) {
        if (!(obj instanceof Map<?, ?> m)) return null;
        String type = YamlUtil.getString(m, "type", "");
        Duration shift = TimeUtil.parseDurationLoose(m.get("shift"));
        if (shift == null) shift = Duration.ZERO;

        Map<String, Object> options = new LinkedHashMap<>();
        Object opt = m.get("options");
        if (opt instanceof Map<?, ?> om) {
            for (Map.Entry<?, ?> e : om.entrySet()) {
                options.put(String.valueOf(e.getKey()), e.getValue());
            }
        }

        return new ActionConfig(type, shift, Collections.unmodifiableMap(options));
    }
}

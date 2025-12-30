package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.config.SimpleYaml;
import top.ourisland.invertotimer.runtime.timer.TimeUtil;

import java.time.Duration;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public record ActionConfig(
        String type,
        Duration shift,
        Map<String, Object> options
) {
    public static ActionConfig fromYaml(final Object obj) {
        if (!(obj instanceof Map<?, ?> m)) return null;
        String type = SimpleYaml.getString(m, "type", "");
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

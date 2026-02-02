package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.config.SimpleYaml;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Config record defining a timer instance.
 *
 * @param id the id of the timer
 * @param description human-readable description
 * @param cron cron5 specification
 * @param time final time of the timer
 * @param limitation whitelist/blacklist of the server
 * @param showcases defined showcases
 * @param actions defined actions
 */
public record TimerConfig(
        String id,
        String description,
        String cron,
        String time,
        ServerLimitation limitation,
        Map<String, ShowcaseConfig> showcases,
        List<ActionConfig> actions
) {
    public static TimerConfig fromYaml(final String id, final Map<?, ?> m) {
        String desc = SimpleYaml.getString(m, "description", id);
        String cron = m.get("cron") == null ? null : String.valueOf(m.get("cron"));
        String time = m.get("time") == null ? null : String.valueOf(m.get("time"));

        ServerLimitation lim = ServerLimitation.fromYaml(m.get("limitation"));

        Map<String, ShowcaseConfig> showcases = new LinkedHashMap<>();
        Object scObj = m.get("showcases");
        if (scObj instanceof Map<?, ?> scm) {
            for (Map.Entry<?, ?> e : scm.entrySet()) {
                showcases.put(String.valueOf(e.getKey()), ShowcaseConfig.fromYaml(e.getValue()));
            }
        }

        List<ActionConfig> actions = new ArrayList<>();
        Object actObj = m.get("actions");
        if (actObj instanceof List<?> list) {
            for (Object o : list) {
                ActionConfig ac = ActionConfig.fromYaml(o);
                if (ac != null) actions.add(ac);
            }
        }

        return new TimerConfig(id, desc, cron, time, lim, showcases, actions);
    }
}

package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.util.YamlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Config record defining a text animation that can be used by {@code {animation:<id>}} placeholder.
 * <p>
 * YAML formats supported:
 * <ul>
 *   <li>Simple (uniform interval): {@code interval: 0.5} + {@code text: [ ... ]}</li>
 *   <li>Advanced (per-frame duration): {@code frames: [ {duration: 0.5, text: "..."}, ... ]}</li>
 * </ul>
 *
 * @param id the animation id
 * @param frames ordered frames (looped)
 * @param totalDurationMs total duration of one loop in milliseconds (>= 1)
 */
public record AnimationConfig(
        String id,
        List<Frame> frames,
        long totalDurationMs
) {

    /**
     * A single animation frame.
     *
     * @param durationMs frame duration in milliseconds (>= 1)
     * @param text frame text (may contain placeholders / MiniMessage)
     */
    public record Frame(
            long durationMs,
            String text
    ) {
    }

    public static AnimationConfig fromYaml(final String id, final Map<?, ?> m) {
        if (m == null) {
            return new AnimationConfig(id, List.of(new Frame(1000, "")), 1000);
        }

        Object framesObj = m.get("frames");
        if (framesObj instanceof List<?> framesList && !framesList.isEmpty()) {
            List<Frame> frames = new ArrayList<>();
            long total = 0;

            for (Object o : framesList) {
                if (!(o instanceof Map<?, ?> fm)) continue;

                double durSec = YamlUtil.getDouble(fm, "duration", 1.0);
                long durMs = Math.max(1, (long) Math.round(durSec * 1000.0));
                String text = YamlUtil.getString(fm, "text", "");

                frames.add(new Frame(durMs, text));
                total += durMs;
            }

            if (frames.isEmpty()) {
                return new AnimationConfig(id, List.of(new Frame(1000, "")), 1000);
            }
            return new AnimationConfig(id, List.copyOf(frames), Math.max(1, total));
        }

        double intervalSec = YamlUtil.getDouble(m, "interval", 1.0);
        long intervalMs = Math.max(1, (long) Math.round(intervalSec * 1000.0));

        Object textObj = m.get("text");
        List<Frame> frames = new ArrayList<>();
        if (textObj instanceof List<?> texts) {
            for (Object o : texts) {
                frames.add(new Frame(intervalMs, o == null ? "" : String.valueOf(o)));
            }
        } else if (textObj != null) {
            frames.add(new Frame(intervalMs, String.valueOf(textObj)));
        }

        if (frames.isEmpty()) {
            frames.add(new Frame(intervalMs, ""));
        }

        long total = intervalMs * (long) frames.size();
        return new AnimationConfig(id, List.copyOf(frames), Math.max(1, total));
    }
}

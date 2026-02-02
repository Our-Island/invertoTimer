package top.ourisland.invertotimer.config;

import lombok.Getter;
import org.slf4j.Logger;
import top.ourisland.invertotimer.config.model.GlobalConfig;
import top.ourisland.invertotimer.config.model.TimerConfig;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public class ConfigManager {
    private final Logger logger;
    private final Path dataDir;

    @Getter
    private GlobalConfig globalConfig = GlobalConfig.defaults();
    private Map<String, TimerConfig> timers = new LinkedHashMap<>();

    public ConfigManager(final Logger logger, final Path dataDir) {
        this.logger = logger;
        this.dataDir = dataDir;
    }

    public void reloadAll() {
        try {
            Files.createDirectories(dataDir);
        } catch (IOException e) {
            logger.error("Cannot create data directory: {}", dataDir, e);
        }

        copyDefaultIfAbsent("config.yml");
        copyDefaultIfAbsent("timer.yml");

        this.globalConfig = loadGlobal();
        this.timers = loadTimers();
    }

    private void copyDefaultIfAbsent(final String filename) {
        final Path dest = dataDir.resolve(filename);
        if (Files.exists(dest)) return;

        try (InputStream in = getClass().getClassLoader().getResourceAsStream(filename)) {
            if (in == null) {
                logger.warn("Default resource {} not found in jar, creating empty file.", filename);
                Files.writeString(dest, "# " + filename + "\n");
                return;
            }
            Files.copy(in, dest);
            logger.info("Generated default {}", dest.getFileName());
        } catch (IOException e) {
            logger.error("Failed to generate default {}", filename, e);
        }
    }

    private GlobalConfig loadGlobal() {
        final Path p = dataDir.resolve("config.yml");
        try {
            final Object root = SimpleYaml.parse(Files.readString(p));
            if (!(root instanceof Map<?, ?> m)) {
                logger.warn("config.yml root is not a map, using defaults.");
                return GlobalConfig.defaults();
            }
            return GlobalConfig.fromYaml(m);
        } catch (Exception e) {
            logger.error("Failed to load config.yml, using defaults.", e);
            return GlobalConfig.defaults();
        }
    }

    private Map<String, TimerConfig> loadTimers() {
        final Path p = dataDir.resolve("timer.yml");
        try {
            final Object root = SimpleYaml.parse(Files.readString(p));
            if (!(root instanceof Map<?, ?> m)) {
                logger.warn("timer.yml root is not a map.");
                return new LinkedHashMap<>();
            }
            Object timersObj = m.get("timers");
            if (!(timersObj instanceof Map<?, ?> timersMap)) {
                logger.warn("timer.yml missing 'timers:' root map.");
                return new LinkedHashMap<>();
            }
            final Map<String, TimerConfig> out = new LinkedHashMap<>();
            for (Map.Entry<?, ?> e : timersMap.entrySet()) {
                final String id = String.valueOf(e.getKey());
                if (e.getValue() instanceof Map<?, ?> tm) {
                    out.put(id, TimerConfig.fromYaml(id, tm));
                }
            }
            return out;
        } catch (Exception e) {
            logger.error("Failed to load timer.yml", e);
            return new LinkedHashMap<>();
        }
    }

    public Map<String, TimerConfig> getTimers() {
        return Collections.unmodifiableMap(timers);
    }
}

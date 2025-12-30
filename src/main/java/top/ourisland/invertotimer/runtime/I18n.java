package top.ourisland.invertotimer.runtime;

import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import top.ourisland.invertotimer.config.ConfigManager;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * I18n handler for invertoTimer.
 * <p>
 * Uses ResourceBundle to load language files from resources/lang/.
 */
public final class I18n {

    private static ResourceBundle bundle;
    private static Logger logger;
    private static ConfigManager configManager;

    private I18n() {
    }

    /**
     * Initialize i18n system.
     */
    public static void init(ConfigManager configManager, Logger logger) {
        I18n.configManager = configManager;
        I18n.logger = logger;
        reload();
    }

    /**
     * Reload language bundle (called on /itimer reload).
     */
    public static void reload() {
        String lang = configManager.getGlobalConfig().lang();
        loadBundle(lang);
    }

    private static void loadBundle(String lang) {
        try {
            bundle = ResourceBundle.getBundle("lang/" + lang);
        } catch (Exception e) {
            bundle = ResourceBundle.getBundle("lang/en_us");
            logger.warn("Failed to load language '{}', fallback to en_us", lang);
        }
    }

    public static Component lang(String key) {
        return Component.text(langStr(key));
    }

    public static Component lang(String key, Object... args) {
        return Component.text(langStr(key, args));
    }

    public static Component langNP(String key) {
        return Component.text(langStrNP(key));
    }

    public static Component langNP(String key, Object... args) {
        return Component.text(langStrNP(key, args));
    }

    public static String langStr(String key) {
        return prefixStr() + pattern(key);
    }

    public static String langStr(String key, Object... args) {
        return prefixStr() + format(key, args);
    }

    public static String langStrNP(String key) {
        return pattern(key);
    }

    public static String langStrNP(String key, Object... args) {
        return format(key, args);
    }

    private static String prefixStr() {
        return bundle.getString("itimer.prefix");
    }

    private static String pattern(String key) {
        return bundle.getString(key);
    }

    private static String format(String key, Object... args) {
        String p = bundle.getString(key);
        return (args == null || args.length == 0) ? p : MessageFormat.format(p, args);
    }
}

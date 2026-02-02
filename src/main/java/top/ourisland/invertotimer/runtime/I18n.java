package top.ourisland.invertotimer.runtime;

import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import top.ourisland.invertotimer.config.ConfigManager;

import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Internationalization (i18n) utility for invertoTimer.
 * <p>
 * This class loads localized messages from {@link ResourceBundle}s located under {@code resources/lang/}. The active
 * language is determined by the global configuration (see {@link ConfigManager}), and can be reloaded at runtime.
 * </p>
 *
 * <h2>Prefix vs. No-Prefix</h2>
 * <ul>
 *   <li>Methods named {@code lang*} / {@code langStr*} automatically prepend the plugin prefix
 *       (key {@code itimer.prefix}).</li>
 *   <li>Methods named {@code lang*NP} / {@code langStr*NP} return the raw localized message
 *       without the prefix.</li>
 * </ul>
 *
 * <h2>Fallback behavior</h2>
 * If the configured language bundle cannot be loaded, this class falls back to {@code en_us}
 * and logs a warning.
 *
 * <p><b>Note:</b> This class stores mutable static state and must be initialized via
 * {@link #init(ConfigManager, Logger)} before use.</p>
 *
 * @author Our-Island
 */
public final class I18n {

    private static ResourceBundle bundle;
    private static Logger logger;
    private static ConfigManager configManager;

    private I18n() {
    }

    /**
     * Initializes the i18n system and loads the initial language bundle.
     * <p>
     * This method must be called once during plugin startup before any translation method is used.
     * </p>
     *
     * @param configManager configuration provider used to resolve the language code
     * @param logger        logger used to output warnings on bundle load failures
     */
    public static void init(ConfigManager configManager, Logger logger) {
        I18n.configManager = configManager;
        I18n.logger = logger;
        reload();
    }

    /**
     * Reloads the current language bundle.
     * <p>
     * Typically invoked when the plugin configuration is reloaded (e.g. via {@code /itimer reload}). The language code
     * is read from the global configuration and the corresponding resource bundle is loaded.
     * </p>
     */
    public static void reload() {
        String lang = configManager.globalConfig().lang();
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

    /**
     * Returns the localized message for the given key as a {@link Component}, with the plugin prefix.
     *
     * @param key translation key
     * @return a text component containing {@code prefix + message}
     */
    public static Component lang(String key) {
        return Component.text(langStr(key));
    }

    /**
     * Returns the localized message for the given key as a {@link String}, with the plugin prefix.
     *
     * @param key translation key
     * @return {@code prefix + message}
     */
    public static String langStr(String key) {
        return prefixStr() + pattern(key);
    }

    /**
     * Returns the configured plugin prefix string.
     * <p>
     * The prefix is read from the resource bundle key {@code itimer.prefix}.
     * </p>
     *
     * @return prefix string
     */
    private static String prefixStr() {
        return bundle.getString("itimer.prefix");
    }

    /**
     * Returns the raw localized pattern for the given key (no formatting, no prefix).
     *
     * @param key translation key
     * @return raw message pattern from the bundle
     */
    private static String pattern(String key) {
        return bundle.getString(key);
    }

    /**
     * Returns the formatted localized message as a {@link Component}, with the plugin prefix.
     * <p>
     * Formatting uses {@link MessageFormat}. If {@code args} is {@code null} or empty, the raw pattern is returned
     * without formatting.
     * </p>
     *
     * @param key  translation key
     * @param args format arguments
     * @return a text component containing {@code prefix + formatted message}
     */
    public static Component lang(String key, Object... args) {
        return Component.text(langStr(key, args));
    }

    /**
     * Returns the formatted localized message as a {@link String}, with the plugin prefix.
     *
     * @param key  translation key
     * @param args format arguments
     * @return {@code prefix + formatted message}
     * @see MessageFormat
     */
    public static String langStr(String key, Object... args) {
        return prefixStr() + format(key, args);
    }

    /**
     * Formats the message pattern for the given key with the provided arguments.
     * <p>
     * Formatting uses {@link MessageFormat}. If {@code args} is {@code null} or empty, the unformatted pattern is
     * returned.
     * </p>
     *
     * @param key  translation key
     * @param args format arguments
     * @return formatted message without prefix
     */
    private static String format(String key, Object... args) {
        String p = bundle.getString(key);
        return (args == null || args.length == 0) ? p : MessageFormat.format(p, args);
    }

    /**
     * Returns the localized message for the given key as a {@link Component}, without the plugin prefix.
     *
     * @param key translation key
     * @return a text component containing the raw localized message
     */
    public static Component langNP(String key) {
        return Component.text(langStrNP(key));
    }

    /**
     * Returns the localized message for the given key as a {@link String}, without the plugin prefix.
     *
     * @param key translation key
     * @return raw localized message
     */
    public static String langStrNP(String key) {
        return pattern(key);
    }

    /**
     * Returns the formatted localized message as a {@link Component}, without the plugin prefix.
     *
     * @param key  translation key
     * @param args format arguments
     * @return a text component containing the formatted localized message
     */
    public static Component langNP(String key, Object... args) {
        return Component.text(langStrNP(key, args));
    }

    /**
     * Returns the formatted localized message as a {@link String}, without the plugin prefix.
     *
     * @param key  translation key
     * @param args format arguments
     * @return formatted localized message
     */
    public static String langStrNP(String key, Object... args) {
        return format(key, args);
    }

    /**
     * Wraps plain text with the plugin prefix and returns it as a {@link Component}.
     *
     * @param text text to be prefixed (may be {@code null})
     * @return a text component containing {@code prefix + text}
     */
    public static Component withPrefixComp(String text) {
        return Component.text(withPrefix(text));
    }

    /**
     * Prepends the plugin prefix to plain text.
     * <p>
     * If {@code text} is {@code null}, it is treated as an empty string.
     * </p>
     *
     * @param text text to be prefixed (may be {@code null})
     * @return {@code prefix + text}
     */
    public static String withPrefix(String text) {
        return prefixStr() + (text == null ? "" : text);
    }

    /**
     * Prepends the plugin prefix to the provided component.
     *
     * @param comp component to be prefixed
     * @return {@code prefix} component appended with {@code comp}
     */
    public static Component withPrefixComp(Component comp) {
        return Component.text(prefixStr()).append(comp);
    }
}

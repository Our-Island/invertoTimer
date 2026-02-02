package top.ourisland.invertotimer.util;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.util.List;
import java.util.Map;

public final class YamlUtil {
    private static final Yaml YAML;

    static {
        LoaderOptions opt = new LoaderOptions();
        YAML = new Yaml(new SafeConstructor(opt));
    }

    private YamlUtil() {
    }

    public static Object parse(String yamlText) {
        if (yamlText == null || yamlText.isBlank()) return Map.of();
        Object o = YAML.load(yamlText);
        return o == null ? Map.of() : o;
    }

    public static String getString(Map<?, ?> m, String key, String def) {
        if (m == null) return def;
        Object v = m.get(key);
        return v == null ? def : String.valueOf(v);
    }

    public static boolean getBool(Map<?, ?> m, String key, boolean def) {
        if (m == null) return def;
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Boolean b) return b;
        return Boolean.parseBoolean(String.valueOf(v));
    }

    public static long getLong(Map<?, ?> m, String key, long def) {
        if (m == null) return def;
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Number n) return n.longValue();
        try {
            return Long.parseLong(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static double getDouble(Map<?, ?> m, String key, double def) {
        if (m == null) return def;
        Object v = m.get(key);
        if (v == null) return def;
        if (v instanceof Number n) return n.doubleValue();
        try {
            return Double.parseDouble(String.valueOf(v));
        } catch (NumberFormatException e) {
            return def;
        }
    }

    public static Map<?, ?> getMap(Map<?, ?> m, String key) {
        if (m == null) return Map.of();
        Object v = m.get(key);
        Map<?, ?> mm = asMap(v);
        return mm == null ? Map.of() : mm;
    }

    public static Map<?, ?> asMap(Object o) {
        return (o instanceof Map<?, ?> m) ? m : null;
    }

    public static List<?> getList(Map<?, ?> m, String key) {
        if (m == null) return List.of();
        Object v = m.get(key);
        List<?> l = asList(v);
        return l == null ? List.of() : l;
    }

    public static List<?> asList(Object o) {
        return (o instanceof List<?> l) ? l : null;
    }
}

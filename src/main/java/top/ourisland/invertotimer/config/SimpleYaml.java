package top.ourisland.invertotimer.config;

import java.util.*;

public final class SimpleYaml {
    private SimpleYaml() {
    }

    public static Object parse(final String yaml) {
        final List<String> lines = yaml.lines().toList();

        final Map<String, Object> root = new LinkedHashMap<>();
        final Deque<Frame> stack = new ArrayDeque<>();
        stack.push(Frame.root(root));

        for (int lineNo = 0; lineNo < lines.size(); lineNo++) {
            final String raw = lines.get(lineNo);
            String line = stripComments(raw);
            if (line.isBlank()) continue;

            final int indent = countIndent(line);
            final String trimmed = line.trim();

            while (stack.size() > 1 && indent < stack.peek().indent) {
                stack.pop();
            }

            if (trimmed.startsWith("- ")) {
                Frame f = stack.peek();

                if (f.container instanceof Map<?, ?> && f.parentMap != null && f.parentKey != null) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> m = (Map<String, Object>) f.container;
                    if (m.isEmpty()) {
                        List<Object> list = new ArrayList<>();
                        f.parentMap.put(f.parentKey, list);
                        f.container = list;
                    }
                }

                if (!(stack.peek().container instanceof List<?>)) {
                    throw new IllegalStateException("List item without list container near line " + (lineNo + 1) + ": " + raw);
                }

                @SuppressWarnings("unchecked")
                List<Object> list = (List<Object>) stack.peek().container;
                String item = trimmed.substring(2).trim();

                if (item.contains(":")) {
                    Map<String, Object> mapItem = new LinkedHashMap<>();
                    parseKeyValueIntoMap(mapItem, item);
                    list.add(mapItem);

                    stack.push(new Frame(indent + 2, mapItem, null, null));
                } else {
                    list.add(parseScalar(item));
                }
                continue;
            }

            if (!(stack.peek().container instanceof Map<?, ?>)) {
                throw new IllegalStateException("Key-value line under non-map near line " + (lineNo + 1) + ": " + raw);
            }

            @SuppressWarnings("unchecked")
            Map<String, Object> map = (Map<String, Object>) stack.peek().container;

            int colon = trimmed.indexOf(':');
            if (colon < 0) throw new IllegalStateException("Invalid YAML line " + (lineNo + 1) + ": " + raw);

            String key = trimmed.substring(0, colon).trim();
            String rest = trimmed.substring(colon + 1).trim();

            if (rest.isEmpty()) {
                Map<String, Object> child = new LinkedHashMap<>();
                map.put(key, child);
                stack.push(new Frame(indent + 2, child, map, key));
            } else {
                map.put(key, parseScalar(rest));
            }
        }

        return root;
    }

    private static String stripComments(final String raw) {
        boolean inSingle = false, inDouble = false;
        StringBuilder sb = new StringBuilder(raw.length());
        for (int i = 0; i < raw.length(); i++) {
            char c = raw.charAt(i);
            if (c == '\'' && !inDouble) inSingle = !inSingle;
            if (c == '"' && !inSingle) inDouble = !inDouble;
            if (c == '#' && !inSingle && !inDouble) break;
            sb.append(c);
        }
        return sb.toString();
    }

    private static int countIndent(String s) {
        int n = 0;
        while (n < s.length() && s.charAt(n) == ' ') n++;
        return n;
    }

    private static void parseKeyValueIntoMap(Map<String, Object> map, String line) {
        int colon = line.indexOf(':');
        String key = line.substring(0, colon).trim();
        String rest = line.substring(colon + 1).trim();
        if (rest.isEmpty()) {
            map.put(key, new LinkedHashMap<String, Object>());
        } else {
            map.put(key, parseScalar(rest));
        }
    }

    private static Object parseScalar(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) return s.substring(1, s.length() - 1);
        if (s.startsWith("'") && s.endsWith("'") && s.length() >= 2) return s.substring(1, s.length() - 1);
        if ("true".equalsIgnoreCase(s)) return Boolean.TRUE;
        if ("false".equalsIgnoreCase(s)) return Boolean.FALSE;
        try {
            if (s.contains(".")) return Double.parseDouble(s);
            return Long.parseLong(s);
        } catch (NumberFormatException ignored) {
        }
        return s;
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

    private static final class Frame {
        final int indent;
        // used for auto-converting placeholder map -> list
        final Map<String, Object> parentMap;
        final String parentKey;
        Object container;

        Frame(int indent, Object container, Map<String, Object> parentMap, String parentKey) {
            this.indent = indent;
            this.container = container;
            this.parentMap = parentMap;
            this.parentKey = parentKey;
        }

        static Frame root(Map<String, Object> root) {
            return new Frame(0, root, null, null);
        }
    }
}

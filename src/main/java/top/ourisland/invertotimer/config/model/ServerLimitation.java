package top.ourisland.invertotimer.config.model;

import top.ourisland.invertotimer.config.SimpleYaml;

import java.util.*;

public class ServerLimitation {
    private final Mode mode;
    private final Set<String> servers;

    public ServerLimitation(final Mode mode, final Set<String> servers) {
        this.mode = mode;
        this.servers = servers;
    }

    public static ServerLimitation fromYaml(final Object obj) {
        if (!(obj instanceof Map<?, ?> m)) return allowAll();

        final String modeStr = SimpleYaml.getString(m, "mode", "blacklist");
        final Mode mode = "whitelist".equalsIgnoreCase(modeStr) ? Mode.WHITELIST : Mode.BLACKLIST;

        final Set<String> set = new HashSet<>();
        Object listObj = m.get("list");
        if (listObj instanceof Collection<?> c) {
            for (Object o : c) set.add(String.valueOf(o).toLowerCase(Locale.ROOT));
        }
        return new ServerLimitation(mode, Set.copyOf(set));
    }

    public static ServerLimitation allowAll() {
        return new ServerLimitation(Mode.BLACKLIST, Set.of());
    }

    public boolean isAllowed(final String serverName) {
        if (serverName == null) return true;
        final boolean contains = servers.contains(serverName.toLowerCase(Locale.ROOT));
        return (mode == Mode.BLACKLIST) != contains;
    }

    public enum Mode {
        BLACKLIST,
        WHITELIST
    }
}

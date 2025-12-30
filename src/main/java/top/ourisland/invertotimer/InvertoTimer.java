package top.ourisland.invertotimer;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import org.slf4j.Logger;
import top.ourisland.invertotimer.config.ConfigManager;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.timer.TimerRunner;

import java.nio.file.Path;
import java.util.Locale;

@Plugin(
        id = "invertotimer",
        name = "invertoTimer",
        version = BuildConstants.VERSION,
        description = "A Velocity countdown timer plugin.",
        url = "https://github.com/Our-Island/invertoTimer",
        authors = {"Chiloven945"}
)
public class InvertoTimer {
    private final ProxyServer proxy;
    private final Logger logger;
    private final Path dataDir;

    private ConfigManager configManager;
    private TimerRunner timerRunner;

    @Inject
    public InvertoTimer(final ProxyServer proxy, final Logger logger, @DataDirectory final Path dataDir) {
        this.proxy = proxy;
        this.logger = logger;
        this.dataDir = dataDir;
    }

    @Subscribe
    public void onProxyInitialization(final ProxyInitializeEvent event) {
        try {
            logger.info("Initializing invertoTimer {}...", BuildConstants.VERSION);

            this.configManager = new ConfigManager(logger, dataDir);
            this.configManager.reloadAll();

            I18n.init(configManager, logger);

            this.timerRunner = new TimerRunner(this, proxy, logger, configManager);
            this.timerRunner.start();

            proxy.getEventManager().register(this, this.timerRunner);

            registerCommands();

            logger.info("invertoTimer loaded: {} timer(s).", configManager.getTimers().size());
        } catch (final Exception e) {
            logger.error("Failed to initialize invertoTimer", e);
        }
    }

    private void registerCommands() {
        final CommandManager cm = proxy.getCommandManager();
        final CommandMeta meta = cm.metaBuilder("invertotimer")
                .aliases("itimer", "invttimer")
                .plugin(this)
                .build();

        cm.register(meta, new RootCommand(this));
    }

    public void reload() {
        configManager.reloadAll();
        timerRunner.reloadFromConfig();
        logger.info("invertoTimer reloaded: {} timer(s).", configManager.getTimers().size());
    }

    // ===== Command =====
    private record RootCommand(InvertoTimer plugin) implements SimpleCommand {

        @Override
        public void execute(final Invocation invocation) {
            final var src = invocation.source();
            final var args = invocation.arguments();

            if (!src.hasPermission("invertotimer.admin")) {
                src.sendMessage(I18n.lang("itimer.command.no_perms"));
                return;
            }

            if (args.length == 0) {
                src.sendMessage(I18n.lang("itimer.command.usage", "/itimer <reload|list|status>"));
                return;
            }

            switch (args[0].toLowerCase(Locale.ROOT)) {
                case "reload" -> {
                    plugin.reload();
                    src.sendMessage(I18n.lang("itimer.command.reload"));
                }
                case "list", "status" -> {
                    final var timers = plugin.configManager.getTimers();
                    if (timers.isEmpty()) {
                        src.sendMessage(I18n.lang("itimer.command.list.failed"));
                        return;
                    }
                    src.sendMessage(I18n.lang("itimer.command.list.header"));
                    timers.forEach((id, cfg) -> {
                        final var next = plugin.timerRunner.peekNextOccurrence(id);
                        src.sendMessage(Component.text(" - " + id + " : " + (next == null ? "N/A" : next)));
                    });
                }
                default -> src.sendMessage(I18n.lang("itimer.command.usage", "/itimer <reload|list|status>"));
            }
        }
    }
}

package top.ourisland.invertotimer.action;

import lombok.NonNull;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.regex.Pattern;

/**
 * An action for command execution. Executor and RegEx filter can be specified.
 *
 * @author Chiloven945
 */
public class CommandAction implements Action {
    private final RuntimeContext ctx;
    private final Executor executor;
    private final String command;
    private final Pattern match;

    public CommandAction(
            @NonNull RuntimeContext ctx,
            Executor executor,
            String command,
            String matchRegex
    ) {
        this.ctx = ctx;
        this.executor = executor == null ? Executor.PLAYER : executor;
        this.command = command == null ? "" : command;

        Pattern compiled = null;
        if (this.executor == Executor.PLAYER && matchRegex != null && !matchRegex.isBlank()) {
            try {
                compiled = Pattern.compile(matchRegex);
            } catch (Exception ignored) {
            }
        }
        this.match = compiled;
    }

    public static Executor parse(String s) {
        if (s == null) return Executor.PLAYER;
        return "console".equalsIgnoreCase(s) ? Executor.CONSOLE : Executor.PLAYER;
    }

    @Override
    public String name() {
        return "command";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.action.command.desc");
    }

    @Override
    public void execute() {
        switch (executor) {
            case CONSOLE -> {
                final String cmd = ctx.renderString(command).trim();
                if (cmd.isBlank()) return;
                ctx.proxy().getCommandManager().executeAsync(
                        ctx.proxy().getConsoleCommandSource(), trimLeadingSlash(cmd)
                );
            }
            case PLAYER -> ctx.players().stream()
                    .filter(ctx::allowed)
                    .filter(p -> match == null || match.matcher(p.getUsername()).matches())
                    .forEach(p -> {
                        final String cmd = ctx.renderString(p, command).trim();
                        if (cmd.isBlank()) return;
                        p.spoofChatInput(cmd);
                    });
        }
    }

    private static String trimLeadingSlash(final String cmd) {
        String s = cmd.trim();
        while (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    public enum Executor {
        CONSOLE,
        PLAYER
    }
}

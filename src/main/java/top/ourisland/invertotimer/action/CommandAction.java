package top.ourisland.invertotimer.action;

import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Objects;
import java.util.regex.Pattern;

public class CommandAction implements Action {
    private final RuntimeContext ctx;
    private final Executor executor;
    private final String command;
    private final Pattern match;

    public CommandAction(RuntimeContext ctx, Executor executor, String command, String matchRegex) {
        this.ctx = Objects.requireNonNull(ctx);
        this.executor = executor == null ? Executor.PLAYER : executor;
        this.command = command == null ? "" : command;

        Pattern compiled = null;
        if (this.executor == Executor.PLAYER && matchRegex != null && !matchRegex.isBlank()) {
            try {
                compiled = Pattern.compile(matchRegex);
            } catch (Exception ignored) {
                compiled = null;
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
        final String cmd = ctx.renderString(command).trim();
        if (cmd.isBlank()) return;

        switch (executor) {
            case CONSOLE -> ctx.proxy().getCommandManager().executeAsync(
                    ctx.proxy().getConsoleCommandSource(), trimLeadingSlash(cmd)
            );
            case PLAYER -> ctx.players().stream()
                    .filter(ctx::allowed)
                    .filter(p -> match == null || match.matcher(p.getUsername()).matches())
                    .forEach(p -> p.spoofChatInput(cmd));
        }
    }

    private static String trimLeadingSlash(final String cmd) {
        String s = cmd.trim();
        while (s.startsWith("/")) s = s.substring(1);
        return s;
    }

    public enum Executor {CONSOLE, PLAYER}
}

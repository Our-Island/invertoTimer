package top.ourisland.invertotimer.action;

import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Objects;
import java.util.regex.Pattern;

public class TransferAction implements Action {
    private final RuntimeContext ctx;
    private final String target;
    private final Pattern transfereePattern;

    public TransferAction(RuntimeContext ctx, String target, String transfereeRegex) {
        this.ctx = Objects.requireNonNull(ctx);
        this.target = target == null ? "" : target;

        String rx = (transfereeRegex == null || transfereeRegex.isBlank()) ? ".*" : transfereeRegex;
        this.transfereePattern = compileOrSafeNone(rx);
    }

    private static Pattern compileOrSafeNone(String rx) {
        try {
            return Pattern.compile(rx);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public String name() {
        return "transfer";
    }

    @Override
    public String description() {
        return I18n.langStrNP("itimer.action.transfer.desc");
    }

    @Override
    public void execute() {
        if (target.isBlank() || transfereePattern == null) return;

        final var serverOpt = ctx.proxy().getServer(target);
        if (serverOpt.isEmpty()) return;

        ctx.players().stream()
                .filter(ctx::allowed)
                .filter(p -> transfereePattern.matcher(p.getUsername()).matches())
                .forEach(p -> p.createConnectionRequest(serverOpt.get()).fireAndForget());
    }
}

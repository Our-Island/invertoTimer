package top.ourisland.invertotimer.action;

import lombok.NonNull;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import top.ourisland.invertotimer.runtime.I18n;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.regex.Pattern;

/**
 * An action for player transferring between servers.
 *
 * @author Chiloven945
 */
public class TransferAction implements Action {
    private final RuntimeContext ctx;
    private final String target;
    private final Pattern transfereePattern;

    public TransferAction(
            @NonNull RuntimeContext ctx,
            String target,
            String transfereeRegex
    ) {
        this.ctx = ctx;
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

        var serverOpt = ctx.proxy().getServer(target);
        if (serverOpt.isEmpty()) {
            ctx.logger().warn("");
            return;
        }

        ctx.players().stream()
                .filter(ctx::allowed)
                .filter(p -> transfereePattern.matcher(p.getUsername()).matches())
                .forEach(p -> {
                    p.createConnectionRequest(serverOpt.get()).connect().thenAccept(result -> {
                        if (!result.isSuccessful()) {
                            Component reasonComp = result.getReasonComponent()
                                    .orElse(Component.text("Unknown reason (Status: " + result.getStatus() + ")"));

                            String reasonStr = PlainTextComponentSerializer.plainText().serialize(reasonComp);
                            ctx.logger().warn("Failed to transfer player {}: {}", p.getUsername(), reasonStr);
                            p.sendMessage(I18n.withPrefixComp(reasonStr));
                        }
                    });
                });
    }
}

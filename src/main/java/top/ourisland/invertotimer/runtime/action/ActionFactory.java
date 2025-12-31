package top.ourisland.invertotimer.runtime.action;

import top.ourisland.invertotimer.action.Action;
import top.ourisland.invertotimer.action.CommandAction;
import top.ourisland.invertotimer.action.TextAction;
import top.ourisland.invertotimer.action.TransferAction;
import top.ourisland.invertotimer.config.model.ActionConfig;
import top.ourisland.invertotimer.runtime.RuntimeContext;

import java.util.Locale;
import java.util.Map;

public final class ActionFactory {
    private ActionFactory() {
    }

    public static Action create(ActionConfig ac, RuntimeContext ctx) {
        final String type = ac.type() == null ? "" : ac.type().toLowerCase(Locale.ROOT);
        final Map<String, Object> opt = ac.options();

        return switch (type) {
            case "text" -> {
                final Object infoRaw = opt.get("info");
                final String textType = String.valueOf(opt.getOrDefault("text-type", "message"));
                yield new TextAction(ctx, TextAction.parse(textType), infoRaw);
            }
            case "transfer" -> new TransferAction(
                    ctx,
                    String.valueOf(opt.getOrDefault("target", "")),
                    String.valueOf(opt.getOrDefault("transferee", ".*"))
            );
            case "command" -> new CommandAction(
                    ctx,
                    CommandAction.parse(String.valueOf(opt.getOrDefault("executor", "player"))),
                    String.valueOf(opt.getOrDefault("command", "")),
                    String.valueOf(opt.getOrDefault("match", ".*"))
            );
            default -> null;
        };
    }
}

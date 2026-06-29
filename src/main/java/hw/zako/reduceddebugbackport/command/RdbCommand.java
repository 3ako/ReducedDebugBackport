package hw.zako.reduceddebugbackport.command;

import hw.zako.reduceddebugbackport.applier.PlayerApplier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public final class RdbCommand implements CommandExecutor {

    private final PlayerApplier applier;

    public RdbCommand(PlayerApplier applier) {
        this.applier = applier;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd,
                             @NotNull String label, String @NotNull [] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Players only.");
            return true;
        }

        final Player playerSender = (Player) sender;
        if (!playerSender.isOp()) {
            playerSender.sendMessage("No permission.");
            return true;
        }

        final int version;
        try {
            version = applier.resolveVersion(playerSender);
        } catch (final Exception ex) {
            playerSender.sendMessage("Protocol detection error: " + ex.getMessage());
            return true;
        }

        final boolean bypass = playerSender.hasPermission(PlayerApplier.BYPASS_PERMISSION);
        final boolean wouldSend = !bypass && version >= applier.minProtocol();
        playerSender.sendMessage("backend=" + applier.sender().getClass().getSimpleName()
                + " protocol=" + version
                + " min=" + applier.minProtocol()
                + " bypass=" + bypass
                + " -> " + (wouldSend ? "SEND" : "SKIP"));

        final boolean enable = args.length == 0 || !args[0].equalsIgnoreCase("off");
        try {
            applier.sender().sendReducedDebugInfo(playerSender, enable);
            playerSender.sendMessage("Forced status " + (enable ? "22 (enable)" : "23 (disable)")
                    + " regardless of threshold.");
        } catch (Exception ex) {
            playerSender.sendMessage("Send error: " + ex.getMessage());
        }

        return true;
    }
}

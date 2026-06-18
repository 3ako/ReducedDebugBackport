package hw.zako.reduceddebugbackport;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public final class RdbCommand implements CommandExecutor {

    private final ReducedDebugBackport plugin;

    public RdbCommand(ReducedDebugBackport plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender s, Command cmd, String label, String[] args) {
        if (!(s instanceof Player)) {
            s.sendMessage("Players only.");
            return true;
        }
        Player p = (Player) s;
        if (!p.isOp()) {
            p.sendMessage("No permission.");
            return true;
        }
        DebugInfoSender sender = plugin.getSender();
        if (sender == null) {
            p.sendMessage("No packet backend available.");
            return true;
        }

        int version;
        try {
            version = plugin.resolveVersion(p);
        } catch (Exception ex) {
            p.sendMessage("protocol detection error: " + ex.getMessage());
            return true;
        }

        boolean bypass = p.hasPermission(ReducedDebugBackport.BYPASS_PERMISSION);
        boolean wouldSend = !bypass && version >= plugin.getMinProtocolVersion();
        p.sendMessage("backend=" + sender.getClass().getSimpleName()
                + " protocol=" + version + " min=" + plugin.getMinProtocolVersion()
                + " bypass=" + bypass
                + " -> normal decision: " + (wouldSend ? "SEND" : "SKIP"));

        boolean enable = !(args.length > 0 && args[0].equalsIgnoreCase("off"));
        try {
            sender.sendReducedDebugInfo(p, enable);
            p.sendMessage("forced status " + (enable ? "22 (enable)" : "23 (disable)")
                    + " regardless of threshold");
        } catch (Exception ex) {
            p.sendMessage("send error: " + ex.getMessage());
        }
        return true;
    }
}

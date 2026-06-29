package hw.zako.reduceddebugbackport;

import hw.zako.reduceddebugbackport.applier.PlayerApplier;
import hw.zako.reduceddebugbackport.command.RdbCommand;
import hw.zako.reduceddebugbackport.listener.PlayerEventListener;
import hw.zako.reduceddebugbackport.sender.DebugInfoSender;
import hw.zako.reduceddebugbackport.sender.PacketEventsSender;
import hw.zako.reduceddebugbackport.sender.ProtocolLibSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReducedDebugBackport extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();

        final int minProtocol = getConfig().getInt("min-protocol-version", 774);
        final boolean logActions = getConfig().getBoolean("log-actions", false);
        final boolean debug = getConfig().getBoolean("debug", false);

        final DebugInfoSender sender = pickBackend();
        if (sender == null) {
            getLogger().severe("Neither 'packetevents' nor 'ProtocolLib' was found — disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        final boolean useVia = getServer().getPluginManager().isPluginEnabled("ViaVersion");
        if (!useVia) {
            getLogger().warning("ViaVersion not found — using backend for version detection. "
                    + "Behind a proxy this may report the server's native version. Install ViaVersion.");
        }

        final PlayerApplier applier = new PlayerApplier(
                sender, useVia, minProtocol, logActions || debug, debug, getLogger()
        );

        final PluginManager pm = getServer().getPluginManager();
        pm.registerEvents(new PlayerEventListener(this, applier), this);
        getCommand("rdb").setExecutor(new RdbCommand(applier));

        // Re-apply to players already online (e.g. '/plugman reload' scenario).
        for (final Player player : getServer().getOnlinePlayers()) {
            applier.apply(player);
        }

        getLogger().info("Enabled: backend=" + sender.getClass().getSimpleName()
                + ", versionSource=" + (useVia ? "ViaVersion" : sender.getClass().getSimpleName())
                + ", minProtocol=" + minProtocol
                + ", debug=" + debug);
    }

    private DebugInfoSender pickBackend() {
        final PluginManager pm = getServer().getPluginManager();
        if (pm.isPluginEnabled("packetevents")) {
            getLogger().info("Using PacketEvents backend.");
            return new PacketEventsSender();
        }
        if (pm.isPluginEnabled("ProtocolLib")) {
            getLogger().info("Using ProtocolLib backend.");
            return new ProtocolLibSender();
        }
        return null;
    }
}

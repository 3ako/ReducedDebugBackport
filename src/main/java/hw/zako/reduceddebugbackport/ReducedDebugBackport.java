package hw.zako.reduceddebugbackport;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class ReducedDebugBackport extends JavaPlugin implements Listener {

    static final String BYPASS_PERMISSION = "reduceddebugbackport.bypass";

    private DebugInfoSender sender;
    private int minProtocolVersion;
    private boolean logActions;
    private boolean debug;
    private boolean useVia;
    private SyncTaskDispatcher dispatcher;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        minProtocolVersion = getConfig().getInt("min-protocol-version", 774);
        logActions = getConfig().getBoolean("log-actions", false);
        debug = getConfig().getBoolean("debug", false);

        sender = pickBackend();
        if (sender == null) {
            getLogger().severe("Neither 'packetevents' nor 'ProtocolLib' was found. The plugin cannot work - disabling.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        useVia = getServer().getPluginManager().isPluginEnabled("ViaVersion");

        dispatcher = new SyncTaskDispatcher(this, 1L);
        getServer().getPluginManager().registerEvents(this, this);
        getCommand("rdb").setExecutor(new RdbCommand(this));

        for (Player p : getServer().getOnlinePlayers()) {
            applyLater(p, 1L);
        }

        getLogger().info("Enabled. Backend: " + sender.getClass().getSimpleName()
                + ", versionSource=" + (useVia ? "ViaVersion" : sender.getClass().getSimpleName())
                + ", min-protocol-version=" + minProtocolVersion + ", debug=" + debug);
        if (!useVia) {
            getLogger().warning("ViaVersion not found - using backend for version detection, "
                    + "which behind a proxy can report the server's native version. Install ViaVersion.");
        }
    }

    @Override
    public void onDisable() {
        if (dispatcher != null) {
            dispatcher.onDisable();
        }
    }

    private DebugInfoSender pickBackend() {
        PluginManager pm = getServer().getPluginManager();
        if (pm.isPluginEnabled("packetevents")) {
            getLogger().info("Using PacketEvents.");
            return new PacketEventsSender();
        }
        if (pm.isPluginEnabled("ProtocolLib")) {
            getLogger().info("Using ProtocolLib.");
            return new ProtocolLibSender();
        }
        return null;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        applyLater(e.getPlayer(), 1L);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) {
        applyLater(e.getPlayer(), 5L);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) {
        applyLater(e.getPlayer(), 5L);
    }

    private void applyLater(Player player, long delayTicks) {
        dispatcher.submit(() -> {
            if (player.isOnline()) {
                apply(player);
            }
        }, delayTicks);
    }

    int resolveVersion(Player player) {
        if (useVia) {
            return ViaVersionResolver.protocolVersion(player);
        }
        return sender.protocolVersion(player);
    }

    DebugInfoSender getSender() {
        return sender;
    }

    int getMinProtocolVersion() {
        return minProtocolVersion;
    }

    private void apply(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            if (debug) {
                getLogger().info("[debug] BYPASS " + player.getName() + " (has " + BYPASS_PERMISSION + ")");
            }
            return;
        }

        int version;
        try {
            version = resolveVersion(player);
        } catch (Exception ex) {
            getLogger().warning("Could not resolve the protocol version for " + player.getName() + ": " + ex.getMessage());
            return;
        }

        if (version < minProtocolVersion) {
            if (debug) {
                getLogger().info("[debug] SKIP " + player.getName()
                        + ": protocol " + version + " < min " + minProtocolVersion);
            }
            return;
        }

        try {
            sender.sendReducedDebugInfo(player, true);
            if (logActions || debug) {
                getLogger().info("[debug] SENT reducedDebugInfo -> " + player.getName()
                        + " (protocol " + version + ")");
            }
        } catch (Exception ex) {
            getLogger().warning("Failed to send reducedDebugInfo to " + player.getName() + ": " + ex.getMessage());
        }
    }

}

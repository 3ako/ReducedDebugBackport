package hw.zako.reduceddebugbackport.listener;

import hw.zako.reduceddebugbackport.applier.PlayerApplier;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public final class PlayerEventListener implements Listener {

    private static final int DELAY_JOIN = 1;
    private static final int DELAY_RESPAWN = 5;
    private static final int DELAY_WORLD_CHANGE = 5;

    private final Plugin plugin;
    private final PlayerApplier applier;

    public PlayerEventListener(Plugin plugin, PlayerApplier applier) {
        this.plugin = plugin;
        this.applier = applier;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) applier.apply(player);
        }, DELAY_JOIN);
    }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent event) {
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) applier.apply(player);
        }, DELAY_RESPAWN);
    }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent event) {
        final Player player = event.getPlayer();
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) applier.apply(player);
        }, DELAY_WORLD_CHANGE);
    }
}

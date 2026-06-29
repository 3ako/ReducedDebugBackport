package hw.zako.reduceddebugbackport.sender;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import org.bukkit.entity.Player;

public final class PacketEventsSender implements DebugInfoSender {

    // Minecraft protocol spec — Entity Status packet:
    // status 22 = enable reduced debug info, 23 = disable.
    private static final int STATUS_ENABLE = 22;
    private static final int STATUS_DISABLE = 23;

    @Override
    public int protocolVersion(Player player) {
        return PacketEvents.getAPI()
                .getPlayerManager()
                .getClientVersion(player)
                .getProtocolVersion();
    }

    @Override
    public void sendReducedDebugInfo(Player player, boolean enabled) {
        PacketEvents.getAPI().getPlayerManager().sendPacket(
                player,
                new WrapperPlayServerEntityStatus(
                        player.getEntityId(),
                        enabled ? STATUS_ENABLE : STATUS_DISABLE
                )
        );
    }
}

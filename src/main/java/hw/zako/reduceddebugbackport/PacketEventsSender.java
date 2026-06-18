package hw.zako.reduceddebugbackport;

import com.github.retrooper.packetevents.PacketEvents;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntityStatus;
import org.bukkit.entity.Player;

public final class PacketEventsSender implements DebugInfoSender {

    @Override
    public int protocolVersion(Player player) {
        return PacketEvents.getAPI().getPlayerManager()
                .getClientVersion(player).getProtocolVersion();
    }

    @Override
    public void sendReducedDebugInfo(Player player, boolean enabled) {
        int status = enabled ? 22 : 23;
        PacketEvents.getAPI().getPlayerManager()
                .sendPacket(player, new WrapperPlayServerEntityStatus(player.getEntityId(), status));
    }
}

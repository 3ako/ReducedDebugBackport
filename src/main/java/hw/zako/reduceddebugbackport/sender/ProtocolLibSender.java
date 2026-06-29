package hw.zako.reduceddebugbackport.sender;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public final class ProtocolLibSender implements DebugInfoSender {

    // Minecraft protocol spec — Entity Status packet:
    // status 22 = enable reduced debug info, 23 = disable.
    private static final byte STATUS_ENABLE = 22;
    private static final byte STATUS_DISABLE = 23;

    @Override
    public int protocolVersion(Player player) {
        return ProtocolLibrary.getProtocolManager().getProtocolVersion(player);
    }

    @Override
    public void sendReducedDebugInfo(Player player, boolean enabled) {
        final PacketContainer packet = ProtocolLibrary.getProtocolManager()
                .createPacket(PacketType.Play.Server.ENTITY_STATUS);
        packet.getIntegers().write(0, player.getEntityId());
        packet.getBytes().write(0, enabled ? STATUS_ENABLE : STATUS_DISABLE);
        try {
            ProtocolLibrary.getProtocolManager().sendServerPacket(player, packet);
        } catch (final Exception e) {
            throw new RuntimeException("ProtocolLib sendServerPacket failed", e);
        }
    }
}

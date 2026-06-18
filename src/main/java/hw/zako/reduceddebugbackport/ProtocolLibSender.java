package hw.zako.reduceddebugbackport;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import org.bukkit.entity.Player;

public final class ProtocolLibSender implements DebugInfoSender {

    private final ProtocolManager protocol = ProtocolLibrary.getProtocolManager();

    @Override
    public int protocolVersion(Player player) {
        return protocol.getProtocolVersion(player);
    }

    @Override
    public void sendReducedDebugInfo(Player player, boolean enabled) {
        PacketContainer packet = protocol.createPacket(PacketType.Play.Server.ENTITY_STATUS);
        packet.getIntegers().write(0, player.getEntityId());
        packet.getBytes().write(0, (byte) (enabled ? 22 : 23));
        try {
            protocol.sendServerPacket(player, packet);
        } catch (Exception e) {
            throw new RuntimeException("ProtocolLib sendServerPacket failed", e);
        }
    }
}

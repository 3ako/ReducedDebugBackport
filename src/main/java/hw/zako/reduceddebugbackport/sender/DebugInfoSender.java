package hw.zako.reduceddebugbackport.sender;

import org.bukkit.entity.Player;

public interface DebugInfoSender {

    int protocolVersion(Player player);

    void sendReducedDebugInfo(Player player, boolean enabled);
}

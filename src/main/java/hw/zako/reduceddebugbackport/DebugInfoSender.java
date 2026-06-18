package hw.zako.reduceddebugbackport;

import org.bukkit.entity.Player;

public interface DebugInfoSender {

    int protocolVersion(Player player);

    void sendReducedDebugInfo(Player player, boolean enabled);

}

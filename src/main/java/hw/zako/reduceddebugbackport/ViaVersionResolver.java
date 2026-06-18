package hw.zako.reduceddebugbackport;

import com.viaversion.viaversion.api.Via;
import org.bukkit.entity.Player;

final class ViaVersionResolver {

    private ViaVersionResolver() {
    }

    static int protocolVersion(Player player) {
        return Via.getAPI().getPlayerVersion(player.getUniqueId());
    }
}

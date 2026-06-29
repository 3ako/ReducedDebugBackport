package hw.zako.reduceddebugbackport.applier;

import hw.zako.reduceddebugbackport.sender.DebugInfoSender;
import org.bukkit.entity.Player;

import java.util.logging.Logger;

public final class PlayerApplier {

    public static final String BYPASS_PERMISSION = "reduceddebugbackport.bypass";

    private final DebugInfoSender sender;
    private final boolean useVia;
    private final int minProtocol;

    private final boolean verbose;
    private final boolean debug;
    private final Logger logger;

    public PlayerApplier(
            final DebugInfoSender sender,
            final boolean useVia,
            final int minProtocol,
            final boolean verbose,
            final boolean debug,
            final Logger logger) {
        this.sender = sender;
        this.useVia = useVia;
        this.minProtocol = minProtocol;
        this.verbose = verbose;
        this.debug = debug;
        this.logger = logger;
    }

    /**
     * Checks the player's protocol version and sends the reduced-debug-info flag
     * if the threshold is met. Must be called from the main thread.
     */
    public void apply(Player player) {
        if (player.hasPermission(BYPASS_PERMISSION)) {
            if (debug) {
                logger.info("[debug] BYPASS " + player.getName()
                        + " — has " + BYPASS_PERMISSION);
            }
            return;
        }

        final int version;
        try {
            version = resolveVersion(player);
        } catch (final Exception ex) {
            logger.warning("Could not resolve protocol version for "
                    + player.getName() + ": " + ex.getMessage());
            return;
        }

        if (version < minProtocol) {
            if (debug) {
                logger.info("[debug] SKIP " + player.getName()
                        + ": protocol " + version + " < min " + minProtocol);
            }
            return;
        }

        try {
            sender.sendReducedDebugInfo(player, true);
            if (verbose) {
                logger.info("[info] SENT reducedDebugInfo -> " + player.getName()
                        + " (protocol " + version + ")");
            }
        } catch (final Exception ex) {
            logger.warning("Failed to send reducedDebugInfo to "
                    + player.getName() + ": " + ex.getMessage());
        }
    }

    public int resolveVersion(Player player) {
        return useVia
                ? ViaVersionResolver.protocolVersion(player)
                : sender.protocolVersion(player);
    }

    public DebugInfoSender sender() {
        return sender;
    }

    public int minProtocol() {
        return minProtocol;
    }
}

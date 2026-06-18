package hw.zako.reduceddebugbackport;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;

/**
 * A single repeating task instead of registering a Bukkit task per player,
 * to avoid churning task ids (they are int and keep incrementing - they overflow on long uptime).
 *
 * Designed for the main thread: submit() and the ticker run on the same server thread,
 * so the currentTick counter is safe without synchronization.
 */
public final class SyncTaskDispatcher {

    private final Plugin plugin;
    private final long period;

    private final PriorityBlockingQueue<ScheduledTask> queue = new PriorityBlockingQueue<>();

    private BukkitTask ticker;
    private long currentTick;

    public SyncTaskDispatcher(final Plugin plugin, final long period) {
        this.plugin = plugin;
        this.period = Math.max(1L, period);
        start();
    }

    public void submit(final Runnable task) {
        submit(task, 0L);
    }

    public void submit(final Runnable safeTask, final long delayTicks) {
        final long delay = Math.max(0L, delayTicks);
        queue.offer(new ScheduledTask(safeTask, currentTick + delay));
    }

    public void start() {
        if (ticker != null && !ticker.isCancelled()) {
            ticker.cancel();
        }
        ticker = new BukkitRunnable() {
            @Override
            public void run() {
                // Own tick counter (Bukkit.getCurrentTick() is missing in the 1.16 API).
                currentTick += period;

                ScheduledTask head = queue.peek();
                while (head != null) {
                    if (head.executeTick > currentTick) break;

                    final ScheduledTask readyTask = queue.poll();
                    if (readyTask == null) continue;

                    tryRunTask(readyTask);

                    head = queue.peek();
                }
            }

            private void tryRunTask(ScheduledTask ready) {
                try {
                    ready.task.run();
                } catch (final Throwable t) {
                    plugin.getLogger().log(Level.SEVERE, "Error in SyncTaskDispatcher", t);
                }
            }
        }.runTaskTimer(plugin, period, period);
    }

    public void onDisable() {
        if (ticker != null && !ticker.isCancelled()) {
            ticker.cancel();
        }
        queue.clear();
    }

    private static final class ScheduledTask implements Comparable<ScheduledTask> {
        private static final AtomicLong SEQ = new AtomicLong(0L);

        private final Runnable task;
        private final long executeTick;
        private final long seq;

        private ScheduledTask(final Runnable task, final long executeTick) {
            this.task = task;
            this.executeTick = executeTick;
            this.seq = SEQ.getAndIncrement();
        }

        @Override
        public int compareTo(final ScheduledTask other) {
            if (this.executeTick < other.executeTick) return -1;
            if (this.executeTick > other.executeTick) return 1;
            return Long.compare(this.seq, other.seq);
        }
    }
}

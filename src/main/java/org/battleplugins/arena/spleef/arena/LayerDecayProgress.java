package org.battleplugins.arena.spleef.arena;

/** Distributes a layer across ticks without rounding the duration down. */
final class LayerDecayProgress {
    private final int blocksPerTick;
    private final long remainder;
    private final long totalTicks;
    private long accumulatedRemainder;
    private long elapsedTicks;

    LayerDecayProgress(int blockCount, long totalTicks) {
        if (blockCount < 0 || totalTicks < 1) {
            throw new IllegalArgumentException("Block count must be nonnegative and duration positive");
        }
        this.blocksPerTick = (int) (blockCount / totalTicks);
        this.remainder = blockCount % totalTicks;
        this.totalTicks = totalTicks;
    }

    int nextBatchSize() {
        if (this.elapsedTicks >= this.totalTicks) {
            return 0;
        }
        this.elapsedTicks++;
        int count = this.blocksPerTick;
        // Equivalent to accumulating the remainder, without overflowing for long durations.
        if (this.accumulatedRemainder >= this.totalTicks - this.remainder) {
            this.accumulatedRemainder -= this.totalTicks - this.remainder;
            count++;
        } else {
            this.accumulatedRemainder += this.remainder;
        }
        return count;
    }
}

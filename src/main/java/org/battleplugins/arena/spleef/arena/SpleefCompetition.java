package org.battleplugins.arena.spleef.arena;

import io.papermc.paper.math.Position;
import org.battleplugins.arena.competition.CompetitionType;
import org.battleplugins.arena.competition.LiveCompetition;
import org.battleplugins.arena.spleef.ArenaSpleef;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SpleefCompetition extends LiveCompetition<SpleefCompetition> {
    private final SpleefArena arena;
    private final SpleefMap map;

    private final List<BukkitTask> decayTasks = new ArrayList<>();
    private final List<Position> pendingDecays = new ArrayList<>();
    private BukkitTask boundaryTask;

    public SpleefCompetition(SpleefArena arena, CompetitionType type, SpleefMap map) {
        super(arena, type, map);

        this.arena = arena;
        this.map = map;
    }

    public void beginBoundaryChecks() {
        this.stopBoundaryChecks();
        if (this.arena.isLeaveOnBoundaryExit()) {
            this.boundaryTask = Bukkit.getScheduler().runTaskTimer(
                    ArenaSpleef.getInstance(), () -> BoundaryExitCheck.check(this), 20, 20);
        }
    }

    public void stopBoundaryChecks() {
        if (this.boundaryTask != null) {
            this.boundaryTask.cancel();
            this.boundaryTask = null;
        }
    }

    @Override
    protected void onDestroy() {
        this.stopBoundaryChecks();
        this.stopLayerDecay();
        super.onDestroy();
    }

    public void beginLayerDecay() {
        List<SpleefMap.Layer> layers = new ArrayList<>(this.map.getLayers());
        layers.sort((layer1, layer2) -> layer2.getBounds().getMaxY() - layer1.getBounds().getMaxY());

        for (int i = 0; i < layers.size(); i++) {
            SpleefMap.Layer layer = layers.get(i);

            long delay = (i + 1) * this.arena.getLayerDecayDelay().toSeconds() * 20;
            BukkitTask decayTask = Bukkit.getScheduler().runTaskLater(ArenaSpleef.getInstance(), () -> {
                Duration layerDecayTime = this.arena.getLayerDecayTime();
                List<Block> blocks = new ArrayList<>(layer.getBounds().getVolume());

                for (int x = layer.getBounds().getMinX(); x <= layer.getBounds().getMaxX(); x++) {
                    for (int y = layer.getBounds().getMinY(); y <= layer.getBounds().getMaxY(); y++) {
                        for (int z = layer.getBounds().getMinZ(); z <= layer.getBounds().getMaxZ(); z++) {
                            blocks.add(this.map.getWorld().getBlockAt(x, y, z));
                        }
                    }
                }

                Collections.shuffle(blocks);

                if (blocks.isEmpty()) {
                    return;
                }

                long totalDecayTimeTicks = Math.max(1, layerDecayTime.toMillis() / 50);
                LayerDecayProgress progress = new LayerDecayProgress(blocks.size(), totalDecayTimeTicks);

                this.decayTasks.add(new BukkitRunnable() {
                    private int index = 0;

                    @Override
                    public void run() {
                        int count = progress.nextBatchSize();
                        for (int i = 0; i < count; i++) {
                            blocks.get(this.index++).setType(Material.AIR);
                        }

                        if (this.index >= blocks.size()) {
                            this.cancel();
                        }
                    }
                }.runTaskTimer(ArenaSpleef.getInstance(), 1, 1));
            }, delay);

            this.decayTasks.add(decayTask);
        }
    }

    public void stopLayerDecay() {
        for (BukkitTask decayTask : this.decayTasks) {
            decayTask.cancel();
        }

        this.decayTasks.clear();
    }

    public void pasteLayers() {
        List<SpleefMap.Layer> layers = this.map.getLayers();
        for (SpleefMap.Layer layer : layers) {
            for (int x = layer.getBounds().getMinX(); x <= layer.getBounds().getMaxX(); x++) {
                for (int y = layer.getBounds().getMinY(); y <= layer.getBounds().getMaxY(); y++) {
                    for (int z = layer.getBounds().getMinZ(); z <= layer.getBounds().getMaxZ(); z++) {
                        this.map.getWorld().getBlockAt(x, y, z).setBlockData(layer.getBlockData());
                    }
                }
            }
        }
    }

    public void decayBlock(Position position) {
        if (this.pendingDecays.contains(position)) {
            return;
        }

        this.pendingDecays.add(position);

        Bukkit.getScheduler().runTaskLater(ArenaSpleef.getInstance(), () -> {
            Block block = this.map.getWorld().getBlockAt(position.blockX(), position.blockY(), position.blockZ());
            block.setType(Material.AIR);

            this.pendingDecays.remove(position);
        }, 5);
    }
}

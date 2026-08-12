package ru.florestdev.magnetiteTowers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Powerable;
import org.bukkit.block.data.type.RedstoneWire;

public class TowerScheduler {
    private static final BlockFace[] INPUT_FACES;
    private final MagnetitePlugin plugin;
    private final Map<BlockPos, ScheduledTask> scanTasks = new ConcurrentHashMap();

    public TowerScheduler(MagnetitePlugin plugin) {
        this.plugin = plugin;
    }

    public void startTower(BlockPos pos) {
        this.stopTower(pos);
        Location loc = pos.toLocation();
        if (loc != null) {
            long period = Math.max(1L, this.plugin.getConfig().getLong("scan-period-ticks", 2L));
            ScheduledTask task = Bukkit.getRegionScheduler().runAtFixedRate(this.plugin, loc, (t) -> this.scan(pos), 20L, period);
            this.scanTasks.put(pos, task);
        }
    }

    public void stopTower(BlockPos pos) {
        ScheduledTask task = (ScheduledTask)this.scanTasks.remove(pos);
        if (task != null) {
            task.cancel();
        }

    }

    public void stopAll() {
        this.scanTasks.values().forEach(ScheduledTask::cancel);
        this.scanTasks.clear();
    }

    private void scan(BlockPos pos) {
        ChannelManager cm = this.plugin.getChannelManager();
        Integer channel = cm.getChannel(pos);
        if (channel == null) {
            this.stopTower(pos);
        } else {
            Block block = pos.toBlock();
            if (block != null && block.getType() == Material.LODESTONE) {
                int power = this.getBlockPowerSafe(block);
                if (power != cm.getLastScanned(pos)) {
                    cm.setLastScanned(pos, power);
                    cm.updateChannelPowerIfChanged(channel);
                }

                boolean active = cm.getChannelPower(channel) > 0;
                this.applyOutput(pos, active);
            }
        }
    }

    private int getBlockPowerSafe(Block block) {
        int maxPower = 0;

        for(BlockFace face : INPUT_FACES) {
            Block relative = block.getRelative(face);
            int power = this.getPowerFromBlock(relative, face.getOppositeFace());
            if (power > maxPower) {
                maxPower = power;
                if (power >= 15) {
                    break;
                }
            }
        }

        return maxPower;
    }

    private int getPowerFromBlock(Block block, BlockFace towardsFace) {
        Material type = block.getType();
        if (type == Material.REDSTONE_WIRE) {
            BlockData data = block.getBlockData();
            if (data instanceof RedstoneWire) {
                RedstoneWire wire = (RedstoneWire)data;
                return wire.getPower();
            } else {
                return 0;
            }
        } else if (type != Material.REPEATER && type != Material.COMPARATOR) {
            if (type == Material.REDSTONE_BLOCK) {
                return 15;
            } else {
                if (type.isBlock() && type.isSolid()) {
                    BlockData data = block.getBlockData();
                    if (data instanceof Powerable) {
                        Powerable powerable = (Powerable)data;
                        if (powerable.isPowered()) {
                            return 15;
                        }
                    }
                }

                return this.getWeakPower(block, towardsFace);
            }
        } else {
            BlockData data = block.getBlockData();
            if (data instanceof Powerable) {
                Powerable powerable = (Powerable)data;
                if (powerable.isPowered()) {
                    if (data instanceof Directional) {
                        Directional directional = (Directional)data;
                        BlockFace facing = directional.getFacing();
                        if (facing == towardsFace) {
                            return 15;
                        }
                    }

                    return this.getWeakPower(block, towardsFace);
                }
            }

            return 0;
        }
    }

    private int getWeakPower(Block block, BlockFace towardsFace) {
        int strong = block.getType().isSolid() ? this.getStrongPower(block) : 0;
        if (strong > 0) {
            return strong;
        } else {
            if (block.getType() == Material.REDSTONE_WIRE) {
                BlockData data = block.getBlockData();
                if (data instanceof RedstoneWire) {
                    RedstoneWire wire = (RedstoneWire)data;
                    int power = wire.getPower();
                    if (power > 0) {
                        return power - 1;
                    }
                }
            }

            return 0;
        }
    }

    private int getStrongPower(Block block) {
        if (block.getType() == Material.REDSTONE_BLOCK) {
            return 15;
        } else {
            BlockData data = block.getBlockData();
            if (data instanceof Powerable) {
                Powerable powerable = (Powerable)data;
                if (powerable.isPowered()) {
                    return 15;
                }
            }

            if (block.getType() == Material.REDSTONE_WIRE) {
                BlockData wireData = block.getBlockData();
                if (wireData instanceof RedstoneWire) {
                    RedstoneWire wire = (RedstoneWire)wireData;
                    int power = wire.getPower();
                    if (power > 0) {
                        return power;
                    }
                }
            }

            return 0;
        }
    }

    private void applyOutput(BlockPos towerPos, boolean active) {
        Block tower = towerPos.toBlock();
        if (tower != null) {
            Block wire = tower.getRelative(BlockFace.UP);
            if (wire.getType() == Material.REDSTONE_WIRE) {
                BlockData data = wire.getBlockData();
                if (data instanceof RedstoneWire) {
                    RedstoneWire wireData = (RedstoneWire)data;
                    int desired = active ? wireData.getMaximumPower() : 0;
                    if (wireData.getPower() != desired) {
                        wireData.setPower(desired);
                        wire.setBlockData(wireData, true);
                    }
                }

            }
        }
    }

    static {
        INPUT_FACES = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.DOWN};
    }
}

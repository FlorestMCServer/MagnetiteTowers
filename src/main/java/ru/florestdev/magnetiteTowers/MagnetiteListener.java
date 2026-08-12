package ru.florestdev.magnetiteTowers;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.RedstoneWire;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class MagnetiteListener implements Listener {
    private final MagnetitePlugin plugin;

    public MagnetiteListener(MagnetitePlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(
            ignoreCancelled = true
    )
    public void onPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        if (item.getType() == Material.LODESTONE) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                PersistentDataContainer pdc = meta.getPersistentDataContainer();
                if (pdc.has(this.plugin.getChannelKey(), PersistentDataType.INTEGER)) {
                    int channel = (Integer)pdc.get(this.plugin.getChannelKey(), PersistentDataType.INTEGER);
                    Block block = event.getBlockPlaced();
                    Block above = block.getRelative(BlockFace.UP);
                    if (above.getType() != Material.AIR) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cНад магнетитом должно быть свободное место — туда встанет выходная антенна (редстоун-пыль).");
                    } else {
                        above.setType(Material.REDSTONE_WIRE);
                        BlockData data = above.getBlockData();
                        if (data instanceof RedstoneWire) {
                            RedstoneWire wire = (RedstoneWire)data;
                            wire.setPower(0);
                            above.setBlockData(wire, false);
                        }

                        BlockPos pos = BlockPos.of(block);
                        this.plugin.getChannelManager().register(pos, channel);
                        this.plugin.getChannelManager().setLastScanned(pos, 0);
                        this.plugin.getTowerScheduler().startTower(pos);
                        event.getPlayer().sendMessage("§bМагнетит настроен на канал §e" + channel + "§b.");
                    }
                }
            }
        }
    }

    @EventHandler(
            ignoreCancelled = true
    )
    public void onBreakMagnetite(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.LODESTONE) {
            BlockPos pos = BlockPos.of(block);
            ChannelManager cm = this.plugin.getChannelManager();
            if (cm.isRegistered(pos)) {
                int channel = cm.getChannel(pos);
                cm.unregister(pos);
                this.plugin.getTowerScheduler().stopTower(pos);
                Block above = block.getRelative(BlockFace.UP);
                if (above.getType() == Material.REDSTONE_WIRE) {
                    above.setType(Material.AIR);
                }

                event.setDropItems(false);
                block.getWorld().dropItemNaturally(block.getLocation(), MagnetiteItems.create(this.plugin, channel));
                event.getPlayer().sendMessage("§7Магнетит канала §e" + channel + " §7снят с сети.");
            }
        }
    }

    @EventHandler(
            ignoreCancelled = true
    )
    public void onBreakOutputWire(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (block.getType() == Material.REDSTONE_WIRE) {
            Block below = block.getRelative(BlockFace.DOWN);
            if (below.getType() == Material.LODESTONE) {
                BlockPos pos = BlockPos.of(below);
                if (this.plugin.getChannelManager().isRegistered(pos)) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cЭто выходная антенна вышки. Сломайте сам магнетит, чтобы убрать вышку.");
                }
            }

        }
    }
}

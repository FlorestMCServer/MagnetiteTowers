package ru.florestdev.magnetiteTowers;

import java.util.List;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public final class MagnetiteItems {
    private MagnetiteItems() {
    }

    public static ItemStack create(MagnetitePlugin plugin, int channel) {
        ItemStack item = new ItemStack(Material.LODESTONE);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§bМагнетит §7[Канал §e" + channel + "§7]");
        meta.setLore(List.of("§7Передаёт редстоун-сигнал", "§7всем магнетитам этого канала"));
        meta.getPersistentDataContainer().set(plugin.getChannelKey(), PersistentDataType.INTEGER, channel);
        item.setItemMeta(meta);
        return item;
    }
}

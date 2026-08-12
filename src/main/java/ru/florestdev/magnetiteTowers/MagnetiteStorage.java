package ru.florestdev.magnetiteTowers;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.bukkit.configuration.file.YamlConfiguration;

public class MagnetiteStorage {
    private final MagnetitePlugin plugin;
    private final File file;

    public MagnetiteStorage(MagnetitePlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "towers.yml");
    }

    public void save(ChannelManager cm) {
        YamlConfiguration cfg = new YamlConfiguration();
        List<Map<String, Object>> list = new ArrayList();

        for(Map.Entry<BlockPos, Integer> entry : cm.getAll().entrySet()) {
            BlockPos pos = (BlockPos)entry.getKey();
            Map<String, Object> m = new LinkedHashMap();
            m.put("world", pos.getWorld().toString());
            m.put("x", pos.getX());
            m.put("y", pos.getY());
            m.put("z", pos.getZ());
            m.put("channel", entry.getValue());
            list.add(m);
        }

        cfg.set("towers", list);

        try {
            this.plugin.getDataFolder().mkdirs();
            cfg.save(this.file);
        } catch (Exception e) {
            this.plugin.getLogger().warning("Не удалось сохранить towers.yml: " + e.getMessage());
        }

    }

    public void load(ChannelManager cm) {
        if (this.file.exists()) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(this.file);
            List<?> list = cfg.getList("towers");
            if (list != null) {
                for(Object o : list) {
                    if (o instanceof Map) {
                        Map<?, ?> m = (Map)o;

                        try {
                            UUID world = UUID.fromString(String.valueOf(m.get("world")));
                            int x = ((Number)m.get("x")).intValue();
                            int y = ((Number)m.get("y")).intValue();
                            int z = ((Number)m.get("z")).intValue();
                            int channel = ((Number)m.get("channel")).intValue();
                            cm.register(new BlockPos(world, x, y, z), channel);
                        } catch (Exception e) {
                            this.plugin.getLogger().warning("Пропущена повреждённая запись вышки: " + e.getMessage());
                        }
                    }
                }

            }
        }
    }
}

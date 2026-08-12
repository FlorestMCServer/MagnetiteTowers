
package ru.florestdev.magnetiteTowers;

import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class MagnetitePlugin extends JavaPlugin {
    private NamespacedKey channelKey;
    private ChannelManager channelManager;
    private MagnetiteStorage storage;
    private TowerScheduler towerScheduler;

    public MagnetitePlugin() {
    }

    public void onEnable() {
        this.channelKey = new NamespacedKey(this, "magnetite_channel");
        this.saveDefaultConfig();
        this.channelManager = new ChannelManager();
        this.storage = new MagnetiteStorage(this);
        this.storage.load(this.channelManager);
        this.towerScheduler = new TowerScheduler(this);

        for(BlockPos pos : this.channelManager.getAll().keySet()) {
            this.towerScheduler.startTower(pos);
        }

        this.getServer().getPluginManager().registerEvents(new MagnetiteListener(this), this);
        MagnetiteCommand cmd = new MagnetiteCommand(this);
        this.getCommand("magnetite").setExecutor(cmd);
        this.getCommand("magnetite").setTabCompleter(cmd);
        this.getLogger().info("Magnetite Towers включён. Загружено вышек: " + this.channelManager.getTotalCount());
    }

    public void onDisable() {
        if (this.towerScheduler != null) {
            this.towerScheduler.stopAll();
        }

        if (this.storage != null && this.channelManager != null) {
            this.storage.save(this.channelManager);
        }

        this.getLogger().info("Magnetite Towers выключен, данные сохранены.");
    }

    public NamespacedKey getChannelKey() {
        return this.channelKey;
    }

    public ChannelManager getChannelManager() {
        return this.channelManager;
    }

    public MagnetiteStorage getStorage() {
        return this.storage;
    }

    public TowerScheduler getTowerScheduler() {
        return this.towerScheduler;
    }
}

package ru.florestdev.magnetiteTowers;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ChannelManager {
    private final ConcurrentHashMap<BlockPos, Integer> towerChannel = new ConcurrentHashMap();
    private final ConcurrentHashMap<Integer, Set<BlockPos>> channelTowers = new ConcurrentHashMap();
    private final ConcurrentHashMap<Integer, Integer> channelPower = new ConcurrentHashMap();
    private final ConcurrentHashMap<BlockPos, Integer> lastScannedPower = new ConcurrentHashMap();

    public ChannelManager() {
    }

    public synchronized void register(BlockPos pos, int channel) {
        this.unregister(pos);
        this.towerChannel.put(pos, channel);
        ((Set)this.channelTowers.computeIfAbsent(channel, (c) -> ConcurrentHashMap.newKeySet())).add(pos);
    }

    public synchronized void unregister(BlockPos pos) {
        Integer old = (Integer)this.towerChannel.remove(pos);
        if (old != null) {
            Set<BlockPos> set = (Set)this.channelTowers.get(old);
            if (set != null) {
                set.remove(pos);
                if (set.isEmpty()) {
                    this.channelTowers.remove(old);
                    this.channelPower.remove(old);
                }
            }
        }

        this.lastScannedPower.remove(pos);
    }

    public boolean isRegistered(BlockPos pos) {
        return this.towerChannel.containsKey(pos);
    }

    public Integer getChannel(BlockPos pos) {
        return (Integer)this.towerChannel.get(pos);
    }

    public Set<BlockPos> getTowers(int channel) {
        return (Set)this.channelTowers.getOrDefault(channel, Collections.emptySet());
    }

    public Set<Integer> getChannels() {
        return this.channelTowers.keySet();
    }

    public Map<BlockPos, Integer> getAll() {
        return this.towerChannel;
    }

    public int getChannelPower(int channel) {
        return (Integer)this.channelPower.getOrDefault(channel, 0);
    }

    public int getLastScanned(BlockPos pos) {
        return (Integer)this.lastScannedPower.getOrDefault(pos, -1);
    }

    public void setLastScanned(BlockPos pos, int power) {
        this.lastScannedPower.put(pos, power);
    }

    public synchronized boolean updateChannelPowerIfChanged(int channel) {
        int max = 0;

        for(BlockPos pos : this.getTowers(channel)) {
            max = Math.max(max, this.getLastScanned(pos));
        }

        int old = this.getChannelPower(channel);
        if (max == old) {
            return false;
        } else {
            this.channelPower.put(channel, max);
            return true;
        }
    }

    public int getTotalCount() {
        return this.towerChannel.size();
    }
}

package ru.florestdev.magnetiteTowers;

import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class BlockPos {
    private final UUID world;
    private final int x;
    private final int y;
    private final int z;

    public BlockPos(UUID world, int x, int y, int z) {
        this.world = world;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public static BlockPos of(Block block) {
        return new BlockPos(block.getWorld().getUID(), block.getX(), block.getY(), block.getZ());
    }

    public static BlockPos of(Location loc) {
        return new BlockPos(loc.getWorld().getUID(), loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    public Block toBlock() {
        World w = Bukkit.getWorld(this.world);
        return w == null ? null : w.getBlockAt(this.x, this.y, this.z);
    }

    public Location toLocation() {
        World w = Bukkit.getWorld(this.world);
        return w == null ? null : new Location(w, (double)this.x, (double)this.y, (double)this.z);
    }

    public UUID getWorld() {
        return this.world;
    }

    public int getX() {
        return this.x;
    }

    public int getY() {
        return this.y;
    }

    public int getZ() {
        return this.z;
    }

    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (!(o instanceof BlockPos)) {
            return false;
        } else {
            BlockPos p = (BlockPos)o;
            return this.x == p.x && this.y == p.y && this.z == p.z && this.world.equals(p.world);
        }
    }

    public int hashCode() {
        return Objects.hash(new Object[]{this.world, this.x, this.y, this.z});
    }

    public String toString() {
        String var10000 = String.valueOf(this.world);
        return var10000 + ":" + this.x + "," + this.y + "," + this.z;
    }
}

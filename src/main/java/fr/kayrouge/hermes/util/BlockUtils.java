package fr.kayrouge.hermes.util;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.BlockPosition;
import com.comphenix.protocol.wrappers.WrappedBlockData;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;

public class BlockUtils {

    private static final ProtocolManager protocolManager = ProtocolLibrary.getProtocolManager();

    public static void sendFakeBlock(Player player, Location loc, Material fakeMaterial) {
        PacketContainer packet = protocolManager.createPacket(PacketType.Play.Server.BLOCK_CHANGE);
        packet.getBlockPositionModifier().write(0, new BlockPosition(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ()));
        packet.getBlockData().write(0, WrappedBlockData.createData(fakeMaterial));

        protocolManager.sendServerPacket(player, packet);
    }

    public static void createCircle(Location center, int radius, Material block, Player player) {
        World world = center.getWorld();
        int x = center.getBlockX();
        int y = center.getBlockY();
        int z = center.getBlockZ();

        int radiusSquare = radius*radius;
        for(int blockX = -radius; blockX < radius; blockX++) {
            for(int blockZ = -radius; blockZ < radius; blockZ++) {
                if((blockX*blockX + blockZ*blockZ) <= radiusSquare) {
                    sendFakeBlock(player, new Location(world, x+blockX, y, z+blockZ), block);
                }
            }
        }
    }

}

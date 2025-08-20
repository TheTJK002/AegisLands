package net.tjkraft.aegislands.network;

import net.minecraft.core.BlockPos;
import net.tjkraft.aegislands.network.trusted.AddTrustedPacket;

import java.util.UUID;

public class AegisLandsNetworkWrapper {
    public static void sendAddTrusted(BlockPos pos, UUID uuid) {
        net.tjkraft.aegislands.network.AegisLandsNetwork.INSTANCE.sendToServer(new AddTrustedPacket(pos, uuid));
    }
}

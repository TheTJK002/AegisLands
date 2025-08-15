package net.tjkraft.claimanchor.network;

import net.minecraft.core.BlockPos;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.AddTrustedPacket;

import java.util.UUID;

public class ClaimAnchorNetworkWrapper {
    public static void sendAddTrusted(BlockPos pos, UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new AddTrustedPacket(pos, uuid));
    }
}

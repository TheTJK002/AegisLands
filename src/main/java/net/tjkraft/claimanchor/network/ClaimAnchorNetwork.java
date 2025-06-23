package net.tjkraft.claimanchor.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.network.claimAnchorTime.ClaimAnchorTime;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.AddTrustedPacket;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.RemoveTrustedPacket;

public class ClaimAnchorNetwork {
    public static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(ClaimAnchor.MOD_ID, "messages"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        INSTANCE = net;

        net.messageBuilder(AddTrustedPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(AddTrustedPacket::decode)
                .encoder(AddTrustedPacket::encode)
                .consumerMainThread(AddTrustedPacket::handle)
                .add();

        net.messageBuilder(RemoveTrustedPacket.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(RemoveTrustedPacket::decode)
                .encoder(RemoveTrustedPacket::encode)
                .consumerMainThread(RemoveTrustedPacket::handle)
                .add();

        net.messageBuilder(ClaimAnchorTime.class, id(), NetworkDirection.PLAY_TO_SERVER)
                .decoder(ClaimAnchorTime::decode)
                .encoder(ClaimAnchorTime::encode)
                .consumerMainThread(ClaimAnchorTime::handle)
                .add();
    }
}

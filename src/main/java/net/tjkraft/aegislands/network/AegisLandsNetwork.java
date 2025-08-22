package net.tjkraft.aegislands.network;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.network.time.AegisLandsTime;
import net.tjkraft.aegislands.network.trusted.AddTrustedPacket;
import net.tjkraft.aegislands.network.trusted.RemoveTrustedPacket;
import net.tjkraft.aegislands.network.openScreen.AegisLandsOpenMainScreen;
import net.tjkraft.aegislands.network.syncPlayers.SyncOnlinePlayersPacket;
import net.tjkraft.aegislands.network.syncPlayers.SyncTrustedPacket;

public class AegisLandsNetwork {
    public static SimpleChannel INSTANCE;

    private static int packetId = 0;

    private static int id() {
        return packetId++;
    }

    public static void register() {
        SimpleChannel net = NetworkRegistry.ChannelBuilder
                .named(new ResourceLocation(AegisLands.MOD_ID, "messages"))
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

        net.messageBuilder(AegisLandsTime.class, id(), NetworkDirection.PLAY_TO_CLIENT)
                .decoder(AegisLandsTime::decode)
                .encoder(AegisLandsTime::encode)
                .consumerMainThread(AegisLandsTime::handle)
                .add();

        net.messageBuilder(SyncOnlinePlayersPacket.class, id())
                .decoder(SyncOnlinePlayersPacket::decode)
                .encoder(SyncOnlinePlayersPacket::encode)
                .consumerMainThread(SyncOnlinePlayersPacket::handle)
                .add();

        net.messageBuilder(SyncTrustedPacket.class, id())
                .decoder(SyncTrustedPacket::decode)
                .encoder(SyncTrustedPacket::encode)
                .consumerMainThread(SyncTrustedPacket::handle)
                .add();

        net.messageBuilder(AegisLandsOpenMainScreen.class, id())
                .decoder(AegisLandsOpenMainScreen::decode)
                .encoder(AegisLandsOpenMainScreen::encode)
                .consumerMainThread(AegisLandsOpenMainScreen::handle)
                .add();
    }
}

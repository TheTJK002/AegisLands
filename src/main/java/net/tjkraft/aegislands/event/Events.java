package net.tjkraft.aegislands.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.network.AegisLandsNetwork;
import net.tjkraft.aegislands.network.syncPlayers.SyncOnlinePlayersPacket;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = AegisLands.MOD_ID)
public class Events {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;

        List<UUID> uuids = server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList();
        AegisLandsNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncOnlinePlayersPacket(uuids));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;

        List<UUID> uuids = server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList();
        net.tjkraft.aegislands.network.AegisLandsNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncOnlinePlayersPacket(uuids));
    }
}
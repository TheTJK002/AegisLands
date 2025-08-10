package net.tjkraft.claimanchor.event;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.syncOnlinePlayers.SyncOnlinePlayersPacket;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = ClaimAnchor.MOD_ID)
public class Events {

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;

        List<UUID> uuids = server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList();
        ClaimAnchorNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncOnlinePlayersPacket(uuids));
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        MinecraftServer server = event.getEntity().getServer();
        if (server == null) return;

        List<UUID> uuids = server.getPlayerList().getPlayers().stream().map(ServerPlayer::getUUID).toList();
        ClaimAnchorNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new SyncOnlinePlayersPacket(uuids));
    }
}
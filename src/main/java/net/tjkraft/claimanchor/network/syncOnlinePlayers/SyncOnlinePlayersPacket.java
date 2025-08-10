package net.tjkraft.claimanchor.network.syncOnlinePlayers;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.claimanchor.menu.custom.trusted.ClaimAnchorTrustedScreen;
import java.util.function.Supplier;

import java.util.List;
import java.util.UUID;

public class SyncOnlinePlayersPacket {
    private final List<UUID> online;

    public SyncOnlinePlayersPacket(List<UUID> online) {
        this.online = online;
    }

    public static void encode(SyncOnlinePlayersPacket msg, FriendlyByteBuf buf) {
        buf.writeCollection(msg.online, FriendlyByteBuf::writeUUID);
    }

    public static SyncOnlinePlayersPacket decode(FriendlyByteBuf buf) {
        return new SyncOnlinePlayersPacket(buf.readList(FriendlyByteBuf::readUUID));
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ClaimAnchorTrustedScreen screen) {
                screen.updateOnlinePlayers(online);
            }
        });
        ctx.get().setPacketHandled(true);
    }

}

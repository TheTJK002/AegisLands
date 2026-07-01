package net.tjkraft.aegislands.network;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.aegislands.menu.claimAnchor.ClaimAnchorScreen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class ClientboundSyncClaimListPacket {
    private final List<String> list;
    private final int type;

    public ClientboundSyncClaimListPacket(List<String> list, int type) {
        this.list = list;
        this.type = type;
    }

    public static void encode(ClientboundSyncClaimListPacket msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.type);
        buf.writeInt(msg.list.size());
        for (String s : msg.list) buf.writeUtf(s);
    }

    public static ClientboundSyncClaimListPacket decode(FriendlyByteBuf buf) {
        int type = buf.readInt();
        int size = buf.readInt();
        List<String> list = new ArrayList<>();
        for (int i = 0; i < size; i++) list.add(buf.readUtf());
        return new ClientboundSyncClaimListPacket(list, type);
    }

    public static void handle(ClientboundSyncClaimListPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            if (Minecraft.getInstance().screen instanceof ClaimAnchorScreen screen) {
                screen.updateClientList(msg.list, msg.type);
            }
        });
        ctx.setPacketHandled(true);
    }
}
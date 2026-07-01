package net.tjkraft.aegislands.network;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.aegislands.world.ClaimManager;

import java.util.List;
import java.util.function.Supplier;

public class ServerboundUpdateClaimPacket {
    private final BlockPos pos;
    private final String entryName;
    private final int type;
    private final boolean isRemoving;

    public ServerboundUpdateClaimPacket(BlockPos pos, String entryName, int type, boolean isRemoving) {
        this.pos = pos;
        this.entryName = entryName;
        this.type = type;
        this.isRemoving = isRemoving;
    }

    public static void encode(ServerboundUpdateClaimPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.entryName);
        buf.writeInt(msg.type);
        buf.writeBoolean(msg.isRemoving);
    }

    public static ServerboundUpdateClaimPacket decode(FriendlyByteBuf buf) {
        return new ServerboundUpdateClaimPacket(buf.readBlockPos(), buf.readUtf(), buf.readInt(), buf.readBoolean());
    }

    public static void handle(ServerboundUpdateClaimPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            ChunkPos chunkPos = new ChunkPos(msg.pos);
            ClaimManager manager = ClaimManager.get(level);

            if (!manager.isChunkClaimed(chunkPos) || !manager.getOwner(chunkPos).equals(player.getUUID())) {
                return;
            }

            if (msg.isRemoving) {
                manager.removeClaimEntry(chunkPos, msg.entryName, msg.type);
            } else {
                manager.addClaimEntry(chunkPos, msg.entryName, msg.type);
            }

            List<String> updatedList = manager.getClaimEntries(chunkPos, msg.type);

            ALMessages.sendToPlayer(new ClientboundSyncClaimListPacket(updatedList, msg.type), player);
        });
        ctx.setPacketHandled(true);
    }
}
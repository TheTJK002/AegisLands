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

public class ServerboundRequestClaimListPacket {
    private final BlockPos pos;
    private final int type;

    public ServerboundRequestClaimListPacket(BlockPos pos, int type) {
        this.pos = pos;
        this.type = type;
    }

    public static void encode(ServerboundRequestClaimListPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeInt(msg.type);
    }

    public static ServerboundRequestClaimListPacket decode(FriendlyByteBuf buf) {
        return new ServerboundRequestClaimListPacket(buf.readBlockPos(), buf.readInt());
    }

    public static void handle(ServerboundRequestClaimListPacket msg, Supplier<NetworkEvent.Context> ctxSupplier) {
        NetworkEvent.Context ctx = ctxSupplier.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            ChunkPos chunkPos = new ChunkPos(msg.pos);
            ClaimManager manager = ClaimManager.get(level);

            List<String> currentEntries = manager.getClaimEntries(chunkPos, msg.type);
            ALMessages.sendToPlayer(new ClientboundSyncClaimListPacket(currentEntries, msg.type), player);
        });
        ctx.setPacketHandled(true);
    }
}
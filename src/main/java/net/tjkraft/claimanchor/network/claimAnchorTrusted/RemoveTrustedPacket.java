package net.tjkraft.claimanchor.network.claimAnchorTrusted;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

public class RemoveTrustedPacket {
    private final BlockPos pos;
    private final UUID target;

    public RemoveTrustedPacket(BlockPos pos, UUID target) {
        this.pos = pos;
        this.target = target;
    }

    public static void encode(RemoveTrustedPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUUID(msg.target);
    }

    public static RemoveTrustedPacket decode(FriendlyByteBuf buf) {
        return new RemoveTrustedPacket(buf.readBlockPos(), buf.readUUID());
    }

    public static void handle(RemoveTrustedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Level level = player.level();
            BlockEntity be = level.getBlockEntity(msg.pos);
            if (be instanceof ClaimAnchorBlockEntity anchor && anchor.getOwner().equals(player.getUUID())) {
                anchor.removeTrusted(msg.target);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
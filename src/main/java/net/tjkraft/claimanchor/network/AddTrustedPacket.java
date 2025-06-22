package net.tjkraft.claimanchor.network;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;

import java.util.UUID;

public class AddTrustedPacket {
    private final BlockPos pos;
    private final UUID target;

    public AddTrustedPacket(BlockPos pos, UUID target) {
        this.pos = pos;
        this.target = target;
    }

    public static void encode(AddTrustedPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUUID(msg.target);
    }

    public static AddTrustedPacket decode(FriendlyByteBuf buf) {
        return new AddTrustedPacket(buf.readBlockPos(), buf.readUUID());
    }

    public static void handle(AddTrustedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            Level level = player.level();
            BlockEntity be = level.getBlockEntity(msg.pos);
            if (be instanceof ClaimAnchorBlockEntity anchor && anchor.getOwner().equals(player.getUUID())) {
                anchor.addTrusted(msg.target);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
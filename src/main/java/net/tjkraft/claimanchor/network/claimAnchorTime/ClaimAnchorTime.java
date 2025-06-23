package net.tjkraft.claimanchor.network.claimAnchorTime;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;

import java.util.UUID;
import java.util.function.Supplier;

public class ClaimAnchorTime {
    private final UUID owner;
    private final BlockPos pos;

    public ClaimAnchorTime(UUID owner, BlockPos pos) {
        this.owner = owner;
        this.pos = pos;
    }

    public static void encode(ClaimAnchorTime msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.owner);
        buf.writeBlockPos(msg.pos);
    }

    public static ClaimAnchorTime decode(FriendlyByteBuf buf) {
        return new ClaimAnchorTime(buf.readUUID(), buf.readBlockPos());
    }

    public static void handle(ClaimAnchorTime msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer sender = ctx.get().getSender();
            if (!(sender.level() instanceof ServerLevel serverLevel)) return;

            BlockEntity be = serverLevel.getBlockEntity(msg.pos);
            if (!(be instanceof ClaimAnchorBlockEntity newAnchor)) return;
            if (!newAnchor.getOwner().equals(msg.owner)) return;

            int radius = 16;
            int highestTimer = 0;

            BlockPos center = msg.pos;
            BlockPos start = center.offset(-radius, -radius, -radius);
            BlockPos end = center.offset(radius, radius, radius);

            for (BlockPos checkPos : BlockPos.betweenClosed(start, end)) {
                if (checkPos.equals(msg.pos)) continue;

                BlockEntity other = serverLevel.getBlockEntity(checkPos);
                if (other instanceof ClaimAnchorBlockEntity anchor
                        && anchor.claimIsActive()
                        && anchor.getOwner().equals(msg.owner)) {
                    highestTimer = Math.max(highestTimer, anchor.getClaimTime());
                }
            }

            newAnchor.setTimerTicks(highestTimer);
        });
        ctx.get().setPacketHandled(true);
    }

}

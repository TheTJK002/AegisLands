package net.tjkraft.claimanchor.network.claimAnchorTrusted;

import java.util.ArrayList;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.syncPlayers.SyncTrustedPacket;

import java.util.UUID;

import static net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity.buildTrustedMap;

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
            if (player == null) return;
            var level = player.level();
            var be = level.getBlockEntity(msg.pos);
            if (!(be instanceof ClaimAnchorBlockEntity anchor)) return;

            anchor.addTrusted(msg.target);

            Map<UUID,String> trustedMap = buildTrustedMap(anchor, player.getServer());
            ClaimAnchorNetwork.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncTrustedPacket(msg.pos, trustedMap)
            );
            anchor.setChanged();
        });
        ctx.get().setPacketHandled(true);
    }
}
package net.tjkraft.aegislands.network.trusted;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.network.AegisLandsNetwork;
import net.tjkraft.aegislands.network.syncPlayers.SyncTrustedPacket;

import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE.buildTrustedMap;

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
            if (player == null) return;
            var level = player.level();
            var be = level.getBlockEntity(msg.pos);
            if (!(be instanceof ClaimAnchorBE anchor)) return;

            anchor.removeTrusted(msg.target);

            Map<UUID,String> trustedMap = buildTrustedMap(anchor, player.getServer());
            AegisLandsNetwork.INSTANCE.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new SyncTrustedPacket(msg.pos, trustedMap)
            );
            anchor.setChanged();
        });
        ctx.get().setPacketHandled(true);
    }
}
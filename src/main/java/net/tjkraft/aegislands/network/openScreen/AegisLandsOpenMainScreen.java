package net.tjkraft.aegislands.network.openScreen;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkHooks;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;

import java.util.function.Supplier;

public class AegisLandsOpenMainScreen {
    private final BlockPos pos;

    public AegisLandsOpenMainScreen(BlockPos pos) {
        this.pos = pos;
    }

    public static void encode(AegisLandsOpenMainScreen msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
    }

    public static AegisLandsOpenMainScreen decode(FriendlyByteBuf buf) {
        return new AegisLandsOpenMainScreen(buf.readBlockPos());
    }

    public static void handle(AegisLandsOpenMainScreen msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player == null) return;

            ServerLevel level = player.serverLevel();
            if (level.getBlockEntity(msg.pos) instanceof ClaimAnchorBE be) {
                String ownerName = be.getOwnerName();
                if (ownerName == null) ownerName = "Unknown";

                String finalOwnerName = ownerName;
                NetworkHooks.openScreen(player, be, (buf) -> {
                    buf.writeBlockPos(msg.pos);
                    buf.writeUtf(finalOwnerName);
                });
            }
        });
        ctx.get().setPacketHandled(true);
    }

}

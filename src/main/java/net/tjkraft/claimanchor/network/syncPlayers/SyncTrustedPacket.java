package net.tjkraft.claimanchor.network.syncPlayers;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.menu.custom.trusted.ClaimAnchorTrustedAddScreen;
import net.tjkraft.claimanchor.menu.custom.trusted.ClaimAnchorTrustedRemoveScreen;

import java.util.*;
import java.util.function.Supplier;

public class SyncTrustedPacket {
    private final BlockPos pos;
    private final Map<UUID, String> trustedMap;

    public SyncTrustedPacket(BlockPos pos, Map<UUID, String> trustedMap) {
        this.pos = pos;
        this.trustedMap = trustedMap;
    }

    public static void encode(SyncTrustedPacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeVarInt(msg.trustedMap.size());
        for (Map.Entry<UUID,String> e : msg.trustedMap.entrySet()) {
            buf.writeUUID(e.getKey());
            buf.writeUtf(e.getValue());
        }
    }

    public static SyncTrustedPacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readVarInt();
        Map<UUID,String> map = new LinkedHashMap<>();
        for (int i=0; i<size; i++) {
            UUID id = buf.readUUID();
            String name = buf.readUtf();
            map.put(id, name);
        }
        return new SyncTrustedPacket(pos, map);
    }

    public static void handle(SyncTrustedPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ClientLevel level = Minecraft.getInstance().level;
            if (level == null) return;
            if (level.getBlockEntity(msg.pos) instanceof ClaimAnchorBlockEntity anchor) {
                anchor.getTrusted().clear();
                anchor.getTrusted().addAll(msg.trustedMap.keySet());
                anchor.getTrustedNames().clear();
                anchor.getTrustedNames().putAll(msg.trustedMap);

                if (Minecraft.getInstance().screen instanceof ClaimAnchorTrustedAddScreen s
                        && s.getAnchor().getBlockPos().equals(msg.pos)) {
                    s.updateOnlinePlayers(Minecraft.getInstance().getConnection()
                            .getOnlinePlayers().stream().map(p->p.getProfile().getId()).toList());
                }
                if (Minecraft.getInstance().screen instanceof ClaimAnchorTrustedRemoveScreen s2
                        && s2.getAnchor().getBlockPos().equals(msg.pos)) {
                    s2.updateTrustedListFromServer();
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}

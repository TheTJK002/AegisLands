package net.tjkraft.aegislands.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlockEntity;

public class AegisLensItem extends Item {

    public AegisLensItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();

        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AegisAnchorBlockEntity claim && claim.getRemainingTicks() > 0) {
                showClaimParticles((ServerLevel) level, claim.getBlockPos());
                player.displayClientMessage(Component.literal("Visualizzazione area attiva per 5 secondi"), true);
                return InteractionResult.SUCCESS;
            }
        }

        return InteractionResult.PASS;
    }

    private void showClaimParticles(ServerLevel level, BlockPos center) {
        ChunkPos chunk = new ChunkPos(center);
        int minY = center.getY() - 16;
        int maxY = center.getY() + 16;

        for (int y = minY; y <= maxY; y += 4) {
            for (int x = 0; x <= 15; x += 4) {
                for (int z = 0; z <= 15; z += 4) {
                    boolean edgeX = x == 0 || x == 15;
                    boolean edgeZ = z == 0 || z == 15;

                    if (edgeX || edgeZ) {
                        BlockPos p = new BlockPos(chunk.getMinBlockX() + x, y, chunk.getMinBlockZ() + z);
                        level.sendParticles(ParticleTypes.END_ROD,
                                p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5,
                                3, 0.1, 0.1, 0.1, 0.0);
                    }
                }
            }
        }
    }
}
package net.tjkraft.claimanchor.item.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.config.CAServerConfig;

public class ClaimMonocle extends Item {
    public ClaimMonocle(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        BlockPos playerPos = player.blockPosition();
        int chunkX = playerPos.getX() >> 4;
        int chunkZ = playerPos.getZ() >> 4;

        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight(); y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos check = new BlockPos((chunkX << 4) + x, y, (chunkZ << 4) + z);
                    if (level.getBlockEntity(check) instanceof ClaimAnchorBlockEntity anchor) {

                        if (anchor.getClaimTime() == 0) {
                            player.displayClientMessage(Component.translatable("msg.claim_anchor.no_time"), true);
                        } else {
                            String timeStr = formatTicksAsTime(anchor.getClaimTime());
                            player.displayClientMessage(Component.literal(timeStr), true);

                            if (level instanceof ServerLevel serverLevel) {
                                showClaimBox(serverLevel, anchor.getBlockPos());
                            }
                        }
                    }
                }
            }
        }
        return InteractionResultHolder.success(player.getItemInHand(hand));
    }

    private void showClaimBox(ServerLevel level, BlockPos anchorPos) {
        int baseX = (anchorPos.getX() >> 4) << 4;
        int baseZ = (anchorPos.getZ() >> 4) << 4;
        int minY = anchorPos.getY() - CAServerConfig.MIN_ANCHOR_Y.get();
        int maxY = anchorPos.getY() + CAServerConfig.MAX_ANCHOR_Y.get();

        for (int x = 0; x <= 15; x++) {
            for (int z = 0; z <= 15; z++) {
                for (int y = minY; y <= maxY; y++) {
                    boolean onXEdge = (x == 0 || x == 15);
                    boolean onZEdge = (z == 0 || z == 15);
                    boolean onYEdge = (y == minY || y == maxY);

                    if ((onXEdge && onZEdge) || (onXEdge && onYEdge) || (onZEdge && onYEdge)) {
                        double px = baseX + x + 0.5;
                        double py = y + 0.5;
                        double pz = baseZ + z + 0.5;

                        level.sendParticles(ParticleTypes.END_ROD, px, py, pz, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    public static String formatTicksAsTime(int ticks) {
        long totalSeconds = ticks / 20L;

        long years = totalSeconds / (60 * 60 * 24 * 365);
        totalSeconds %= (60 * 60 * 24 * 365);

        long months = totalSeconds / (60 * 60 * 24 * 30);
        totalSeconds %= (60 * 60 * 24 * 30);

        long days = totalSeconds / (60 * 60 * 24);
        totalSeconds %= (60 * 60 * 24);

        long hours = totalSeconds / (60 * 60);
        totalSeconds %= (60 * 60);

        long minutes = totalSeconds / 60;
        long seconds = totalSeconds % 60;

        return String.format("Claim Time: %02dy : %02dmo : %02dd : %02dh : %02dm : %02ds", years, months, days, hours, minutes, seconds);
    }

}

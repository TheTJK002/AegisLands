package net.tjkraft.claimanchor.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.tjkraft.claimanchor.block.blockEntity.CABlockEntity;
import net.tjkraft.claimanchor.block.blockEntity.custom.AnchorTracker;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.config.CAServerConfig;
import net.tjkraft.claimanchor.menu.custom.main.ClaimAnchorMainMenu;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.claimAnchorTime.ClaimAnchorTime;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimAnchorBlock extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public ClaimAnchorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof ClaimAnchorBlockEntity claimAnchorBlock) {
                UUID owner = claimAnchorBlock.getOwner();
                if (owner != null && pPlayer.getUUID().equals(owner)) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, new SimpleMenuProvider((id, inv, p) -> new ClaimAnchorMainMenu(id, inv, claimAnchorBlock, claimAnchorBlock.getOwnerName()), Component.literal("Claim Anchor")), buf -> {
                        buf.writeBlockPos(pPos);
                        buf.writeUtf(claimAnchorBlock.getOwnerName() != null ? claimAnchorBlock.getOwnerName() : "Unknown");
                    });
                }
            } else {
                throw new IllegalStateException("Our Container provider is missing!");
            }
        }
        return InteractionResult.sidedSuccess(pLevel.isClientSide());
    }


    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide && placer instanceof Player player) {

            UUID uuid = player.getUUID();
            int count = AnchorTracker.getAnchors(uuid);
            int limit = CAServerConfig.LIMIT_CLAIM_ANCHOR_PER_PLAYER.get();

            if (count >= limit) {
                player.displayClientMessage(Component.translatable("msg.claim_anchor.reach_limited", limit), true);
                level.destroyBlock(pos, true);
                return;
            }

            int minChunkDistance = CAServerConfig.MIN_CLAIM_CHUNK_DISTANCE.get();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            if (level instanceof ServerLevel serverLevel) {
                for (int dx = -minChunkDistance; dx <= minChunkDistance; dx++) {
                    for (int dz = -minChunkDistance; dz <= minChunkDistance; dz++) {
                        int cx = chunkX + dx;
                        int cz = chunkZ + dz;

                        LevelChunk chunk = serverLevel.getChunk(cx, cz);
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            if (be instanceof ClaimAnchorBlockEntity anchor) {
                                UUID owner = anchor.getOwner();
                                if (owner != null && !owner.equals(uuid)) {
                                    player.displayClientMessage(Component.translatable("msg.claim_anchor.close_claim", minChunkDistance), true);
                                    level.destroyBlock(pos, true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBlockEntity claimAnchor) {
                claimAnchor.setOwner(uuid);
                AnchorTracker.increment(uuid);
                ClaimAnchorNetwork.INSTANCE.sendToServer(new ClaimAnchorTime(uuid, pos));
            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBlockEntity anchor) {
                UUID owner = anchor.getOwner();
                if (owner != null && !owner.equals(player.getUUID()) && anchor.getClaimTime() > 0) {
                    player.displayClientMessage(Component.translatable("msg.claim_anchor.owner_break"), true);
                    return false;
                }
            }
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }


    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBlockEntity claimAnchor) {
                AnchorTracker.decrement(claimAnchor.getOwner());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ClaimAnchorBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, CABlockEntity.CLAIM_ANCHOR_BE.get(), (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}

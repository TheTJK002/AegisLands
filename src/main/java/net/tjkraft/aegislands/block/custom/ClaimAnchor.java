package net.tjkraft.aegislands.block.custom;

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
import net.minecraftforge.network.PacketDistributor;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.block.blockEntity.custom.AnchorTracker;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.menu.custom.main.AegisLandsMainMenu;
import net.tjkraft.aegislands.network.AegisLandsNetwork;
import net.tjkraft.aegislands.network.time.AegisLandsTime;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimAnchor extends BaseEntityBlock {
    public static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public ClaimAnchor(Properties pProperties) {
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
            if (entity instanceof ClaimAnchorBE claimAnchorBlock) {
                UUID owner = claimAnchorBlock.getOwner();
                if (owner != null && pPlayer.getUUID().equals(owner)) {
                    NetworkHooks.openScreen((ServerPlayer) pPlayer, new SimpleMenuProvider((id, inv, p) -> new AegisLandsMainMenu(id, inv, claimAnchorBlock, claimAnchorBlock.getOwnerName()), Component.literal("Claim Anchor")), buf -> {
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
            int limit = ALServerConfig.LIMIT_CLAIM_ANCHOR_PER_PLAYER.get();

            if (count >= limit) {
                player.displayClientMessage(Component.translatable("msg.aegis_lands.reach_limited", limit), true);
                level.destroyBlock(pos, true);
                return;
            }

            int minChunkDistance = ALServerConfig.MIN_CLAIM_CHUNK_DISTANCE.get();
            int chunkX = pos.getX() >> 4;
            int chunkZ = pos.getZ() >> 4;

            if (level instanceof ServerLevel serverLevel) {
                for (int dx = -minChunkDistance; dx <= minChunkDistance; dx++) {
                    for (int dz = -minChunkDistance; dz <= minChunkDistance; dz++) {
                        int cx = chunkX + dx;
                        int cz = chunkZ + dz;

                        LevelChunk chunk = serverLevel.getChunk(cx, cz);
                        for (BlockEntity be : chunk.getBlockEntities().values()) {
                            if (be instanceof ClaimAnchorBE anchor) {
                                UUID owner = anchor.getOwner();
                                if (owner != null && !owner.equals(uuid)) {
                                    player.displayClientMessage(Component.translatable("msg.aegis_lands.close_claim", minChunkDistance), true);
                                    level.destroyBlock(pos, true);
                                    return;
                                }
                            }
                        }
                    }
                }
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBE claimAnchor) {
                claimAnchor.setOwner(uuid);
                AnchorTracker.increment(uuid);
                AegisLandsNetwork.INSTANCE.send(PacketDistributor.ALL.noArg(), new AegisLandsTime(uuid, pos));

            }
        }
    }

    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest, FluidState fluid) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBE anchor) {
                UUID owner = anchor.getOwner();
                if (owner != null && !owner.equals(player.getUUID()) && anchor.getClaimTime() > 0) {
                    player.displayClientMessage(Component.translatable("msg.aegis_lands.owner_break"), true);
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
            if (be instanceof ClaimAnchorBE claimAnchor) {
                AnchorTracker.decrement(claimAnchor.getOwner());
                claimAnchor.drops();
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ClaimAnchorBE(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ALBlockEntity.CLAIM_ANCHOR_BE.get(), (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}

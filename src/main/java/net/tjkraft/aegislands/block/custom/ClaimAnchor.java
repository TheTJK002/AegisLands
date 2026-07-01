package net.tjkraft.aegislands.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import net.tjkraft.aegislands.blockTile.ALBlockTiles;
import net.tjkraft.aegislands.blockTile.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.world.ClaimManager;
import org.jetbrains.annotations.Nullable;

public class ClaimAnchor extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(4, 0, 4, 12, 8, 12);

    public ClaimAnchor() {
        super(BlockBehaviour.Properties.of().destroyTime(1.2f).requiresCorrectToolForDrops());
    }

    @Override
    public VoxelShape getShape(BlockState pState, BlockGetter pLevel, BlockPos pPos, CollisionContext pContext) {
        return SHAPE;
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    //

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && placer instanceof Player player) {
            ChunkPos chunkPos = new ChunkPos(pos);
            ClaimManager manager = ClaimManager.get(level);

            if (manager.isChunkClaimed(chunkPos)) {
                player.sendSystemMessage(Component.literal("§cQuesto chunk è già protetto!"));
                level.destroyBlock(pos, true); // Rompe il blocco e glielo ridà
                return;
            }

            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBE anchorBe) {
                anchorBe.setOwner(player.getUUID());
                manager.claimChunk(chunkPos, player.getUUID());
                player.sendSystemMessage(Component.literal("§aArea Protetta! Hai 2 minuti (Gratis) per alimentarla nello slot."));
            }
        }
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBE anchorBe) {
                if (anchorBe.getOwner() != null && anchorBe.getOwner().equals(player.getUUID())) {
                    NetworkHooks.openScreen((ServerPlayer) player, anchorBe, pos);
                } else {
                    player.sendSystemMessage(Component.literal("§cNon sei il proprietario di questa Ancora!"));
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof ClaimAnchorBE anchorBe) {
                if (!level.isClientSide()) {
                    ClaimManager.get(level).unclaimChunk(new ChunkPos(pos));
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    //

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new ClaimAnchorBE(pPos, pState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ALBlockTiles.CLAIM_ANCHOR_BE.get(), ClaimAnchorBE::serverTick);
    }
}

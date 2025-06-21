package net.tjkraft.aegislands.block.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.block.blockEntity.custom.AegisAnchorBlockEntity;
import net.tjkraft.aegislands.block.blockEntity.custom.AnchorTracker;
import net.tjkraft.aegislands.config.ALServerConfig;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AegisAnchorBlock extends BaseEntityBlock {
    public AegisAnchorBlock(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    public InteractionResult use(BlockState pState, Level pLevel, BlockPos pPos, Player pPlayer, InteractionHand pHand, BlockHitResult pHit) {
        if (!pLevel.isClientSide()) {
            BlockEntity entity = pLevel.getBlockEntity(pPos);
            if (entity instanceof AegisAnchorBlockEntity) {
                NetworkHooks.openScreen((ServerPlayer) pPlayer, (AegisAnchorBlockEntity) entity, pPos);
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
            int limit = ALServerConfig.MAX_ANCHOR_PER_PLAYER.get();

            if (count >= limit) {
                player.sendSystemMessage(Component.literal("Hai già raggiunto il limite di Anchor: " + limit));
                level.destroyBlock(pos, true);
            } else {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof AegisAnchorBlockEntity anchor) {
                    anchor.setOwner(uuid);
                    AnchorTracker.increment(uuid);
                }
            }
        }
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof AegisAnchorBlockEntity anchor) {
                AnchorTracker.decrement(anchor.getOwner());
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new AegisAnchorBlockEntity(pPos, pState);
    }

    @Override
    public @Nullable <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, ALBlockEntity.AEGIS_ANCHOR_BE.get(), (pLevel1, pPos, pState1, pBlockEntity) -> pBlockEntity.tick(pLevel1, pPos, pState1));
    }
}

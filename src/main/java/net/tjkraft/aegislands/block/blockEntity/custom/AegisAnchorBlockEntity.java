package net.tjkraft.aegislands.block.blockEntity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.menu.custom.AegisAnchorMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AegisAnchorBlockEntity extends BlockEntity implements MenuProvider {
    private UUID owner;
    private long expireTime = 0;

    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public AegisAnchorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ALBlockEntity.AEGIS_ANCHOR_BE.get(), pPos, pBlockState);
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public UUID getOwner() {
        setChanged();
        return owner;
    }

    public void extendClaim(int time) {
        expireTime += time * 20;
        setChanged();
    }

    public boolean isClaimActive() {
        return expireTime > 0;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            if (!isClaimActive()) {}

            ItemStack stack = this.itemHandler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                if (ALServerConfig.PAYMENT_CLAIM.get().contains(itemId)) {
                    stack.shrink(1);
                    this.extendClaim(60);
                    this.setChanged();
                }
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putLong("ExpireTime", expireTime);
        tag.put("Inventory", itemHandler.serializeNBT());
        itemHandler.deserializeNBT(tag.getCompound("inventory"));
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
        expireTime = tag.getLong("ExpireTime");
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Aegis Anchor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AegisAnchorMenu(pContainerId, pPlayerInventory, this);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        tag.putLong("ExpireTime", expireTime);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        super.handleUpdateTag(tag);
        this.expireTime = tag.getLong("ExpireTime");
    }
}

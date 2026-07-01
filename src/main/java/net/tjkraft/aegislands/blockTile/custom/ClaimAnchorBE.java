package net.tjkraft.aegislands.blockTile.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.tjkraft.aegislands.blockTile.ALBlockTiles;
import net.tjkraft.aegislands.item.ALItems;
import net.tjkraft.aegislands.menu.claimAnchor.ClaimAnchorMenu;
import net.tjkraft.aegislands.world.ClaimManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ClaimAnchorBE extends BlockEntity implements MenuProvider {
    private UUID ownerUUID;
    private int timeLeft = 2400;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return index == 0 ? timeLeft : 0;
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) timeLeft = value;
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public ClaimAnchorBE(BlockPos pPos, BlockState pBlockState) {
        super(ALBlockTiles.CLAIM_ANCHOR_BE.get(), pPos, pBlockState);
    }

    public void setOwner(UUID uuid) {
        this.ownerUUID = uuid;
        setChanged();
    }

    public UUID getOwner() {
        return this.ownerUUID;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, ClaimAnchorBE be) {
        if (level.isClientSide) return;

        ItemStack itemInSlot = be.itemHandler.getStackInSlot(0);
        if (!itemInSlot.isEmpty()) {
            int timeToAdd = getFuelDuration(itemInSlot.getItem());
            if (timeToAdd > 0) {
                be.timeLeft += timeToAdd;
                itemInSlot.shrink(1);
                be.itemHandler.setStackInSlot(0, itemInSlot);
                be.setChanged();
            }
        }

        boolean hasUpgrade = be.itemHandler.getStackInSlot(1).is(ALItems.CHUNK_LOADER_UPGRADE.get());

        if (be.timeLeft > 0) {
            be.timeLeft--;
        } else {
            ClaimManager.get(level).unclaimChunk(new ChunkPos(pos));
            level.destroyBlock(pos, false);
        }
    }

    private static int getFuelDuration(Item item) {
        if (item == Items.GOLD_INGOT) return 6000;
        if (item == Items.DIAMOND) return 24000;
        if (item == Items.EMERALD) return 12000;
        return 0;
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
    protected void saveAdditional(CompoundTag nbt) {
        nbt.put("Inventory", itemHandler.serializeNBT());
        nbt.putInt("TimeLeft", timeLeft);
        if (ownerUUID != null) {
            nbt.putUUID("Owner", ownerUUID);
        }
        super.saveAdditional(nbt);
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);
        itemHandler.deserializeNBT(nbt.getCompound("Inventory"));
        timeLeft = nbt.getInt("TimeLeft");
        if (nbt.hasUUID("Owner")) {
            ownerUUID = nbt.getUUID("Owner");
        }
    }

    //

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.aegis_lands.claim_anchor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int id, Inventory inventory, Player pPlayer) {
        return new ClaimAnchorMenu(id, inventory, this, this.data);
    }
}

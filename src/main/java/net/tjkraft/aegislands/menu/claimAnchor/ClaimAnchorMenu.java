package net.tjkraft.aegislands.menu.claimAnchor;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.tjkraft.aegislands.blockTile.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.menu.ALMenus;

public class ClaimAnchorMenu extends AbstractContainerMenu {
    private final ClaimAnchorBE blockEntity;
    private final ContainerData data;
    private final BlockPos blockPos;

    public ClaimAnchorMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(1));
    }

    public ClaimAnchorMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ALMenus.CLAIM_ANCHOR_MENU.get(), id);
        checkContainerDataCount(data, 1);
        this.blockEntity = (ClaimAnchorBE) entity;
        this.blockPos = entity.getBlockPos();
        this.data = data;

        addDataSlots(data);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            this.addSlot(new SlotItemHandler(handler, 0, 93, 55));
            this.addSlot(new SlotItemHandler(handler, 1, 192, 15));
        });

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 21 + col * 18, 100 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 21 + col * 18, 158));
        }
    }

    public int getTimeLeft() {
        return this.data.get(0);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (index < 2) {
                if (!this.moveItemStackTo(itemstack1, 2, 38, true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 2, false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.setByPlayer(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, blockEntity.getBlockState().getBlock());
    }

    public BlockPos getBlockPos() {
        return this.blockPos;
    }
}
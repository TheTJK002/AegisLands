package net.tjkraft.aegislands.menu.custom;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.tjkraft.aegislands.block.blockEntity.custom.AegisAnchorBlockEntity;
import net.tjkraft.aegislands.menu.ALMenuTypes;
import org.jetbrains.annotations.Nullable;

public class AegisAnchorMenu extends AbstractContainerMenu {
    public final AegisAnchorBlockEntity blockEntity;
    private final ContainerLevelAccess access;

    public AegisAnchorMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()));
    }

    public AegisAnchorMenu(int id, Inventory inv, BlockEntity entity) {
        super(ALMenuTypes.AEGIS_ANCHOR_MENU.get(), id);
        this.blockEntity = (AegisAnchorBlockEntity) entity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 80, 30));
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            this.addSlot(new Slot(inv, hotbar, 8 + hotbar * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        ItemStack copy = ItemStack.EMPTY;
        Slot slot = this.slots.get(pIndex);
        if (slot != null && slot.hasItem()) {
            ItemStack original = slot.getItem();
            copy = original.copy();

            if (pIndex == 0) {
                if (!this.moveItemStackTo(original, 1, slots.size(), true)) return ItemStack.EMPTY;
            } else {
                if (!this.moveItemStackTo(original, 0, 1, false)) return ItemStack.EMPTY;
            }

            if (original.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return  this.blockEntity != null && this.blockEntity.getOwner() != null &&
                ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()).evaluate((level, pos) ->
                        level.getBlockState(pos).is(blockEntity.getBlockState().getBlock()) &&
                                pPlayer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64, true);
    }
}

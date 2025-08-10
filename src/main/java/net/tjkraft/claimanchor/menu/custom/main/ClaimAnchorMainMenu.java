package net.tjkraft.claimanchor.menu.custom.main;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.menu.CAMenuTypes;
import org.jetbrains.annotations.Nullable;

public class ClaimAnchorMainMenu extends AbstractContainerMenu {
    public final ClaimAnchorBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    public final ContainerData data;
    public final String ownerName;

    public ClaimAnchorMainMenu(int id, Inventory inv, FriendlyByteBuf buf) {
        this(id, inv, inv.player.level().getBlockEntity(buf.readBlockPos()), buf.readUtf());
    }

    public ClaimAnchorMainMenu(int id, Inventory inv, BlockEntity entity, @Nullable String ownerName) {
        super(CAMenuTypes.CLAIM_ANCHOR_MENU.get(), id);
        this.blockEntity = (ClaimAnchorBlockEntity) entity;
        this.access = ContainerLevelAccess.create(entity.getLevel(), entity.getBlockPos());
        this.data = this.blockEntity.data;
        this.ownerName = ownerName;
        this.addDataSlots(this.data);

        this.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(iItemHandler -> {
            this.addSlot(new SlotItemHandler(iItemHandler, 0, 80, 56));
        });

        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 8 + col * 18, 100 + row * 18));
            }
        }
        for (int hotbar = 0; hotbar < 9; hotbar++) {
            this.addSlot(new Slot(inv, hotbar, 8 + hotbar * 18, 158));
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
        return this.blockEntity != null && this.blockEntity.getOwner() != null && ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()).evaluate((level, pos) -> level.getBlockState(pos).is(blockEntity.getBlockState().getBlock()) && pPlayer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64, true);
    }
}

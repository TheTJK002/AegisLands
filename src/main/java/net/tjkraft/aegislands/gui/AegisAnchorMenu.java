package net.tjkraft.aegislands.gui;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import net.tjkraft.aegislands.block.ALBlocks;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlockEntity;

public class AegisAnchorMenu extends AbstractContainerMenu {
    private final AegisAnchorBlockEntity blockEntity;

    public AegisAnchorMenu(int id, Inventory playerInventory, AegisAnchorBlockEntity blockEntity) {
        super(ALGUI.CLAIM_BLOCK_MENU.get(), id);
        this.blockEntity = blockEntity;

        this.addSlot(new SlotItemHandler(blockEntity.getInventory(), 0, 80, 35));

        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(playerInventory, col + row * 9 + 9, 8 + col * 18, 84 + row * 18));
            }
        }

        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(playerInventory, col, 8 + col * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return null;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    public AegisAnchorBlockEntity getBlockEntity() {
        return blockEntity;
    }
}

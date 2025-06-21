package net.tjkraft.aegislands.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlockEntity;

public class AegisAnchorScreen extends AbstractContainerScreen<AegisAnchorMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(AegisLands.MOD_ID, "textures/gui/claim_block.png");

    public AegisAnchorScreen(AegisAnchorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        pGuiGraphics.blit(TEXTURE, leftPos, topPos, 0,0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY) {
        AegisAnchorBlockEntity be = menu.getBlockEntity();
        if (be == null) return;

        String ownerName = be.getOwnerName();
        long remainingTicks = be.getRemainingTicks();
        String timeFormatted = formatTicksToTime(remainingTicks);

        pGuiGraphics.drawString(Minecraft.getInstance().font, "Proprietario: " + ownerName, 8, 6, 0x404040);
        pGuiGraphics.drawString(Minecraft.getInstance().font, "Tempo rimanente: " + timeFormatted, 8, 18, 0x404040);
    }

    private String formatTicksToTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
    }
}

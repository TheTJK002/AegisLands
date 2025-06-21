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
    private static final ResourceLocation TEXTURE = new ResourceLocation("yourmodid", "textures/gui/claim_block.png");

    public AegisAnchorScreen(AegisAnchorMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics poseStack, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, TEXTURE);
        poseStack.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics poseStack, int mouseX, int mouseY, float partialTick) {
        renderBackground(poseStack);
        super.render(poseStack, mouseX, mouseY, partialTick);
        renderTooltip(poseStack, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics poseStack, int mouseX, int mouseY) {
        AegisAnchorBlockEntity blockEntity = menu.getBlockEntity();
        if (blockEntity != null) {
            String owner = blockEntity.getOwnerName();
            long timeLeft = blockEntity.getRemainingTicks();
            poseStack.drawString(font, "Owner: " + owner, 8, 6, 0x404040);
            poseStack.drawString(font, "Time Left: " + formatTime(timeLeft), 8, 18, 0x404040);
        }
    }

    private String formatTime(long ticks) {
        long seconds = ticks / 20;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        seconds %= 60;
        minutes %= 60;
        return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
    }
}

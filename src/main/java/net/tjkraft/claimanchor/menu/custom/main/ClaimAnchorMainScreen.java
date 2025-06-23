package net.tjkraft.claimanchor.menu.custom.main;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.menu.custom.trusted.ClaimAnchorTrustedScreen;

public class ClaimAnchorMainScreen extends AbstractContainerScreen<ClaimAnchorMainMenu> {
    private final ClaimAnchorBlockEntity anchor;
    private final Inventory inventory;


    private static final ResourceLocation TEXTURE = new ResourceLocation(ClaimAnchor.MOD_ID, "textures/gui/claim_anchor_gui.png");

    public ClaimAnchorMainScreen(ClaimAnchorMainMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.anchor = pMenu.blockEntity;
        this.inventory = pPlayerInventory;
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 88;
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.literal("Gestisci Player"), b -> {
            Minecraft.getInstance().setScreen(new ClaimAnchorTrustedScreen(anchor));
        }).pos(leftPos + 30, topPos + 40).size(120, 20).build());
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        String timeStr = formatTicks(menu.data.get(0));
        graphics.drawString(font, timeStr, leftPos + 10, topPos + 24, 0xFFFFFF);
        renderTooltip(graphics, mouseX, mouseY);
    }

    String formatTicks(int ticks) {
        int seconds = ticks / 20;
        int minutes = seconds / 60;
        int hours = minutes / 60;
        int days = hours / 24;
        int years = days / 365;

        days %= 365;
        hours %= 24;
        minutes %= 60;
        seconds %= 60;

        return String.format("%02dy : %02dd : %02dh : %02dm : %02ds", years, days, hours, minutes, seconds);
    }

}

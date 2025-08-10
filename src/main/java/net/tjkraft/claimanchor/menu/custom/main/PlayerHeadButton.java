package net.tjkraft.claimanchor.menu.custom.main;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class PlayerHeadButton extends Button {
    private final ResourceLocation skin;
    private final Component tooltip;

    public PlayerHeadButton(int x, int y, ResourceLocation skin, Component tooltip, OnPress onPress) {
        super(x, y, 16, 16, Component.empty(), onPress, DEFAULT_NARRATION);
        this.skin = skin;
        this.tooltip = tooltip;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);

        RenderSystem.setShaderTexture(0, skin);
        guiGraphics.blit(skin, this.getX() + 4, this.getY() + 4, 8, 8, 8, 8, 64, 64); // base
        guiGraphics.blit(skin, this.getX() + 4, this.getY() + 4, 40, 8, 8, 8, 64, 64); // overlay

        if (this.isHoveredOrFocused()) {
            guiGraphics.renderTooltip(
                    Minecraft.getInstance().font,
                    tooltip,
                    mouseX, mouseY
            );
        }
    }
}

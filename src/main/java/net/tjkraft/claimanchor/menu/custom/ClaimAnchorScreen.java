package net.tjkraft.claimanchor.menu.custom;

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
import net.tjkraft.claimanchor.network.AddTrustedPacket;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.RemoveTrustedPacket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClaimAnchorScreen extends AbstractContainerScreen<ClaimAnchorMenu> {
    private final List<UUID> onlinePlayers;
    private final Map<UUID, String> uuidToName;
    private final ClaimAnchorBlockEntity anchor;
    private int listStartY = 20;


    private static final ResourceLocation TEXTURE = new ResourceLocation(ClaimAnchor.MOD_ID, "textures/gui/claim_anchor_gui.png");

    public ClaimAnchorScreen(ClaimAnchorMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.anchor = pMenu.blockEntity;
        this.imageWidth = 176;
        this.imageHeight = 182;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 88;

        this.onlinePlayers = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                .map(info -> info.getProfile().getId())
                .collect(Collectors.toList());

        this.uuidToName = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                .collect(Collectors.toMap(
                        info -> info.getProfile().getId(),
                        info -> info.getProfile().getName()
                ));
    }

    @Override
    protected void init() {
        super.init();
        int y = listStartY;
        for (UUID uuid : onlinePlayers) {

            Button btnAdd = Button.builder(Component.literal("+"), b -> sendAdd(uuid))
                    .pos(leftPos + 120, topPos + y).size(20, 20)
                    .build();

            Button btnRemove = Button.builder(Component.literal("-"), b -> sendRemove(uuid))
                    .pos(leftPos + 145, topPos + y).size(20, 20)
                    .build();

            addRenderableWidget(btnAdd);
            addRenderableWidget(btnRemove);
            y += 24;
        }
    }

    private void sendAdd(UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new AddTrustedPacket(anchor.getBlockPos(), uuid));
    }

    private void sendRemove(UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new RemoveTrustedPacket(anchor.getBlockPos(), uuid));
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
        int y = 20;
        for (UUID uuid : onlinePlayers) {
            String name = uuidToName.getOrDefault(uuid, "???");
            graphics.drawString(this.font, name, leftPos + 10, topPos + y + 6, 0xFFFFFF);
            y += 24;
        }
        renderTooltip(graphics, mouseX, mouseY);
    }
}

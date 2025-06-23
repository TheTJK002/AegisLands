package net.tjkraft.claimanchor.menu.custom.trusted;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.menu.custom.main.ClaimAnchorMainMenu;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.AddTrustedPacket;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.RemoveTrustedPacket;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ClaimAnchorTrustedScreen extends AbstractContainerScreen<ClaimAnchorMainMenu> {
    private final List<UUID> onlinePlayers;
    private final Map<UUID, String> uuidToName;
    private final ClaimAnchorBlockEntity anchor;

    private static final ResourceLocation TEXTURE = new ResourceLocation(ClaimAnchor.MOD_ID, "textures/gui/claim_anchor_list_player_gui.png");

    public ClaimAnchorTrustedScreen(ClaimAnchorMainMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, Component.literal("Player List"));
        this.imageWidth = 176;
        this.imageHeight = 179;
        this.titleLabelX = imageWidth / 3;
        this.inventoryLabelY = 10000;

        this.anchor = pMenu.blockEntity;
        UUID ownerUUID = anchor.getOwner();
        this.onlinePlayers = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                .map(info -> info.getProfile().getId())
                .filter(uuid -> !uuid.equals(ownerUUID))
                .collect(Collectors.toList());

        this.uuidToName = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                .collect(Collectors.toMap(
                        info -> info.getProfile().getId(),
                        info -> info.getProfile().getName()
                ));
    }

    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int VISIBLE = 7;

    @Override
    protected void init() {
        clearWidgets();
        super.init();
        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
            init();
        }).pos(leftPos + 176, topPos + 76).size(12, 12).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, onlinePlayers.size() - VISIBLE), scrollOffset + 1);
            init();
        }).pos(leftPos + 176, topPos + 91).size(12, 12).build());

        int y = 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);

            Button add = Button.builder(Component.literal("+"), b -> {
                sendAdd(uuid);
            }).pos(leftPos + 120, topPos + y).size(16, 16).build();
            Button rem = Button.builder(Component.literal("-"), b -> {
                sendRemove(uuid);
            }).pos(leftPos + 145, topPos + y).size(16, 16).build();

            addRenderableWidget(add);
            addRenderableWidget(rem);

            y += ENTRY_HEIGHT;
        }
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
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int y = topPos + 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);
            String name = uuidToName.getOrDefault(uuid, "???");
            PlayerInfo info = Minecraft.getInstance().getConnection()
                    .getOnlinePlayers().stream()
                    .filter(pi -> pi.getProfile().getId().equals(uuid))
                    .findFirst().orElse(null);

            if (info != null) {
                ResourceLocation skin = info.getSkinLocation();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, skin);
                pGuiGraphics.blit(skin, leftPos + 10, y + 4, 8, 8, 8, 8, 64, 64);
                pGuiGraphics.blit(skin, leftPos + 10, y + 4, 40, 8, 8, 8, 64, 64);

                pGuiGraphics.drawString(font, name, leftPos + 20, y + 4, 0xFFFFFF);
                y += ENTRY_HEIGHT;
            }
        }
    }

    private void sendAdd(UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new AddTrustedPacket(anchor.getBlockPos(), uuid));
    }

    private void sendRemove(UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new RemoveTrustedPacket(anchor.getBlockPos(), uuid));
    }
}

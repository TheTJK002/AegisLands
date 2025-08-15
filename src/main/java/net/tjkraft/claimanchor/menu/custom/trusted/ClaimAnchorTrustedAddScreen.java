package net.tjkraft.claimanchor.menu.custom.trusted;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.network.ClaimAnchorNetworkWrapper;

import java.util.*;

public class ClaimAnchorTrustedAddScreen extends Screen {
    private final ClaimAnchorBlockEntity anchor;
    private final int imageWidth = 176;
    private final int imageHeight = 179;
    private int leftPos, topPos;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int VISIBLE = 3;

    private static final ResourceLocation TEXTURE = new ResourceLocation(ClaimAnchor.MOD_ID, "textures/gui/claim_anchor_list_player_gui.png");

    private final List<UUID> onlinePlayers = new ArrayList<>();
    private final Map<UUID, String> uuidToName = new HashMap<>();

    public ClaimAnchorTrustedAddScreen(ClaimAnchorBlockEntity anchor) {
        super(Component.empty());
        this.anchor = anchor;
        syncFromConnection();
    }

    public ClaimAnchorBlockEntity getAnchor() {
        return this.anchor;
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        updateOnlinePlayersFromConnection();
    }

    private void syncFromConnection() {
        uuidToName.clear();
        if (Minecraft.getInstance().getConnection() == null) return;
        for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            uuidToName.put(info.getProfile().getId(), info.getProfile().getName());
        }
    }

    private void updateOnlinePlayersFromConnection() {
        onlinePlayers.clear();
        if (Minecraft.getInstance().getConnection() == null) return;
        UUID ownerUUID = this.anchor.getOwner();
        for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            UUID uuid = info.getProfile().getId();
            if ((ownerUUID == null || !uuid.equals(ownerUUID)) && !anchor.getTrusted().contains(uuid)) {
                onlinePlayers.add(uuid);
            }
        }
        scrollOffset = 0;
        refreshVisiblePlayers();
    }

    @Override
    public void renderBackground(GuiGraphics pGuiGraphics) {
        super.renderBackground(pGuiGraphics);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (this.width - imageWidth) / 2;
        int y = (this.height - imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pGuiGraphics);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        int y = topPos + 30;
        if (Minecraft.getInstance().getConnection() == null) return;

        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);
            String name = uuidToName.getOrDefault(uuid, "???");
            PlayerInfo info = Minecraft.getInstance().getConnection().getOnlinePlayers().stream()
                    .filter(pi -> pi.getProfile().getId().equals(uuid)).findFirst().orElse(null);

            if (info != null) {
                ResourceLocation skin = info.getSkinLocation();
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
                RenderSystem.setShaderTexture(0, skin);
                pGuiGraphics.blit(skin, leftPos + 10, y + 4, 8, 8, 8, 8, 64, 64);
                pGuiGraphics.blit(skin, leftPos + 10, y + 4, 40, 8, 8, 8, 64, 64);

                pGuiGraphics.drawString(font, name, leftPos + 28, y + 4, 0xFFFFFF);

                y += ENTRY_HEIGHT;
            }
        }
    }

    public void updateOnlinePlayers(List<UUID> newOnline) {
        UUID ownerUUID = this.anchor.getOwner();
        this.onlinePlayers.clear();
        for (UUID id : newOnline) {
            if ((ownerUUID == null || !id.equals(ownerUUID)) && !anchor.getTrusted().contains(id)) {
                this.onlinePlayers.add(id);
            }
        }

        this.uuidToName.clear();
        if (Minecraft.getInstance().getConnection() != null) {
            for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
                this.uuidToName.put(info.getProfile().getId(), info.getProfile().getName());
            }
        }

        this.scrollOffset = 0;
        refreshVisiblePlayers();
    }

    private void refreshVisiblePlayers() {
        clearWidgets();

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
            refreshVisiblePlayers();
        }).pos(leftPos + imageWidth, topPos + 76).size(12, 12).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, onlinePlayers.size() - VISIBLE), scrollOffset + 1);
            refreshVisiblePlayers();
        }).pos(leftPos + imageWidth, topPos + 91).size(12, 12).build());

        int yOffset = 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);

            Button add = Button.builder(Component.literal("+"), b -> {
                ClaimAnchorNetworkWrapper.sendAddTrusted(anchor.getBlockPos(), uuid);

                onlinePlayers.remove(uuid);
                refreshVisiblePlayers();
            }).pos(leftPos + 150, topPos + yOffset).size(16, 16).build();

            addRenderableWidget(add);
            yOffset += ENTRY_HEIGHT;
        }
    }
}

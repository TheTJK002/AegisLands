package net.tjkraft.claimanchor.menu.custom.trusted;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.RemoveTrustedPacket;

import java.util.*;

public class ClaimAnchorTrustedRemoveScreen extends Screen {
    private final ClaimAnchorBlockEntity anchor;
    private final int imageWidth = 176;
    private final int imageHeight = 179;
    private int leftPos, topPos;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int VISIBLE = 7;

    private static final ResourceLocation TEXTURE =
            new ResourceLocation(ClaimAnchor.MOD_ID, "textures/gui/claim_anchor_list_player_gui.png");

    private final List<UUID> trustedPlayers = new ArrayList<>();
    private final Map<UUID, String> uuidToName = new HashMap<>();

    public ClaimAnchorTrustedRemoveScreen(ClaimAnchorBlockEntity anchor) {
        super(Component.literal("Trusted Players"));
        this.anchor = anchor;
        rebuildTrustedList();
    }

    public ClaimAnchorBlockEntity getAnchor() {
        return anchor;
    }

    private void rebuildTrustedList() {
        trustedPlayers.clear();
        trustedPlayers.addAll(anchor.getTrusted());
        uuidToName.clear();

        for (UUID id : trustedPlayers) {
            uuidToName.put(id, anchor.getTrustedNames()
                    .getOrDefault(id, id.toString().substring(0, 8)));
        }

        scrollOffset = 0;
        refreshVisiblePlayers();
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
        rebuildTrustedList();
    }

    @Override
    public void renderBackground(GuiGraphics g) {
        super.renderBackground(g);
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        g.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partialTicks) {
        renderBackground(g);
        super.render(g, mouseX, mouseY, partialTicks);

        int y = topPos + 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, trustedPlayers.size()); i++) {
            UUID uuid = trustedPlayers.get(i);
            String name = uuidToName.getOrDefault(uuid, uuid.toString().substring(0, 8));
            g.drawString(font, name, leftPos + 20, y + 4, 0xFFFFFF);
            y += ENTRY_HEIGHT;
        }
    }

    private void refreshVisiblePlayers() {
        clearWidgets();

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
            refreshVisiblePlayers();
        }).pos(leftPos + imageWidth, topPos + 76).size(12, 12).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, trustedPlayers.size() - VISIBLE), scrollOffset + 1);
            refreshVisiblePlayers();
        }).pos(leftPos + imageWidth, topPos + 91).size(12, 12).build());

        int y = 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, trustedPlayers.size()); i++) {
            UUID uuid = trustedPlayers.get(i);
            Button remove = Button.builder(Component.literal("-"), b -> {
                ClaimAnchorNetwork.INSTANCE.sendToServer(new RemoveTrustedPacket(anchor.getBlockPos(), uuid));

                trustedPlayers.remove(uuid);
                refreshVisiblePlayers();
            }).pos(leftPos + 150, topPos + y).size(16, 16).build();
            addRenderableWidget(remove);
            y += ENTRY_HEIGHT;
        }
    }

    public void updateTrustedListFromServer() {
        rebuildTrustedList();
    }
}
package net.tjkraft.aegislands.menu.custom.trusted;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.network.AegisLandsNetwork;
import net.tjkraft.aegislands.network.AegisLandsNetworkWrapper;
import net.tjkraft.aegislands.network.openScreen.AegisLandsOpenMainScreen;

import java.io.PrintStream;
import java.util.*;
import java.util.logging.Logger;

public class AegisLandsTrustedAddScreen extends Screen {
    private final ClaimAnchorBE anchor;
    private final int imageWidth = 176;
    private final int imageHeight = 179;
    private int leftPos, topPos;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int VISIBLE = 3;

    private static final ResourceLocation TEXTURE = new ResourceLocation(AegisLands.MOD_ID, "textures/gui/claim_anchor_list_player_gui.png");

    private final List<UUID> onlinePlayers = new ArrayList<>();
    private final Map<UUID, String> uuidToName = new HashMap<>();

    public AegisLandsTrustedAddScreen(ClaimAnchorBE anchor) {
        super(Component.empty());
        this.anchor = anchor;
        syncFromConnection();
    }

    public ClaimAnchorBE getAnchor() {
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
            System.out.printf(uuidToName.put(info.getProfile().getId(), info.getProfile().getName()));
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

        addRenderableWidget(Button.builder(Component.literal("•"), b -> {
            AegisLandsNetwork.INSTANCE.sendToServer(new AegisLandsOpenMainScreen(anchor.getBlockPos()));
        }).tooltip(Tooltip.create(Component.translatable("gui.aegis_lands.go_back"))).pos(leftPos + imageWidth, topPos + 91).size(12, 12).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, onlinePlayers.size() - VISIBLE), scrollOffset + 1);
            refreshVisiblePlayers();
        }).pos(leftPos + imageWidth, topPos + 106).size(12, 12).build());

        int yOffset = 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);

            Button add = Button.builder(Component.literal("+"), b -> {
                AegisLandsNetworkWrapper.sendAddTrusted(anchor.getBlockPos(), uuid);
                onlinePlayers.remove(uuid);
                refreshVisiblePlayers();
            }).pos(leftPos + 150, topPos + yOffset).size(16, 16).build();

            addRenderableWidget(add);
            yOffset += ENTRY_HEIGHT;
        }
    }

    public static void tickUpdate(AegisLandsTrustedAddScreen screen) {
        if (Minecraft.getInstance().getConnection() == null) return;

        UUID ownerUUID = screen.getAnchor().getOwner();
        Set<UUID> trusted = new HashSet<>(screen.getAnchor().getTrusted());
        List<UUID> current = new ArrayList<>();

        for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            UUID uuid = info.getProfile().getId();
            if ((ownerUUID == null || !uuid.equals(ownerUUID)) && !trusted.contains(uuid)) {
                current.add(uuid);
            }
        }

        if (!new HashSet<>(current).equals(new HashSet<>(screen.onlinePlayers))) {
            screen.updateOnlinePlayers(current);
        }
    }

}

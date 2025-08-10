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
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.AddTrustedPacket;
import net.tjkraft.claimanchor.network.claimAnchorTrusted.RemoveTrustedPacket;

import java.util.*;

public class ClaimAnchorTrustedScreen extends Screen {
    private final ClaimAnchorBlockEntity anchor;
    private final int imageWidth = 176;
    private final int imageHeight = 179;
    private int leftPos, topPos;
    private int scrollOffset = 0;
    private static final int ENTRY_HEIGHT = 20;
    private static final int VISIBLE = 7;
    private int tickCounter = 0;

    private static final ResourceLocation TEXTURE = new ResourceLocation(ClaimAnchor.MOD_ID, "textures/gui/claim_anchor_list_player_gui.png");

    private final List<UUID> onlinePlayers = new ArrayList<>();
    private final Map<UUID, String> uuidToName = new HashMap<>();

    public ClaimAnchorTrustedScreen(ClaimAnchorBlockEntity anchor) {
        super(Component.empty());
        this.anchor = anchor;
        syncFromConnection();
    }

    @Override
    protected void init() {
        this.leftPos = (this.width - imageWidth) / 2;
        this.topPos = (this.height - imageHeight) / 2;
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
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);
            String name = uuidToName.getOrDefault(uuid, "???");
            PlayerInfo info = Minecraft.getInstance().getConnection().getOnlinePlayers().stream().filter(pi -> pi.getProfile().getId().equals(uuid)).findFirst().orElse(null);

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

    private void syncFromConnection() {
        UUID ownerUUID = this.anchor.getOwner();
        this.onlinePlayers.clear();
        this.uuidToName.clear();

        for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            UUID uuid = info.getProfile().getId();
            if (ownerUUID == null || !uuid.equals(ownerUUID)) {
                this.onlinePlayers.add(uuid);
                this.uuidToName.put(uuid, info.getProfile().getName());
            }
        }
    }

    @Override
    public void tick() {
        super.tick();
        tickCounter++;
        if (tickCounter >= 300) {
            tickCounter = 0;
            updateOnlinePlayers(Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map(info -> info.getProfile().getId()).toList());
        }
    }

    private void sendAdd(UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new AddTrustedPacket(anchor.getBlockPos(), uuid));
    }

    private void sendRemove(UUID uuid) {
        ClaimAnchorNetwork.INSTANCE.sendToServer(new RemoveTrustedPacket(anchor.getBlockPos(), uuid));
    }

    public void updateOnlinePlayers(List<UUID> newOnline) {
        UUID ownerUUID = this.anchor.getOwner();
        this.onlinePlayers.clear();
        for (UUID id : newOnline) {
            if (ownerUUID == null || !id.equals(ownerUUID)) {
                this.onlinePlayers.add(id);
            }
        }

        this.uuidToName.clear();
        for (PlayerInfo info : Minecraft.getInstance().getConnection().getOnlinePlayers()) {
            this.uuidToName.put(info.getProfile().getId(), info.getProfile().getName());
        }

        this.scrollOffset = 0;
        refreshVisiblePlayers();
    }

    private void refreshVisiblePlayers() {
        clearWidgets();

        addRenderableWidget(Button.builder(Component.literal("▲"), b -> {
            scrollOffset = Math.max(0, scrollOffset - 1);
            refreshVisiblePlayers();
        }).pos(leftPos + 176, topPos + 76).size(12, 12).build());

        addRenderableWidget(Button.builder(Component.literal("▼"), b -> {
            scrollOffset = Math.min(Math.max(0, onlinePlayers.size() - VISIBLE), scrollOffset + 1);
            refreshVisiblePlayers();
        }).pos(leftPos + 176, topPos + 91).size(12, 12).build());

        int y = 30;
        for (int i = scrollOffset; i < Math.min(scrollOffset + VISIBLE, onlinePlayers.size()); i++) {
            UUID uuid = onlinePlayers.get(i);
            Button add = Button.builder(Component.literal("+"), b -> sendAdd(uuid)).pos(leftPos + 120, topPos + y).size(16, 16).build();
            Button rem = Button.builder(Component.literal("-"), b -> sendRemove(uuid)).pos(leftPos + 145, topPos + y).size(16, 16).build();

            addRenderableWidget(add);
            addRenderableWidget(rem);

            y += ENTRY_HEIGHT;
        }
    }
}

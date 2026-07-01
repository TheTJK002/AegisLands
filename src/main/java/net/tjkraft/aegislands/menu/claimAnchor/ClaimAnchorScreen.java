package net.tjkraft.aegislands.menu.claimAnchor;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.network.ALMessages;
import net.tjkraft.aegislands.network.ServerboundRequestClaimListPacket;
import net.tjkraft.aegislands.network.ServerboundUpdateClaimPacket;

import java.util.ArrayList;
import java.util.List;

public class ClaimAnchorScreen extends AbstractContainerScreen<ClaimAnchorMenu> {
    private static final ResourceLocation MAIN_TEXTURE = new ResourceLocation(AegisLands.MOD_ID, "textures/gui/claim_anchor_gui.png");
    private static final ResourceLocation SUB_TEXTURE = new ResourceLocation(AegisLands.MOD_ID, "textures/gui/claim_anchor_list_gui.png");
    private static final ResourceLocation GUI_BTN = new ResourceLocation(AegisLands.MOD_ID, "textures/gui/claim_anchor_btn.png");

    private int activeTab = -1;
    private EditBox inputField;
    private Button addButton;
    private List<String> currentList = new ArrayList<>();

    public ClaimAnchorScreen(ClaimAnchorMenu menu, Inventory playerInv, Component title) {
        super(menu, playerInv, title);
        this.imageWidth = 200;
        this.imageHeight = 182;
        this.inventoryLabelX = 21;
        this.inventoryLabelY = 88;
        this.titleLabelX = 21;
    }

    private int guiLeft() {
        return (this.width - this.imageWidth) / 2;
    }

    private int guiTop() {
        return (this.height - this.imageHeight) / 2;
    }

    @Override
    protected void init() {
        super.init();

        int x = guiLeft();
        int y = guiTop();

        this.inputField = new EditBox(this.font, x + imageWidth + 12, y + 156, 140, 16, Component.literal(""));
        this.inputField.setMaxLength(32);
        this.inputField.setBordered(true);
        this.inputField.setVisible(false);
        this.addWidget(this.inputField);

        this.addButton = Button.builder(Component.literal("+"), button -> {
            String text = this.inputField.getValue().trim();
            if (!text.isEmpty() && activeTab != -1) {
                ALMessages.sendToServer(new ServerboundUpdateClaimPacket(this.menu.getBlockPos(), text, activeTab, false));
                this.inputField.setValue("");
            }
        }).bounds(x + imageWidth + 156, y + 156, 16, 16).build();

        this.addButton.visible = false;
        this.addRenderableWidget(this.addButton);

        if (activeTab != -1) {
            ALMessages.sendToServer(new ServerboundRequestClaimListPacket(this.menu.getBlockPos(), activeTab));
        }
    }

    public void updateClientList(List<String> newList, int type) {
        if (this.activeTab == type) {
            this.currentList = newList;
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            this.onClose();
            return true;
        }
        if (this.inputField.keyPressed(keyCode, scanCode, modifiers) || this.inputField.canConsumeInput()) {
            return true;
        }
        return true;
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

        int x = guiLeft();
        int y = guiTop();

        RenderSystem.setShaderTexture(0, MAIN_TEXTURE);
        graphics.blit(MAIN_TEXTURE, x, y, 0, 0, this.imageWidth, this.imageHeight);

        if (activeTab != -1) {
            RenderSystem.setShaderTexture(0, SUB_TEXTURE);
            int subWidth = 176;
            int subHeight = 179;
            graphics.blit(SUB_TEXTURE, x + imageWidth + 4, y, 0, 0, subWidth, subHeight);

            this.inputField.visible = true;
            this.addButton.visible = true;

            MutableComponent tabTitle = switch (activeTab) {
                case 0 -> Component.translatable("gui.aegis_lands.players");
                case 1 -> Component.translatable("gui.aegis_lands.entities");
                case 2 -> Component.translatable("gui.aegis_lands.blocks");
                default -> null;
            };
            graphics.drawString(this.font, tabTitle, x + imageWidth + 10, y + 6, 0x404040, false);

            int startY = y + 24;
            for (int i = 0; i < currentList.size(); i++) {
                int rowY = startY + (i * 16);
                if (rowY > y + 150) break;

                String entry = currentList.get(i);
                String displayEntry = entry.length() > 24 ? entry.substring(0, 24) + "..." : entry;
                graphics.drawString(this.font, displayEntry, x + imageWidth + 12, rowY, 0xFFFFFF, true);

                graphics.drawString(this.font, "§c[-]", x + imageWidth + 156, rowY, 0xFFFFFF, false);
            }
        } else {
            this.inputField.visible = false;
            this.addButton.visible = false;
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = guiLeft();
        int y = guiTop();

        if (mouseX >= x - 22 && mouseX < x) {
            int oldTab = activeTab;

            if (mouseY >= y + 28 && mouseY < y + 48) activeTab = 0;
            if (mouseY >= y + 50 && mouseY < y + 70) activeTab = 1;
            if (mouseY >= y + 72 && mouseY < y + 92) activeTab = 2;

            if (oldTab != activeTab) {
                this.currentList.clear();
                this.inputField.setValue("");
                ALMessages.sendToServer(new ServerboundRequestClaimListPacket(this.menu.getBlockPos(), activeTab));
            }
            return true;
        }

        if (activeTab != -1) {
            int startY = y + 24;

            for (int i = 0; i < currentList.size(); i++) {
                int rowY = startY + (i * 16);

                int bx = x + imageWidth + 156;
                int by = rowY;

                if (mouseX >= bx && mouseX <= bx + 16 && mouseY >= by && mouseY <= by + 10) {
                    String entryToRemove = currentList.get(i);
                    ALMessages.sendToServer(new ServerboundUpdateClaimPacket(this.menu.getBlockPos(), entryToRemove, activeTab, true));
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, delta);
        if (activeTab != -1) {
            this.inputField.render(graphics, mouseX, mouseY, delta);
        }
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        if (mouseX >= x - 22 && mouseX < x) {
            if (mouseY >= y + 28 && mouseY < y + 41) {
                graphics.renderTooltip(this.font, Component.translatable("gui.aegis_lands.players"), mouseX, mouseY);
            } else if (mouseY >= y + 43 && mouseY < y + 56) {
                graphics.renderTooltip(this.font, Component.translatable("gui.aegis_lands.entities"), mouseX, mouseY);
            } else if (mouseY >= y + 58 && mouseY < y + 71) {
                graphics.renderTooltip(this.font, Component.translatable("gui.aegis_lands.blocks"), mouseX, mouseY);
            }
        } else {
            renderTooltip(graphics, mouseX, mouseY);
        }
    }
}
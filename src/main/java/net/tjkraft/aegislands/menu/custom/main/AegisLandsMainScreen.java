package net.tjkraft.aegislands.menu.custom.main;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.menu.custom.trusted.AegisLandsTrustedAddScreen;
import net.tjkraft.aegislands.menu.custom.trusted.AegisLandsTrustedRemoveScreen;

import java.util.UUID;

public class AegisLandsMainScreen extends AbstractContainerScreen<AegisLandsMainMenu> {
    private final ClaimAnchorBE anchor;
    private final Inventory inventory;


    private static final ResourceLocation TEXTURE = new ResourceLocation(AegisLands.MOD_ID, "textures/gui/claim_anchor_gui.png");

    public AegisLandsMainScreen(AegisLandsMainMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
        this.anchor = pMenu.blockEntity;
        this.inventory = pPlayerInventory;
        this.imageWidth = 201;
        this.imageHeight = 182;
        this.inventoryLabelX = 8;
        this.inventoryLabelY = 88;
    }

    @Override
    protected void init() {
        super.init();
        Player player = Minecraft.getInstance().player;
        UUID uuid = player.getUUID();

        PlayerInfo info = Minecraft.getInstance().getConnection().getOnlinePlayers().stream().filter(p -> p.getProfile().getId().equals(uuid)).findFirst().orElse(null);

        if (info != null) {
            addRenderableWidget(Button.builder(Component.literal("+"), b -> {
                Minecraft.getInstance().setScreen(new AegisLandsTrustedAddScreen(anchor));
            }).size(16, 16).tooltip(Tooltip.create(Component.translatable("gui.aegis_lands.add_player"))).pos(leftPos + 150, topPos + 8).build());

            addRenderableWidget(Button.builder(Component.literal("-"), b -> {
                Minecraft.getInstance().setScreen(new AegisLandsTrustedRemoveScreen(anchor));
            }).size(16, 16).tooltip(Tooltip.create(Component.translatable("gui.aegis_lands.remove_player"))).pos(leftPos + 150, topPos + 28).build());
        }
    }

    @Override
    protected void renderBg(GuiGraphics pGuiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;
        pGuiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        //Ghost Slot
        String itemIdStr = ALServerConfig.PAYMENT_CLAIM.get().toString();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemIdStr));
        ItemStack ghostStack = item != null ? new ItemStack(item) : ItemStack.EMPTY;

        PoseStack poseStack = pGuiGraphics.pose();
        poseStack.pushPose();
        poseStack.translate(0, 0, 100);
        RenderSystem.enableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 0.3f);

        int slotX = leftPos + 80;
        int slotY = topPos + 56;
        pGuiGraphics.renderItem(ghostStack, slotX, slotY);
        pGuiGraphics.renderItemDecorations(this.font, ghostStack, slotX, slotY);

        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
        poseStack.popPose();

    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
        //Owner Name
        graphics.drawString(font, Component.translatable("gui.aegis_lands.owner", menu.ownerName), leftPos + 8, topPos + 18, 0xFFFFFF);
        renderTooltip(graphics, mouseX, mouseY);
    }

}

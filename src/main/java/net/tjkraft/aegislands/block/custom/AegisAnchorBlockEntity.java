package net.tjkraft.aegislands.block.custom;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.tjkraft.aegislands.block.ALBlockEntities;
import net.tjkraft.aegislands.gui.AegisAnchorMenu;

import java.util.UUID;

public class AegisAnchorBlockEntity extends BlockEntity implements MenuProvider {
    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private UUID owner;
    private long expirationTick;

    public AegisAnchorBlockEntity( BlockPos pPos, BlockState pBlockState) {
        super(ALBlockEntities.AEGIS_ANCHOR_BE.get(), pPos, pBlockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        ItemStack stack = this.inventory.getStackInSlot(0);
        if (stack.is(Items.DIAMOND)) {
            stack.shrink(1);
            int seconds = 30;
            this.extendTime(seconds * 20L);
            this.setChanged();
        }
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public UUID getOwner() {
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
    }

    public void extendTime(long ticks) {
        if (level == null) return;

        long current = level.getGameTime();
        if (expirationTick < current) expirationTick = current;

        expirationTick += ticks;
        setChanged();
    }


    public long getRemainingTicks() {
        long now = level != null ? level.getGameTime() : 0;
        return Math.max(0, expirationTick - now);
    }

    public String getOwnerName() {
        if (owner == null || level == null || level.getServer() == null) return "Unknown";
        MinecraftServer server = level.getServer();
        GameProfile profile = server.getProfileCache().get(owner).orElse(null);
        return profile != null ? profile.getName() : owner.toString();
    }

    public boolean isInsideActiveClaim(BlockPos check) {
        if (getRemainingTicks() <= 0) return false;

        BlockPos center = this.getBlockPos();
        ChunkPos centerChunk = new ChunkPos(center);
        ChunkPos checkChunk = new ChunkPos(check);

        if (!centerChunk.equals(checkChunk)) return false;

        int dy = check.getY() - center.getY();
        return dy >= -16 && dy <= 16;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putLong("ExpirationTick", expirationTick);
        tag.put("Inventory", inventory.serializeNBT());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
        if (tag.contains("ExpirationTick")) {
            expirationTick = tag.getLong("ExpirationTick");
        }
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    public void onLoad() {
        super.onLoad();
        if (level != null && !level.isClientSide) {
            level.scheduleTick(worldPosition, getBlockState().getBlock(), 1);
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Claim Block");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
        return new AegisAnchorMenu(id, inv, this);
    }
}

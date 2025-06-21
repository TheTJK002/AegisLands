package net.tjkraft.aegislands.block.blockEntity.custom;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.config.ALServerConfig;

import java.util.UUID;

public class AegisAnchorBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(1);
    public AegisAnchorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(ALBlockEntity.AEGIS_ANCHOR_BE.get(), pPos, pBlockState);
    }

    private UUID owner;

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    private long expireTime = 0;

    public void extendClaim(int minutes) {
        long now = System.currentTimeMillis();
        if (expireTime < now) expireTime = now;
        expireTime += minutes * 20L;
        setChanged();
    }

    public boolean isClaimActive() {
        return System.currentTimeMillis() < expireTime;
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {
            if (!isClaimActive()) {
            }

            ItemStack stack = inventory.getStackInSlot(0);
            if (!stack.isEmpty()) {
                ResourceLocation id = ForgeRegistries.ITEMS.getKey(stack.getItem());
                if (ALServerConfig.PAYMENT_CLAIM.get().contains(id.toString())) {
                    this.extendClaim(1);
                    stack.shrink(1);
                    setChanged();
                }
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("ExpireTime", expireTime);
        if (owner != null) tag.putUUID("Owner", owner);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        expireTime = tag.getLong("ExpireTime");
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
    }
}

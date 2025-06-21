package net.tjkraft.aegislands.block.custom;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.tjkraft.aegislands.block.ALBlockEntities;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.gui.AegisAnchorMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class AegisAnchorBlockEntity extends BlockEntity implements MenuProvider {
    private UUID owner;
    private long expirationTick = -1;
    private long destructionTick = -1;
    public static final int CLAIM_RADIUS = 8;
    public static final int CLAIM_HEIGHT = 8;
    public final ContainerData data;

    private final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public AegisAnchorBlockEntity(BlockPos pos, BlockState state) {
        super(ALBlockEntities.AEGIS_ANCHOR_BE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return 0;
            }

            @Override
            public void set(int pIndex, int pValue) {

            }

            @Override
            public int getCount() {
                return 0;
            }
        };
    }

    public void setOwner(UUID uuid) {
        this.owner = uuid;
        setChanged();
    }

    public UUID getOwner() {
        return owner;
    }

    public void tick(Level pLevel, BlockPos pPos, BlockState pState) {
        if (this.level == null || this.level.isClientSide) return;

        long currentTime = pLevel.getGameTime();

        if (this.expirationTick > 0 && currentTime >= expirationTick) {
            if (this.destructionTick < 0) {
                this.destructionTick = currentTime + 6000;
                setChanged();
            } else if (currentTime >= this.destructionTick) {
                pLevel.destroyBlock(getBlockPos(), false);
                pLevel.removeBlockEntity(getBlockPos());
            }
        }

        ItemStack stack = inventory.getStackInSlot(0);
        if (!stack.isEmpty() && stack.is(Items.DIRT)) {
            addTime(ALServerConfig.CLAIM_EXTENSION_TIME.get());
            stack.shrink(1);
            setChanged();
        }
    }

    public void addTime(int seconds) {
        if (level instanceof ServerLevel serverLevel) {
            long ticksToAdd = seconds * 20L;
            long currentGameTime = serverLevel.getGameTime();

            if (expirationTick < currentGameTime) {
                expirationTick = currentGameTime + ticksToAdd;
            } else {
                expirationTick += ticksToAdd;
            }
            destructionTick = -1;
            setChanged();
        }
    }

    public void claimArea() {
        if (level == null || level.isClientSide) return;

        BlockPos center = getBlockPos();
        for (int x = -CLAIM_RADIUS; x <= CLAIM_RADIUS; x++) {
            for (int z = -CLAIM_RADIUS; z <= CLAIM_RADIUS; z++) {
                for (int y = -CLAIM_HEIGHT; y <= CLAIM_HEIGHT; y++) {
                    if (Math.abs(x) == CLAIM_RADIUS || Math.abs(z) == CLAIM_RADIUS || Math.abs(y) == CLAIM_HEIGHT) {
                        BlockPos borderPos = center.offset(x, y, z);
                        ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, borderPos.getX() + 0.5, borderPos.getY() + 0.5, borderPos.getZ() + 0.5, 1, 0, 0, 0, 0);
                    }
                }
            }
        }
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap) {
        if(cap == ForgeCapabilities.ITEM_HANDLER) return lazyItemHandler.cast();
        return super.getCapability(cap);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> inventory);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
        expirationTick = tag.getLong("ExpirationTick");
        destructionTick = tag.getLong("DestructionTick");
        inventory.deserializeNBT(tag.getCompound("Inventory"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putLong("ExpirationTick", expirationTick);
        tag.putLong("DestructionTick", destructionTick);
        tag.put("Inventory", inventory.serializeNBT());
        super.saveAdditional(tag);
    }

    public String getOwnerName() {
        if (owner == null || level == null || level.getServer() == null) return "Unknown";
        MinecraftServer server = level.getServer();
        GameProfile profile = server.getProfileCache().get(owner).orElse(null);
        return profile != null ? profile.getName() : owner.toString();
    }

    public long getRemainingTicks() {
        long current = level != null ? level.getGameTime() : 0;
        return Math.max(0, expirationTick - current);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Aegis Anchor");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AegisAnchorMenu(pContainerId, pPlayerInventory, this, this.data);
    }
}

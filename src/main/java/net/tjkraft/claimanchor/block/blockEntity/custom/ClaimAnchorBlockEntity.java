package net.tjkraft.claimanchor.block.blockEntity.custom;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.claimanchor.block.blockEntity.CABlockEntity;
import net.tjkraft.claimanchor.config.CAServerConfig;
import net.tjkraft.claimanchor.menu.custom.main.ClaimAnchorMainMenu;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public class ClaimAnchorBlockEntity extends BlockEntity implements MenuProvider {
    private UUID owner;
    public final ContainerData data;
    public int claimTime = 0;


    private final ItemStackHandler itemHandler = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };
    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ClaimAnchorBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(CABlockEntity.CLAIM_ANCHOR_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> ClaimAnchorBlockEntity.this.claimTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> ClaimAnchorBlockEntity.this.claimTime = pValue;
                }
            }

            @Override
            public int getCount() {
                return 1;
            }
        };
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (!level.isClientSide) {

            ItemStack stack = this.itemHandler.getStackInSlot(0);
            if (!stack.isEmpty()) {
                String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();
                if (CAServerConfig.PAYMENT_CLAIM.get().contains(itemId)) {
                    stack.shrink(1);
                    this.extendTime(CAServerConfig.CLAIM_TIME.get());
                    this.setChanged();
                }
            }

            if (this.claimIsActive()) {
                this.claimTime--;
                if (owner == this.getOwner()) {
                    this.takeClaimTime(this.getClaimTime());
                    setChanged();
                }
                setChanged();
            }
            synchronizeClaimTimers(this.getClaimTime());

            if (this.claimIsActive()) {
                ServerLevel serverLevel = (ServerLevel) level;
                double x = pos.getX() + 0.5 + (level.random.nextDouble() - 0.5);
                double y = pos.getY() + 0.5 + (level.random.nextDouble() - 0.5);
                double z = pos.getZ() + 0.5 + (level.random.nextDouble() - 0.5);
                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y, z, 1, 0, 0, 0, 0);
            }
        }
    }

    public UUID getOwner() {
        setChanged();
        return owner;
    }

    public void setOwner(UUID owner) {
        this.owner = owner;
        setChanged();
    }

    @Nullable
    public String getOwnerName() {
        if (owner == null || level == null || level.isClientSide) return null;

        MinecraftServer server = ((ServerLevel) level).getServer();
        ServerPlayer player = server.getPlayerList().getPlayer(owner);
        if (player != null) {
            return player.getName().getString();
        }

        Optional<GameProfile> profile = server.getProfileCache().get(owner);
        return profile.map(GameProfile::getName).orElse("Unknown");
    }


    public int getClaimTime() {
        setChanged();
        return claimTime;
    }

    private void extendTime(int time) {
        claimTime += time * 20;
        setChanged();
    }

    private void takeClaimTime(int claimTime) {
        this.claimTime = claimTime;
        setChanged();
    }

    public boolean claimIsActive() {
        if (claimTime > 0) {
            setChanged();
            return true;
        }
        return false;
    }

    private final Set<UUID> trustedPlayers = new HashSet<>();

    public void addTrusted(UUID uuid) {
        trustedPlayers.add(uuid);
        setChanged();
    }

    public void removeTrusted(UUID uuid) {
        trustedPlayers.remove(uuid);
        setChanged();
    }

    public boolean hasAccess(UUID uuid) {
        return uuid.equals(owner) || trustedPlayers.contains(uuid);
    }

    private void synchronizeClaimTimers(int newTimer) {
        if (level == null || level.isClientSide) return;
        if (owner == null) return;

        ChunkPos center = new ChunkPos(this.getBlockPos());
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                ChunkPos chunkPos = new ChunkPos(center.x + dx, center.z + dz);
                LevelChunk chunk = ((ServerLevel) level).getChunk(chunkPos.x, chunkPos.z);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    if (be instanceof ClaimAnchorBlockEntity other && owner.equals(other.getOwner()) && other != this) {
                        other.claimTime = newTimer;
                        other.setChanged();
                    }
                }
            }
        }
    }

    public void setTimerTicks(int ticks) {
        this.claimTime = ticks;
        setChanged();
        synchronizeClaimTimers(ticks);
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return lazyItemHandler.cast();
        }
        return super.getCapability(cap, side);
    }

    @Override
    public void onLoad() {
        super.onLoad();
        lazyItemHandler = LazyOptional.of(() -> itemHandler);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        lazyItemHandler.invalidate();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        ListTag list = new ListTag();
        for (UUID uuid : trustedPlayers) {
            list.add(NbtUtils.createUUID(uuid));
        }
        tag.put("Trusted", list);
        tag.put("Inventory", itemHandler.serializeNBT());
        tag.putInt("Claim Time", claimTime);

    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("Owner")) owner = tag.getUUID("Owner");
        trustedPlayers.clear();
        ListTag list = tag.getList("Trusted", Tag.TAG_INT_ARRAY);
        for (Tag t : list) {
            trustedPlayers.add(NbtUtils.loadUUID((IntArrayTag) t));
        }
        itemHandler.deserializeNBT(tag.getCompound("Inventory"));
        claimTime = tag.getInt("Claim Time");
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.claim_anchor.claim_anchor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new ClaimAnchorMainMenu(pContainerId, pPlayerInventory, this, this.getOwnerName());
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag nbt = super.getUpdateTag();
        saveAdditional(nbt);
        return nbt;
    }
}
package net.tjkraft.aegislands.block.blockEntity.custom;

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
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
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
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.item.ALItems;
import net.tjkraft.aegislands.menu.custom.main.AegisLandsMainMenu;
import net.tjkraft.aegislands.network.AegisLandsNetwork;
import net.tjkraft.aegislands.network.syncPlayers.SyncTrustedPacket;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ClaimAnchorBE extends BlockEntity implements MenuProvider {
    private UUID owner;
    public final ContainerData data;
    public int claimTime = 0;
    private final Map<UUID, String> trustedNames = new HashMap<>();

    private final static int SLOT_PAYMENT = 0;
    private final static int SLOT_UPGRADE = 1;
    private boolean chunkForced = false;

    private final ItemStackHandler itemHandler = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, @NotNull ItemStack stack) {
            if (stack.isEmpty()) return false;
            String itemId = ForgeRegistries.ITEMS.getKey(stack.getItem()).toString();

            if (slot == SLOT_PAYMENT) {
                return ALServerConfig.PAYMENT_CLAIM.get().contains(itemId);
            } else if (slot == SLOT_UPGRADE) {
                return stack.is(ALItems.CHUNK_LOADER_UPGRADE.get());
            }
            return false;
        }
    };

    private LazyOptional<IItemHandler> lazyItemHandler = LazyOptional.empty();

    public ClaimAnchorBE(BlockPos pPos, BlockState pBlockState) {
        super(ALBlockEntity.CLAIM_ANCHOR_BE.get(), pPos, pBlockState);
        this.data = new ContainerData() {
            @Override
            public int get(int pIndex) {
                return switch (pIndex) {
                    case 0 -> ClaimAnchorBE.this.claimTime;
                    default -> 0;
                };
            }

            @Override
            public void set(int pIndex, int pValue) {
                switch (pIndex) {
                    case 0 -> ClaimAnchorBE.this.claimTime = pValue;
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
                if (ALServerConfig.PAYMENT_CLAIM.get().contains(itemId)) {
                    stack.shrink(1);
                    this.extendTime(ALServerConfig.CLAIM_TIME.get());
                    this.setChanged();
                }
            }

            if (this.claimIsActive()) {
                checkChunkLoading();
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

    public boolean hasChunkUpgrade() {
        ItemStack upg = this.itemHandler.getStackInSlot(SLOT_UPGRADE);
        return !upg.isEmpty();
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

    public Set<UUID> getTrusted() {
        return trustedPlayers;
    }

    public Map<UUID, String> getTrustedNames() {
        return trustedNames;
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
                    if (be instanceof ClaimAnchorBE other && owner.equals(other.getOwner()) && other != this) {
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
        if (!level.isClientSide) checkChunkLoading();
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (!level.isClientSide && chunkForced) releaseChunk();
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
        return Component.translatable("block.aegis_lands.claim_anchor");
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        if (!this.level.isClientSide && pPlayer instanceof ServerPlayer sp) {
            Map<UUID, String> trustedMap = buildTrustedMap(this, sp.getServer());
            AegisLandsNetwork.INSTANCE.send(PacketDistributor.PLAYER.with(() -> sp), new SyncTrustedPacket(this.getBlockPos(), trustedMap));
        }
        return new AegisLandsMainMenu(pContainerId, pPlayerInventory, this, this.getOwnerName());
    }

    public static Map<UUID, String> buildTrustedMap(ClaimAnchorBE anchor, MinecraftServer server) {
        Map<UUID, String> map = new LinkedHashMap<>();
        for (UUID id : anchor.getTrusted()) {
            String name = server.getProfileCache().get(id).map(GameProfile::getName).orElse(id.toString().substring(0, 8));
            map.put(id, name);
        }
        return map;
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            inventory.setItem(i, itemHandler.getStackInSlot(i));
        }
        Containers.dropContents(this.level, this.worldPosition, inventory);
    }

    // -------- Render logic --------

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

    // -------- Chunk loading logic --------

    private static final net.minecraft.server.level.TicketType<BlockPos> CLAIM_ANCHOR_TICKET =
            net.minecraft.server.level.TicketType.create("claim_anchor", (a, b) -> 0);

    private void checkChunkLoading() {
        if (!(level instanceof ServerLevel server)) return;
        boolean shouldForce = hasChunkUpgrade();
        ChunkPos pos = new ChunkPos(this.worldPosition);

        if (shouldForce && !chunkForced) {
            ForgeChunkManager.forceChunk(server, AegisLands.MOD_ID,
                    this.worldPosition, pos.x, pos.z, true, true);
            chunkForced = true;
        } else if (!shouldForce && chunkForced) {
            ForgeChunkManager.forceChunk(server, AegisLands.MOD_ID,
                    this.worldPosition, pos.x, pos.z, false, true);
            chunkForced = false;
        }
    }

    private void releaseChunk() {
        if (!(level instanceof ServerLevel server)) return;
        ChunkPos chunk = new ChunkPos(this.worldPosition);
        server.getChunkSource().removeRegionTicket(CLAIM_ANCHOR_TICKET, chunk, 1, this.worldPosition);
        chunkForced = false;
    }
}
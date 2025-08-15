package net.tjkraft.claimanchor.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;
import net.tjkraft.claimanchor.config.CAServerConfig;

@Mod.EventBusSubscriber(modid = ClaimAnchor.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (shouldBlock(event.getPlayer(), event.getPos(), (Level) event.getLevel())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (shouldBlock(player, event.getPos(), (Level) event.getLevel())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockPos.MutableBlockPos mutableBlockPos = event.getPos().mutable();
        switch (event.getFace()) {
            case DOWN -> mutableBlockPos.setY(mutableBlockPos.getY() - 1);
            case UP -> mutableBlockPos.setY(mutableBlockPos.getY() + 1);
            case NORTH -> mutableBlockPos.setZ(mutableBlockPos.getZ() - 1);
            case SOUTH -> mutableBlockPos.setZ(mutableBlockPos.getZ() + 1);
            case WEST -> mutableBlockPos.setZ(mutableBlockPos.getX() - 1);
            case EAST -> mutableBlockPos.setZ(mutableBlockPos.getX() + 1);
        }
        BlockState state = level.getBlockState(event.getPos());

        ResourceLocation blockId = ForgeRegistries.BLOCKS.getKey(state.getBlock());

        if (CAServerConfig.CLAIM_ALLOWED_BLOCKS.get().contains(blockId.toString())) return;

        if (shouldBlock(player, mutableBlockPos, level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        Entity target = event.getTarget();
        Level level = event.getLevel();
        ResourceLocation entityId = ForgeRegistries.ENTITY_TYPES.getKey(target.getType());

        if (CAServerConfig.CLAIM_ALLOWED_ENTITIES.get().contains(entityId.toString())) return;

        if (shouldBlock(player, target.blockPosition(), level)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onEntityDamage(LivingAttackEvent event) {
        Entity target = event.getEntity();
        if (target.level().isClientSide) return;
        //if (isEntityAllowed(target)) return;
        if (target instanceof Player) return;

        if (event.getSource().getEntity() instanceof Player player) {
            if (shouldBlock(player, target.blockPosition(), target.level())) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onEntityMount(EntityMountEvent event) {
        if (!event.isMounting()) return;

        Entity rider = event.getEntityMounting();
        Entity mount = event.getEntityBeingMounted();

        if (rider == null || mount == null) return;
        if (isEntityAllowed(mount)) return;

        if (event.getEntity() instanceof Player player) {
            if (shouldBlock(player, mount.blockPosition(), mount.level())) {
                event.setCanceled(true);
            }
        }
    }


    private static boolean isEntityAllowed(Entity entity) {
        EntityType<?> type = entity.getType();
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(type);
        if (id == null) return false;

        for (String entry : CAServerConfig.CLAIM_ALLOWED_ENTITIES.get()) {
            if (entry.startsWith("#")) {
                ResourceLocation tagId = new ResourceLocation(entry.substring(1));
                TagKey<EntityType<?>> tagKey = TagKey.create(Registries.ENTITY_TYPE, tagId);
                if (type.is(tagKey)) return true;
            } else {
                if (entry.equals(id.toString())) return true;
            }
        }

        return false;
    }

    private static boolean shouldBlock(Player player, BlockPos targetPos, Level level) {
        LevelChunk levelChunk = level.getChunkAt(targetPos);
        ChunkPos chunkPos = levelChunk.getPos();
        int minX = chunkPos.getMinBlockX();
        int maxX = chunkPos.getMaxBlockX();

        int minZ = chunkPos.getMinBlockZ();
        int maxZ = chunkPos.getMaxBlockZ();

        int minY = targetPos.getY() - CAServerConfig.MIN_ANCHOR_Y.get();
        int maxY = targetPos.getY() + CAServerConfig.MAX_ANCHOR_Y.get();

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockEntity be = level.getBlockEntity(pos);
                    if (be instanceof ClaimAnchorBlockEntity anchor) {
                        if (anchor.getClaimTime() > 0) {
                            if (!anchor.hasAccess(player.getUUID())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}
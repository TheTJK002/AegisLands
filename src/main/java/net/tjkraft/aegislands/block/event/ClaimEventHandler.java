package net.tjkraft.aegislands.block.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.world.ClaimManager;

import java.util.Objects;

@Mod.EventBusSubscriber(modid = AegisLands.MOD_ID)
public class ClaimEventHandler {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getPlayer();
        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        ClaimManager manager = ClaimManager.get(event.getLevel());

        if (manager.isChunkClaimed(chunkPos)) {
            BlockState state = event.getState();
            boolean isClaimAnchor = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock()))
                    .getPath().equals("claim_anchor");

            if (isClaimAnchor) {
                if (!manager.getOwner(chunkPos).equals(player.getUUID())) {
                    event.setCanceled(true);
                    player.sendSystemMessage(Component.literal("§cSolo l'owner del territorio può rompere il Claim Anchor!"));
                    return;
                }
            } else {
                if (!manager.isTrusted(chunkPos, player.getUUID(), player.getName().getString())) {
                    String blockId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock())).toString();

                    if (!manager.getClaimEntries(chunkPos, 2).contains(blockId)) {
                        event.setCanceled(true);
                        sendDenyMessage(player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (event.getLevel().isClientSide()) return;

            ChunkPos chunkPos = new ChunkPos(event.getPos());
            ClaimManager manager = ClaimManager.get(event.getLevel());

            if (manager.isChunkClaimed(chunkPos)) {
                if (!manager.isTrusted(chunkPos, player.getUUID(), player.getName().getString())) {
                    String blockId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(event.getPlacedBlock().getBlock())).toString();

                    if (!manager.getClaimEntries(chunkPos, 2).contains(blockId)) {
                        event.setCanceled(true);
                        sendDenyMessage(player);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide()) return;

        Player player = event.getEntity();
        BlockPos pos = event.getPos();
        ChunkPos chunkPos = new ChunkPos(pos);
        ClaimManager manager = ClaimManager.get(event.getLevel());

        if (manager.isChunkClaimed(chunkPos)) {
            if (manager.isTrusted(chunkPos, player.getUUID(), player.getName().getString())) {
                return;
            }

            BlockState state = event.getLevel().getBlockState(pos);
            String blockId = Objects.requireNonNull(ForgeRegistries.BLOCKS.getKey(state.getBlock())).toString(); // Es: "minecraft:chest"

            if (!manager.getClaimEntries(chunkPos, 2).contains(blockId)) {
                event.setCanceled(true);
                sendDenyMessage(player);
            }
        }
    }

    @SubscribeEvent
    public static void onAttackEntity(AttackEntityEvent event) {
        if (event.getEntity().level().isClientSide()) return;

        Player player = event.getEntity();
        Entity target = event.getTarget();
        ChunkPos chunkPos = new ChunkPos(target.blockPosition());
        ClaimManager manager = ClaimManager.get(player.level());

        if (manager.isChunkClaimed(chunkPos)) {
            if (manager.isTrusted(chunkPos, player.getUUID(), player.getName().getString())) {
                return;
            }

            String entityId = Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(target.getType())).toString();

            if (!manager.getClaimEntries(chunkPos, 1).contains(entityId)) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("§cNon hai i permessi per interagire con questa entità!"));
            }
        }
    }

    private static void sendDenyMessage(Player player) {
        player.sendSystemMessage(Component.literal("§cNon hai i permessi per interagire in questo chunk protetto!"));
    }
}
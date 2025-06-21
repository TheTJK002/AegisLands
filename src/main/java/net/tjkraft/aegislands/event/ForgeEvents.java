package net.tjkraft.aegislands.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlockEntity;

@Mod.EventBusSubscriber(modid = AegisLands.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!isAllowed(player, event.getPos())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (!isAllowed(event.getPlayer(), event.getPos())) event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        if (!isAllowed(event.getEntity(), event.getPos())) event.setCanceled(true);
    }

    private static boolean isAllowed(Player player, BlockPos pos) {
        Level level = player.level();

        ChunkPos chunk = new ChunkPos(pos);
        BlockPos start = chunk.getBlockAt(0, 0, 0);
        BlockPos end = chunk.getBlockAt(15, level.getMaxBuildHeight(), 15);

        for (BlockPos check : BlockPos.betweenClosed(start, end)) {
            BlockEntity be = level.getBlockEntity(check);
            if (be instanceof AegisAnchorBlockEntity claim) {
                if (claim.isInsideActiveClaim(pos) && !player.getUUID().equals(claim.getOwner())) {
                    return false;
                }
            }
        }

        return true;
    }


}

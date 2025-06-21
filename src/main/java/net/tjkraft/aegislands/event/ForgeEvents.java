package net.tjkraft.aegislands.event;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
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
        if (!isAllowed(player, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        Player player = event.getPlayer();
        if (!isAllowed(player, event.getPos())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!isAllowed(player, event.getPos())) {
            event.setCanceled(true);
        }
    }

    public static boolean isAllowed(Player player, BlockPos pos) {
        Level level = player.level();
        for (BlockPos nearby : BlockPos.betweenClosed(
                pos.offset(-8, -8, -8), pos.offset(8, 8, 8))) {
            BlockEntity be = level.getBlockEntity(nearby);
            if (be instanceof AegisAnchorBlockEntity claimBlock) {
                if (claimBlock.getOwner() != null && !claimBlock.getOwner().equals(player.getUUID())) {
                    BlockPos center = claimBlock.getBlockPos();
                    if (withinClaimedArea(center, pos)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    private static boolean withinClaimedArea(BlockPos center, BlockPos target) {
        return Math.abs(center.getX() - target.getX()) <= 8 &&
                Math.abs(center.getZ() - target.getZ()) <= 8 &&
                Math.abs(center.getY() - target.getY()) <= 8;
    }
}

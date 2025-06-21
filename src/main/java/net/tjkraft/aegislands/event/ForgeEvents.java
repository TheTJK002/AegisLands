package net.tjkraft.aegislands.event;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.blockEntity.custom.AegisAnchorBlockEntity;

@Mod.EventBusSubscriber(modid = AegisLands.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEvents {

    @SubscribeEvent
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (shouldBlock(event.getPlayer(), event.getPos(), (Level) event.getLevel())) {
            event.setCanceled(true);
            event.getPlayer().sendSystemMessage(Component.literal("Questa zona è protetta da un Aegis Anchor nel chunk!"));
        }
    }

    @SubscribeEvent
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (event.getEntity() instanceof Player player) {
            if (shouldBlock(player, event.getPos(), (Level) event.getLevel())) {
                event.setCanceled(true);
                player.sendSystemMessage(Component.literal("Questa zona è protetta da un Aegis Anchor nel chunk!"));
            }
        }
    }


    private static boolean shouldBlock(Player player, BlockPos targetPos, Level level) {
        int chunkX = targetPos.getX() >> 4;
        int chunkZ = targetPos.getZ() >> 4;

        int anchorMinY = targetPos.getY() - 16;
        int anchorMaxY = targetPos.getY() + 16;

        for (int y = anchorMinY; y <= anchorMaxY; y++) {
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    BlockPos checkPos = new BlockPos((chunkX << 4) + x, y, (chunkZ << 4) + z);
                    BlockEntity be = level.getBlockEntity(checkPos);
                    if (be instanceof AegisAnchorBlockEntity anchor) {
                        if (!player.getUUID().equals(anchor.getOwner())) {
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }
}

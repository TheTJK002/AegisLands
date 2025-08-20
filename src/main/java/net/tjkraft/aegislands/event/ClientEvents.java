package net.tjkraft.aegislands.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.menu.custom.trusted.AegisLandsTrustedAddScreen;

@Mod.EventBusSubscriber(modid = AegisLands.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        refreshTrustedAddScreen();
    }

    @SubscribeEvent
    public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        refreshTrustedAddScreen();
    }

    private static void refreshTrustedAddScreen() {
        if (Minecraft.getInstance().screen instanceof AegisLandsTrustedAddScreen s) {
            s.updateOnlinePlayers(Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map(p -> p.getProfile().getId()).toList());
        }
    }
}

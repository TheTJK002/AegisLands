package net.tjkraft.claimanchor.event;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.menu.custom.trusted.ClaimAnchorTrustedAddScreen;

@Mod.EventBusSubscriber(modid = ClaimAnchor.MOD_ID, value = Dist.CLIENT)
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
        if (Minecraft.getInstance().screen instanceof ClaimAnchorTrustedAddScreen s) {
            s.updateOnlinePlayers(Minecraft.getInstance().getConnection().getOnlinePlayers().stream().map(p -> p.getProfile().getId()).toList());
        }
    }
}

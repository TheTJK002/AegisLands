package net.tjkraft.claimanchor;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tjkraft.claimanchor.block.CABlocks;
import net.tjkraft.claimanchor.block.blockEntity.CABlockEntity;
import net.tjkraft.claimanchor.config.CAServerConfig;
import net.tjkraft.claimanchor.item.CAItems;
import net.tjkraft.claimanchor.menu.CAMenuTypes;
import net.tjkraft.claimanchor.menu.custom.ClaimAnchorMainScreen;
import net.tjkraft.claimanchor.network.ClaimAnchorNetwork;
import org.slf4j.Logger;

@Mod(ClaimAnchor.MOD_ID)
public class ClaimAnchor {
    public static final String MOD_ID = "claim_anchor";
    private static final Logger LOGGER = LogUtils.getLogger();

    public ClaimAnchor() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        CAItems.ITEMS.register(modEventBus);
        CABlocks.BLOCKS.register(modEventBus);
        CABlockEntity.BLOCK_ENTITIES.register(modEventBus);
        CAMenuTypes.MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, CAServerConfig.SERVER_CONFIG);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(ClaimAnchorNetwork::register);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(CAMenuTypes.CLAIM_ANCHOR_MENU.get(), ClaimAnchorMainScreen::new);

        }
    }
}

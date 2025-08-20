package net.tjkraft.aegislands;

import com.mojang.logging.LogUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.tjkraft.aegislands.block.ALBlocks;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.item.ALItems;
import net.tjkraft.aegislands.menu.ALMenuTypes;
import net.tjkraft.aegislands.menu.custom.main.AegisLandsMainScreen;
import net.tjkraft.aegislands.network.AegisLandsNetwork;
import org.slf4j.Logger;

@Mod(AegisLands.MOD_ID)
public class AegisLands {
    public static final String MOD_ID = "aegis_lands";
    private static final Logger LOGGER = LogUtils.getLogger();

    public AegisLands() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ALItems.ITEMS.register(modEventBus);
        ALBlocks.BLOCKS.register(modEventBus);
        ALBlockEntity.BLOCK_ENTITIES.register(modEventBus);
        ALMenuTypes.MENUS.register(modEventBus);

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);
        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ALServerConfig.SERVER_CONFIG);

        modEventBus.addListener(this::addCreative);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(AegisLandsNetwork::register);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.FUNCTIONAL_BLOCKS) {
            event.accept(ALBlocks.CLAIM_ANCHOR.get());
            event.accept(ALItems.CLAIM_MONOCLE.get());
            event.accept(ALItems.CHUNK_LOADER_UPGRADE.get());
        }
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {
            MenuScreens.register(ALMenuTypes.CLAIM_ANCHOR_MENU.get(), AegisLandsMainScreen::new);
        }
    }
}

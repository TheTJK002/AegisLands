package net.tjkraft.aegislands;

import com.mojang.logging.LogUtils;
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
import net.tjkraft.aegislands.block.ALBlocks;
import net.tjkraft.aegislands.block.blockEntity.ALBlockEntity;
import net.tjkraft.aegislands.config.ALServerConfig;
import net.tjkraft.aegislands.item.ALItems;
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

        modEventBus.addListener(this::commonSetup);
        MinecraftForge.EVENT_BUS.register(this);

        ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, ALServerConfig.SERVER_CONFIG);

    }

    private void commonSetup(final FMLCommonSetupEvent event) {
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
    }

    @Mod.EventBusSubscriber(modid = MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void onClientSetup(FMLClientSetupEvent event) {}
    }
}

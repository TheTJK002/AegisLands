package net.tjkraft.aegislands.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.item.custom.ChunkLoaderUpgrade;
import net.tjkraft.aegislands.item.custom.ClaimMonocle;

public class ALItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AegisLands.MOD_ID);

    public static final RegistryObject<Item> CLAIM_MONOCLE = ITEMS.register("claim_monocle", () -> new ClaimMonocle(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> CHUNK_LOADER_UPGRADE = ITEMS.register("chunk_loader_upgrade", () -> new ChunkLoaderUpgrade(new Item.Properties().stacksTo(1)));
}

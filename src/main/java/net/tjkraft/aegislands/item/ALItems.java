package net.tjkraft.aegislands.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.item.custom.AegisLensItem;

public class ALItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, AegisLands.MOD_ID);

    public static final RegistryObject<Item> AEGIS_LENS = ITEMS.register("aegis_lens", () -> new AegisLensItem(new Item.Properties().stacksTo(1)));

}

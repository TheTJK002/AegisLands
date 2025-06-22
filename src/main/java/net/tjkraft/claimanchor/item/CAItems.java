package net.tjkraft.claimanchor.item;

import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.item.custom.ClaimMonocle;

public class CAItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ClaimAnchor.MOD_ID);

    public static final RegistryObject<Item> CLAIM_MONOCLE = ITEMS.register("claim_monocle", () -> new ClaimMonocle(new Item.Properties().stacksTo(1)));
}

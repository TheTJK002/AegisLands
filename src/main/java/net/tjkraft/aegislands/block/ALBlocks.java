package net.tjkraft.aegislands.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlock;
import net.tjkraft.aegislands.item.ALItems;

import java.util.function.Supplier;

public class ALBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AegisLands.MOD_ID);

    public static final RegistryObject<Block> AEGIS_ANCHOR = registerBlock("aegis_anchor", () -> new AegisAnchorBlock(BlockBehaviour.Properties.of()));

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ALItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}

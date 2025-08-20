package net.tjkraft.aegislands.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.custom.ClaimAnchor;
import net.tjkraft.aegislands.item.ALItems;

import java.util.function.Supplier;

public class ALBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, AegisLands.MOD_ID);

    public static final RegistryObject<Block> CLAIM_ANCHOR = registerBlock("claim_anchor", () -> new ClaimAnchor(BlockBehaviour.Properties.of().sound(SoundType.ANVIL).strength(0.5f, 0.5f)));

    public static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ALItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }
}

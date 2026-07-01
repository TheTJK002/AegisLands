package net.tjkraft.aegislands.blockTile;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.ALBlocks;
import net.tjkraft.aegislands.blockTile.custom.ClaimAnchorBE;

public class ALBlockTiles {
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AegisLands.MOD_ID);


    public static final RegistryObject<BlockEntityType<ClaimAnchorBE>> CLAIM_ANCHOR_BE = TILES.register("claim_anchor_be", () -> BlockEntityType.Builder.of(ClaimAnchorBE::new, ALBlocks.CLAIM_ANCHOR.get()).build(null));

}

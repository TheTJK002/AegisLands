package net.tjkraft.aegislands.block;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlockEntity;

public class ALBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AegisLands.MOD_ID);

    public static final RegistryObject<BlockEntityType<AegisAnchorBlockEntity>> AEGIS_ANCHOR_BE = BLOCK_ENTITY.register("aegis_anchor_be", () -> BlockEntityType.Builder.of(AegisAnchorBlockEntity::new).build(null));

}

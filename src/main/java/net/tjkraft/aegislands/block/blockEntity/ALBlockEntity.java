package net.tjkraft.aegislands.block.blockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.ALBlocks;
import net.tjkraft.aegislands.block.blockEntity.custom.AegisAnchorBlockEntity;

public class ALBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AegisLands.MOD_ID);

    public static final RegistryObject<BlockEntityType<AegisAnchorBlockEntity>> AEGIS_ANCHOR_BE = BLOCK_ENTITIES.register("aegis_anchor_be", () -> BlockEntityType.Builder.of(AegisAnchorBlockEntity::new, ALBlocks.AEGIS_ANCHOR.get()).build(null));

}

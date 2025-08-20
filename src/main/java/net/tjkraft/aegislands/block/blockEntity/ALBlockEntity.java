package net.tjkraft.aegislands.block.blockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.ALBlocks;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;

public class ALBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, AegisLands.MOD_ID);

    public static final RegistryObject<BlockEntityType<ClaimAnchorBE>> CLAIM_ANCHOR_BE = BLOCK_ENTITIES.register("claim_anchor", () -> BlockEntityType.Builder.of(ClaimAnchorBE::new, ALBlocks.CLAIM_ANCHOR.get()).build(null));

}

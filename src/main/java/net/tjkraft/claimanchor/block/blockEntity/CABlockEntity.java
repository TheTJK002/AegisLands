package net.tjkraft.claimanchor.block.blockEntity;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.block.CABlocks;
import net.tjkraft.claimanchor.block.blockEntity.custom.ClaimAnchorBlockEntity;

public class CABlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ClaimAnchor.MOD_ID);

    public static final RegistryObject<BlockEntityType<ClaimAnchorBlockEntity>> CLAIM_ANCHOR_BE = BLOCK_ENTITIES.register("claim_anchor", () -> BlockEntityType.Builder.of(ClaimAnchorBlockEntity::new, CABlocks.CLAIM_ANCHOR.get()).build(null));

}

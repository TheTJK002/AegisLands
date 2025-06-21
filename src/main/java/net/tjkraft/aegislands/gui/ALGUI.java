package net.tjkraft.aegislands.gui;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.custom.AegisAnchorBlockEntity;

public class ALGUI {
    public static final DeferredRegister<MenuType<?>> GUI = DeferredRegister.create(Registries.MENU, AegisLands.MOD_ID);

    public static final RegistryObject<MenuType<AegisAnchorMenu>> CLAIM_BLOCK_MENU =
            GUI.register("claim_block_menu", () -> IForgeMenuType.create((windowId, inv, data) -> {
                Level level = inv.player.level();
                BlockPos pos = data.readBlockPos();
                BlockEntity entity = level.getBlockEntity(pos);
                if (entity instanceof AegisAnchorBlockEntity claimBlock) {
                    return new AegisAnchorMenu(windowId, inv, claimBlock);
                }
                return null;
            }));

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return GUI.register(name, () -> IForgeMenuType.create(factory));
    }
}

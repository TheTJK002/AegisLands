package net.tjkraft.aegislands.gui;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;

public class ALGUI {
    public static final DeferredRegister<MenuType<?>> GUI = DeferredRegister.create(Registries.MENU, AegisLands.MOD_ID);

    public static final RegistryObject<MenuType<AegisAnchorMenu>> AEGIS_ANCHOR_MENU = registerMenuType("aegis_anchor_menu", AegisAnchorMenu::new);


    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return GUI.register(name, () -> IForgeMenuType.create(factory));
    }
}

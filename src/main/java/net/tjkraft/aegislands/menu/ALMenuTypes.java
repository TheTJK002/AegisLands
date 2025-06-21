package net.tjkraft.aegislands.menu;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.menu.custom.AegisAnchorMenu;

public class ALMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AegisLands.MOD_ID);

    public static final RegistryObject<MenuType<AegisAnchorMenu>> AEGIS_ANCHOR_MENU = registerMenuType("aegis_anchor_menu", AegisAnchorMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
}

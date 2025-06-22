package net.tjkraft.claimanchor.menu;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.claimanchor.ClaimAnchor;
import net.tjkraft.claimanchor.menu.custom.ClaimAnchorMenu;

public class CAMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, ClaimAnchor.MOD_ID);

    public static final RegistryObject<MenuType<ClaimAnchorMenu>> CLAIM_ANCHOR_MENU = registerMenuType("claim_anchor_menu", ClaimAnchorMenu::new);

    private static <T extends AbstractContainerMenu> RegistryObject<MenuType<T>> registerMenuType(String name, IContainerFactory<T> factory) {
        return MENUS.register(name, () -> IForgeMenuType.create(factory));
    }
}

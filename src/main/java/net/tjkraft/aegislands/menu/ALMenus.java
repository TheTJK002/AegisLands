package net.tjkraft.aegislands.menu;

import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.menu.claimAnchor.ClaimAnchorMenu;

public class ALMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(ForgeRegistries.MENU_TYPES, AegisLands.MOD_ID);

    public static final RegistryObject<MenuType<ClaimAnchorMenu>> CLAIM_ANCHOR_MENU =
            MENUS.register("claim_anchor_menu", () -> IForgeMenuType.create(ClaimAnchorMenu::new));
}

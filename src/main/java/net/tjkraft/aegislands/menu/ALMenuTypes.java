package net.tjkraft.aegislands.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.tjkraft.aegislands.AegisLands;
import net.tjkraft.aegislands.block.blockEntity.custom.ClaimAnchorBE;
import net.tjkraft.aegislands.menu.custom.main.AegisLandsMainMenu;

public class ALMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(ForgeRegistries.MENU_TYPES, AegisLands.MOD_ID);

    public static final RegistryObject<MenuType<AegisLandsMainMenu>> CLAIM_ANCHOR_MENU =
            MENUS.register("claim_anchor_main", () ->
                    IForgeMenuType.create((windowId, inv, buf) -> {
                        BlockPos pos = buf.readBlockPos();
                        String ownerName = buf.readUtf(32767);

                        var level = inv.player.level();
                        var be = level.getBlockEntity(pos);
                        if (be instanceof ClaimAnchorBE anchor) {
                            return new AegisLandsMainMenu(windowId, inv, anchor, ownerName);
                        }
                        return null;
                    })
            );
}

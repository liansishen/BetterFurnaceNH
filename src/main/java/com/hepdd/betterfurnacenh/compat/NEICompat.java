package com.hepdd.betterfurnacenh.compat;

import net.minecraft.item.ItemStack;

import com.hepdd.betterfurnacenh.BFNH;
import com.hepdd.betterfurnacenh.client.gui.GuiBFFurnace;

import codechicken.nei.api.API;
import codechicken.nei.recipe.DefaultOverlayHandler;
import cpw.mods.fml.common.Optional;

public class NEICompat {

    @Optional.Method(modid = "NotEnoughItems")
    public static void register() {
        API.registerGuiOverlay(GuiBFFurnace.class, "smelting");
        API.registerGuiOverlayHandler(GuiBFFurnace.class, new DefaultOverlayHandler(6, 18), "smelting");

        API.hideItem(new ItemStack(BFNH.ironFurnaceLit));
        API.hideItem(new ItemStack(BFNH.goldFurnaceLit));
        API.hideItem(new ItemStack(BFNH.diamondFurnaceLit));
        if (BFNH.ironBlastFurnaceLit != null) {
            API.hideItem(new ItemStack(BFNH.ironBlastFurnaceLit));
            API.hideItem(new ItemStack(BFNH.goldBlastFurnaceLit));
            API.hideItem(new ItemStack(BFNH.diamondBlastFurnaceLit));
        }
    }
}

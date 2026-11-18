package com.hepdd.betterfurnacenh.tileentities;

import net.minecraft.item.ItemStack;

import ganymedes01.etfuturum.recipes.BlastFurnaceRecipes;

public class TileEntityBFBlastFurnace extends TileEntityBFFurnace {

    public TileEntityBFBlastFurnace() {
        super(EnumFurnaceTier.IRON);
        refreshSpeedMultiplier();
    }

    public TileEntityBFBlastFurnace(EnumFurnaceTier tier) {
        super(tier);
        refreshSpeedMultiplier();
    }

    @Override
    protected void refreshSpeedMultiplier() {
        speedMultiplier = getTier().getSpeedMultiplier() * 2f;
    }

    @Override
    protected String getDefaultInventoryName() {
        return "container.bfnh." + getTier().name()
            .toLowerCase() + "_blast_furnace";
    }

    @Override
    protected ItemStack lookupSmeltingResult(ItemStack stack) {
        if (stack == null) return null;
        return BlastFurnaceRecipes.smelting()
            .getSmeltingResult(stack);
    }
}

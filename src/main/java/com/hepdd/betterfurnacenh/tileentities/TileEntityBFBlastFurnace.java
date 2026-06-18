package com.hepdd.betterfurnacenh.tileentities;

import net.minecraft.item.ItemStack;

import ganymedes01.etfuturum.recipes.BlastFurnaceRecipes;

public class TileEntityBFBlastFurnace extends TileEntityBFFurnace {

    public TileEntityBFBlastFurnace() {
        super(EnumFurnaceTier.IRON);
    }

    public TileEntityBFBlastFurnace(EnumFurnaceTier tier) {
        super(tier);
    }

    @Override
    public float getSpeedMultiplier() {
        return super.getSpeedMultiplier() * 2f;
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
    protected boolean isSmeltable(ItemStack stack) {
        if (stack == null) return false;
        return BlastFurnaceRecipes.smelting()
            .getSmeltingResult(stack) != null;
    }

    @Override
    public boolean canSmelt() {
        if (getStackInSlot(0) == null) return false;
        ItemStack result = BlastFurnaceRecipes.smelting()
            .getSmeltingResult(getStackInSlot(0));
        if (result == null) return false;
        if (getStackInSlot(2) == null) return true;
        if (!getStackInSlot(2).isItemEqual(result)) return false;
        int newSize = getStackInSlot(2).stackSize + result.stackSize;
        return newSize <= getInventoryStackLimit() && newSize <= getStackInSlot(2).getMaxStackSize();
    }

    @Override
    public void smeltItem() {
        if (!canSmelt()) return;
        ItemStack result = BlastFurnaceRecipes.smelting()
            .getSmeltingResult(getStackInSlot(0));
        ItemStack output = getStackInSlot(2);
        if (output == null) {
            setInventorySlotContents(2, result.copy());
        } else if (output.getItem() == result.getItem()) {
            output.stackSize += result.stackSize;
        }
        decrStackSize(0, 1);
    }
}

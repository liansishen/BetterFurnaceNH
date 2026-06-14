package com.hepdd.betterfurnacenh.creativetab;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import com.hepdd.betterfurnacenh.BFNH;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class CreativeTabBFNH extends CreativeTabs {

    public CreativeTabBFNH() {
        super(BFNH.MODID);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public Item getTabIconItem() {
        return Item.getItemFromBlock(BFNH.diamondFurnaceIdle);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public ItemStack getIconItemStack() {
        return new ItemStack(BFNH.diamondFurnaceIdle);
    }
}

package com.hepdd.betterfurnacenh.compat;

import net.minecraft.block.Block;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

import com.hepdd.betterfurnacenh.BFNH;
import com.hepdd.betterfurnacenh.blocks.BlockBFBlastFurnace;
import com.hepdd.betterfurnacenh.tileentities.EnumFurnaceTier;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFBlastFurnace;

import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModBlocks;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;

public final class EFRCompat {

    private EFRCompat() {}

    public static void preInit() {
        BFNH.LOG.info("Et-Futurum-Requiem detected, registering blast furnaces");

        BFNH.ironBlastFurnaceIdle = new BlockBFBlastFurnace(false, EnumFurnaceTier.IRON);
        BFNH.ironBlastFurnaceLit = new BlockBFBlastFurnace(true, EnumFurnaceTier.IRON);
        BFNH.registerFurnacePair(BFNH.ironBlastFurnaceIdle, BFNH.ironBlastFurnaceLit, "iron_blast_furnace");

        BFNH.goldBlastFurnaceIdle = new BlockBFBlastFurnace(false, EnumFurnaceTier.GOLD);
        BFNH.goldBlastFurnaceLit = new BlockBFBlastFurnace(true, EnumFurnaceTier.GOLD);
        BFNH.registerFurnacePair(BFNH.goldBlastFurnaceIdle, BFNH.goldBlastFurnaceLit, "gold_blast_furnace");

        BFNH.diamondBlastFurnaceIdle = new BlockBFBlastFurnace(false, EnumFurnaceTier.DIAMOND);
        BFNH.diamondBlastFurnaceLit = new BlockBFBlastFurnace(true, EnumFurnaceTier.DIAMOND);
        BFNH.registerFurnacePair(BFNH.diamondBlastFurnaceIdle, BFNH.diamondBlastFurnaceLit, "diamond_blast_furnace");

        GameRegistry.registerTileEntity(TileEntityBFBlastFurnace.class, BFNH.MODID + ":blast_furnace");
    }

    public static void init() {
        Block efrBlastFurnace = ModBlocks.BLAST_FURNACE.get();
        if (efrBlastFurnace == null || BFNH.ironBlastFurnaceIdle == null) {
            return;
        }

        Object ironMatBlast = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Iron, 1);
        Object goldMatBlast = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Gold, 1);

        GameRegistry.addRecipe(
            new ItemStack(BFNH.ironBlastFurnaceIdle),
            "III",
            "IFI",
            "III",
            'I',
            ironMatBlast,
            'F',
            new ItemStack(efrBlastFurnace));

        GameRegistry.addRecipe(
            new ItemStack(BFNH.goldBlastFurnaceIdle),
            "III",
            "IFI",
            "III",
            'I',
            goldMatBlast,
            'F',
            new ItemStack(BFNH.ironBlastFurnaceIdle));

        GameRegistry.addRecipe(
            new ItemStack(BFNH.diamondBlastFurnaceIdle),
            "III",
            "IFI",
            "III",
            'I',
            Items.diamond,
            'F',
            new ItemStack(BFNH.goldBlastFurnaceIdle));
    }
}

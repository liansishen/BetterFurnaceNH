package com.hepdd.betterfurnacenh;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.hepdd.betterfurnacenh.blocks.BlockBFFurnace;
import com.hepdd.betterfurnacenh.compat.NEICompat;
import com.hepdd.betterfurnacenh.creativetab.CreativeTabBFNH;
import com.hepdd.betterfurnacenh.items.ItemHopperUpgrade;
import com.hepdd.betterfurnacenh.network.BFNHNet;
import com.hepdd.betterfurnacenh.tileentities.EnumFurnaceTier;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFBlastFurnace;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.SidedProxy;
import cpw.mods.fml.common.event.FMLInitializationEvent;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.registry.GameRegistry;
import ganymedes01.etfuturum.ModBlocks;
import gregtech.api.enums.Materials;
import gregtech.api.enums.OrePrefixes;
import gregtech.api.util.GTOreDictUnificator;

@Mod(
    modid = BFNH.MODID,
    version = Tags.VERSION,
    name = "BFNH",
    acceptedMinecraftVersions = "[1.7.10]",
    guiFactory = "com.hepdd.betterfurnacenh.config.BFNHGuiFactory")
public class BFNH {

    public static final String MODID = "betterfurnacenh";
    public static final Logger LOG = LogManager.getLogger(MODID);

    public static final CreativeTabBFNH creativeTab = new CreativeTabBFNH();

    @Mod.Instance(MODID)
    public static BFNH instance;

    @SidedProxy(
        clientSide = "com.hepdd.betterfurnacenh.ClientProxy",
        serverSide = "com.hepdd.betterfurnacenh.CommonProxy")
    public static CommonProxy proxy;

    public static Block ironFurnaceIdle;
    public static Block ironFurnaceLit;
    public static Block goldFurnaceIdle;
    public static Block goldFurnaceLit;
    public static Block diamondFurnaceIdle;
    public static Block diamondFurnaceLit;

    public static Block ironBlastFurnaceIdle;
    public static Block ironBlastFurnaceLit;
    public static Block goldBlastFurnaceIdle;
    public static Block goldBlastFurnaceLit;
    public static Block diamondBlastFurnaceIdle;
    public static Block diamondBlastFurnaceLit;

    public static Item hopperUpgrade;

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event) {
        proxy.preInit(event);

        ironFurnaceIdle = new BlockBFFurnace(false, EnumFurnaceTier.IRON, false);
        ironFurnaceLit = new BlockBFFurnace(true, EnumFurnaceTier.IRON, false);
        registerFurnacePair(ironFurnaceIdle, ironFurnaceLit, "iron_furnace");

        goldFurnaceIdle = new BlockBFFurnace(false, EnumFurnaceTier.GOLD, false);
        goldFurnaceLit = new BlockBFFurnace(true, EnumFurnaceTier.GOLD, false);
        registerFurnacePair(goldFurnaceIdle, goldFurnaceLit, "gold_furnace");

        diamondFurnaceIdle = new BlockBFFurnace(false, EnumFurnaceTier.DIAMOND, false);
        diamondFurnaceLit = new BlockBFFurnace(true, EnumFurnaceTier.DIAMOND, false);
        registerFurnacePair(diamondFurnaceIdle, diamondFurnaceLit, "diamond_furnace");

        GameRegistry.registerTileEntity(TileEntityBFFurnace.class, MODID + ":furnace");
        GameRegistry.registerTileEntity(TileEntityBFBlastFurnace.class, MODID + ":blast_furnace");

        hopperUpgrade = new ItemHopperUpgrade();
        GameRegistry.registerItem(hopperUpgrade, "hopper_upgrade");

        NetworkRegistry.INSTANCE.registerGuiHandler(this, proxy);
    }

    private void registerFurnacePair(Block idle, Block lit, String name) {
        GameRegistry.registerBlock(idle, ItemBlock.class, name);
        GameRegistry.registerBlock(lit, ItemBlock.class, name + "_lit");
        idle.setBlockName(MODID + "_" + name);
        lit.setBlockName(MODID + "_" + name + "_lit");
        idle.setCreativeTab(creativeTab);
        lit.setCreativeTab(null);
        BlockBFFurnace.registerBlockPair(idle, lit);
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event) {
        proxy.init(event);
        BFNHNet.init();

        GameRegistry.addShapelessRecipe(new ItemStack(hopperUpgrade), new ItemStack(Blocks.hopper));

        Object ironMat = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Iron, 1);
        Object goldMat = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Gold, 1);

        GameRegistry.addRecipe(
            new ItemStack(ironFurnaceIdle),
            "III",
            "IFI",
            "III",
            'I',
            ironMat,
            'F',
            new ItemStack(Blocks.furnace));

        GameRegistry.addRecipe(
            new ItemStack(goldFurnaceIdle),
            "III",
            "IFI",
            "III",
            'I',
            goldMat,
            'F',
            new ItemStack(ironFurnaceIdle));

        GameRegistry.addRecipe(
            new ItemStack(diamondFurnaceIdle),
            "III",
            "IFI",
            "III",
            'I',
            Items.diamond,
            'F',
            new ItemStack(goldFurnaceIdle));
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event) {
        proxy.postInit(event);

        if (!Loader.isModLoaded("etfuturum")) {
            NEICompat.register();
            return;
        }

        LOG.info("Et-Futurum-Requiem detected, registering blast furnaces");

        ironBlastFurnaceIdle = new BlockBFFurnace(false, EnumFurnaceTier.IRON, true);
        ironBlastFurnaceLit = new BlockBFFurnace(true, EnumFurnaceTier.IRON, true);
        registerFurnacePair(ironBlastFurnaceIdle, ironBlastFurnaceLit, "iron_blast_furnace");

        goldBlastFurnaceIdle = new BlockBFFurnace(false, EnumFurnaceTier.GOLD, true);
        goldBlastFurnaceLit = new BlockBFFurnace(true, EnumFurnaceTier.GOLD, true);
        registerFurnacePair(goldBlastFurnaceIdle, goldBlastFurnaceLit, "gold_blast_furnace");

        diamondBlastFurnaceIdle = new BlockBFFurnace(false, EnumFurnaceTier.DIAMOND, true);
        diamondBlastFurnaceLit = new BlockBFFurnace(true, EnumFurnaceTier.DIAMOND, true);
        registerFurnacePair(diamondBlastFurnaceIdle, diamondBlastFurnaceLit, "diamond_blast_furnace");

        Block efrBlastFurnace = ModBlocks.BLAST_FURNACE.get();
        if (efrBlastFurnace != null) {
            Object ironMatBlast = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Iron, 1);
            Object goldMatBlast = GTOreDictUnificator.get(OrePrefixes.plate, Materials.Gold, 1);

            GameRegistry.addRecipe(
                new ItemStack(ironBlastFurnaceIdle),
                "III",
                "IFI",
                "III",
                'I',
                ironMatBlast,
                'F',
                new ItemStack(efrBlastFurnace));

            GameRegistry.addRecipe(
                new ItemStack(goldBlastFurnaceIdle),
                "III",
                "IFI",
                "III",
                'I',
                goldMatBlast,
                'F',
                new ItemStack(ironBlastFurnaceIdle));

            GameRegistry.addRecipe(
                new ItemStack(diamondBlastFurnaceIdle),
                "III",
                "IFI",
                "III",
                'I',
                Items.diamond,
                'F',
                new ItemStack(goldBlastFurnaceIdle));
        }

        NEICompat.register();
    }

    @Mod.EventHandler
    public void serverStarting(FMLServerStartingEvent event) {
        proxy.serverStarting(event);
    }
}

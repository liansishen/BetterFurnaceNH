package com.hepdd.betterfurnacenh.blocks;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import com.hepdd.betterfurnacenh.tileentities.EnumFurnaceTier;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFBlastFurnace;

public class BlockBFBlastFurnace extends BlockBFFurnace {

    public BlockBFBlastFurnace(boolean isBurning, EnumFurnaceTier tier) {
        super(isBurning, tier, true);
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        return new TileEntityBFBlastFurnace(getTier());
    }
}

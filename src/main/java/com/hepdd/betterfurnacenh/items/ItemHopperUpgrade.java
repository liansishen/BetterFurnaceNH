package com.hepdd.betterfurnacenh.items;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import com.hepdd.betterfurnacenh.BFNH;
import com.hepdd.betterfurnacenh.Config;
import com.hepdd.betterfurnacenh.client.overlay.GridMath;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemHopperUpgrade extends Item {

    @SideOnly(Side.CLIENT)
    private IIcon itemIcon;

    public ItemHopperUpgrade() {
        setMaxStackSize(16);
        setUnlocalizedName("betterfurnacenh_hopper_upgrade");
        setCreativeTab(BFNH.creativeTab);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        itemIcon = register.registerIcon("betterfurnacenh:hopper_upgrade");
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int meta) {
        return itemIcon;
    }

    @Override
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addInformation(ItemStack stack, EntityPlayer player, List tooltip, boolean showAdvanced) {
        double itemsPerSec = (double) Config.hopperItemsPerTransfer / Config.hopperTransferRate * 20.0;
        String rate = String.format("%.1f", itemsPerSec);
        tooltip.add(StatCollector.translateToLocal("tooltip.bfnh.hopper_upgrade.install"));
        tooltip.add(StatCollector.translateToLocalFormatted("tooltip.bfnh.hopper_upgrade.input", rate));
        tooltip.add(StatCollector.translateToLocalFormatted("tooltip.bfnh.hopper_upgrade.output", rate));
        tooltip.add(StatCollector.translateToLocal("tooltip.bfnh.hopper_upgrade.remove"));
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side,
        float hitX, float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityBFFurnace)) return false;
        TileEntityBFFurnace furnace = (TileEntityBFFurnace) te;

        ForgeDirection targetSide = GridMath
            .determineWrenchingSide(ForgeDirection.getOrientation(side), hitX, hitY, hitZ);
        if (targetSide != ForgeDirection.UP && targetSide != ForgeDirection.DOWN) return false;
        if (furnace.isHopperInstalled(targetSide)) return false;

        if (!world.isRemote) {
            furnace.installHopper(targetSide);
            furnace.markDirty();
            world.markBlockForUpdate(x, y, z);
            if (!player.capabilities.isCreativeMode) {
                stack.stackSize--;
                if (stack.stackSize <= 0) {
                    player.inventory.setInventorySlotContents(player.inventory.currentItem, null);
                }
            }
        }
        return true;
    }
}

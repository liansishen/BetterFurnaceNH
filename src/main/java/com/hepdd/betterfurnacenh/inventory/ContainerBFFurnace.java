package com.hepdd.betterfurnacenh.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ContainerBFFurnace extends Container {

    private final TileEntityBFFurnace tileFurnace;
    private int lastCookTime;
    private int lastBurnTime;
    private int lastItemBurnTime;
    private int lastFluidAmount;
    private int lastFluidID;
    private int lastTopHopperInstalled;
    private int lastBottomHopperInstalled;
    private int lastTopHopperEnabled;
    private int lastBottomHopperEnabled;

    public ContainerBFFurnace(InventoryPlayer invPlayer, TileEntityBFFurnace tileFurnace) {
        this.tileFurnace = tileFurnace;

        addSlotToContainer(new Slot(tileFurnace, 0, 56, 17));
        addSlotToContainer(new Slot(tileFurnace, 1, 56, 53));
        addSlotToContainer(new SlotFurnace(invPlayer.player, tileFurnace, 2, 116, 35));

        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 9; j++) {
                addSlotToContainer(new Slot(invPlayer, j + i * 9 + 9, 8 + j * 18, 84 + i * 18));
            }
        }
        for (int i = 0; i < 9; i++) {
            addSlotToContainer(new Slot(invPlayer, i, 8 + i * 18, 142));
        }
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);
        crafting.sendProgressBarUpdate(this, 0, (int) tileFurnace.furnaceCookTime);
        crafting.sendProgressBarUpdate(this, 1, (int) tileFurnace.furnaceBurnTime);
        crafting.sendProgressBarUpdate(this, 2, (int) tileFurnace.currentItemBurnTime);
        FluidStack fluid = tileFurnace.fluidTank.getFluid();
        crafting.sendProgressBarUpdate(this, 4, fluid != null ? fluid.getFluidID() : -1);
        crafting.sendProgressBarUpdate(this, 3, fluid != null ? fluid.amount : 0);
        crafting.sendProgressBarUpdate(this, 5, tileFurnace.isHopperInstalled(ForgeDirection.UP) ? 1 : 0);
        crafting.sendProgressBarUpdate(this, 6, tileFurnace.isHopperInstalled(ForgeDirection.DOWN) ? 1 : 0);
        crafting.sendProgressBarUpdate(this, 7, tileFurnace.isHopperEnabled(ForgeDirection.UP) ? 1 : 0);
        crafting.sendProgressBarUpdate(this, 8, tileFurnace.isHopperEnabled(ForgeDirection.DOWN) ? 1 : 0);
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();
        for (Object crafter : crafters) {
            ICrafting crafting = (ICrafting) crafter;

            int currentCookTime = (int) tileFurnace.furnaceCookTime;
            int currentBurnTime = (int) tileFurnace.furnaceBurnTime;
            int currentItemBurn = (int) tileFurnace.currentItemBurnTime;
            if (lastCookTime != currentCookTime) {
                crafting.sendProgressBarUpdate(this, 0, currentCookTime);
            }
            if (lastBurnTime != currentBurnTime) {
                crafting.sendProgressBarUpdate(this, 1, currentBurnTime);
            }
            if (lastItemBurnTime != currentItemBurn) {
                crafting.sendProgressBarUpdate(this, 2, currentItemBurn);
            }

            FluidStack fluid = tileFurnace.fluidTank.getFluid();
            int fluidAmount = fluid != null ? fluid.amount : 0;
            int fluidID = fluid != null ? fluid.getFluidID() : -1;

            if (lastFluidID != fluidID) {
                crafting.sendProgressBarUpdate(this, 4, fluidID);
            }
            if (lastFluidAmount != fluidAmount) {
                crafting.sendProgressBarUpdate(this, 3, fluidAmount);
            }

            int topInst = tileFurnace.isHopperInstalled(ForgeDirection.UP) ? 1 : 0;
            int botInst = tileFurnace.isHopperInstalled(ForgeDirection.DOWN) ? 1 : 0;
            int topEn = tileFurnace.isHopperEnabled(ForgeDirection.UP) ? 1 : 0;
            int botEn = tileFurnace.isHopperEnabled(ForgeDirection.DOWN) ? 1 : 0;

            if (lastTopHopperInstalled != topInst) {
                crafting.sendProgressBarUpdate(this, 5, topInst);
            }
            if (lastBottomHopperInstalled != botInst) {
                crafting.sendProgressBarUpdate(this, 6, botInst);
            }
            if (lastTopHopperEnabled != topEn) {
                crafting.sendProgressBarUpdate(this, 7, topEn);
            }
            if (lastBottomHopperEnabled != botEn) {
                crafting.sendProgressBarUpdate(this, 8, botEn);
            }
        }
        lastCookTime = (int) tileFurnace.furnaceCookTime;
        lastBurnTime = (int) tileFurnace.furnaceBurnTime;
        lastItemBurnTime = (int) tileFurnace.currentItemBurnTime;
        FluidStack fluid = tileFurnace.fluidTank.getFluid();
        lastFluidAmount = fluid != null ? fluid.amount : 0;
        lastFluidID = fluid != null ? fluid.getFluidID() : -1;
        lastTopHopperInstalled = tileFurnace.isHopperInstalled(ForgeDirection.UP) ? 1 : 0;
        lastBottomHopperInstalled = tileFurnace.isHopperInstalled(ForgeDirection.DOWN) ? 1 : 0;
        lastTopHopperEnabled = tileFurnace.isHopperEnabled(ForgeDirection.UP) ? 1 : 0;
        lastBottomHopperEnabled = tileFurnace.isHopperEnabled(ForgeDirection.DOWN) ? 1 : 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case 0:
                tileFurnace.furnaceCookTime = value;
                break;
            case 1:
                tileFurnace.furnaceBurnTime = value;
                break;
            case 2:
                tileFurnace.currentItemBurnTime = value;
                break;
            case 3:
                FluidStack fluid = tileFurnace.fluidTank.getFluid();
                if (fluid != null) {
                    fluid.amount = value;
                } else if (value > 0 && lastFluidID >= 0) {
                    Fluid f = FluidRegistry.getFluid(lastFluidID);
                    if (f != null) {
                        tileFurnace.fluidTank.setFluid(new FluidStack(f, value));
                    }
                }
                break;
            case 4:
                lastFluidID = value;
                break;
            case 5:
                tileFurnace.setHopperInstalled(ForgeDirection.UP, value != 0);
                break;
            case 6:
                tileFurnace.setHopperInstalled(ForgeDirection.DOWN, value != 0);
                break;
            case 7:
                tileFurnace.setHopperEnabled(ForgeDirection.UP, value != 0);
                break;
            case 8:
                tileFurnace.setHopperEnabled(ForgeDirection.DOWN, value != 0);
                break;
        }
    }

    @Override
    public boolean canInteractWith(EntityPlayer player) {
        return tileFurnace.isUseableByPlayer(player);
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {
        ItemStack itemstack = null;
        Slot slot = (Slot) inventorySlots.get(slotIndex);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (slotIndex == 2) {
                if (!mergeItemStack(itemstack1, 3, 39, true)) return null;
                slot.onSlotChange(itemstack1, itemstack);
            } else if (slotIndex != 1 && slotIndex != 0) {
                FluidStack fluidStack = FluidContainerRegistry.getFluidForFilledItem(itemstack1);
                if (FurnaceRecipes.smelting()
                    .getSmeltingResult(itemstack1) != null) {
                    if (!mergeItemStack(itemstack1, 0, 1, false)) return null;
                } else if (TileEntityFurnace.isItemFuel(itemstack1)
                    || (fluidStack != null && tileFurnace.isValidFuel(fluidStack))) {
                        if (!mergeItemStack(itemstack1, 1, 2, false)) return null;
                    } else if (slotIndex >= 3 && slotIndex < 30) {
                        if (!mergeItemStack(itemstack1, 30, 39, false)) return null;
                    } else if (slotIndex >= 30 && slotIndex < 39) {
                        if (!mergeItemStack(itemstack1, 3, 30, false)) return null;
                    }
            } else if (!mergeItemStack(itemstack1, 3, 39, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
            if (itemstack1.stackSize == itemstack.stackSize) return null;
            slot.onPickupFromSlot(player, itemstack1);
        }
        return itemstack;
    }
}

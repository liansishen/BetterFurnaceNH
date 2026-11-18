package com.hepdd.betterfurnacenh.inventory;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.inventory.SlotFurnace;
import net.minecraft.item.ItemStack;
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

    private static final int ID_COOK_TIME_X100 = 0;
    private static final int ID_BURN_TIME_LOW = 1;
    private static final int ID_ITEM_BURN_TIME_LOW = 2;
    private static final int ID_FLUID_AMOUNT = 3;
    private static final int ID_FLUID_ID = 4;
    private static final int ID_TOP_HOPPER_INSTALLED = 5;
    private static final int ID_BOTTOM_HOPPER_INSTALLED = 6;
    private static final int ID_TOP_HOPPER_ENABLED = 7;
    private static final int ID_BOTTOM_HOPPER_ENABLED = 8;
    private static final int ID_BURN_TIME_HIGH = 9;
    private static final int ID_ITEM_BURN_TIME_HIGH = 10;

    private final TileEntityBFFurnace tileFurnace;

    private int lastCookTimeX100;
    private int lastBurnTimeLow;
    private int lastBurnTimeHigh;
    private int lastItemBurnTimeLow;
    private int lastItemBurnTimeHigh;
    private int lastFluidAmount;
    private int lastFluidID = -1;
    private int lastTopHopperInstalled;
    private int lastBottomHopperInstalled;
    private int lastTopHopperEnabled;
    private int lastBottomHopperEnabled;

    private int clientBurnTimeLow;
    private int clientBurnTimeHigh;
    private int clientItemBurnTimeLow;
    private int clientItemBurnTimeHigh;
    private int clientFluidID = -1;
    private int clientFluidAmount = 0;

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

    public TileEntityBFFurnace getTileFurnace() {
        return tileFurnace;
    }

    public static int toSyncedCookTimeX100(float cookTime) {
        if (cookTime <= 0f || Float.isNaN(cookTime)) return 0;
        int scaled = Math.round(cookTime * 100f);
        return Math.max(0, Math.min(scaled, 32767));
    }

    public static int toSyncedBurnTicks(float ticks) {
        if (ticks <= 0f || Float.isNaN(ticks)) return 0;
        double ceil = Math.ceil(ticks);
        return ceil >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) ceil;
    }

    public static int packLow16(int value) {
        return value & 0xFFFF;
    }

    public static int packHigh16(int value) {
        return (value >>> 16) & 0xFFFF;
    }

    public static int unpackInt32(int high16, int low16) {
        return ((high16 & 0xFFFF) << 16) | (low16 & 0xFFFF);
    }

    @Override
    public void addCraftingToCrafters(ICrafting crafting) {
        super.addCraftingToCrafters(crafting);

        int cookX100 = toSyncedCookTimeX100(tileFurnace.furnaceCookTime);
        int burnTicks = toSyncedBurnTicks(tileFurnace.furnaceBurnTime);
        int burnLow = packLow16(burnTicks);
        int burnHigh = packHigh16(burnTicks);
        int itemBurnTicks = toSyncedBurnTicks(tileFurnace.currentItemBurnTime);
        int itemBurnLow = packLow16(itemBurnTicks);
        int itemBurnHigh = packHigh16(itemBurnTicks);

        FluidStack fluid = tileFurnace.fluidTank.getFluid();
        int fluidID = (fluid != null && fluid.amount > 0) ? fluid.getFluidID() : -1;
        int fluidAmount = (fluid != null && fluid.amount > 0) ? fluid.amount : 0;

        int topInst = tileFurnace.isHopperInstalled(ForgeDirection.UP) ? 1 : 0;
        int botInst = tileFurnace.isHopperInstalled(ForgeDirection.DOWN) ? 1 : 0;
        int topEn = tileFurnace.isHopperEnabled(ForgeDirection.UP) ? 1 : 0;
        int botEn = tileFurnace.isHopperEnabled(ForgeDirection.DOWN) ? 1 : 0;

        crafting.sendProgressBarUpdate(this, ID_COOK_TIME_X100, cookX100);
        crafting.sendProgressBarUpdate(this, ID_BURN_TIME_HIGH, burnHigh);
        crafting.sendProgressBarUpdate(this, ID_BURN_TIME_LOW, burnLow);
        crafting.sendProgressBarUpdate(this, ID_ITEM_BURN_TIME_HIGH, itemBurnHigh);
        crafting.sendProgressBarUpdate(this, ID_ITEM_BURN_TIME_LOW, itemBurnLow);
        crafting.sendProgressBarUpdate(this, ID_FLUID_ID, fluidID);
        crafting.sendProgressBarUpdate(this, ID_FLUID_AMOUNT, fluidAmount);
        crafting.sendProgressBarUpdate(this, ID_TOP_HOPPER_INSTALLED, topInst);
        crafting.sendProgressBarUpdate(this, ID_BOTTOM_HOPPER_INSTALLED, botInst);
        crafting.sendProgressBarUpdate(this, ID_TOP_HOPPER_ENABLED, topEn);
        crafting.sendProgressBarUpdate(this, ID_BOTTOM_HOPPER_ENABLED, botEn);

        lastCookTimeX100 = cookX100;
        lastBurnTimeLow = burnLow;
        lastBurnTimeHigh = burnHigh;
        lastItemBurnTimeLow = itemBurnLow;
        lastItemBurnTimeHigh = itemBurnHigh;
        lastFluidID = fluidID;
        lastFluidAmount = fluidAmount;
        lastTopHopperInstalled = topInst;
        lastBottomHopperInstalled = botInst;
        lastTopHopperEnabled = topEn;
        lastBottomHopperEnabled = botEn;
    }

    @Override
    public void detectAndSendChanges() {
        super.detectAndSendChanges();

        int cookX100 = toSyncedCookTimeX100(tileFurnace.furnaceCookTime);
        int burnTicks = toSyncedBurnTicks(tileFurnace.furnaceBurnTime);
        int burnLow = packLow16(burnTicks);
        int burnHigh = packHigh16(burnTicks);
        int itemBurnTicks = toSyncedBurnTicks(tileFurnace.currentItemBurnTime);
        int itemBurnLow = packLow16(itemBurnTicks);
        int itemBurnHigh = packHigh16(itemBurnTicks);

        FluidStack fluid = tileFurnace.fluidTank.getFluid();
        int fluidID = (fluid != null && fluid.amount > 0) ? fluid.getFluidID() : -1;
        int fluidAmount = (fluid != null && fluid.amount > 0) ? fluid.amount : 0;

        int topInst = tileFurnace.isHopperInstalled(ForgeDirection.UP) ? 1 : 0;
        int botInst = tileFurnace.isHopperInstalled(ForgeDirection.DOWN) ? 1 : 0;
        int topEn = tileFurnace.isHopperEnabled(ForgeDirection.UP) ? 1 : 0;
        int botEn = tileFurnace.isHopperEnabled(ForgeDirection.DOWN) ? 1 : 0;

        for (Object crafter : crafters) {
            ICrafting crafting = (ICrafting) crafter;

            if (lastCookTimeX100 != cookX100) {
                crafting.sendProgressBarUpdate(this, ID_COOK_TIME_X100, cookX100);
            }
            if (lastBurnTimeHigh != burnHigh) {
                crafting.sendProgressBarUpdate(this, ID_BURN_TIME_HIGH, burnHigh);
            }
            if (lastBurnTimeLow != burnLow) {
                crafting.sendProgressBarUpdate(this, ID_BURN_TIME_LOW, burnLow);
            }
            if (lastItemBurnTimeHigh != itemBurnHigh) {
                crafting.sendProgressBarUpdate(this, ID_ITEM_BURN_TIME_HIGH, itemBurnHigh);
            }
            if (lastItemBurnTimeLow != itemBurnLow) {
                crafting.sendProgressBarUpdate(this, ID_ITEM_BURN_TIME_LOW, itemBurnLow);
            }
            if (lastFluidID != fluidID) {
                crafting.sendProgressBarUpdate(this, ID_FLUID_ID, fluidID);
            }
            if (lastFluidAmount != fluidAmount) {
                crafting.sendProgressBarUpdate(this, ID_FLUID_AMOUNT, fluidAmount);
            }
            if (lastTopHopperInstalled != topInst) {
                crafting.sendProgressBarUpdate(this, ID_TOP_HOPPER_INSTALLED, topInst);
            }
            if (lastBottomHopperInstalled != botInst) {
                crafting.sendProgressBarUpdate(this, ID_BOTTOM_HOPPER_INSTALLED, botInst);
            }
            if (lastTopHopperEnabled != topEn) {
                crafting.sendProgressBarUpdate(this, ID_TOP_HOPPER_ENABLED, topEn);
            }
            if (lastBottomHopperEnabled != botEn) {
                crafting.sendProgressBarUpdate(this, ID_BOTTOM_HOPPER_ENABLED, botEn);
            }
        }

        lastCookTimeX100 = cookX100;
        lastBurnTimeLow = burnLow;
        lastBurnTimeHigh = burnHigh;
        lastItemBurnTimeLow = itemBurnLow;
        lastItemBurnTimeHigh = itemBurnHigh;
        lastFluidID = fluidID;
        lastFluidAmount = fluidAmount;
        lastTopHopperInstalled = topInst;
        lastBottomHopperInstalled = botInst;
        lastTopHopperEnabled = topEn;
        lastBottomHopperEnabled = botEn;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void updateProgressBar(int id, int value) {
        switch (id) {
            case ID_COOK_TIME_X100:
                tileFurnace.furnaceCookTime = Math.max(0f, value / 100f);
                break;
            case ID_BURN_TIME_LOW:
                clientBurnTimeLow = value & 0xFFFF;
                tileFurnace.furnaceBurnTime = Math.max(0, unpackInt32(clientBurnTimeHigh, clientBurnTimeLow));
                break;
            case ID_BURN_TIME_HIGH:
                clientBurnTimeHigh = value & 0xFFFF;
                tileFurnace.furnaceBurnTime = Math.max(0, unpackInt32(clientBurnTimeHigh, clientBurnTimeLow));
                break;
            case ID_ITEM_BURN_TIME_LOW:
                clientItemBurnTimeLow = value & 0xFFFF;
                tileFurnace.currentItemBurnTime = Math
                    .max(0, unpackInt32(clientItemBurnTimeHigh, clientItemBurnTimeLow));
                break;
            case ID_ITEM_BURN_TIME_HIGH:
                clientItemBurnTimeHigh = value & 0xFFFF;
                tileFurnace.currentItemBurnTime = Math
                    .max(0, unpackInt32(clientItemBurnTimeHigh, clientItemBurnTimeLow));
                break;
            case ID_FLUID_AMOUNT:
                clientFluidAmount = value & 0xFFFF;
                applyClientFluid();
                break;
            case ID_FLUID_ID:
                clientFluidID = value;
                applyClientFluid();
                break;
            case ID_TOP_HOPPER_INSTALLED:
                tileFurnace.setHopperInstalled(ForgeDirection.UP, value != 0);
                break;
            case ID_BOTTOM_HOPPER_INSTALLED:
                tileFurnace.setHopperInstalled(ForgeDirection.DOWN, value != 0);
                break;
            case ID_TOP_HOPPER_ENABLED:
                tileFurnace.setHopperEnabled(ForgeDirection.UP, value != 0);
                break;
            case ID_BOTTOM_HOPPER_ENABLED:
                tileFurnace.setHopperEnabled(ForgeDirection.DOWN, value != 0);
                break;
        }
    }

    @SideOnly(Side.CLIENT)
    private void applyClientFluid() {
        if (clientFluidID < 0 || clientFluidAmount <= 0) {
            tileFurnace.fluidTank.setFluid(null);
            return;
        }
        Fluid f = FluidRegistry.getFluid(clientFluidID);
        if (f == null) {
            tileFurnace.fluidTank.setFluid(null);
            return;
        }
        FluidStack current = tileFurnace.fluidTank.getFluid();
        if (current == null || current.getFluid() != f) {
            tileFurnace.fluidTank.setFluid(new FluidStack(f, clientFluidAmount));
        } else {
            current.amount = clientFluidAmount;
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
                if (tileFurnace.canSmeltItem(itemstack1)) {
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

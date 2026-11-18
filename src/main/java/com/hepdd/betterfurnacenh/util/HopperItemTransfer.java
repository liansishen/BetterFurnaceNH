package com.hepdd.betterfurnacenh.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public final class HopperItemTransfer {

    private HopperItemTransfer() {}

    public static ItemStack extractOneItem(IInventory inv, ForgeDirection side) {
        if (inv == null) return null;
        ISidedInventory sided = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
        int sideOrd = side.ordinal();
        int[] slots = sided != null ? sided.getAccessibleSlotsFromSide(sideOrd) : null;
        int count = sided != null ? (slots != null ? slots.length : 0) : inv.getSizeInventory();

        for (int i = 0; i < count; i++) {
            int slot = sided != null ? slots[i] : i;
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (sided != null && !sided.canExtractItem(slot, stack, sideOrd)) continue;
            ItemStack extracted = inv.decrStackSize(slot, 1);
            if (extracted != null && extracted.stackSize > 0) {
                inv.markDirty();
                return extracted;
            }
        }
        return null;
    }

    public static boolean insertItem(IInventory inv, ItemStack stack, ForgeDirection side) {
        if (inv == null || stack == null || stack.stackSize <= 0) return false;
        ISidedInventory sided = inv instanceof ISidedInventory ? (ISidedInventory) inv : null;
        int sideOrd = side.ordinal();
        int[] slots = sided != null ? sided.getAccessibleSlotsFromSide(sideOrd) : null;
        int count = sided != null ? (slots != null ? slots.length : 0) : inv.getSizeInventory();

        int remaining = stack.stackSize;
        int invMax = inv.getInventoryStackLimit();
        for (int i = 0; i < count && remaining > 0; i++) {
            int slot = sided != null ? slots[i] : i;
            if (!inv.isItemValidForSlot(slot, stack)) continue;
            if (sided != null && !sided.canInsertItem(slot, stack, sideOrd)) continue;

            ItemStack existing = inv.getStackInSlot(slot);
            if (existing != null) {
                if (!existing.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(existing, stack)) continue;
                int space = Math.min(invMax, existing.getMaxStackSize()) - existing.stackSize;
                if (space <= 0) continue;
                int toAdd = Math.min(remaining, space);
                existing.stackSize += toAdd;
                remaining -= toAdd;
            } else {
                int toAdd = Math.min(remaining, Math.min(invMax, stack.getMaxStackSize()));
                if (toAdd <= 0) continue;
                ItemStack copy = stack.copy();
                copy.stackSize = toAdd;
                inv.setInventorySlotContents(slot, copy);
                remaining -= toAdd;
            }
        }
        if (remaining != stack.stackSize) {
            inv.markDirty();
            stack.stackSize = remaining;
            return true;
        }
        return false;
    }
}

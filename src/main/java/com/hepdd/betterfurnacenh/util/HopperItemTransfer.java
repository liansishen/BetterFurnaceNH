package com.hepdd.betterfurnacenh.util;

import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.ForgeDirection;

public final class HopperItemTransfer {

    private HopperItemTransfer() {}

    public static ItemStack extractOneItem(IInventory inv, ForgeDirection side) {
        if (inv == null) return null;
        int[] slots;
        if (inv instanceof ISidedInventory) {
            slots = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side.ordinal());
        } else {
            slots = new int[inv.getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = i;
        }
        for (int slot : slots) {
            ItemStack stack = inv.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (inv instanceof ISidedInventory) {
                if (!((ISidedInventory) inv).canExtractItem(slot, stack, side.ordinal())) continue;
            }
            ItemStack extracted = inv.decrStackSize(slot, 1);
            if (extracted != null && extracted.stackSize > 0) {
                inv.markDirty();
                return extracted;
            }
        }
        return null;
    }

    public static boolean insertItem(IInventory inv, ItemStack stack, ForgeDirection side) {
        if (inv == null || stack == null) return false;
        int[] slots;
        if (inv instanceof ISidedInventory) {
            slots = ((ISidedInventory) inv).getAccessibleSlotsFromSide(side.ordinal());
        } else {
            slots = new int[inv.getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = i;
        }
        int remaining = stack.stackSize;
        for (int slot : slots) {
            if (remaining <= 0) break;
            if (inv instanceof ISidedInventory) {
                if (!((ISidedInventory) inv).canInsertItem(slot, stack, side.ordinal())) continue;
            }
            ItemStack existing = inv.getStackInSlot(slot);
            int max = inv.getInventoryStackLimit();
            if (existing != null) {
                if (!existing.isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(existing, stack)) continue;
                int space = Math.min(max, existing.getMaxStackSize()) - existing.stackSize;
                if (space <= 0) continue;
                int toAdd = Math.min(remaining, space);
                existing.stackSize += toAdd;
                remaining -= toAdd;
            } else {
                int space = max;
                int toAdd = Math.min(remaining, Math.min(space, stack.getMaxStackSize()));
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

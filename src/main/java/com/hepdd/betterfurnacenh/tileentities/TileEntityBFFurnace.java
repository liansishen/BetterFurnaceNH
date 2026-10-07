package com.hepdd.betterfurnacenh.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.FurnaceRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityFurnace;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTank;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;

import com.hepdd.betterfurnacenh.Config;
import com.hepdd.betterfurnacenh.blocks.BlockBFFurnace;
import com.hepdd.betterfurnacenh.util.HopperItemTransfer;

public class TileEntityBFFurnace extends TileEntity implements ISidedInventory, IFluidHandler {

    private static final int[] slotsTop = new int[] { 0 };
    private static final int[] slotsBottom = new int[] { 2, 1 };
    private static final int[] slotsSides = new int[] { 1 };

    private ItemStack[] inventory = new ItemStack[3];
    public float furnaceBurnTime;
    public float currentItemBurnTime;
    public float furnaceCookTime;
    private String customName;

    public final FluidTank fluidTank;

    private EnumFurnaceTier tier;
    protected float speedMultiplier;

    private int fluidConsumeTimer = 0;
    private float fluidConsumeAccum = 0f;

    private boolean topHopperInstalled = false;
    private boolean bottomHopperInstalled = false;
    private boolean topHopperEnabled = true;
    private boolean bottomHopperEnabled = true;
    private int hopperCooldown = 0;

    public TileEntityBFFurnace() {
        this(EnumFurnaceTier.IRON);
    }

    public TileEntityBFFurnace(EnumFurnaceTier tier) {
        this.tier = tier;
        this.speedMultiplier = tier.getSpeedMultiplier();
        this.fluidTank = new FluidTank(Config.fluidTankCapacity);
    }

    public EnumFurnaceTier getTier() {
        return tier;
    }

    public void setTier(EnumFurnaceTier tier) {
        this.tier = tier;
        this.speedMultiplier = tier.getSpeedMultiplier();
    }

    public float getSpeedMultiplier() {
        return speedMultiplier;
    }

    protected void refreshSpeedMultiplier() {
        speedMultiplier = tier.getSpeedMultiplier();
    }

    public boolean isHopperInstalled(ForgeDirection side) {
        if (side == ForgeDirection.UP) return topHopperInstalled;
        if (side == ForgeDirection.DOWN) return bottomHopperInstalled;
        return false;
    }

    public boolean isHopperEnabled(ForgeDirection side) {
        if (side == ForgeDirection.UP) return topHopperEnabled;
        if (side == ForgeDirection.DOWN) return bottomHopperEnabled;
        return false;
    }

    public void setHopperEnabled(ForgeDirection side, boolean enabled) {
        if (side == ForgeDirection.UP) topHopperEnabled = enabled;
        else if (side == ForgeDirection.DOWN) bottomHopperEnabled = enabled;
    }

    public void setHopperInstalled(ForgeDirection side, boolean installed) {
        if (side == ForgeDirection.UP) topHopperInstalled = installed;
        else if (side == ForgeDirection.DOWN) bottomHopperInstalled = installed;
    }

    public boolean installHopper(ForgeDirection side) {
        if (side == ForgeDirection.UP && !topHopperInstalled) {
            topHopperInstalled = true;
            topHopperEnabled = true;
            return true;
        }
        if (side == ForgeDirection.DOWN && !bottomHopperInstalled) {
            bottomHopperInstalled = true;
            bottomHopperEnabled = true;
            return true;
        }
        return false;
    }

    public boolean removeHopper(ForgeDirection side) {
        if (side == ForgeDirection.UP && topHopperInstalled) {
            topHopperInstalled = false;
            topHopperEnabled = true;
            return true;
        }
        if (side == ForgeDirection.DOWN && bottomHopperInstalled) {
            bottomHopperInstalled = false;
            bottomHopperEnabled = true;
            return true;
        }
        return false;
    }

    public int getInstalledHopperCount() {
        int count = 0;
        if (topHopperInstalled) count++;
        if (bottomHopperInstalled) count++;
        return count;
    }

    protected boolean isSmeltable(ItemStack stack) {
        if (stack == null) return false;
        return FurnaceRecipes.smelting()
            .getSmeltingResult(stack) != null;
    }

    private void tickHopperAutoIO() {
        hopperCooldown++;
        if (hopperCooldown < Config.hopperTransferRate) return;
        hopperCooldown = 0;

        if (topHopperInstalled && topHopperEnabled) {
            doAutoInput();
        }
        if (bottomHopperInstalled && bottomHopperEnabled) {
            doAutoOutput();
        }
    }

    private void doAutoInput() {
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
        if (!(te instanceof IInventory)) return;
        IInventory source = (IInventory) te;
        ForgeDirection extractSide = ForgeDirection.DOWN;

        if (inventory[0] != null && inventory[0].stackSize >= inventory[0].getMaxStackSize()) return;

        int[] slots;
        if (source instanceof ISidedInventory) {
            slots = ((ISidedInventory) source).getAccessibleSlotsFromSide(extractSide.ordinal());
        } else {
            slots = new int[source.getSizeInventory()];
            for (int i = 0; i < slots.length; i++) slots[i] = i;
        }

        for (int slot : slots) {
            ItemStack stack = source.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;
            if (!isSmeltable(stack)) continue;
            if (source instanceof ISidedInventory) {
                if (!((ISidedInventory) source).canExtractItem(slot, stack, extractSide.ordinal())) continue;
            }
            if (inventory[0] != null) {
                if (!inventory[0].isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(inventory[0], stack)) continue;
            }
            ItemStack extracted = source.decrStackSize(slot, 1);
            if (extracted == null || extracted.stackSize <= 0) continue;
            source.markDirty();
            if (inventory[0] == null) {
                inventory[0] = extracted;
            } else {
                inventory[0].stackSize += extracted.stackSize;
            }
            markDirty();
            break;
        }
    }

    private void doAutoOutput() {
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        if (!(te instanceof IInventory)) return;
        IInventory target = (IInventory) te;

        for (int slot : slotsBottom) {
            ItemStack stack = inventory[slot];
            if (stack == null || stack.stackSize <= 0) continue;
            if (!canExtractItem(slot, stack, ForgeDirection.DOWN.ordinal())) continue;
            ItemStack toMove = stack.copy();
            toMove.stackSize = 1;
            if (HopperItemTransfer.insertItem(target, toMove, ForgeDirection.UP)) {
                decrStackSize(slot, 1);
                markDirty();
                return;
            }
        }
    }

    @Override
    public int getSizeInventory() {
        return inventory.length;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return inventory[slot];
    }

    @Override
    public ItemStack decrStackSize(int slot, int amount) {
        if (inventory[slot] != null) {
            if (inventory[slot].stackSize <= amount) {
                ItemStack stack = inventory[slot];
                inventory[slot] = null;
                return stack;
            }
            ItemStack stack = inventory[slot].splitStack(amount);
            if (inventory[slot].stackSize == 0) {
                inventory[slot] = null;
            }
            return stack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int slot) {
        if (inventory[slot] != null) {
            ItemStack stack = inventory[slot];
            inventory[slot] = null;
            return stack;
        }
        return null;
    }

    @Override
    public void setInventorySlotContents(int slot, ItemStack stack) {
        inventory[slot] = stack;
        if (stack != null && stack.stackSize > getInventoryStackLimit()) {
            stack.stackSize = getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return hasCustomInventoryName() ? customName : getDefaultInventoryName();
    }

    protected String getDefaultInventoryName() {
        return tier.getContainerName();
    }

    @Override
    public boolean hasCustomInventoryName() {
        return customName != null && customName.length() > 0;
    }

    public void setCustomName(String name) {
        customName = name;
    }

    @Override
    public int getInventoryStackLimit() {
        return 64;
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer player) {
        return worldObj.getTileEntity(xCoord, yCoord, zCoord) == this
            && player.getDistanceSq(xCoord + 0.5D, yCoord + 0.5D, zCoord + 0.5D) <= 64.0D;
    }

    @Override
    public void openInventory() {}

    @Override
    public void closeInventory() {}

    @Override
    public boolean isItemValidForSlot(int slot, ItemStack stack) {
        if (slot == 2) return false;
        if (slot == 1) {
            if (TileEntityFurnace.isItemFuel(stack)) return true;
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(stack);
            return fluid != null && isValidFuel(fluid);
        }
        return true;
    }

    @Override
    public int[] getAccessibleSlotsFromSide(int side) {
        switch (side) {
            case 0:
                return slotsBottom;
            case 1:
                return slotsTop;
            default:
                return slotsSides;
        }
    }

    @Override
    public boolean canInsertItem(int slot, ItemStack stack, int side) {
        return isItemValidForSlot(slot, stack);
    }

    @Override
    public boolean canExtractItem(int slot, ItemStack stack, int side) {
        if (side == 0 && slot == 1) {
            return FluidContainerRegistry.isEmptyContainer(stack) && !FluidContainerRegistry.isFilledContainer(stack);
        }
        return true;
    }

    public boolean isBurning() {
        return furnaceBurnTime > 0f;
    }

    public int getCookProgressScaled(int scale) {
        return (int) (furnaceCookTime * scale / 200f);
    }

    public int getBurnTimeRemainingScaled(int scale) {
        if (currentItemBurnTime <= 0f) {
            currentItemBurnTime = 200f;
        }
        return (int) (furnaceBurnTime * scale / currentItemBurnTime);
    }

    @Override
    public void updateEntity() {
        boolean wasBurning = furnaceBurnTime > 0f;
        boolean changed = false;

        if (!worldObj.isRemote) {
            refreshSpeedMultiplier();
            if (fluidTank.getCapacity() != Config.fluidTankCapacity) {
                fluidTank.setCapacity(Config.fluidTankCapacity);
            }

            if (furnaceBurnTime > 0f) {
                furnaceBurnTime -= speedMultiplier;
            }

            fluidConsumeTimer++;
            if (fluidConsumeTimer >= 20) {
                fluidConsumeTimer = 0;
                if (canSmelt() && furnaceBurnTime < 400f) {
                    tryConsumeFluidFuel();
                }
            }

            if (tryFillFuelContainer()) {
                changed = true;
            }

            if (furnaceBurnTime <= 0f && canSmelt()) {
                if (inventory[1] != null) {
                    int solidBurn = TileEntityFurnace.getItemBurnTime(inventory[1]);
                    furnaceBurnTime = solidBurn;
                    currentItemBurnTime = solidBurn;
                    if (furnaceBurnTime > 0f) {
                        changed = true;
                        inventory[1].stackSize--;
                        if (inventory[1].stackSize == 0) {
                            inventory[1] = inventory[1].getItem()
                                .getContainerItem(inventory[1]);
                        }
                    }
                }
            }

            if (isBurning() && canSmelt()) {
                furnaceCookTime += speedMultiplier;
                if (furnaceCookTime >= 200f) {
                    furnaceCookTime -= 200f;
                    smeltItem();
                    changed = true;
                }
            } else {
                furnaceCookTime = 0f;
            }

            if (wasBurning != (furnaceBurnTime > 0f)) {
                changed = true;
                updateBlockState(furnaceBurnTime > 0f);
            }

            tickHopperAutoIO();
        }

        if (changed) {
            markDirty();
        }
    }

    private boolean tryFillFuelContainer() {
        ItemStack fuel = inventory[1];
        if (fuel == null) return false;
        FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(fuel);
        if (fluid == null || !isValidFuel(fluid) || fluidTank.fill(fluid, false) != fluid.amount) return false;

        ItemStack empty = FluidContainerRegistry.drainFluidContainer(fuel);
        if (fuel.stackSize > 1 && empty != null) {
            ItemStack output = inventory[2];
            int limit = Math.min(getInventoryStackLimit(), empty.getMaxStackSize());
            if (output != null) {
                if (!output.isItemEqual(empty) || !ItemStack.areItemStackTagsEqual(output, empty)) return false;
                if (output.stackSize + empty.stackSize > limit) return false;
            } else if (empty.stackSize > limit) {
                return false;
            }
        }

        fluidTank.fill(fluid, true);
        fuel.stackSize--;
        if (fuel.stackSize == 0) {
            inventory[1] = empty;
        } else if (empty != null) {
            if (inventory[2] == null) {
                inventory[2] = empty;
            } else {
                inventory[2].stackSize += empty.stackSize;
            }
        }
        return true;
    }

    private void tryConsumeFluidFuel() {
        FluidStack fluid = fluidTank.getFluid();
        if (fluid == null || fluid.amount <= 0) return;

        int burnPerBucket = 0;
        Fluid lava = FluidRegistry.LAVA;
        Fluid creosote = FluidRegistry.getFluid("creosote");

        if (lava != null && fluid.isFluidEqual(new FluidStack(lava, 1))) {
            burnPerBucket = Config.lavaBurnPerBucket;
        } else if (creosote != null && fluid.isFluidEqual(new FluidStack(creosote, 1))) {
            burnPerBucket = Config.creosoteBurnPerBucket;
        }

        if (burnPerBucket <= 0) return;

        float ideal = 20f * speedMultiplier * 1000f / burnPerBucket;
        fluidConsumeAccum += ideal;
        int consume = (int) fluidConsumeAccum;
        if (consume <= 0) return;

        if (fluid.amount < consume) consume = fluid.amount;
        fluidTank.drain(consume, true);
        fluidConsumeAccum -= consume;

        currentItemBurnTime = 20f * speedMultiplier;
        furnaceBurnTime += currentItemBurnTime;
    }

    public boolean canSmelt() {
        if (inventory[0] == null) return false;
        ItemStack result = FurnaceRecipes.smelting()
            .getSmeltingResult(inventory[0]);
        if (result == null) return false;
        if (inventory[2] == null) return true;
        if (!inventory[2].isItemEqual(result)) return false;
        int newSize = inventory[2].stackSize + result.stackSize;
        return newSize <= getInventoryStackLimit() && newSize <= inventory[2].getMaxStackSize();
    }

    public void smeltItem() {
        if (!canSmelt()) return;
        ItemStack result = FurnaceRecipes.smelting()
            .getSmeltingResult(inventory[0]);
        if (inventory[2] == null) {
            inventory[2] = result.copy();
        } else if (inventory[2].getItem() == result.getItem()) {
            inventory[2].stackSize += result.stackSize;
        }
        inventory[0].stackSize--;
        if (inventory[0].stackSize <= 0) {
            inventory[0] = null;
        }
    }

    protected void updateBlockState(boolean burning) {
        BlockBFFurnace.updateFurnaceBlockState(burning, worldObj, xCoord, yCoord, zCoord);
    }

    public boolean isValidFuel(FluidStack fluid) {
        Fluid lava = FluidRegistry.LAVA;
        Fluid creosote = FluidRegistry.getFluid("creosote");
        if (fluid == null) return false;
        if (lava != null && fluid.isFluidEqual(new FluidStack(lava, 1))) return true;
        if (creosote != null && fluid.isFluidEqual(new FluidStack(creosote, 1))) return true;
        return false;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (!isValidFuel(resource)) return 0;
        return fluidTank.fill(resource, doFill);
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return isValidFuel(new FluidStack(fluid, 1));
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { fluidTank.getInfo() };
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        NBTTagList items = nbt.getTagList("Items", 10);
        inventory = new ItemStack[getSizeInventory()];
        for (int i = 0; i < items.tagCount(); i++) {
            NBTTagCompound itemTag = items.getCompoundTagAt(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < inventory.length) {
                inventory[slot] = ItemStack.loadItemStackFromNBT(itemTag);
            }
        }
        furnaceBurnTime = nbt.getShort("BurnTime");
        furnaceCookTime = nbt.getShort("CookTime");
        currentItemBurnTime = TileEntityFurnace.getItemBurnTime(inventory[1]);

        int tierOrdinal = nbt.getInteger("Tier");
        if (tierOrdinal >= 0 && tierOrdinal < EnumFurnaceTier.values().length) {
            tier = EnumFurnaceTier.values()[tierOrdinal];
            speedMultiplier = tier.getSpeedMultiplier();
        }

        if (nbt.hasKey("CustomName", 8)) {
            customName = nbt.getString("CustomName");
        }

        fluidTank.setCapacity(Config.fluidTankCapacity);
        if (nbt.hasKey("Fluid")) {
            fluidTank.readFromNBT(nbt.getCompoundTag("Fluid"));
        }

        topHopperInstalled = nbt.getBoolean("TopHopper");
        bottomHopperInstalled = nbt.getBoolean("BottomHopper");
        topHopperEnabled = nbt.getBoolean("TopHopperEn");
        bottomHopperEnabled = nbt.getBoolean("BottomHopperEn");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setShort("BurnTime", (short) furnaceBurnTime);
        nbt.setShort("CookTime", (short) furnaceCookTime);
        nbt.setInteger("Tier", tier.ordinal());

        NBTTagList items = new NBTTagList();
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] != null) {
                NBTTagCompound itemTag = new NBTTagCompound();
                itemTag.setByte("Slot", (byte) i);
                inventory[i].writeToNBT(itemTag);
                items.appendTag(itemTag);
            }
        }
        nbt.setTag("Items", items);

        if (hasCustomInventoryName()) {
            nbt.setString("CustomName", customName);
        }

        NBTTagCompound fluidNbt = new NBTTagCompound();
        fluidTank.writeToNBT(fluidNbt);
        nbt.setTag("Fluid", fluidNbt);

        nbt.setBoolean("TopHopper", topHopperInstalled);
        nbt.setBoolean("BottomHopper", bottomHopperInstalled);
        nbt.setBoolean("TopHopperEn", topHopperEnabled);
        nbt.setBoolean("BottomHopperEn", bottomHopperEnabled);
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        readFromNBT(pkt.func_148857_g());
    }
}

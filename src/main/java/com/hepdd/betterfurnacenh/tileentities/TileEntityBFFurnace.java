package com.hepdd.betterfurnacenh.tileentities;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.Item;
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

    private static Fluid cachedCreosote;
    private static boolean creosoteLookupDone = false;

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

    private boolean inputRecipeDirty = true;
    private Item cachedInputItem;
    private int cachedInputMeta;
    private NBTTagCompound cachedInputTagRef;
    private ItemStack cachedSmeltResult;

    private boolean fuelCacheDirty = true;
    private Item cachedFuelItem;
    private int cachedFuelMeta;
    private NBTTagCompound cachedFuelTagRef;
    private FluidStack cachedFuelFluid;

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
        refreshSpeedMultiplier();
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

    protected ItemStack lookupSmeltingResult(ItemStack stack) {
        if (stack == null) return null;
        return FurnaceRecipes.smelting()
            .getSmeltingResult(stack);
    }

    protected boolean isSmeltable(ItemStack stack) {
        return stack != null && lookupSmeltingResult(stack) != null;
    }

    public boolean canSmeltItem(ItemStack stack) {
        return isSmeltable(stack);
    }

    protected ItemStack getCachedSmeltingResult() {
        ItemStack input = inventory[0];
        if (input == null || input.stackSize <= 0) {
            cachedInputItem = null;
            cachedInputTagRef = null;
            cachedSmeltResult = null;
            inputRecipeDirty = false;
            return null;
        }
        Item item = input.getItem();
        int meta = input.getItemDamage();
        NBTTagCompound tagRef = input.getTagCompound();
        if (inputRecipeDirty || item != cachedInputItem || meta != cachedInputMeta || tagRef != cachedInputTagRef) {
            cachedInputItem = item;
            cachedInputMeta = meta;
            cachedInputTagRef = tagRef;
            cachedSmeltResult = lookupSmeltingResult(input);
            inputRecipeDirty = false;
        }
        return cachedSmeltResult;
    }

    private FluidStack getCachedFuelContainerFluid() {
        ItemStack fuel = inventory[1];
        if (fuel == null || fuel.stackSize <= 0) {
            cachedFuelItem = null;
            cachedFuelTagRef = null;
            cachedFuelFluid = null;
            fuelCacheDirty = false;
            return null;
        }
        Item item = fuel.getItem();
        int meta = fuel.getItemDamage();
        NBTTagCompound tagRef = fuel.getTagCompound();
        if (fuelCacheDirty || item != cachedFuelItem || meta != cachedFuelMeta || tagRef != cachedFuelTagRef) {
            cachedFuelItem = item;
            cachedFuelMeta = meta;
            cachedFuelTagRef = tagRef;
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(fuel);
            cachedFuelFluid = (fluid != null && isValidFuel(fluid)) ? fluid : null;
            fuelCacheDirty = false;
        }
        return cachedFuelFluid;
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
        if (yCoord + 1 >= worldObj.getHeight()) return;
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord + 1, zCoord);
        if (!(te instanceof IInventory)) return;
        IInventory source = (IInventory) te;
        ISidedInventory sided = source instanceof ISidedInventory ? (ISidedInventory) source : null;
        int extractSide = ForgeDirection.DOWN.ordinal();

        int remainingQuota = Math.max(1, Config.hopperItemsPerTransfer);
        if (inventory[0] != null) {
            if (getCachedSmeltingResult() == null) return;
            int maxStack = Math.min(getInventoryStackLimit(), inventory[0].getMaxStackSize());
            int space = maxStack - inventory[0].stackSize;
            if (space <= 0) return;
            remainingQuota = Math.min(remainingQuota, space);
        }

        int[] slots = sided != null ? sided.getAccessibleSlotsFromSide(extractSide) : null;
        int count = sided != null ? (slots != null ? slots.length : 0) : source.getSizeInventory();

        boolean transferred = false;
        Item lastRejectedItem = null;
        int lastRejectedMeta = -1;

        for (int i = 0; i < count && remainingQuota > 0; i++) {
            int slot = sided != null ? slots[i] : i;
            ItemStack stack = source.getStackInSlot(slot);
            if (stack == null || stack.stackSize <= 0) continue;

            if (inventory[0] != null) {
                if (!inventory[0].isItemEqual(stack) || !ItemStack.areItemStackTagsEqual(inventory[0], stack)) {
                    continue;
                }
                if (sided != null && !sided.canExtractItem(slot, stack, extractSide)) continue;
            } else {
                if (sided != null && !sided.canExtractItem(slot, stack, extractSide)) continue;
                if (stack.getTagCompound() == null && stack.getItem() == lastRejectedItem
                    && stack.getItemDamage() == lastRejectedMeta) {
                    continue;
                }
                if (!isSmeltable(stack)) {
                    if (stack.getTagCompound() == null) {
                        lastRejectedItem = stack.getItem();
                        lastRejectedMeta = stack.getItemDamage();
                    }
                    continue;
                }
                int maxStack = Math.min(getInventoryStackLimit(), stack.getMaxStackSize());
                remainingQuota = Math.min(remainingQuota, maxStack);
            }

            int toExtract = Math.min(remainingQuota, stack.stackSize);
            if (toExtract <= 0) break;

            ItemStack extracted = source.decrStackSize(slot, toExtract);
            if (extracted == null || extracted.stackSize <= 0) continue;

            if (inventory[0] == null) {
                inventory[0] = extracted;
                inputRecipeDirty = true;
                int maxStack = Math.min(getInventoryStackLimit(), inventory[0].getMaxStackSize());
                remainingQuota = Math.min(remainingQuota - extracted.stackSize, maxStack - inventory[0].stackSize);
            } else {
                inventory[0].stackSize += extracted.stackSize;
                remainingQuota -= extracted.stackSize;
            }
            transferred = true;
        }

        if (transferred) {
            source.markDirty();
            markDirty();
        }
    }

    private void doAutoOutput() {
        if (yCoord <= 0) return;
        TileEntity te = worldObj.getTileEntity(xCoord, yCoord - 1, zCoord);
        if (!(te instanceof IInventory)) return;
        IInventory target = (IInventory) te;

        int remainingQuota = Math.max(1, Config.hopperItemsPerTransfer);
        boolean transferred = false;

        for (int slot : slotsBottom) {
            if (remainingQuota <= 0) break;
            ItemStack stack = inventory[slot];
            if (stack == null || stack.stackSize <= 0) continue;
            if (!canExtractItem(slot, stack, ForgeDirection.DOWN.ordinal())) continue;

            int tryCount = Math.min(remainingQuota, stack.stackSize);
            ItemStack toMove = stack.copy();
            toMove.stackSize = tryCount;
            if (HopperItemTransfer.insertItem(target, toMove, ForgeDirection.UP)) {
                int moved = tryCount - toMove.stackSize;
                if (moved > 0) {
                    decrStackSize(slot, moved);
                    remainingQuota -= moved;
                    transferred = true;
                }
            }
        }

        if (transferred) {
            markDirty();
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
            if (slot == 0) inputRecipeDirty = true;
            else if (slot == 1) fuelCacheDirty = true;

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
            if (slot == 0) inputRecipeDirty = true;
            else if (slot == 1) fuelCacheDirty = true;

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
        if (slot == 0) inputRecipeDirty = true;
        else if (slot == 1) fuelCacheDirty = true;
    }

    @Override
    public void markDirty() {
        inputRecipeDirty = true;
        fuelCacheDirty = true;
        super.markDirty();
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

    public static int computeCookProgressScaled(float cookTime, int scale) {
        if (cookTime <= 0f || scale <= 0) return 0;
        int scaled = (int) (cookTime * scale / 200f);
        return Math.max(0, Math.min(scaled, scale));
    }

    public static int computeBurnTimeScaled(float burnTime, float itemBurnTime, int scale) {
        if (burnTime <= 0f || scale <= 0) return 0;
        float denom = itemBurnTime > 0f ? itemBurnTime : burnTime;
        int scaled = (int) (burnTime * scale / denom);
        return Math.max(0, Math.min(scaled, scale));
    }

    public int getCookProgressScaled(int scale) {
        return computeCookProgressScaled(furnaceCookTime, scale);
    }

    public int getBurnTimeRemainingScaled(int scale) {
        return computeBurnTimeScaled(furnaceBurnTime, currentItemBurnTime, scale);
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
                furnaceBurnTime = Math.max(0f, furnaceBurnTime - speedMultiplier);
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
                        fuelCacheDirty = true;
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
        if (fuel == null || fuel.stackSize <= 0) return false;
        FluidStack fluid = getCachedFuelContainerFluid();
        if (fluid == null) return false;
        if (fluidTank.getFluidAmount() + fluid.amount > fluidTank.getCapacity()) return false;
        if (fluidTank.fill(fluid, false) != fluid.amount) return false;

        ItemStack singleFuel = fuel.copy();
        singleFuel.stackSize = 1;
        ItemStack empty = FluidContainerRegistry.drainFluidContainer(singleFuel);
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
        if (fuel.stackSize <= 0) {
            inventory[1] = empty;
        } else if (empty != null) {
            if (inventory[2] == null) {
                inventory[2] = empty;
            } else {
                inventory[2].stackSize += empty.stackSize;
            }
        }
        fuelCacheDirty = true;
        return true;
    }

    private void tryConsumeFluidFuel() {
        FluidStack fluid = fluidTank.getFluid();
        if (fluid == null || fluid.amount <= 0) return;

        int burnPerBucket = 0;
        Fluid fluidType = fluid.getFluid();
        if (fluidType == FluidRegistry.LAVA) {
            burnPerBucket = Config.lavaBurnPerBucket;
        } else {
            Fluid creosote = getCreosoteFluid();
            if (creosote != null && fluidType == creosote) {
                burnPerBucket = Config.creosoteBurnPerBucket;
            }
        }

        if (burnPerBucket <= 0) return;

        float ideal = 20f * speedMultiplier * 1000f / burnPerBucket;
        fluidConsumeAccum += ideal;
        int consume = (int) fluidConsumeAccum;
        if (consume <= 0) return;

        if (fluid.amount < consume) consume = fluid.amount;
        fluidTank.drain(consume, true);
        fluidConsumeAccum -= consume;

        float addedBurn = 20f * speedMultiplier;
        furnaceBurnTime += addedBurn;
        if (furnaceBurnTime > currentItemBurnTime) {
            currentItemBurnTime = furnaceBurnTime;
        } else if (currentItemBurnTime <= 0f) {
            currentItemBurnTime = addedBurn;
        }
    }

    public boolean canSmelt() {
        ItemStack result = getCachedSmeltingResult();
        if (result == null) return false;
        ItemStack output = inventory[2];
        if (output == null) return true;
        if (!output.isItemEqual(result) || !ItemStack.areItemStackTagsEqual(output, result)) return false;
        int newSize = output.stackSize + result.stackSize;
        return newSize <= getInventoryStackLimit() && newSize <= output.getMaxStackSize();
    }

    public void smeltItem() {
        if (!canSmelt()) return;
        ItemStack result = getCachedSmeltingResult();
        if (result == null) return;
        if (inventory[2] == null) {
            inventory[2] = result.copy();
        } else {
            inventory[2].stackSize += result.stackSize;
        }
        decrStackSize(0, 1);
    }

    protected void updateBlockState(boolean burning) {
        BlockBFFurnace.updateFurnaceBlockState(burning, worldObj, xCoord, yCoord, zCoord);
    }

    private static Fluid getCreosoteFluid() {
        if (!creosoteLookupDone) {
            cachedCreosote = FluidRegistry.getFluid("creosote");
            creosoteLookupDone = true;
        }
        return cachedCreosote;
    }

    public boolean isValidFuel(Fluid fluid) {
        if (fluid == null) return false;
        if (fluid == FluidRegistry.LAVA) return true;
        Fluid creosote = getCreosoteFluid();
        return creosote != null && fluid == creosote;
    }

    public boolean isValidFuel(FluidStack fluid) {
        return fluid != null && isValidFuel(fluid.getFluid());
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
        return isValidFuel(fluid);
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return false;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return new FluidTankInfo[] { fluidTank.getInfo() };
    }

    public static float readCompatibleNonNegativeFloat(NBTTagCompound nbt, String key) {
        if (nbt == null) return 0f;
        if (nbt.hasKey(key, 5)) {
            float val = nbt.getFloat(key);
            return Float.isNaN(val) ? 0f : Math.max(0f, val);
        }
        if (nbt.hasKey(key, 99)) {
            return Math.max(0f, nbt.getShort(key));
        }
        return 0f;
    }

    public static float resolveCompatibleItemBurnTime(NBTTagCompound nbt, float burnTime, int fallbackSlotBurnTime) {
        if (nbt != null && nbt.hasKey("ItemBurnTime", 99)) {
            float saved = nbt.getFloat("ItemBurnTime");
            if (Float.isNaN(saved) || saved < 0f) saved = 0f;
            if (burnTime > 0f && saved < burnTime) {
                return burnTime;
            }
            return saved;
        }
        int safeSlotBurn = Math.max(0, fallbackSlotBurnTime);
        if (burnTime <= 0f) {
            return safeSlotBurn;
        }
        return safeSlotBurn >= burnTime ? safeSlotBurn : Math.max(burnTime, 200f);
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
        inputRecipeDirty = true;
        fuelCacheDirty = true;

        furnaceBurnTime = readCompatibleNonNegativeFloat(nbt, "BurnTime");
        furnaceCookTime = readCompatibleNonNegativeFloat(nbt, "CookTime");
        int fallbackSlotBurn = inventory[1] != null ? TileEntityFurnace.getItemBurnTime(inventory[1]) : 0;
        currentItemBurnTime = resolveCompatibleItemBurnTime(nbt, furnaceBurnTime, fallbackSlotBurn);
        fluidConsumeAccum = readCompatibleNonNegativeFloat(nbt, "FluidConsumeAccum");

        int tierOrdinal = nbt.getInteger("Tier");
        if (tierOrdinal >= 0 && tierOrdinal < EnumFurnaceTier.VALUES.length) {
            tier = EnumFurnaceTier.VALUES[tierOrdinal];
            refreshSpeedMultiplier();
        }

        if (nbt.hasKey("CustomName", 8)) {
            customName = nbt.getString("CustomName");
        }

        fluidTank.setCapacity(Config.fluidTankCapacity);
        if (nbt.hasKey("Fluid", 10)) {
            fluidTank.readFromNBT(nbt.getCompoundTag("Fluid"));
        }

        topHopperInstalled = nbt.getBoolean("TopHopper");
        bottomHopperInstalled = nbt.getBoolean("BottomHopper");
        topHopperEnabled = !nbt.hasKey("TopHopperEn") || nbt.getBoolean("TopHopperEn");
        bottomHopperEnabled = !nbt.hasKey("BottomHopperEn") || nbt.getBoolean("BottomHopperEn");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setFloat("BurnTime", furnaceBurnTime);
        nbt.setFloat("CookTime", furnaceCookTime);
        nbt.setFloat("ItemBurnTime", currentItemBurnTime);
        nbt.setFloat("FluidConsumeAccum", fluidConsumeAccum);
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
        nbt.setBoolean("TopHopper", topHopperInstalled);
        nbt.setBoolean("BottomHopper", bottomHopperInstalled);
        if (hasCustomInventoryName()) {
            nbt.setString("CustomName", customName);
        }
        return new S35PacketUpdateTileEntity(xCoord, yCoord, zCoord, 1, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        NBTTagCompound nbt = pkt.func_148857_g();
        if (nbt == null) return;
        topHopperInstalled = nbt.getBoolean("TopHopper");
        bottomHopperInstalled = nbt.getBoolean("BottomHopper");
        if (nbt.hasKey("CustomName", 8)) {
            customName = nbt.getString("CustomName");
        }
    }
}

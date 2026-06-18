package com.hepdd.betterfurnacenh.blocks;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidStack;

import com.hepdd.betterfurnacenh.BFNH;
import com.hepdd.betterfurnacenh.client.overlay.GridMath;
import com.hepdd.betterfurnacenh.tileentities.EnumFurnaceTier;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFBlastFurnace;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import gregtech.api.GregTechAPI;
import gregtech.api.util.GTUtility;

public class BlockBFFurnace extends BlockContainer {

    private final boolean isBurning;
    private final EnumFurnaceTier tier;
    private final boolean isBlastFurnace;
    private static boolean keepInventory = false;

    private static final Map<Block, Block> IDLE_TO_LIT = new HashMap<>();
    private static final Map<Block, Block> LIT_TO_IDLE = new HashMap<>();

    public static void registerBlockPair(Block idle, Block lit) {
        IDLE_TO_LIT.put(idle, lit);
        LIT_TO_IDLE.put(lit, idle);
    }

    @SideOnly(Side.CLIENT)
    private IIcon iconTop;
    @SideOnly(Side.CLIENT)
    private IIcon iconFront;
    @SideOnly(Side.CLIENT)
    private IIcon iconSide;
    @SideOnly(Side.CLIENT)
    private IIcon iconBottom;

    public BlockBFFurnace(boolean isBurning, EnumFurnaceTier tier, boolean isBlastFurnace) {
        super(Material.rock);
        this.isBurning = isBurning;
        this.tier = tier;
        this.isBlastFurnace = isBlastFurnace;
        String name = "betterfurnacenh_" + tier.name()
            .toLowerCase() + (isBlastFurnace ? "_blast" : "") + "_furnace" + (isBurning ? "_lit" : "");
        setBlockName(name);
        setHardness(3.5F);
        setStepSound(soundTypePiston);
        if (isBurning) {
            setLightLevel(0.875F);
        }
    }

    @Override
    public TileEntity createNewTileEntity(World world, int meta) {
        if (isBlastFurnace) {
            return new TileEntityBFBlastFurnace(tier);
        }
        return new TileEntityBFFurnace(tier);
    }

    @Override
    public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int side, float hitX,
        float hitY, float hitZ) {
        TileEntity te = world.getTileEntity(x, y, z);
        if (!(te instanceof TileEntityBFFurnace)) return false;
        TileEntityBFFurnace furnace = (TileEntityBFFurnace) te;

        ItemStack held = player.getHeldItem();

        if (held != null && GTUtility.isStackInList(held, GregTechAPI.sCrowbarList)) {
            ForgeDirection targetSide = GridMath
                .determineWrenchingSide(ForgeDirection.getOrientation(side), hitX, hitY, hitZ);
            if (targetSide == ForgeDirection.UP || targetSide == ForgeDirection.DOWN) {
                if (furnace.isHopperInstalled(targetSide)) {
                    if (!world.isRemote) {
                        furnace.removeHopper(targetSide);
                        furnace.markDirty();
                        world.markBlockForUpdate(x, y, z);
                        ItemStack hopperDrop = new ItemStack(BFNH.hopperUpgrade, 1);
                        if (!player.inventory.addItemStackToInventory(hopperDrop)) {
                            player.dropPlayerItemWithRandomChoice(hopperDrop, false);
                        }
                    }
                    return true;
                }
            }
        }

        if (held != null) {
            FluidStack fluid = FluidContainerRegistry.getFluidForFilledItem(held);
            if (fluid != null && furnace.isValidFuel(fluid)) {
                int filled = furnace.fill(null, fluid, false);
                if (filled > 0) {
                    furnace.fill(null, fluid, true);
                    if (!player.capabilities.isCreativeMode) {
                        ItemStack empty = held.getItem()
                            .getContainerItem(held);
                        held.stackSize--;
                        if (held.stackSize <= 0) {
                            player.inventory.setInventorySlotContents(player.inventory.currentItem, empty);
                        } else if (empty != null) {
                            if (!player.inventory.addItemStackToInventory(empty)) {
                                player.dropPlayerItemWithRandomChoice(empty, false);
                            }
                        }
                    }
                    return true;
                }
            }
        }

        if (!world.isRemote) {
            player.openGui(BFNH.instance, 0, world, x, y, z);
        }
        return true;
    }

    @Override
    public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase placer, ItemStack stack) {
        int dir = MathHelper.floor_double((double) (placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
        int meta = (dir + 2) & 3;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);

        if (stack.hasDisplayName()) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityBFFurnace) {
                ((TileEntityBFFurnace) te).setCustomName(stack.getDisplayName());
            }
        }
    }

    @Override
    public void breakBlock(World world, int x, int y, int z, Block block, int meta) {
        if (!keepInventory) {
            TileEntity te = world.getTileEntity(x, y, z);
            if (te instanceof TileEntityBFFurnace) {
                TileEntityBFFurnace furnace = (TileEntityBFFurnace) te;
                for (int i = 0; i < furnace.getSizeInventory(); i++) {
                    ItemStack stack = furnace.getStackInSlot(i);
                    if (stack != null) {
                        float fx = world.rand.nextFloat() * 0.8F + 0.1F;
                        float fy = world.rand.nextFloat() * 0.8F + 0.1F;
                        float fz = world.rand.nextFloat() * 0.8F + 0.1F;
                        EntityItem entity = new EntityItem(world, x + fx, y + fy, z + fz, stack.copy());
                        entity.motionX = world.rand.nextGaussian() * 0.05F;
                        entity.motionY = world.rand.nextGaussian() * 0.05F + 0.2F;
                        entity.motionZ = world.rand.nextGaussian() * 0.05F;
                        world.spawnEntityInWorld(entity);
                    }
                }
                int hopperCount = furnace.getInstalledHopperCount();
                for (int i = 0; i < hopperCount; i++) {
                    ItemStack hopperStack = new ItemStack(BFNH.hopperUpgrade, 1);
                    float fx = world.rand.nextFloat() * 0.8F + 0.1F;
                    float fy = world.rand.nextFloat() * 0.8F + 0.1F;
                    float fz = world.rand.nextFloat() * 0.8F + 0.1F;
                    EntityItem entity = new EntityItem(world, x + fx, y + fy, z + fz, hopperStack);
                    entity.motionX = world.rand.nextGaussian() * 0.05F;
                    entity.motionY = world.rand.nextGaussian() * 0.05F + 0.2F;
                    entity.motionZ = world.rand.nextGaussian() * 0.05F;
                    world.spawnEntityInWorld(entity);
                }
            }
        }
        super.breakBlock(world, x, y, z, block, meta);
    }

    public static void updateFurnaceBlockState(boolean burning, World world, int x, int y, int z) {
        Block currentBlock = world.getBlock(x, y, z);
        Block targetBlock = burning ? IDLE_TO_LIT.get(currentBlock) : LIT_TO_IDLE.get(currentBlock);
        if (targetBlock == null) return;

        int meta = world.getBlockMetadata(x, y, z);
        TileEntity te = world.getTileEntity(x, y, z);
        keepInventory = true;
        world.setBlock(x, y, z, targetBlock);
        keepInventory = false;
        world.setBlockMetadataWithNotify(x, y, z, meta, 2);
        if (te != null) {
            te.validate();
            world.setTileEntity(x, y, z, te);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void randomDisplayTick(World world, int x, int y, int z, Random rand) {
        if (!isBurning) return;
        int meta = world.getBlockMetadata(x, y, z);
        int[] vanillaDirs = { 3, 4, 2, 5 };
        int dir = vanillaDirs[meta % vanillaDirs.length];
        float fx = x + 0.5F;
        float fy = y + rand.nextFloat() * 6.0F / 16.0F;
        float fz = z + 0.5F;
        float f3 = 0.52F;
        float f4 = rand.nextFloat() * 0.6F - 0.3F;

        if (dir == 4) {
            fx -= f3;
            fz += f4;
        } else if (dir == 5) {
            fx += f3;
            fz += f4;
        } else if (dir == 2) {
            fx += f4;
            fz -= f3;
        } else if (dir == 3) {
            fx += f4;
            fz += f3;
        }

        world.spawnParticle("smoke", fx, fy, fz, 0.0D, 0.0D, 0.0D);
        world.spawnParticle("flame", fx, fy, fz, 0.0D, 0.0D, 0.0D);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIcon(int side, int meta) {
        if (side == 1) return iconTop;
        if (side == 0) return iconBottom;
        if (meta == 0 && side == 3) return iconFront;
        if (meta == 1 && side == 4) return iconFront;
        if (meta == 2 && side == 2) return iconFront;
        if (meta == 3 && side == 5) return iconFront;
        return iconSide;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerBlockIcons(IIconRegister iconRegister) {
        String prefix = "betterfurnacenh:";
        String suffix = isBlastFurnace ? "_blast" : "";
        String tierName = tier.name()
            .toLowerCase();

        iconTop = iconRegister.registerIcon(prefix + tierName + suffix + "_top");
        iconFront = iconRegister.registerIcon(prefix + tierName + suffix + "_front" + (isBurning ? "_on" : "_off"));
        iconSide = iconRegister.registerIcon(prefix + tierName + suffix + "_side");
        iconBottom = iconRegister.registerIcon(prefix + tierName + suffix + "_bottom");
    }

    @Override
    public boolean hasComparatorInputOverride() {
        return true;
    }

    @Override
    public int getComparatorInputOverride(World world, int x, int y, int z, int side) {
        return Container.calcRedstoneFromInventory((IInventory) world.getTileEntity(x, y, z));
    }
}

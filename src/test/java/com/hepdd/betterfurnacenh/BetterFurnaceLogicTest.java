package com.hepdd.betterfurnacenh;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.ForgeDirection;

import org.junit.After;
import org.junit.Test;

import com.hepdd.betterfurnacenh.inventory.ContainerBFFurnace;
import com.hepdd.betterfurnacenh.network.PacketToggleHopper;
import com.hepdd.betterfurnacenh.tileentities.EnumFurnaceTier;
import com.hepdd.betterfurnacenh.tileentities.TileEntityBFFurnace;
import com.hepdd.betterfurnacenh.util.WrenchGridUtil;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class BetterFurnaceLogicTest {

    @After
    public void resetConfigDefaults() {
        Config.ironSpeedMultiplier = 1.5f;
        Config.goldSpeedMultiplier = 1.5f;
        Config.diamondSpeedMultiplier = 1.5f;
        EnumFurnaceTier.refreshCachedMultipliers();
    }

    @Test
    public void testServerAndOptionalModBytecodeIsolation() throws IOException {
        String wrenchGridBytes = readClassAscii("com/hepdd/betterfurnacenh/util/WrenchGridUtil.class");
        assertFalse(wrenchGridBytes.contains("org/lwjgl/"));

        String blockBytes = readClassAscii("com/hepdd/betterfurnacenh/blocks/BlockBFFurnace.class");
        assertFalse(blockBytes.contains("org/lwjgl/"));
        assertFalse(blockBytes.contains("client/overlay/GridMath"));
        assertFalse(blockBytes.contains("TileEntityBFBlastFurnace"));
        assertFalse(blockBytes.contains("ganymedes01/etfuturum/"));

        String itemBytes = readClassAscii("com/hepdd/betterfurnacenh/items/ItemHopperUpgrade.class");
        assertFalse(itemBytes.contains("org/lwjgl/"));
        assertFalse(itemBytes.contains("client/overlay/GridMath"));

        String mainModBytes = readClassAscii("com/hepdd/betterfurnacenh/BFNH.class");
        assertFalse(mainModBytes.contains("ganymedes01/etfuturum/"));
        assertFalse(mainModBytes.contains("TileEntityBFBlastFurnace"));
        assertFalse(mainModBytes.contains("codechicken/nei/"));
        assertFalse(mainModBytes.contains("compat/NEICompat"));
    }

    @Test
    public void testWrenchGridDetermination() {
        assertEquals(ForgeDirection.UNKNOWN, WrenchGridUtil.determineWrenchingSide(null, 0.5f, 0.5f, 0.5f));
        assertEquals(
            ForgeDirection.UNKNOWN,
            WrenchGridUtil.determineWrenchingSide(ForgeDirection.UNKNOWN, 0.5f, 0.5f, 0.5f));

        // Center hits return the clicked face itself
        assertEquals(ForgeDirection.UP, WrenchGridUtil.determineWrenchingSide(ForgeDirection.UP, 0.5f, 1.0f, 0.5f));
        assertEquals(ForgeDirection.DOWN, WrenchGridUtil.determineWrenchingSide(ForgeDirection.DOWN, 0.5f, 0.0f, 0.5f));

        // Corner hits return opposite side
        assertEquals(ForgeDirection.DOWN, WrenchGridUtil.determineWrenchingSide(ForgeDirection.UP, 0.1f, 1.0f, 0.1f));
        assertEquals(
            ForgeDirection.NORTH,
            WrenchGridUtil.determineWrenchingSide(ForgeDirection.SOUTH, 0.9f, 0.9f, 1.0f));

        // Side band hits on NORTH/SOUTH map top/bottom strips to UP/DOWN
        assertEquals(ForgeDirection.UP, WrenchGridUtil.determineWrenchingSide(ForgeDirection.NORTH, 0.5f, 0.85f, 0.0f));
        assertEquals(
            ForgeDirection.DOWN,
            WrenchGridUtil.determineWrenchingSide(ForgeDirection.NORTH, 0.5f, 0.15f, 0.0f));

        // Negative fractional hit coordinates wrap cleanly into [0, 1)
        assertEquals(
            ForgeDirection.UP,
            WrenchGridUtil.determineWrenchingSide(ForgeDirection.WEST, 0.0f, -0.15f, -0.5f));
    }

    @Test
    public void testLegacyShortAndModernFloatNbtCompatibility() {
        // Legacy save storing TAG_Short
        NBTTagCompound legacyNbt = new NBTTagCompound();
        legacyNbt.setShort("BurnTime", (short) 1400);
        legacyNbt.setShort("CookTime", (short) 125);
        assertEquals(1400f, TileEntityBFFurnace.readCompatibleNonNegativeFloat(legacyNbt, "BurnTime"), 0.001f);
        assertEquals(125f, TileEntityBFFurnace.readCompatibleNonNegativeFloat(legacyNbt, "CookTime"), 0.001f);
        // Legacy save lacks ItemBurnTime; when fuel slot is empty, fallback must >= burnTime (1400f) to prevent
        // negative V
        assertEquals(1400f, TileEntityBFFurnace.resolveCompatibleItemBurnTime(legacyNbt, 1400f, 0), 0.001f);
        // Legacy save with matching coal still in fuel slot preserves 1600t denominator
        assertEquals(1600f, TileEntityBFFurnace.resolveCompatibleItemBurnTime(legacyNbt, 1400f, 1600), 0.001f);

        // Legacy save where furnace just finished burning with -1 short must clamp to 0 (never wrap to 65535!)
        NBTTagCompound finishedLegacyNbt = new NBTTagCompound();
        finishedLegacyNbt.setShort("BurnTime", (short) -1);
        assertEquals(0f, TileEntityBFFurnace.readCompatibleNonNegativeFloat(finishedLegacyNbt, "BurnTime"), 0.001f);

        // Modern save storing TAG_Float with fractional speed progress and high-calorific fuel (> 32767)
        NBTTagCompound modernNbt = new NBTTagCompound();
        modernNbt.setFloat("BurnTime", 98765.5f);
        modernNbt.setFloat("CookTime", 133.375f);
        modernNbt.setFloat("ItemBurnTime", 100000f);
        assertEquals(98765.5f, TileEntityBFFurnace.readCompatibleNonNegativeFloat(modernNbt, "BurnTime"), 0.001f);
        assertEquals(133.375f, TileEntityBFFurnace.readCompatibleNonNegativeFloat(modernNbt, "CookTime"), 0.001f);
        assertEquals(100000f, TileEntityBFFurnace.resolveCompatibleItemBurnTime(modernNbt, 98765.5f, 0), 0.001f);
    }

    @Test
    public void testBurnAndCookScalingClamping() {
        assertEquals(0, TileEntityBFFurnace.computeBurnTimeScaled(0f, 1600f, 13));
        assertEquals(0, TileEntityBFFurnace.computeBurnTimeScaled(-5f, 1600f, 13));
        assertEquals(6, TileEntityBFFurnace.computeBurnTimeScaled(800f, 1600f, 13));

        // Even if itemBurnTime is 0 or smaller than burnTime, result is strictly clamped to [0, 13]
        assertEquals(13, TileEntityBFFurnace.computeBurnTimeScaled(1400f, 0f, 13));
        assertEquals(13, TileEntityBFFurnace.computeBurnTimeScaled(380f, 30f, 13));

        assertEquals(0, TileEntityBFFurnace.computeCookProgressScaled(-1f, 24));
        assertEquals(12, TileEntityBFFurnace.computeCookProgressScaled(100f, 24));
        assertEquals(24, TileEntityBFFurnace.computeCookProgressScaled(250f, 24));
    }

    @Test
    public void testProgressBar32BitSplitAndCookPrecision() {
        int[] testBurnValues = { 0, 1, 1600, 32767, 32768, 40000, 65535, 65536, 100000, 1_500_000, Integer.MAX_VALUE };
        for (int original : testBurnValues) {
            // Simulate S31PacketWindowProperty 16-bit signed short wire truncation
            short wireHigh = (short) ContainerBFFurnace.packHigh16(original);
            short wireLow = (short) ContainerBFFurnace.packLow16(original);
            int restored = ContainerBFFurnace.unpackInt32(wireHigh, wireLow);
            assertEquals("Mismatch for burn ticks " + original, original, restored);
        }

        // Sub-tick remaining burn time (> 0) must ceil to 1 so client isBurning() stays true
        assertEquals(1, ContainerBFFurnace.toSyncedBurnTicks(0.25f));
        assertEquals(0, ContainerBFFurnace.toSyncedBurnTicks(0f));
        assertEquals(0, ContainerBFFurnace.toSyncedBurnTicks(-0.5f));

        // Fractional cook time preserves 0.01 precision within a single signed short
        int syncedCook = ContainerBFFurnace.toSyncedCookTimeX100(133.37f);
        assertEquals(13337, syncedCook);
        assertTrue(syncedCook <= Short.MAX_VALUE);
    }

    @Test
    public void testTierSpeedMultiplierCacheRefresh() {
        EnumFurnaceTier.refreshCachedMultipliers();
        assertEquals(1.5f, EnumFurnaceTier.IRON.getSpeedMultiplier(), 0.0001f);
        assertEquals(2.25f, EnumFurnaceTier.GOLD.getSpeedMultiplier(), 0.0001f);
        assertEquals(3.375f, EnumFurnaceTier.DIAMOND.getSpeedMultiplier(), 0.0001f);

        Config.ironSpeedMultiplier = 2.0f;
        Config.goldSpeedMultiplier = 2.0f;
        Config.diamondSpeedMultiplier = 2.5f;
        EnumFurnaceTier.refreshCachedMultipliers();

        assertEquals(2.0f, EnumFurnaceTier.IRON.getSpeedMultiplier(), 0.0001f);
        assertEquals(4.0f, EnumFurnaceTier.GOLD.getSpeedMultiplier(), 0.0001f);
        assertEquals(10.0f, EnumFurnaceTier.DIAMOND.getSpeedMultiplier(), 0.0001f);
    }

    @Test
    public void testPacketToggleHopperCompactWireFormat() {
        ByteBuf buf = Unpooled.buffer();
        try {
            PacketToggleHopper outTop = new PacketToggleHopper(true);
            outTop.toBytes(buf);
            assertEquals(1, buf.readableBytes());

            PacketToggleHopper inTop = new PacketToggleHopper();
            inTop.fromBytes(buf);
            ByteBuf checkBuf = Unpooled.buffer();
            try {
                inTop.toBytes(checkBuf);
                assertTrue(checkBuf.readBoolean());
            } finally {
                checkBuf.release();
            }
        } finally {
            buf.release();
        }
    }

    private static String readClassAscii(String resourcePath) throws IOException {
        try (InputStream in = BetterFurnaceLogicTest.class.getClassLoader()
            .getResourceAsStream(resourcePath)) {
            assertNotNull("Missing class resource: " + resourcePath, in);
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buf = new byte[4096];
            int r;
            while ((r = in.read(buf)) != -1) {
                out.write(buf, 0, r);
            }
            return new String(out.toByteArray(), StandardCharsets.ISO_8859_1);
        }
    }
}

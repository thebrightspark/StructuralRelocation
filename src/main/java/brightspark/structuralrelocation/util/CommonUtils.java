package brightspark.structuralrelocation.util;

import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.message.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonUtils
{
    public static SimpleNetworkWrapper NETWORK;

    public static void regNetwork()
    {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(StructuralRelocation.MOD_ID);
        NETWORK.registerMessage(MessageUpdateClientContainer.Handler.class, MessageUpdateClientContainer.class, 0, Side.CLIENT);
        NETWORK.registerMessage(MessageGuiTeleport.Handler.class, MessageGuiTeleport.class, 1, Side.SERVER);
        NETWORK.registerMessage(MessageUpdateTeleporterLocation.Handler.class, MessageUpdateTeleporterLocation.class, 2, Side.CLIENT);
        NETWORK.registerMessage(MessageUpdateTeleporterCurBlock.Handler.class, MessageUpdateTeleporterCurBlock.class, 3, Side.CLIENT);
        NETWORK.registerMessage(MessageUpdateClientTeleporterObstruction.Handler.class, MessageUpdateClientTeleporterObstruction.class, 4, Side.CLIENT);
        NETWORK.registerMessage(MessageSpawnParticleBlock.Handler.class, MessageSpawnParticleBlock.class, 5, Side.CLIENT);
    }

    /**
     * Returns a string of the inputted number with commas added to group the digits.
     */
    public static String addDigitGrouping(int number)
    {
        return addDigitGrouping(Integer.toString(number));
    }

    /**
     * Returns a string of the inputted number with commas added to group the digits.
     */
    public static String addDigitGrouping(String number)
    {
        StringBuilder sb = new StringBuilder(number);
        for(int i = number.length() - 3; i > 0; i -= 3)
            sb.insert(i, ',');
        return sb.toString();
    }

    /**
     * Returns the BlockPos in a readable String with some colour formatting.
     */
    public static String posToString(BlockPos pos)
    {
        return TextFormatting.WHITE + "X: " + TextFormatting.GRAY + pos.getX() +
                TextFormatting.WHITE + " Y: " + TextFormatting.GRAY + pos.getY() +
                TextFormatting.WHITE + " Z: " + TextFormatting.GRAY + pos.getZ();
    }

    public static World getWorldByDimId(int dimId)
    {
        FMLCommonHandler handler = FMLCommonHandler.instance();
        return handler.getSide() == Side.SERVER || handler.getEffectiveSide() == Side.SERVER ? handler.getMinecraftServerInstance().getWorld(dimId) : null;
    }
}

package brightspark.structuralrelocation.util;

import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.message.*;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class CommonUtils
{
    public static SimpleNetworkWrapper NETWORK;

    public static void regNetwork(Side side)
    {
        NETWORK = NetworkRegistry.INSTANCE.newSimpleChannel(StructuralRelocation.MOD_ID);
        regMessage(MessageUpdateClientContainer.Handler.class, MessageUpdateClientContainer.class, 0, Side.CLIENT, side);
        regMessage(MessageGuiTeleport.Handler.class, MessageGuiTeleport.class, 1, Side.SERVER, side);
        regMessage(MessageUpdateTeleporterLocation.Handler.class, MessageUpdateTeleporterLocation.class, 2, Side.CLIENT, side);
        regMessage(MessageUpdateTeleporterCurBlock.Handler.class, MessageUpdateTeleporterCurBlock.class, 3, Side.CLIENT, side);
        regMessage(MessageUpdateClientTeleporterObstruction.Handler.class, MessageUpdateClientTeleporterObstruction.class, 4, Side.CLIENT, side);
        regMessage(MessageSpawnParticleBlock.Handler.class, MessageSpawnParticleBlock.class, 5, Side.CLIENT, side);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> void regMessage(Class<? extends IMessageHandler<REQ, REPLY>> messageHandler, Class<REQ> requestMessageType, int discriminator, Side receivingSide, Side thisSide)
    {
        IMessageHandler<? super REQ, ? extends REPLY> handler = receivingSide == thisSide ? instantiate(messageHandler) : new DummyHandler<>();
        NETWORK.registerMessage(handler, requestMessageType, discriminator, receivingSide);
    }

    private static <REQ extends IMessage, REPLY extends IMessage> IMessageHandler<? super REQ, ? extends REPLY> instantiate(Class<? extends IMessageHandler<? super REQ, ? extends REPLY>> handler)
    {
        try
        {
            return handler.newInstance();
        }
        catch (ReflectiveOperationException e)
        {
            throw new RuntimeException(e);
        }
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

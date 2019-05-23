package brightspark.structuralrelocation;

import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Config(modid = StructuralRelocation.MOD_ID)
@Config.LangKey(StructuralRelocation.MOD_ID + "config.title")
public class SRConfig
{
    @Config.Comment("If true, then debug messages will be printed to the console about teleportation for every block")
    public static boolean debugTeleportMessages = false;

    @Config.Comment("Whether the teleporters can move fluids")
    public static boolean canTeleportFluids = true;

    @Config.Comment("The maximum size of a dimension of a selected area")
    @Config.RangeInt(min = 1)
    public static int maxTeleportAreaSize = 64;

    /*
        ENERGY CALCULATION:
        Energy Cost = energyPerBlockBase + (energyPerDistanceMultiplier * 20 * log(distance))
        Multiply cost by energyAcrossDimensionsMultiplier if going across dimensions
     */

    @Config.Comment("The base amount of energy used to teleport each block")
    @Config.RangeInt(min = 0)
    public static int energyPerBlockBase = 500;

    @Config.Comment("The multiplier for energy per meter a block will be teleported")
    @Config.RangeDouble(min = 0F, max = Float.MAX_VALUE)
    public static float energyPerDistanceMultiplier = 1F;

    @Config.Comment("The multiplier for teleporting across dimensions")
    @Config.RangeDouble(min = 1F, max = Float.MAX_VALUE)
    public static float energyAcrossDimensionsMultiplier = 2F;

    @Config.Comment("The colour of the boxes which are rendered by the Selector and Debugger")
    public static String boxRenderColour = "0xFF0000";

    @Mod.EventBusSubscriber(modid = StructuralRelocation.MOD_ID)
    public static class Handler
    {
        @SubscribeEvent
        public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
        {
            if(event.getModID().equalsIgnoreCase(StructuralRelocation.MOD_ID))
                ConfigManager.sync(StructuralRelocation.MOD_ID, Config.Type.INSTANCE);
        }
    }
}

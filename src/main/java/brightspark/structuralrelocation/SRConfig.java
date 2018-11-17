package brightspark.structuralrelocation;

import net.minecraftforge.common.config.Config;

@Config(modid = StructuralRelocation.MOD_ID)
@Config.LangKey(StructuralRelocation.MOD_ID + "config.title")
public class SRConfig
{
    @Config.Name("Debug Teleport Messages")
    @Config.Comment("If true, then debug messages will be printed to the console about teleportation for every block")
    public static boolean debugTeleportMessages = false;

    @Config.Name("Can Teleport Fluids")
    @Config.Comment("Whether the teleporters can move fluids")
    public static boolean canTeleportFluids = true;

    @Config.Name("Max Teleport Area Size")
    @Config.Comment("The maximum size of a dimension of a selected area")
    @Config.RangeInt(min = 1)
    public static int maxTeleportAreaSize = 64;

    /*
        ENERGY CALCULATION:
        Energy Cost = energyPerBlockBase + (energyPerDistanceMultiplier * 20 * log(distance))
        Multiply cost by energyAcrossDimensionsMultiplier if going across dimensions
     */

    @Config.Name("Energy Per Block")
    @Config.Comment("The base amount of energy used to teleport each block")
    @Config.RangeInt(min = 0)
    public static int energyPerBlockBase = 500;

    @Config.Name("Energy Per Distance Multiplier")
    @Config.Comment("The multiplier for energy per meter a block will be teleported")
    @Config.RangeDouble(min = 0F, max = Float.MAX_VALUE)
    public static float energyPerDistanceMultiplier = 1F;

    @Config.Name("Energy Across Dimensions Multiplier")
    @Config.Comment("The multiplier for teleporting across dimensions")
    @Config.RangeDouble(min = 1F, max = Float.MAX_VALUE)
    public static float energyAcrossDimensionsMultiplier = 2F;

    @Config.Name("Box Render Colour")
    @Config.Comment("The colour of the boxes which are rendered by the Selector and Debugger")
    public static String boxRenderColour = "0xFF0000";

    @Config.Name("Teleport Wait Ticks")
    @Config.Comment("This is the time an Area Teleporter will wait when it tries to teleport to/from an unloaded chunk before trying again")
    @Config.RangeInt(min = 1)
    public static int teleportWaitTicks = 200;
}

package brightspark.structuralrelocation;

import java.awt.*;

public class Config
{
    /**
     * If true, then debug messages will be printed to the console about teleportation for every block
     */
    public static boolean debugTeleportMessages = false;

    /**
     * Whether the teleporters can move fluids
     */
    public static boolean canTeleportFluids = true;

    /**
     * The maximum size of a dimension of a selected area
     */
    public static int maxTeleportAreaSize = 64;

    /*
        ENERGY CALCULATION:
        Energy Cost = energyPerBlockBase + (energyPerDistanceMultiplier * 20 * log(distance))
        Multiply cost by energyAcrossDimensionsMultiplier if going across dimensions
     */

    /**
     * The base amount of energy used to teleport each block
     */
    public static int energyPerBlockBase = 500;

    /**
     * The multiplier for energy per meter a block will be teleported
     */
    public static float energyPerDistanceMultiplier = 1F;

    /**
     * The multiplier for teleporting across dimensions
     */
    public static float energyAcrossDimensionsMultiplier = 2F;

    /**
     * The colour of the boxes which are rendered by the Selector and Debugger
     */
    public static Color boxRenderColour = new Color(0xFF0000);
}

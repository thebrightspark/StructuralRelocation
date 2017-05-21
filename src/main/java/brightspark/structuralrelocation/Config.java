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

    /**
     * The amount of energy used to teleport each block
     */
    public static int energyPerBlockTeleport = 500;

    /**
     * The colour of the boxes which are rendered by the Selector and Debugger
     */
    public static Color boxRenderColour = new Color(0xFF0000);
}

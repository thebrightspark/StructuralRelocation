package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Config;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.util.LogHelper;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.io.File;

@Mod.EventBusSubscriber
public class ConfigHandler
{
    public static final String GENERAL = Configuration.CATEGORY_GENERAL;

    public static Configuration config;

    public static void init(File configFile)
    {
        if(config == null)
        {
            config = new Configuration(configFile);
            loadConfiguration();
        }
    }

    private static void loadConfiguration()
    {
        Config.debugTeleportMessages = config.getBoolean("debugTeleportMessages", GENERAL, Config.debugTeleportMessages, "If true, then debug messages will be printed to the console about teleportation for every block");
        Config.canTeleportFluids = config.getBoolean("canTeleportFluids", GENERAL, Config.canTeleportFluids, "Whether the teleporters can move fluids");
        Config.maxTeleportAreaSize = config.getInt("maxTeleportAreaSize", GENERAL, Config.maxTeleportAreaSize, 2, Integer.MAX_VALUE, "The maximum size of a dimension of a selected area");
        Config.energyPerBlockBase = config.getInt("energyPerBlockBase", GENERAL, Config.energyPerBlockBase, 0, Integer.MAX_VALUE, "The base amount of energy used to teleport each block");
        Config.energyPerDistanceMultiplier = config.getFloat("energyPerDistanceMultiplier", GENERAL, Config.energyPerDistanceMultiplier, 0F, Float.MAX_VALUE, "The multiplier for energy per meter a block will be teleported");
        Config.energyAcrossDimensionsMultiplier = config.getFloat("energyAcrossDimensionsMultiplier", GENERAL, Config.energyAcrossDimensionsMultiplier, 1F, Float.MAX_VALUE, "The multiplier for teleporting across dimensions");

        String colourString = config.getString("boxRenderColour", GENERAL, "0xFF0000", "The colour of the boxes which are rendered by the Selector and Debugger.\nThis should be written in the format '0x123456' for hexadecimal values, '123,123,123' for RGB or just an integer.");
        if(colourString.startsWith("0x"))
        {
            //Hexadecimal value
            try
            {
                Config.boxRenderColour = new Color(Integer.parseInt(colourString.substring(2), 16));
            }
            catch(NumberFormatException e)
            {
                LogHelper.error("Couldn't parse colour " + colourString + " for the config boxRenderColour as a hexadecimal value. Using default value.");
            }
        }
        else if(colourString.contains(","))
        {
            //RGB value
            String[] rgb = colourString.split(",");
            if(rgb.length == 3)
            {
                try
                {
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    Config.boxRenderColour = new Color(r, g, b);
                }
                catch(NumberFormatException e)
                {
                    LogHelper.error("Couldn't parse colour" + colourString + " for the config boxRenderColour as an RGB value. Using default value.");
                }
                catch(IllegalArgumentException e)
                {
                    LogHelper.error("Couldn't parse colour" + colourString + " for the config boxRenderColour as an RGB value - values must be between 0 and 255. Using default value.");
                }
            }
            else
                LogHelper.error("Couldn't parse colour" + colourString + " for the config boxRenderColour as an RGB value. Using default value.");
        }
        else
        {
            //Integer
            try
            {
                Config.boxRenderColour = new Color(Integer.parseInt(colourString));
            }
            catch(NumberFormatException e)
            {
                LogHelper.error("Couldn't parse colour " + colourString + " for the config boxRenderColour as an integer. Using default value.");
            }
        }

        if(config.hasChanged())
            config.save();
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equalsIgnoreCase(StructuralRelocation.MOD_ID))
            loadConfiguration();
    }
}

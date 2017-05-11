package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Config;
import brightspark.structuralrelocation.StructuralRelocation;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

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
        Config.maxTeleportAreaSize = config.getInt("maxTeleportAreaSize", GENERAL, Config.maxTeleportAreaSize, 2, Integer.MAX_VALUE, "The maximum size of a dimension of a selected area");

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

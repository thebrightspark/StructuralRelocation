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
    @Config.Comment("Client side only configs")
    public static final Client client = new Client();

    @Config.Comment("Common configs which are required on both client and server side")
    public static final Common common = new Common();

    @Config.Comment("Server side only configs")
    public static final Server server = new Server();

    public static class Client
    {
        @Config.Name("Box Render Colour")
        @Config.Comment("The colour of the boxes which are rendered by the Selector and Debugger")
        public String boxRenderColour = "0xFF0000";

        @Config.Name("Enable Teleport Animation")
        @Config.Comment({
                "Enabled the fancy teleporting animation",
                "Note that if disabled, then blocks will instantly disappear from their source positions and will reappear at their destination positions after the time set by the 'Teleport Animation Time Ticks' config"
        })
        public boolean enableTeleportAnimation = true;
    }

    public static class Common
    {
        @Config.Name("Max Teleport Area Size")
        @Config.Comment("The maximum size of a dimension of a selected area")
        @Config.RangeInt(min = 1)
        public int maxTeleportAreaSize = 64;

        @Config.Name("Teleport Animation Time Ticks")
        @Config.Comment("The length of the teleporting animation in ticks")
        @Config.RangeInt(min = 0)
        public int teleportAnimationTimeTicks = 10;

        /*
            ENERGY CALCULATION:
            Energy Cost = energyPerBlockBase + (energyPerDistanceMultiplier * 20 * log(distance))
            Multiply cost by energyAcrossDimensionsMultiplier if going across dimensions
        */

        @Config.Name("Energy Per Block")
        @Config.Comment("The base amount of energy used to teleport each block")
        @Config.RangeInt(min = 0)
        public int energyPerBlockBase = 500;

        @Config.Name("Energy Per Distance Multiplier")
        @Config.Comment("The multiplier for energy per meter a block will be teleported")
        @Config.RangeDouble(min = 0F, max = Float.MAX_VALUE)
        public float energyPerDistanceMultiplier = 1F;

        @Config.Name("Energy Across Dimensions Multiplier")
        @Config.Comment("The multiplier for teleporting across dimensions")
        @Config.RangeDouble(min = 1F, max = Float.MAX_VALUE)
        public float energyAcrossDimensionsMultiplier = 2F;
    }

    public static class Server
    {
        @Config.Name("Debug Teleport Messages")
        @Config.Comment("If true, then debug messages will be printed to the console about teleportation for every block")
        public boolean debugTeleportMessages = false;

        @Config.Name("Can Teleport Fluids")
        @Config.Comment("Whether the teleporters can move fluids")
        public boolean canTeleportFluids = true;

        @Config.Name("Teleport Wait Ticks")
        @Config.Comment("The amount of time an Area Teleporter will wait when it tries to teleport to/from an unloaded chunk before trying again")
        @Config.RangeInt(min = 1)
        public int teleportWaitTicks = 200;

        @Config.Name("Number Of Blocks To Teleport Per Tick")
        @Config.Comment({
                "The number of blocks the Area Teleporter will attempt to teleport each tick",
                "Warning: Be aware that large values may lag the server"
        })
        @Config.RangeInt(min = 1)
        public int numBlockPerTick = 1;
    }

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

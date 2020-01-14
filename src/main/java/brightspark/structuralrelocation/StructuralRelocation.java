package brightspark.structuralrelocation;

import brightspark.structuralrelocation.handler.GuiHandler;
import brightspark.structuralrelocation.handler.RegistrationHandler;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;
import org.apache.logging.log4j.Logger;

@Mod(modid = StructuralRelocation.MOD_ID, name = StructuralRelocation.MOD_NAME, version = StructuralRelocation.VERSION)
public class StructuralRelocation
{
    public static final String MOD_ID = "structuralrelocation";
    public static final String MOD_NAME = "Structural Relocation";
    public static final String VERSION = "@VERSION@";
    public static final String GUI_TEXTURE_DIR = "textures/gui/";

    public static Logger LOGGER;

    @Mod.Instance(MOD_ID)
    public static StructuralRelocation instance;

    public static final CreativeTabs SR_TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack createIcon()
        {
            return new ItemStack(SRBlocks.area_teleporter);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        LOGGER = event.getModLog();
        CommonUtils.regNetwork(event.getSide());
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        RegistrationHandler.regTileEntities();
        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }
}

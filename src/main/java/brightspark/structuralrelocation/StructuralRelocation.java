package brightspark.structuralrelocation;

import brightspark.structuralrelocation.handler.GuiHandler;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.launchwrapper.Launch;
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
    public static final String VERSION = "1.10.2-0.0.9.2";
    public static final String GUI_TEXTURE_DIR = "textures/gui/";

    public static Boolean IS_DEV;
    public static Logger LOGGER;

    @Mod.Instance(MOD_ID)
    public static StructuralRelocation instance;

    public static final CreativeTabs SR_TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public ItemStack getTabIconItem()
        {
            return new ItemStack(Items.ENDER_PEARL);
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //Initialize item, blocks, textures/models and configs here

        LOGGER = event.getModLog();
        IS_DEV = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");

        CommonUtils.regNetwork();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        //Initialize GUIs, tile entities, recipies, event handlers here

        SRBlocks.regTileEntities();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }
}

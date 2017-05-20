package brightspark.structuralrelocation;

import brightspark.structuralrelocation.handler.ConfigHandler;
import brightspark.structuralrelocation.handler.GuiHandler;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.init.SRRecipes;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.fml.common.network.NetworkRegistry;

@Mod(modid = StructuralRelocation.MOD_ID, name = StructuralRelocation.MOD_NAME, version = StructuralRelocation.VERSION)
public class StructuralRelocation
{
    public static final String MOD_ID = "structuralrelocation";
    public static final String MOD_NAME = "Structural Relocation";
    public static final String VERSION = "1.10.2-0.0.7.1";
    public static final String GUI_TEXTURE_DIR = "textures/gui/";

    @Mod.Instance(MOD_ID)
    public static StructuralRelocation instance;

    public static final CreativeTabs SR_TAB = new CreativeTabs(MOD_ID)
    {
        @Override
        public Item getTabIconItem()
        {
            return Items.ENDER_PEARL;
        }

        @Override
        public String getTranslatedTabLabel()
        {
            return MOD_NAME;
        }
    };

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        //Initialize item, blocks, textures/models and configs here

        ConfigHandler.init(event.getSuggestedConfigurationFile());
        CommonUtils.regNetwork();
    }

    @Mod.EventHandler
    public void init(FMLInitializationEvent event)
    {
        //Initialize GUIs, tile entities, recipies, event handlers here

        SRBlocks.regTileEntities();
        SRRecipes.init();

        NetworkRegistry.INSTANCE.registerGuiHandler(instance, new GuiHandler());
    }

    @Mod.EventHandler
    public void postInit(FMLPostInitializationEvent event)
    {
        //Run stuff after mods have initialized here

    }

}

package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.block.BakedModelTeleporter;
import brightspark.structuralrelocation.init.SRBlocks;
import brightspark.structuralrelocation.init.SRItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMapperBase;
import net.minecraft.item.Item;
import net.minecraft.util.registry.IRegistry;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

@Mod.EventBusSubscriber(modid = StructuralRelocation.MOD_ID, value = Side.CLIENT)
public class ModelHandler
{
    private static ModelResourceLocation singleTeleporterMRL, areaTeleporterMRL;

    @SubscribeEvent
    public static void regModels(ModelRegistryEvent event)
    {
        //Register all item models
        regModel(SRItems.selector);
        regModel(SRItems.debugger);

        //Register block models
        regModel(SRBlocks.single_teleporter);
        regModel(SRBlocks.area_teleporter);
        regModel(SRBlocks.creative_generator);

        singleTeleporterMRL = createMRL(SRBlocks.single_teleporter);
        areaTeleporterMRL = createMRL(SRBlocks.area_teleporter);
        regPlainStateMapper(SRBlocks.single_teleporter, singleTeleporterMRL);
        regPlainStateMapper(SRBlocks.area_teleporter, areaTeleporterMRL);
    }

    @SubscribeEvent
    public static void modelBakeEvent(ModelBakeEvent event)
    {
        IRegistry<ModelResourceLocation, IBakedModel> registry = event.getModelRegistry();
        replaceTeleporterModel(registry, singleTeleporterMRL);
        replaceTeleporterModel(registry, areaTeleporterMRL);
    }

    private static ModelResourceLocation createMRL(Block block)
    {
        //noinspection ConstantConditions
        return new ModelResourceLocation(block.getRegistryName(), null);
    }

    private static void regPlainStateMapper(Block block, ModelResourceLocation mrl)
    {
        ModelLoader.setCustomStateMapper(block, new StateMapperBase() {
            @Override
            protected ModelResourceLocation getModelResourceLocation(IBlockState state) {
                return mrl;
            }
        });
    }

    private static void regModel(Item item)
    {
        //noinspection ConstantConditions
        ModelLoader.setCustomModelResourceLocation(item, 0, new ModelResourceLocation(item.getRegistryName(), "inventory"));
    }

    private static void regModel(Block block)
    {
        regModel(Item.getItemFromBlock(block));
    }

    private static void replaceTeleporterModel(IRegistry<ModelResourceLocation, IBakedModel> registry, ModelResourceLocation mrl)
    {
        registry.putObject(mrl, new BakedModelTeleporter(registry.getObject(mrl)));
    }
}

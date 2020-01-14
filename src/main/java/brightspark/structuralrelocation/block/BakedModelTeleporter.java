package brightspark.structuralrelocation.block;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.block.model.ItemOverrideList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.property.IExtendedBlockState;

import javax.annotation.Nullable;
import java.util.List;

public class BakedModelTeleporter implements IBakedModel {
    private IBakedModel teleporterModel;

    public BakedModelTeleporter(IBakedModel teleporterModel) {
        this.teleporterModel = teleporterModel;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable IBlockState state, @Nullable EnumFacing side, long rand) {
        IBakedModel model = teleporterModel;
        if (state instanceof IExtendedBlockState) {
            IBlockState camoState = ((IExtendedBlockState) state).getValue(AbstractBlockTeleporter.PROP_CAMO);
            if (camoState != null && camoState != Blocks.AIR.getDefaultState())
                model = Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelShapes().getModelForState(camoState);
        }
        return model.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return teleporterModel.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return teleporterModel.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return teleporterModel.isBuiltInRenderer();
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return teleporterModel.getParticleTexture();
    }

    @Override
    public ItemOverrideList getOverrides() {
        return teleporterModel.getOverrides();
    }

    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return teleporterModel.getItemCameraTransforms();
    }
}

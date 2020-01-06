package brightspark.structuralrelocation.particle;

import brightspark.structuralrelocation.SRConfig;
import brightspark.structuralrelocation.util.RedrawableTesselator;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class ParticleBlock extends Particle
{
	private boolean inverse;
	private Map<String, Object> data = new HashMap<>();
	private TeleportAnimation animation = SRConfig.client.teleportAnimation;
	private RedrawableTesselator redrawableTesselator;

	public ParticleBlock(World worldIn, BlockPos pos, IBlockState state, boolean inverse)
	{
		super(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		particleMaxAge = SRConfig.common.teleportAnimationTimeTicks;
		this.inverse = inverse;
		Color colour = Color.getHSBColor(rand.nextFloat(), randFloat(0.3f), randFloat(0.7f));
		float[] rgb = colour.getRGBColorComponents(null);
		setRBGColorF(rgb[0], rgb[1], rgb[2]);
		animation.init(data, rand);

		//Construct buffer to use for rendering
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		int bufferSize = 0;
		for(EnumFacing facing : EnumFacing.values())
			bufferSize += model.getQuads(state, facing, 0L).size();
		bufferSize += model.getQuads(state, null, 0L).size();
		redrawableTesselator = new RedrawableTesselator(bufferSize * 4 * 28, GL11.GL_QUADS, DefaultVertexFormats.BLOCK,
			bufferBuilder -> Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer()
				.renderModel(world, model, state, BlockPos.ORIGIN, bufferBuilder, false));
	}

	private float randFloat(float min)
	{
		return min + rand.nextFloat() * (1f - min);
	}

	@Override
	public int getFXLayer()
	{
		return 3;
	}

	@Override
	public void onUpdate()
	{
		if(particleAge++ >= particleMaxAge || !SRConfig.client.enableTeleportAnimation)
			setExpired();

		if(particleAge == particleMaxAge)
		{
			world.playSound(posX, posY, posZ, SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.BLOCKS, 0.75F, rand.nextFloat() + 0.5F, false);
			if(!inverse)
				for(int i = 0; i < rand.nextInt(10) + 5; i++)
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBlockTeleport(world, posX, posY, posZ));
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		GlStateManager.pushMatrix();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableFog();

		GlStateManager.translate(posX - Particle.interpPosX, posY - Particle.interpPosY, posZ - Particle.interpPosZ);

		if (particleAge <= particleMaxAge)
			animation.preRender(data, entityIn, partialTicks, posX, posY, posZ, particleAge, particleMaxAge, inverse);
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);
		redrawableTesselator.draw();

		GlStateManager.popMatrix();
		GlStateManager.enableFog();
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}
}

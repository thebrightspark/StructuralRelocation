package brightspark.structuralrelocation.particle;

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

public class ParticleBlock extends Particle
{
	private boolean inverse;
	private float rotX, rotY, rotZ;
	private RedrawableTesselator redrawableTesselator;

	public ParticleBlock(World worldIn, BlockPos pos, IBlockState state, boolean inverse)
	{
		super(worldIn, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
		particleMaxAge = 10;
		this.inverse = inverse;
		rotX = randRotation();
		rotY = randRotation();
		rotZ = randRotation();
		Color colour = Color.getHSBColor(rand.nextFloat(), randFloat(0.3f), randFloat(0.7f));
		float[] rgb = colour.getRGBColorComponents(null);
		setRBGColorF(rgb[0], rgb[1], rgb[2]);

		//Construct buffer to use for rendering
		IBakedModel model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		int bufferSize = 0;
		for(EnumFacing facing : EnumFacing.values())
			bufferSize += model.getQuads(state, facing, 0L).size();
		bufferSize += model.getQuads(state, null, 0L).size();
		redrawableTesselator = new RedrawableTesselator(bufferSize * 4 * 28, bufferBuilder -> {
			bufferBuilder.begin(GL11.GL_QUADS, DefaultVertexFormats.BLOCK);
			Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer()
				.renderModel(world, model, state, BlockPos.ORIGIN, bufferBuilder, false);
			bufferBuilder.finishDrawing();
		});
	}

	private float randFloat(float min)
	{
		return min + rand.nextFloat() * (1f - min);
	}

	private float randRotation()
	{
		return rand.nextFloat() * 2 - 0.5F;
	}

	@Override
	public int getFXLayer()
	{
		return 3;
	}

	@Override
	public void onUpdate()
	{
		if(particleAge++ >= particleMaxAge)
			setExpired();

		if(particleAge == particleMaxAge)
		{
			world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.75F, rand.nextFloat() + 0.5F);
			if(!inverse)
				for(int i = 0; i < rand.nextInt(10) + 5; i++)
					Minecraft.getMinecraft().effectRenderer.addEffect(new ParticleBlockTeleport(world, posX, posY, posZ));
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		if(particleAge > particleMaxAge || (particleAge == particleMaxAge && partialTicks > 0F))
			return;

		GlStateManager.pushMatrix();
		GlStateManager.enableAlpha();
		GlStateManager.enableBlend();
		GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
		GlStateManager.disableFog();

		double agePct = ((double) particleAge + partialTicks) / (double) particleMaxAge;
		float ageProgressInv = (float) Math.sin((agePct * (Math.PI / 2)) + (Math.PI / 2));
		float ageProgress = 1 - ageProgressInv;
		double translationX = posX - entityIn.posX;
		double translationY = posY - entityIn.posY;
		double translationZ = posZ - entityIn.posZ;
		GlStateManager.translate(translationX, translationY, translationZ);
		if(inverse)
		{
			GlStateManager.rotate(ageProgressInv * 200F, rotX, rotY, rotZ);
			GlStateManager.scale(ageProgress, ageProgress, ageProgress);
		}
		else
		{
			GlStateManager.rotate(ageProgress * 200F, rotX, rotY, rotZ);
			GlStateManager.scale(ageProgressInv, ageProgressInv, ageProgressInv);
		}
		GlStateManager.translate(-0.5F, -0.5F, -0.5F);

		redrawableTesselator.draw();

		GlStateManager.popMatrix();
		GlStateManager.enableFog();
		GlStateManager.disableBlend();
		GlStateManager.disableAlpha();
	}
}

package brightspark.structuralrelocation.particle;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.block.model.IBakedModel;
import net.minecraft.entity.Entity;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.awt.*;

public class ParticleBlock extends Particle
{
	private boolean inverse;
	private IBlockState state;
	private IBakedModel model;
	private float rotX, rotY, rotZ;

	public ParticleBlock(World worldIn, BlockPos pos, IBlockState state, boolean inverse)
	{
		super(worldIn, pos.getX(), pos.getY(), pos.getZ());
		particleMaxAge = 20;
		this.inverse = inverse;
		this.state = state;
		model = Minecraft.getMinecraft().getBlockRendererDispatcher().getModelForState(state);
		rotX = randRotation();
		rotY = randRotation();
		rotZ = randRotation();
		Color colour = Color.getHSBColor(rand.nextFloat(), randFloat(0.3f), randFloat(0.7f));
		float[] rgb = colour.getRGBColorComponents(null);
		this.setRBGColorF(rgb[0], rgb[1], rgb[2]);
	}

	private float randFloat(float min)
	{
		return min + rand.nextFloat() * (1f - min);
	}

	private float randRotation()
	{
		return rand.nextFloat() * 0.5F;
	}

	private float randSpeed()
	{
		return (rand.nextFloat() - 0.5F) * 0.2F;
	}

	@Override
	public void onUpdate()
	{
		if(particleAge++ >= particleMaxAge)
			this.setExpired();

		if(particleAge == particleMaxAge)
		{
			world.playSound(null, posX, posY, posZ, SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.BLOCKS, 0.75F, rand.nextFloat() + 0.5F);
			for(int i = 0; i < rand.nextInt(10) + 5; i++)
				world.spawnParticle(EnumParticleTypes.PORTAL, posX, posY, posZ, randSpeed(), randSpeed(), randSpeed());
		}
	}

	@Override
	public void renderParticle(BufferBuilder buffer, Entity entityIn, float partialTicks, float rotationX, float rotationZ, float rotationYZ, float rotationXY, float rotationXZ)
	{
		GlStateManager.translate(posX, posY, posZ);
		float agePct = (float) particleAge / (float) particleMaxAge;
		GlStateManager.rotate(agePct, rotX, rotY, rotZ);
		float agePctInv = 1 - agePct;
//		GlStateManager.color(1F, 1F, 1F, agePctInv);
		GlStateManager.scale(agePctInv, agePctInv, agePctInv);
		GlStateManager.translate(-posX, -posY, -posZ);

//		Minecraft.getMinecraft().getBlockRendererDispatcher().getBlockModelRenderer()
//			.renderModel(world, model, state, new BlockPos(posX, posY, posZ), buffer, false);

		RenderGlobal.renderFilledBox(posX, posY, posZ, posX + 1, posY + 1, posZ + 1, getRedColorF(), getGreenColorF(), getBlueColorF(), agePctInv);

		GlStateManager.color(1F, 1F, 1F, 1F);
		GlStateManager.scale(1F, 1F, 1F);
	}
}

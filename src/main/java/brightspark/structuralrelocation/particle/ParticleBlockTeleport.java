package brightspark.structuralrelocation.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.Random;

public class ParticleBlockTeleport extends Particle
{
	protected ParticleBlockTeleport(World worldIn, double posXIn, double posYIn, double posZIn)
	{
		super(worldIn, posXIn, posYIn, posZIn);
		int iconIndex = rand.nextInt(9);
		setParticleTextureIndex(iconIndex);
		setMaxAge(20);
		double iconVelMult = (double) (8 - iconIndex) / 16D;
		Vec3d vec = createRandVector(rand).scale(rand.nextDouble() * 0.2D * iconVelMult);
		motionX = vec.x;
		motionY = vec.y;
		motionZ = vec.z;
		particleScale = rand.nextFloat() / 2 + 0.75F;
		float f = rand.nextFloat() * 0.6F + 0.4F;
		this.particleRed = f * 0.9F;
		this.particleGreen = f * 0.3F;
		this.particleBlue = f;
	}

	private static Vec3d createRandVector(Random random)
	{
		double phi = random.nextDouble() * 2 * Math.PI;
		double theta = Math.acos((random.nextDouble() * 2D) - 1D);
		double x = Math.sin(theta) * Math.cos(phi);
		double y = Math.sin(theta) * Math.sin(phi);
		double z = Math.cos(theta);
		return new Vec3d(x, y, z);
	}

	@Override
	public void onUpdate()
	{
		prevPosX = posX;
		prevPosY = posY;
		prevPosZ = posZ;

		if (particleAge++ >= particleMaxAge)
			setExpired();

		move(motionX, motionY, motionZ);
		motionX *= 0.9800000190734863D;
		motionY *= 0.9800000190734863D;
		motionZ *= 0.9800000190734863D;

		if (onGround)
		{
			motionX *= 0.699999988079071D;
			motionZ *= 0.699999988079071D;
		}

		setAlphaF(1F - ((float) particleAge / (float) particleMaxAge));
	}
}

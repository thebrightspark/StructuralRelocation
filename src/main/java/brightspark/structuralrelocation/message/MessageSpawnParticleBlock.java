package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.particle.ParticleBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSpawnParticleBlock implements IMessage
{
	private BlockPos pos;
	private boolean inverse;

	public MessageSpawnParticleBlock() {}

	public MessageSpawnParticleBlock(BlockPos pos, boolean inverse)
	{
		this.pos = pos;
		this.inverse = inverse;
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		pos = BlockPos.fromLong(buf.readLong());
		inverse = buf.readBoolean();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeLong(pos.toLong());
		buf.writeBoolean(inverse);
	}

	public static class Handler implements IMessageHandler<MessageSpawnParticleBlock, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSpawnParticleBlock message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(() -> mc.effectRenderer.addEffect(new ParticleBlock(mc.world, message.pos, mc.world.getBlockState(message.pos), message.inverse)));
			return null;
		}
	}
}

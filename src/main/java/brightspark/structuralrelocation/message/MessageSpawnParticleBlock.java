package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.particle.ParticleBlock;
import io.netty.buffer.ByteBuf;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageSpawnParticleBlock implements IMessage
{
	private boolean inverse;
	private BlockPos pos;
	private int blockId, blockMeta;

	public MessageSpawnParticleBlock() {}

	public MessageSpawnParticleBlock(boolean inverse, BlockPos pos, IBlockState state)
	{
		this.inverse = inverse;
		this.pos = pos;
		this.blockId = Block.getIdFromBlock(state.getBlock());
		this.blockMeta = state.getBlock().getMetaFromState(state);
	}

	@Override
	public void fromBytes(ByteBuf buf)
	{
		inverse = buf.readBoolean();
		pos = BlockPos.fromLong(buf.readLong());
		blockId = buf.readInt();
		blockMeta = buf.readInt();
	}

	@Override
	public void toBytes(ByteBuf buf)
	{
		buf.writeBoolean(inverse);
		buf.writeLong(pos.toLong());
		buf.writeInt(blockId);
		buf.writeInt(blockMeta);
	}

	public static class Handler implements IMessageHandler<MessageSpawnParticleBlock, IMessage>
	{
		@Override
		public IMessage onMessage(MessageSpawnParticleBlock message, MessageContext ctx)
		{
			Minecraft mc = Minecraft.getMinecraft();
			mc.addScheduledTask(() -> {
				Block block = Block.getBlockById(message.blockId);
				IBlockState state = block.getStateFromMeta(message.blockMeta);
				mc.effectRenderer.addEffect(new ParticleBlock(mc.world, message.pos, state, message.inverse));
			});
			return null;
		}
	}
}

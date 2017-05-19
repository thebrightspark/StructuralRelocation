package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateClientTeleporterObstruction implements IMessage
{
    public BlockPos telePos, obstPos;

    public MessageUpdateClientTeleporterObstruction() {}

    public MessageUpdateClientTeleporterObstruction(BlockPos telePos, BlockPos obstPos)
    {
        this.telePos = telePos;
        this.obstPos = obstPos;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        telePos = BlockPos.fromLong(buf.readLong());
        if(!buf.readBoolean()) obstPos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeLong(telePos.toLong());
        buf.writeBoolean(obstPos == null);
        if(obstPos != null) buf.writeLong(obstPos.toLong());
    }

    public static class Handler implements IMessageHandler<MessageUpdateClientTeleporterObstruction, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageUpdateClientTeleporterObstruction message, MessageContext ctx)
        {
            final IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    TileEntity te = Minecraft.getMinecraft().world.getTileEntity(message.telePos);
                    if(te != null && te instanceof TileAreaTeleporter)
                        ((TileAreaTeleporter) te).lastBlockInTheWay = message.obstPos;
                }
            });
            return null;
        }
    }
}

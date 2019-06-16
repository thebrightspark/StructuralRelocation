package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.gui.ContainerTeleporter;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateTeleporterCurBlock implements IMessage
{
    public BlockPos pos;

    public MessageUpdateTeleporterCurBlock() {}

    public MessageUpdateTeleporterCurBlock(BlockPos pos)
    {
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        if(!buf.readBoolean()) pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeBoolean(pos == null);
        if(pos != null) buf.writeLong(pos.toLong());
    }

    public static class Handler implements IMessageHandler<MessageUpdateTeleporterCurBlock, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageUpdateTeleporterCurBlock message, MessageContext ctx)
        {
            final IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(() -> {
                Container container = Minecraft.getMinecraft().player.openContainer;
                if(container instanceof ContainerTeleporter)
                    ((ContainerTeleporter) container).updateTeleporter(message.pos);
            });
            return null;
        }
    }
}

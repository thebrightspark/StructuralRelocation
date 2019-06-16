package brightspark.structuralrelocation.message;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * This message will send an integer from the server to the client.
 * This is needed since using the progress bar method in the container truncates the values to a short.
 */
public class MessageUpdateClientContainer implements IMessage
{
    public int id, value;

    public MessageUpdateClientContainer() {}

    public MessageUpdateClientContainer(int fieldId, int value)
    {
        id = fieldId;
        this.value = value;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        id = buf.readInt();
        value = buf.readInt();
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(id);
        buf.writeInt(value);
    }

    public static class Handler implements IMessageHandler<MessageUpdateClientContainer, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageUpdateClientContainer message, MessageContext ctx)
        {
            final IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(() -> Minecraft.getMinecraft().player.openContainer.updateProgressBar(message.id, message.value));
            return null;
        }
    }
}

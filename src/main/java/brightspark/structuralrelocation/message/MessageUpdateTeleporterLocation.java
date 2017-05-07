package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.gui.ContainerTeleporter;
import brightspark.structuralrelocation.util.LogHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.inventory.Container;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class MessageUpdateTeleporterLocation implements IMessage
{
    public Location location;
    public LocationArea area;

    public MessageUpdateTeleporterLocation() {}

    public MessageUpdateTeleporterLocation(Location location)
    {
        this.location = location;
    }

    public MessageUpdateTeleporterLocation(LocationArea area)
    {
        this.area = area;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        if(buf.readBoolean())
        {
            int dimID = buf.readInt();
            BlockPos pos1 = BlockPos.fromLong(buf.readLong());
            BlockPos pos2 = BlockPos.fromLong(buf.readLong());
            area = new LocationArea(dimID, pos1, pos2);
        }
        else
        {
            int dimID = buf.readInt();
            BlockPos pos = BlockPos.fromLong(buf.readLong());
            location = new Location(dimID, pos);
        }
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        boolean isArea = location == null;
        buf.writeBoolean(isArea);
        if(isArea)
        {
            buf.writeInt(area.dimensionId);
            buf.writeLong(area.pos1.toLong());
            buf.writeLong(area.pos2.toLong());
        }
        else
        {
            buf.writeInt(location.dimensionId);
            buf.writeLong(location.position.toLong());
        }
    }

    public static class Handler implements IMessageHandler<MessageUpdateTeleporterLocation, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageUpdateTeleporterLocation message, MessageContext ctx)
        {
            final IThreadListener mainThread = Minecraft.getMinecraft();
            mainThread.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    Minecraft mc = Minecraft.getMinecraft();
                    Container container = mc.player.openContainer;
                    LogHelper.info("Updating Teleporter");
                    if(container instanceof ContainerTeleporter)
                    {
                        if(message.location == null)
                            ((ContainerTeleporter) container).updateTeleporter(message.area);
                        else
                            ((ContainerTeleporter) container).updateTeleporter(message.location);
                        //Update the GUI
                        /*
                        GuiScreen gui = mc.currentScreen;
                        if(gui instanceof GuiTeleporter)
                            ((GuiTeleporter) gui).updateIcons();
                        */
                    }
                }
            });
            return null;
        }
    }
}

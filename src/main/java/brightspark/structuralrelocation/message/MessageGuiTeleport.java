package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.util.LogHelper;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

import java.util.UUID;

public class MessageGuiTeleport implements IMessage
{
    public int buttonId;
    public UUID playerUUID;
    public BlockPos pos;

    public MessageGuiTeleport() {}

    public MessageGuiTeleport(int buttonId, EntityPlayer player, BlockPos pos)
    {
        this.buttonId = buttonId;
        this.playerUUID = player.getUniqueID();
        this.pos = pos;
    }

    @Override
    public void fromBytes(ByteBuf buf)
    {
        buttonId = buf.readInt();
        long mostSig = buf.readLong();
        long leastSig = buf.readLong();
        playerUUID = new UUID(mostSig, leastSig);
        pos = BlockPos.fromLong(buf.readLong());
    }

    @Override
    public void toBytes(ByteBuf buf)
    {
        buf.writeInt(buttonId);
        buf.writeLong(playerUUID.getMostSignificantBits());
        buf.writeLong(playerUUID.getLeastSignificantBits());
        buf.writeLong(pos.toLong());
    }

    public static class Handler implements IMessageHandler<MessageGuiTeleport, IMessage>
    {
        @Override
        public IMessage onMessage(final MessageGuiTeleport message, final MessageContext ctx)
        {
            final IThreadListener mainThread = ctx.getServerHandler().playerEntity.getServerWorld();
            mainThread.addScheduledTask(new Runnable()
            {
                @Override
                public void run()
                {
                    WorldServer world = ctx.getServerHandler().playerEntity.getServerWorld();
                    EntityPlayer player = world.getPlayerEntityByUUID(message.playerUUID);
                    if(player == null)
                    {
                        LogHelper.warn("Player could not be found when trying to click teleporter GUI button. UUID: " + message.playerUUID.toString());
                        return;
                    }

                    TileEntity te = world.getTileEntity(message.pos);
                    if(!(te instanceof AbstractTileTeleporter))
                    {
                        LogHelper.warn("Teleporter could not be found when trying to click teleporter GUI button. Pos: " + message.pos.toString());
                        return;
                    }
                    AbstractTileTeleporter teleporter = (AbstractTileTeleporter) te;

                    switch(message.buttonId)
                    {
                        case 0: //Teleport
                            teleporter.teleport(player);
                            break;
                        case 1: //Copy
                            //Do nothing atm
                            break;
                        case 2: //Stop
                            if(teleporter instanceof TileAreaTeleporter)
                                ((TileAreaTeleporter) teleporter).stop();
                            break;
                        default:
                            LogHelper.warn("Unhandled button ID '" + message.buttonId + "' for teleporter GUI!");
                    }
                }
            });
            return null;
        }
    }
}

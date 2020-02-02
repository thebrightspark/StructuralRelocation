package brightspark.structuralrelocation.message;

import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.gui.ContainerTeleporter;
import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IThreadListener;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
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
            final IThreadListener mainThread = ctx.getServerHandler().player.getServerWorld();
            mainThread.addScheduledTask(() -> {
                WorldServer world = ctx.getServerHandler().player.getServerWorld();
                EntityPlayer player = world.getPlayerEntityByUUID(message.playerUUID);
                if(player == null)
                {
                    StructuralRelocation.LOGGER.warn("Player could not be found when trying to click teleporter GUI button. UUID: " + message.playerUUID.toString());
                    return;
                }

                if (!(player.openContainer instanceof ContainerTeleporter))
                {
                    StructuralRelocation.LOGGER.warn("Player does not have the teleporter GUI open. UUID: " + message.playerUUID.toString());
                    return;
                }

                if (!player.openContainer.canInteractWith(player))
                {
                    StructuralRelocation.LOGGER.warn("Player can't interact with open teleporter GUI. UUID: " + message.playerUUID.toString());
                    return;
                }

                TileEntity te = world.getTileEntity(message.pos);
                if(!(te instanceof AbstractTileTeleporter))
                {
                    StructuralRelocation.LOGGER.warn("Teleporter could not be found when trying to click teleporter GUI button. Pos: " + message.pos.toString());
                    return;
                }
                AbstractTileTeleporter teleporter = (AbstractTileTeleporter) te;

                switch(message.buttonId)
                {
                    case 0: //Teleport
                        teleporter.teleport(message.playerUUID);
                        break;
                    case 1: //Copy
                        if(player.capabilities.isCreativeMode)
                            teleporter.copy(message.playerUUID);
                        else
                            player.sendMessage(new TextComponentString("You must be in creative to copy blocks at the moment!"));
                        break;
                    case 2: //Stop
                        if(teleporter instanceof TileAreaTeleporter)
                            ((TileAreaTeleporter) teleporter).stop();
                        break;
                    default:
                        StructuralRelocation.LOGGER.warn("Unhandled button ID '" + message.buttonId + "' for teleporter GUI!");
                }
            });
            return null;
        }
    }
}

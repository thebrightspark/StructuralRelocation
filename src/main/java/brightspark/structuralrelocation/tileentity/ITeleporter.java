package brightspark.structuralrelocation.tileentity;

import net.minecraft.entity.player.EntityPlayer;

public interface ITeleporter
{
    /**
     * Tries to start the teleporting
     * Player argument is the one who activated the block, and is used to send messages to
     */
    void teleport(EntityPlayer player);
}

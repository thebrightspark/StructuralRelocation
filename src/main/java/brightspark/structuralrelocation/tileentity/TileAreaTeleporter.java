package brightspark.structuralrelocation.tileentity;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

/**
 * Created by Mark on 17/04/2017.
 */
public class TileAreaTeleporter extends TileEntity implements ITickable
{
    private LocationArea toMove;
    private Location target;
    private BlockPos curBlock;

    public TileAreaTeleporter() {}

    public void setAreaToMove(LocationArea area)
    {
        toMove = area;
    }

    public void setTarget(Location target)
    {
        this.target = target;
    }

    public void teleort()
    {
        //Called from the block when right clicked
        if(worldObj.isRemote || toMove == null || target == null || curBlock != null)
            return;

        //TODO: Starting an area teleport
    }

    @Override
    public void update()
    {
        //TODO: Finish teleporting an area
    }
}

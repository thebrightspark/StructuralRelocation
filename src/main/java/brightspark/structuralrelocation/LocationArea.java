package brightspark.structuralrelocation;

import brightspark.structuralrelocation.util.LogHelper;
import com.google.common.base.Objects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

public class LocationArea implements INBTSerializable<NBTTagCompound>
{
    public int dimensionId;
    public BlockPos pos1, pos2;

    public LocationArea(int dimensionId, BlockPos position1, BlockPos position2)
    {
        this.dimensionId = dimensionId;
        this.pos1 = new BlockPos(position1);
        this.pos2 = new BlockPos(position2);
    }

    public LocationArea(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    /**
     * Gets the position where the X, Y and Z are at their smallest within the area.
     */
    public BlockPos getStartingPoint()
    {
        int x = Math.min(pos1.getX(), pos2.getX());
        int y = Math.min(pos1.getY(), pos2.getY());
        int z = Math.min(pos1.getZ(), pos2.getZ());
        return new BlockPos(x, y, z);
    }

    /**
     * Gets the position where the X, Y and Z are at their largest within the area.
     */
    public BlockPos getEndPoint()
    {
        int x = Math.max(pos1.getX(), pos2.getX());
        int y = Math.max(pos1.getY(), pos2.getY());
        int z = Math.max(pos1.getZ(), pos2.getZ());
        return new BlockPos(x, y, z);
    }

    /**
     * Gets the a position which represents the size of the area.
     * It's basically the end point minus the start point.
     */
    public BlockPos getRelativeEndPoint()
    {
        return getEndPoint().subtract(getStartingPoint());
    }

    /**
     * Get the length of the area along the axis given.
     */
    public int getSize(EnumFacing.Axis axis)
    {
        BlockPos minPos = getStartingPoint();
        BlockPos maxPos = getEndPoint();
        switch(axis)
        {
            case X:
                return maxPos.getX() - minPos.getX() + 1;
            case Y:
                return maxPos.getY() - minPos.getY() + 1;
            case Z:
                return maxPos.getZ() - minPos.getZ() + 1;
            default:
                LogHelper.error("Unhandled axis!?");
                return -1;
        }
    }

    /**
     * Checks the size of the area in each dimension to check if any are greater than the config's max size
     */
    public boolean isTooBig()
    {
        for(EnumFacing.Axis axis : EnumFacing.Axis.values())
            if(getSize(axis) > Config.maxTeleportAreaSize)
                return true;
        return false;
    }

    public String getSizeString()
    {
        return getSize(EnumFacing.Axis.X) + " x " + getSize(EnumFacing.Axis.Y) + " x " + getSize(EnumFacing.Axis.Z);
    }

    public boolean isEqual(LocationArea area)
    {
        return area != null && area.dimensionId == dimensionId && area.pos1.equals(pos1) && area.pos2.equals(pos2);
    }

    /**
     * Checks if the block position is adjacent to the area
     */
    public boolean isAdjacent(BlockPos pos)
    {
        Vec3d posVec = new Vec3d((double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d);
        AxisAlignedBB areaBox = new AxisAlignedBB(getStartingPoint(), getEndPoint().add(1, 1, 1));
        AxisAlignedBB areaBoxBigger = areaBox.expandXyz(1d);
        boolean isOutsideArea = !areaBox.isVecInside(posVec);
        boolean isOnEdgeOfArea = areaBoxBigger.isVecInside(posVec);
        return isOutsideArea && isOnEdgeOfArea;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", dimensionId);
        tag.setLong("position1", pos1.toLong());
        tag.setLong("position2", pos2.toLong());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        dimensionId = tag.getInteger("dimension");
        pos1 = BlockPos.fromLong(tag.getLong("position1"));
        pos2 = BlockPos.fromLong(tag.getLong("position2"));
    }

    @Override
    public String toString()
    {
        return Objects.toStringHelper(this)
                .add("dim", dimensionId)
                .add("pos1", pos1.toString())
                .add("pos2", pos2.toString()).toString();
    }
}

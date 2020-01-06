package brightspark.structuralrelocation;

import com.google.common.base.MoreObjects;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.INBTSerializable;

public class LocationArea implements INBTSerializable<NBTTagCompound>
{
    public int dimensionId;
    private BlockPos min, max;

    public LocationArea(int dimensionId, BlockPos pos1, BlockPos pos2)
    {
        this.dimensionId = dimensionId;
        min = new BlockPos(Math.min(pos1.getX(), pos2.getX()), Math.min(pos1.getY(), pos2.getY()), Math.min(pos1.getZ(), pos2.getZ()));
        max = new BlockPos(Math.max(pos1.getX(), pos2.getX()), Math.max(pos1.getY(), pos2.getY()), Math.max(pos1.getZ(), pos2.getZ()));
    }

    public LocationArea(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public int getDimensionId()
    {
        return dimensionId;
    }

    public BlockPos getMin()
    {
        return min;
    }

    public BlockPos getMax()
    {
        return max;
    }

    /**
     * Gets the a position which represents the size of the area.
     * It's basically the end point minus the start point.
     */
    public BlockPos getRelativeEndPoint()
    {
        return getMax().subtract(getMin());
    }

    /**
     * Get the length of the area along the axis given.
     */
    public int getSize(EnumFacing.Axis axis)
    {
        switch(axis)
        {
            case X:
                return max.getX() - min.getX() + 1;
            case Y:
                return max.getY() - min.getY() + 1;
            case Z:
                return max.getZ() - min.getZ() + 1;
            default:
                StructuralRelocation.LOGGER.error("Unhandled axis!?");
                return -1;
        }
    }

    /**
     * Checks the size of the area in each dimension to check if any are greater than the config's max size
     */
    public boolean isTooBig()
    {
        for(EnumFacing.Axis axis : EnumFacing.Axis.values())
            if(getSize(axis) > SRConfig.common.maxTeleportAreaSize)
                return true;
        return false;
    }

    public String getSizeString()
    {
        return getSize(EnumFacing.Axis.X) + " x " + getSize(EnumFacing.Axis.Y) + " x " + getSize(EnumFacing.Axis.Z);
    }

    public boolean isEqual(LocationArea area)
    {
        return area != null && area.dimensionId == dimensionId && area.min.equals(min) && area.max.equals(max);
    }

    /**
     * Checks if the block position is adjacent to the area
     */
    public boolean isAdjacent(BlockPos pos)
    {
        Vec3d posVec = new Vec3d((double) pos.getX() + 0.5d, (double) pos.getY() + 0.5d, (double) pos.getZ() + 0.5d);
        AxisAlignedBB areaBox = new AxisAlignedBB(getMin(), getMax().add(1, 1, 1));
        AxisAlignedBB areaBoxBigger = areaBox.grow(1d);
        boolean isOutsideArea = !areaBox.contains(posVec);
        boolean isOnEdgeOfArea = areaBoxBigger.contains(posVec);
        return isOutsideArea && isOnEdgeOfArea;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setInteger("dimension", dimensionId);
        tag.setLong("position1", min.toLong());
        tag.setLong("position2", max.toLong());
        return tag;
    }

    @Override
    public void deserializeNBT(NBTTagCompound tag)
    {
        dimensionId = tag.getInteger("dimension");
        min = BlockPos.fromLong(tag.getLong("position1"));
        max = BlockPos.fromLong(tag.getLong("position2"));
    }

    @Override
    public String toString()
    {
        return MoreObjects.toStringHelper(this)
                .add("dim", dimensionId)
                .add("pos1", min.toString())
                .add("pos2", max.toString()).toString();
    }
}

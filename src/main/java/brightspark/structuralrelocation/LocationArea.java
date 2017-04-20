package brightspark.structuralrelocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
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
}

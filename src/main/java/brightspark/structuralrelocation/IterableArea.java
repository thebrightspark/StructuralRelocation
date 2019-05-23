package brightspark.structuralrelocation;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.INBTSerializable;

public class IterableArea implements INBTSerializable<NBTTagCompound>
{
    private LocationArea area;
    private BlockPos.MutableBlockPos curPos;

    public IterableArea() {}

    public IterableArea(NBTTagCompound nbt)
    {
        deserializeNBT(nbt);
    }

    public LocationArea getArea()
    {
        return area;
    }

    public void setArea(LocationArea area)
    {
        this.area = area;
    }

    public BlockPos getCurPos()
    {
        return curPos == null ? null : curPos.toImmutable();
    }

    public void setCurPos(BlockPos pos)
    {
        if(pos == null)
            curPos = null;
        else if(curPos == null)
            curPos = new BlockPos.MutableBlockPos(pos);
        else
            curPos.setPos(pos);
    }

    public BlockPos getCurPosOffset()
    {
        return curPos == null || area == null ? null : curPos.subtract(area.getMin());
    }

    public void resetCurPos()
    {
        if(area != null)
            curPos = new BlockPos.MutableBlockPos(area.getMin());
    }

    public void next()
    {
        if(area == null)
            return;
        if(curPos == null)
            resetCurPos();
        //Move to next pos
        curPos.move(EnumFacing.EAST);

        //If reached max X then go back to min X and add 1 to Z
        if(curPos.getX() > area.getMax().getX())
            curPos.setPos(area.getMin().getX(), curPos.getY(), curPos.south().getZ());

        //If reached max Z then go back to min Z and add 1 to Y
        if(curPos.getZ() > area.getMax().getZ())
            curPos.setPos(curPos.getX(), curPos.up().getY(), area.getMin().getZ());

        //If reached max Y, then finished iterating
        if(curPos.getY() > area.getMax().getY())
            curPos = null;
    }

    @Override
    public NBTTagCompound serializeNBT()
    {
        NBTTagCompound nbt = new NBTTagCompound();
        if(area != null)
            nbt.setTag("area", area.serializeNBT());
        if(curPos != null)
            nbt.setLong("curPos", curPos.toLong());
        return nbt;
    }

    @Override
    public void deserializeNBT(NBTTagCompound nbt)
    {
        area = nbt.hasKey("area") ? new LocationArea(nbt.getCompoundTag("area")) : null;
        curPos = nbt.hasKey("curPos") ? new BlockPos.MutableBlockPos(BlockPos.fromLong(nbt.getLong("curPos"))) : null;
    }
}

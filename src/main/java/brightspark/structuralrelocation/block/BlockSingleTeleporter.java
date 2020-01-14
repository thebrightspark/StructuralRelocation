package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class BlockSingleTeleporter extends AbstractBlockTeleporter
{
    public BlockSingleTeleporter()
    {
        super("single_teleporter");
    }

    @Override
    public TileEntity createNewTileEntity(World worldIn, int meta)
    {
        return new TileSingleTeleporter();
    }
}

package brightspark.structuralrelocation.block;

import brightspark.structuralrelocation.gui.ContainerTeleporter;
import brightspark.structuralrelocation.gui.GuiTeleporter;
import brightspark.structuralrelocation.item.ItemSelector;
import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;

public abstract class AbstractBlockTeleporter extends AbstractBlockContainer
{
    public AbstractBlockTeleporter(String name, Material mat)
    {
        super(name, mat);
        setHasGui();
    }

    @SideOnly(Side.CLIENT)
    public GuiScreen getGui(InventoryPlayer invPlayer, TileEntity te)
    {
        return new GuiTeleporter(invPlayer, (AbstractTileTeleporter) te);
    }

    public Container getContainer(InventoryPlayer invPlayer, TileEntity te)
    {
        return new ContainerTeleporter(invPlayer, (AbstractTileTeleporter) te);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ)
    {
        //LogHelper.info("Item Activated With: " + (heldItem == null ? "Null" : heldItem.toString()));
        if(heldItem != null && heldItem.getItem() instanceof ItemSelector)
            return false;
        return super.onBlockActivated(world, pos, state, player, hand, heldItem, side, hitX, hitY, hitZ);

        /*
        TileEntity te = world.getTileEntity(pos);
        if(te instanceof AbstractTileTeleporter && !player.isSneaking())
        {
            //Try to start teleporting
            ((AbstractTileTeleporter) te).teleport(player);
        }
        return true;
        */
    }
}

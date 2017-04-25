package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.block.BlockAreaTeleporter;
import brightspark.structuralrelocation.block.BlockSingleTeleporter;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagInt;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;

import java.util.List;

public class ItemSelector extends ItemBasic
{
    private enum EnumSelection
    {
        SINGLE,
        AREA;

        public static EnumSelection getMode(int id)
        {
            if(id < 0) id = 0;
            if(id >= values().length) id = values().length - 1;
            return values()[id];
        }

        public EnumSelection next()
        {
            int nextI = ordinal() + 1;
            if(nextI >= values().length)
                nextI = 0;
            return values()[nextI];
        }
    }

    private Location areaLoc1 = null;

    public ItemSelector()
    {
        super("selector");
        setMaxStackSize(1);
    }

    private static void setTarget(ItemStack stack, Location location)
    {
        stack.setTagInfo("location", location.serializeNBT());
    }

    private static void setArea(ItemStack stack, LocationArea area)
    {
        stack.setTagInfo("area", area.serializeNBT());
    }

    private static Location getTarget(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return null;
        NBTTagCompound locTag = tag.getCompoundTag("location");
        return locTag.getSize() == 0 ? null : new Location(locTag);
    }

    private static LocationArea getArea(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return null;
        NBTTagCompound areaTag = tag.getCompoundTag("area");
        return areaTag.getSize() == 0 ? null : new LocationArea(areaTag);
    }

    private static void nextMode(ItemStack stack)
    {
        EnumSelection mode = getMode(stack);
        stack.setTagInfo("mode", new NBTTagInt(mode.next().ordinal()));
    }

    private static EnumSelection getMode(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return EnumSelection.SINGLE;
        return EnumSelection.getMode(tag.getInteger("mode"));
    }

    /**
     * This is called when the item is used, before the block is activated.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        TileEntity te = world.getTileEntity(pos);
        boolean hasTarget = getTarget(stack) != null;
        boolean hasArea = getArea(stack) != null;
        boolean flag = false;
        if((hasTarget || hasArea) && te != null)
        {
            switch(getMode(stack))
            {
                case AREA:
                    //Set area
                    if(!hasArea) break;
                    flag = true;
                    if(te instanceof TileAreaTeleporter)
                        ((TileAreaTeleporter) te).setAreaToMove(getArea(stack));
                    if(world.isRemote)
                        player.addChatMessage(new TextComponentString("Teleporter Area Set!"));
                    break;
                case SINGLE:
                    //Set target
                    if(!hasTarget) break;
                    flag = true;
                    if(te instanceof TileSingleTeleporter)
                        ((TileSingleTeleporter) te).setTarget(getTarget(stack));
                    if(te instanceof TileAreaTeleporter)
                        ((TileAreaTeleporter) te).setTarget(getTarget(stack));
                    if(world.isRemote)
                        player.addChatMessage(new TextComponentString("Teleporter Target Set!"));
            }
        }

        if(!flag)
        {
            //Set target/area location
            Block block = world.getBlockState(pos).getBlock();
            if(!(block instanceof BlockSingleTeleporter) && !(block instanceof BlockAreaTeleporter))
            {
                BlockPos posToSave = player.isSneaking() ? pos : pos.offset(side);
                switch(getMode(stack))
                {
                    case SINGLE:
                        //Set target
                        setTarget(stack, new Location(player.dimension, posToSave));
                        if(world.isRemote) player.addChatMessage(new TextComponentString("Set Target"));
                        break;
                    case AREA:
                        if(areaLoc1 == null)
                        {
                            //Set the first location
                            areaLoc1 = new Location(player.dimension, posToSave);
                            if(world.isRemote) player.addChatMessage(new TextComponentString("Position 1 set!"));
                        }
                        else
                        {
                            if(areaLoc1.dimensionId != player.dimension)
                            {
                                //Trying to set 2nd position in a different dimension
                                if(world.isRemote) player.addChatMessage(new TextComponentString("Both positions must be in the same dimension!"));
                            }
                            else
                            {
                                //Set the second position and complete the area
                                setArea(stack, new LocationArea(areaLoc1.dimensionId, areaLoc1.position, posToSave));
                                if(world.isRemote) player.addChatMessage(new TextComponentString("Position 2 set!"));
                            }
                            areaLoc1 = null;
                        }
                }
                return EnumActionResult.SUCCESS;
            }
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(playerIn.isSneaking())
        {
            //Switch mode
            nextMode(itemStackIn);
            if(worldIn.isRemote)
                playerIn.addChatMessage(new TextComponentString("Change mode to " + getMode(itemStackIn).toString().toLowerCase() + " mode."));
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
    }

    private static String posToString(BlockPos pos)
    {
        return TextFormatting.WHITE + "X: " + TextFormatting.GRAY + pos.getX() +
                TextFormatting.WHITE + " Y: " + TextFormatting.GRAY + pos.getY() +
                TextFormatting.WHITE + " Z: " + TextFormatting.GRAY + pos.getZ();
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        EnumSelection mode = getMode(stack);
        tooltip.add(TextFormatting.YELLOW + "Mode: " + TextFormatting.GRAY + mode.toString().toUpperCase());
        tooltip.add("");
        switch(mode)
        {
            case SINGLE:
                Location loc = getTarget(stack);
                if(loc == null)
                    tooltip.add("No location set!");
                else
                {
                    BlockPos target = loc.position;
                    tooltip.add(TextFormatting.GOLD + "Target Dimension ID: " + TextFormatting.GRAY + loc.dimensionId);
                    tooltip.add(TextFormatting.GOLD + "Target Position: " + posToString(target));
                }
                break;
            case AREA:
                if(areaLoc1 == null)
                {
                    LocationArea area = getArea(stack);
                    if(area == null)
                        tooltip.add("No area set!");
                    else
                    {
                        //Area set
                        BlockPos pos1 = area.pos1;
                        BlockPos pos2 = area.pos2;
                        tooltip.add(TextFormatting.GOLD + "Target Dimension ID: " + TextFormatting.GRAY + area.dimensionId);
                        tooltip.add(TextFormatting.GOLD + "Target Area Pos 1: " + posToString(pos1));
                        tooltip.add(TextFormatting.GOLD + "Target Area Pos 2: " + posToString(pos2));
                    }
                }
                else
                {
                    //Only the first position is set
                    BlockPos pos = areaLoc1.position;
                    tooltip.add(TextFormatting.GOLD + "Target Dimension ID: " + TextFormatting.GRAY + areaLoc1.dimensionId);
                    tooltip.add(TextFormatting.GOLD + "Target Area Pos 1: " + posToString(pos));
                    tooltip.add(TextFormatting.GOLD + "Target Area Pos 2: " + TextFormatting.GRAY + "Not yet set");
                }
        }
    }
}

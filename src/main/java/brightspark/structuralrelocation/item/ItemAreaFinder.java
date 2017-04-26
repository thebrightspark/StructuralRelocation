package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.block.BlockAreaTeleporter;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class ItemAreaFinder extends ItemBasic
{
    private Location location1;

    public ItemAreaFinder()
    {
        super("areaFinder");
        setMaxStackSize(1);
    }

    public static void setArea(ItemStack stack, LocationArea area)
    {
        stack.setTagInfo("area", area.serializeNBT());
    }

    public static LocationArea getArea(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) return null;
        NBTTagCompound areaTag = tag.getCompoundTag("area");
        return areaTag.getSize() == 0 ? null : new LocationArea(areaTag);
    }

    public static boolean isAreaSet(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag != null && tag.hasKey("area");
    }

    public static void clearArea(ItemStack stack)
    {
        CommonUtils.clearNBTKeys(stack, "area");
    }

    /**
     * This is called when the item is used, before the block is activated.
     * @param stack The Item Stack
     * @param player The Player that used the item
     * @param world The Current World
     * @param pos Target position
     * @param side The side of the target hit
     * @param hand Which hand the item is being held in.
     * @return Return PASS to allow vanilla handling, any other to skip normal code.
     */
    @Override
    public EnumActionResult onItemUseFirst(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side, float hitX, float hitY, float hitZ, EnumHand hand)
    {
        if(!(world.getBlockState(pos).getBlock() instanceof BlockAreaTeleporter))
        {
            if(isAreaSet(stack))
            {
                //Don't try and overwrite what's already saved
                if(world.isRemote)
                    player.sendMessage(new TextComponentString("Area already set. Shift right click in the air to clear the current area."));
                return EnumActionResult.SUCCESS;
            }
            if(location1 == null)
            {
                //Set the first position
                location1 = new Location(player.dimension, player.isSneaking() ? pos : pos.offset(side));
                if(world.isRemote) player.sendMessage(new TextComponentString("Position 1 set!"));
            }
            else
            {
                if(location1.dimensionId != player.dimension)
                {
                    //Trying to set 2nd position in a different dimension
                    location1 = null;
                    if(world.isRemote) player.sendMessage(new TextComponentString("Both positions must be in the same dimension!"));
                }
                else
                {
                    //Set the second position and complete the area
                    setArea(stack, new LocationArea(location1.dimensionId, location1.position, player.isSneaking() ? pos : pos.offset(side)));
                    location1 = null;
                    if(world.isRemote) player.sendMessage(new TextComponentString("Position 2 set!"));
                }
            }
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(playerIn.isSneaking())
        {
            //Clear any data already set
            clearArea(itemStackIn);
            location1 = null;
            if(worldIn.isRemote) playerIn.sendMessage(new TextComponentString("Area Cleared"));
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        if(location1 != null)
        {
            //Only the first position is set
            BlockPos pos = location1.position;
            tooltip.add("Target Dimension ID: " + location1.dimensionId);
            tooltip.add("Target Area Pos 1: X: " + pos.getX() + ", Y: " + pos.getY() + ", Z: " + pos.getZ());
            tooltip.add("Target Area Pos 2: Not yet set");
        }
        else
        {
            LocationArea area = getArea(stack);
            if(area != null)
            {
                BlockPos pos1 = area.pos1;
                BlockPos pos2 = area.pos2;
                tooltip.add("Target Dimension ID: " + area.dimensionId);
                tooltip.add("Target Area Pos 1: X: " + pos1.getX() + ", Y: " + pos1.getY() + ", Z: " + pos1.getZ());
                tooltip.add("Target Area Pos 2: X: " + pos2.getX() + ", Y: " + pos2.getY() + ", Z: " + pos2.getZ());
            }
        }
    }
}

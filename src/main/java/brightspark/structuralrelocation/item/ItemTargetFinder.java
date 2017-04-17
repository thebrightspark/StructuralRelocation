package brightspark.structuralrelocation.item;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.block.BlockSingleTeleporter;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.World;

import java.util.List;

public class ItemTargetFinder extends ItemBasic
{
    public ItemTargetFinder()
    {
        super("targetFinder");
        setMaxStackSize(1);
    }

    public static void setTarget(ItemStack stack, Location location)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag == null) tag = new NBTTagCompound();
        stack.setTagCompound(location.saveToNBT(tag));
    }

    public static void setTarget(ItemStack stack, int currentDimensionId, BlockPos pos)
    {
        setTarget(stack, new Location(currentDimensionId, pos));
    }

    public static Location getTarget(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        return tag == null ? null : new Location(tag);
    }

    public static void clearTarget(ItemStack stack)
    {
        NBTTagCompound tag = stack.getTagCompound();
        if(tag != null)
        {
            tag.removeTag("position");
            tag.removeTag("dimension");
        }
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
        if(!(world.getBlockState(pos).getBlock() instanceof BlockSingleTeleporter))
        {
            setTarget(stack, player.dimension, player.isSneaking() ? pos : pos.offset(side));
            player.addChatMessage(new TextComponentString("Set Target"));
            return EnumActionResult.SUCCESS;
        }
        return EnumActionResult.PASS;
    }

    @Override
    public ActionResult<ItemStack> onItemRightClick(ItemStack itemStackIn, World worldIn, EntityPlayer playerIn, EnumHand hand)
    {
        if(playerIn.isSneaking())
        {
            clearTarget(itemStackIn);
            return new ActionResult<ItemStack>(EnumActionResult.SUCCESS, itemStackIn);
        }
        return new ActionResult<ItemStack>(EnumActionResult.PASS, itemStackIn);
    }

    @Override
    public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced)
    {
        Location location = getTarget(stack);
        if(location != null)
        {
            BlockPos target = location.position;
            tooltip.add("Target Dimension ID: " + location.dimensionId);
            tooltip.add("Target Position: X: " + target.getX() + ", Y: " + target.getY() + ", Z: " + target.getZ());
        }
    }
}

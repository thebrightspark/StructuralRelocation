package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.SRConfig;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.init.SRItems;
import brightspark.structuralrelocation.item.ItemDebugger;
import brightspark.structuralrelocation.item.ItemSelector;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.awt.*;
import java.util.Iterator;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler
{
    private static Minecraft mc = Minecraft.getMinecraft();
    private static Color boxRenderColour;
    private static String prevBoxColour = null;

    private static Color getBoxColour()
    {
        String colourString = SRConfig.boxRenderColour;
        if(prevBoxColour == null)
            prevBoxColour = colourString;
        else if(prevBoxColour.equals(colourString))
            return boxRenderColour;

        if(colourString.startsWith("0x"))
        {
            //Hexadecimal value
            try
            {
                boxRenderColour = new Color(Integer.parseInt(colourString.substring(2), 16));
            }
            catch(NumberFormatException e)
            {
                StructuralRelocation.LOGGER.error("Couldn't parse colour " + colourString + " for the config boxRenderColour as a hexadecimal value. Using default value.");
            }
        }
        else if(colourString.contains(","))
        {
            //RGB value
            String[] rgb = colourString.split(",");
            if(rgb.length == 3)
            {
                try
                {
                    int r = Integer.parseInt(rgb[0].trim());
                    int g = Integer.parseInt(rgb[1].trim());
                    int b = Integer.parseInt(rgb[2].trim());
                    boxRenderColour = new Color(r, g, b);
                }
                catch(NumberFormatException e)
                {
                    StructuralRelocation.LOGGER.error("Couldn't parse colour" + colourString + " for the config boxRenderColour as an RGB value. Using default value.");
                }
                catch(IllegalArgumentException e)
                {
                    StructuralRelocation.LOGGER.error("Couldn't parse colour" + colourString + " for the config boxRenderColour as an RGB value - values must be between 0 and 255. Using default value.");
                }
            }
            else
                StructuralRelocation.LOGGER.error("Couldn't parse colour" + colourString + " for the config boxRenderColour as an RGB value. Using default value.");
        }
        else
        {
            //Integer
            try
            {
                boxRenderColour = new Color(Integer.parseInt(colourString));
            }
            catch(NumberFormatException e)
            {
                StructuralRelocation.LOGGER.error("Couldn't parse colour " + colourString + " for the config boxRenderColour as an integer. Using default value.");
            }
        }

        return boxRenderColour;
    }

    private static ItemStack getHeldItem(Item item)
    {
        Iterator<ItemStack> heldItems = mc.player.getHeldEquipment().iterator();
        while(heldItems.hasNext())
        {
            ItemStack held = heldItems.next();
            if(held != null && held.getItem().equals(item))
                return held;
        }
        return null;
    }

    private static void renderBox(BlockPos pos, double partialTicks)
    {
        renderBox(new AxisAlignedBB(pos).grow(0.001d), partialTicks);
    }

    private static void renderBox(BlockPos pos1, BlockPos pos2, double partialTicks)
    {
        renderBox(new AxisAlignedBB(pos1, pos2).grow(0.001d), partialTicks);
    }

    private static void renderBox(AxisAlignedBB box, double partialTicks)
    {
        //Get player's actual position
        EntityPlayerSP player = mc.player;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        //Render the box
        GlStateManager.pushMatrix();
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.glLineWidth(5f);
        GlStateManager.disableTexture2D();
        GlStateManager.translate(-x, -y, -z);
        float[] rgb = getBoxColour().getRGBColorComponents(null);
        RenderGlobal.renderFilledBox(box, rgb[0], rgb[1], rgb[2], 0.2f);
        RenderGlobal.drawSelectionBoundingBox(box, rgb[0], rgb[1], rgb[2], 0.4f);
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void renderSelection(RenderWorldLastEvent event)
    {
        //Get held Selector item
        ItemStack heldItem = getHeldItem(SRItems.itemSelector);
        if(heldItem != null)
        {
            //Get selector mode
            ItemSelector.EnumSelection mode = ItemSelector.getMode(heldItem);
            if(mode == ItemSelector.EnumSelection.SINGLE)
            {
                Location location = ItemSelector.getTarget(heldItem);
                if(location == null || location.dimensionId != mc.player.dimension) return;
                //Render single selected position
                renderBox(location.position, event.getPartialTicks());
            }
            else
            {
                Location areaLoc1 = ItemSelector.getAreaLoc1(heldItem);
                if(areaLoc1 == null)
                {
                    LocationArea area = ItemSelector.getArea(heldItem);
                    if(area == null || area.dimensionId != mc.player.dimension) return;
                    //Render selected area
                    renderBox(area.getMin(), area.getMax().add(1, 1, 1), event.getPartialTicks());

                }
                else
                {
                    if(areaLoc1.dimensionId != mc.player.dimension) return;
                    //Render first area location
                    renderBox(areaLoc1.position, event.getPartialTicks());
                }
            }
        }

        //Get held Debugger item
        heldItem = getHeldItem(SRItems.itemDebugger);
        if(heldItem != null)
        {
            //Get selected teleporter
            Location location = ItemDebugger.getTeleporterLoc(heldItem);
            if(location == null || location.dimensionId != mc.player.dimension) return;
            TileEntity te = mc.world.getTileEntity(location.position);
            if(te != null && te instanceof TileAreaTeleporter && ((TileAreaTeleporter) te).lastBlockInTheWay != null)
                renderBox(((TileAreaTeleporter) te).lastBlockInTheWay, event.getPartialTicks());
        }
    }
}

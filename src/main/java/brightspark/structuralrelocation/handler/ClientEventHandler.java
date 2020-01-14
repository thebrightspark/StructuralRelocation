package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.SRConfig;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.init.SRItems;
import brightspark.structuralrelocation.item.ItemDebugger;
import brightspark.structuralrelocation.item.ItemSelector;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.util.RedrawableTesselator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import org.lwjgl.opengl.GL11;

import java.awt.*;

@Mod.EventBusSubscriber(modid = StructuralRelocation.MOD_ID, value = Side.CLIENT)
public class ClientEventHandler
{
    private static Minecraft mc = Minecraft.getMinecraft();
    private static Color boxRenderColour;
    private static String prevBoxColour = null;

    private static final RedrawableTesselator redrawableTargetDirs =
        new RedrawableTesselator(13 * 4 * 28, GL11.GL_LINES, DefaultVertexFormats.POSITION_COLOR, bufferBuilder -> {
            float alpha = 0.5F;
            float[] colorParts = Color.BLUE.getRGBComponents(null);
            bufferBuilder.pos(0, 0, 0).color(colorParts[0], colorParts[1], colorParts[2], alpha).endVertex();
            bufferBuilder.pos(0, 0, 0.5D).color(colorParts[0], colorParts[1], colorParts[2], alpha).endVertex();

            colorParts = Color.RED.getRGBComponents(null);
            bufferBuilder.pos(0, 0, 0).color(colorParts[0], colorParts[1], colorParts[2], alpha).endVertex();
            bufferBuilder.pos(0.5D, 0, 0).color(colorParts[0], colorParts[1], colorParts[2], alpha).endVertex();

            colorParts = Color.GREEN.getRGBComponents(null);
            bufferBuilder.pos(0, 0, 0).color(colorParts[0], colorParts[1], colorParts[2], alpha).endVertex();
            bufferBuilder.pos(0, 0.5D, 0).color(colorParts[0], colorParts[1], colorParts[2], alpha).endVertex();
        });

    private static Color getBoxColour()
    {
        String colourString = SRConfig.client.boxRenderColour;
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
                StructuralRelocation.LOGGER.error("Couldn't parse colour {} for the config boxRenderColour as a hexadecimal value. Using default value.", colourString);
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
                    StructuralRelocation.LOGGER.error("Couldn't parse colour {} for the config boxRenderColour as an RGB value. Using default value.", colourString);
                }
                catch(IllegalArgumentException e)
                {
                    StructuralRelocation.LOGGER.error("Couldn't parse colour {} for the config boxRenderColour as an RGB value - values must be between 0 and 255. Using default value.", colourString);
                }
            }
            else
                StructuralRelocation.LOGGER.error("Couldn't parse colour {} for the config boxRenderColour as an RGB value. Using default value.", colourString);
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
                StructuralRelocation.LOGGER.error("Couldn't parse colour {} for the config boxRenderColour as an integer. Using default value.", colourString);
            }
        }

        return boxRenderColour;
    }

    private static ItemStack getHeldItem(Item item)
    {
        for(ItemStack held : mc.player.getHeldEquipment())
            if(held != null && held.getItem().equals(item))
                return held;
        return null;
    }

    private static Vec3d getActualPlayerPos(double partialTicks)
    {
        EntityPlayerSP player = mc.player;
        double x = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
        double y = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
        double z = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
        return new Vec3d(x, y, z);
    }

    private static void preRender()
    {
        GlStateManager.pushMatrix();
        GlStateManager.glLineWidth(5f);
        GlStateManager.enableAlpha();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.disableTexture2D();
    }

    private static void postRender()
    {
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }

    private static void renderBox(BlockPos pos, double partialTicks, boolean fill)
    {
        preRender();
        renderBox(new AxisAlignedBB(pos).grow(0.001d), partialTicks, fill);
        if(!fill) renderTargetDirections(pos, partialTicks);
        postRender();
    }

    private static void renderBox(BlockPos pos1, BlockPos pos2, double partialTicks)
    {
        preRender();
        renderBox(new AxisAlignedBB(pos1, pos2).grow(0.001d), partialTicks, true);
        postRender();
    }

    private static void renderBox(AxisAlignedBB box, double partialTicks, boolean fill)
    {
        GlStateManager.pushMatrix();
        Vec3d playerPos = getActualPlayerPos(partialTicks);
        GlStateManager.translate(-playerPos.x, -playerPos.y, -playerPos.z);
        float[] rgb = getBoxColour().getRGBColorComponents(null);
        if(fill) RenderGlobal.renderFilledBox(box, rgb[0], rgb[1], rgb[2], 0.2f);
        RenderGlobal.drawSelectionBoundingBox(box, rgb[0], rgb[1], rgb[2], 0.4f);
        GlStateManager.popMatrix();
    }

    private static void renderTargetDirections(BlockPos pos, double partialTicks)
    {
        GlStateManager.pushMatrix();
        Vec3d translation = new Vec3d(pos).subtract(getActualPlayerPos(partialTicks)).add(0.5D, 0.5D, 0.5D);
        GlStateManager.translate(translation.x, translation.y, translation.z);
        redrawableTargetDirs.draw();
        GlStateManager.popMatrix();
    }

    @SubscribeEvent
    public static void renderSelection(RenderWorldLastEvent event)
    {
        float partialTicks = event.getPartialTicks();

        //Get held Selector item
        ItemStack heldItem = getHeldItem(SRItems.selector);
        if(heldItem != null)
        {
            //Get selector mode
            ItemSelector.EnumSelection mode = ItemSelector.getMode(heldItem);
            if(mode == ItemSelector.EnumSelection.SINGLE)
            {
                Location location = ItemSelector.getTarget(heldItem);
                if(location == null || location.dimensionId != mc.player.dimension) return;
                //Render single selected position
                renderBox(location.position, partialTicks, false);
            }
            else
            {
                Location areaLoc1 = ItemSelector.getAreaLoc1(heldItem);
                if(areaLoc1 == null)
                {
                    LocationArea area = ItemSelector.getArea(heldItem);
                    if(area == null || area.dimensionId != mc.player.dimension) return;
                    //Render selected area
                    renderBox(area.getMin(), area.getMax().add(1, 1, 1), partialTicks);

                }
                else
                {
                    if(areaLoc1.dimensionId != mc.player.dimension) return;
                    //Render first area location
                    renderBox(areaLoc1.position, partialTicks, true);
                }
            }
        }

        //Get held Debugger item
        heldItem = getHeldItem(SRItems.debugger);
        if(heldItem != null)
        {
            //Get selected teleporter
            Location location = ItemDebugger.getTeleporterLoc(heldItem);
            if(location == null || location.dimensionId != mc.player.dimension) return;
            TileEntity te = mc.world.getTileEntity(location.position);
            if(te instanceof TileAreaTeleporter && ((TileAreaTeleporter) te).lastBlockInTheWay != null)
                renderBox(((TileAreaTeleporter) te).lastBlockInTheWay, partialTicks, true);
        }
    }
}

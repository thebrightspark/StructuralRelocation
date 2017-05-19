package brightspark.structuralrelocation.handler;

import brightspark.structuralrelocation.Config;
import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
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

import java.util.Iterator;

@Mod.EventBusSubscriber(Side.CLIENT)
public class ClientEventHandler
{
    private static Minecraft mc = Minecraft.getMinecraft();

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
        renderBox(new AxisAlignedBB(pos).expandXyz(0.001d), partialTicks);
    }

    private static void renderBox(BlockPos pos1, BlockPos pos2, double partialTicks)
    {
        renderBox(new AxisAlignedBB(pos1, pos2).expandXyz(0.001d), partialTicks);
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
        float[] rgb = Config.boxRenderColour.getRGBColorComponents(null);
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
                LocationArea area = ItemSelector.getArea(heldItem);
                if(area == null || area.dimensionId != mc.player.dimension) return;
                //Render selected area
                renderBox(area.getStartingPoint(), area.getEndPoint().add(1, 1, 1), event.getPartialTicks());
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

package brightspark.structuralrelocation.gui;

import brightspark.structuralrelocation.Location;
import brightspark.structuralrelocation.LocationArea;
import brightspark.structuralrelocation.StructuralRelocation;
import brightspark.structuralrelocation.message.MessageGuiTeleport;
import brightspark.structuralrelocation.tileentity.AbstractTileTeleporter;
import brightspark.structuralrelocation.tileentity.TileAreaTeleporter;
import brightspark.structuralrelocation.tileentity.TileSingleTeleporter;
import brightspark.structuralrelocation.util.CommonUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class GuiTeleporter extends GuiContainer
{
    public ResourceLocation guiImage = new ResourceLocation(StructuralRelocation.MOD_ID, StructuralRelocation.GUI_TEXTURE_DIR + "teleporter.png");
    protected int textColour = 4210752;
    public AbstractTileTeleporter teleporter;
    protected boolean isAreaTeleporter;
    protected final Rectangle energyBar = new Rectangle(11, 16, 10, 63);
    protected List<Icon> iconList = new ArrayList<>();

    private Icon iconTarget, iconArea, iconStatus;

    public GuiTeleporter(InventoryPlayer invPlayer, AbstractTileTeleporter teleporter)
    {
        super(new ContainerTeleporter(invPlayer, teleporter));
        this.teleporter = teleporter;
        isAreaTeleporter = teleporter instanceof TileAreaTeleporter;
        xSize = 176;
        ySize = 175;
    }

    /**
     * Adds the buttons (and other controls) to the screen in question. Called when the GUI is displayed and when the
     * window resizes, the buttonList is cleared beforehand.
     */
    @Override
    public void initGui()
    {
        super.initGui();

        if(isAreaTeleporter)
        {
            buttonList.add(new Button(25, 57, "Teleport"));
            buttonList.add(new Button(74, 57, "Copy"));
            buttonList.add(new Button(123, 57, "Stop", false));
        }
        else
        {
            buttonList.add(new Button(41, 57, "Teleport"));
            buttonList.add(new Button(107, 57, "Copy"));
        }

        /*
        Icons:
        0 - Target block
        1 - Destination area
        2 - Teleporter status (teleporting progress, if out of power, etc)
         */
        if(isAreaTeleporter)
        {
            iconList.add(iconTarget = new Icon(46, 23, 0, EnumIconBackground.OFF));
            iconList.add(iconArea = new Icon(87, 23, 1, EnumIconBackground.OFF));
            iconList.add(iconStatus = new Icon(128, 23, 2, EnumIconBackground.OFF));
        }
        else
        {
            iconList.add(iconTarget = new Icon(66, 23, 0, EnumIconBackground.OFF));
            iconList.add(iconStatus = new Icon(108, 23, 2, EnumIconBackground.OFF));
        }
    }

    public void updateIcons()
    {
        if(isAreaTeleporter)
        {
            TileAreaTeleporter areaTE = (TileAreaTeleporter) teleporter;

            //Target Icon
            Location target = areaTE.getTarget();
            List<String> tooltipTarget = new ArrayList<>();
            tooltipTarget.add(TextFormatting.GOLD + "Destination Target:");
            if(target == null)
                tooltipTarget.add(TextFormatting.RED + "Target not set!");
            else
            {
                tooltipTarget.add(TextFormatting.YELLOW + "Dimension ID: " + TextFormatting.GRAY + target.dimensionId);
                tooltipTarget.add(TextFormatting.YELLOW + "Position: " + CommonUtils.posToString(target.position));
            }
            iconTarget.setTooltip(tooltipTarget);
            iconTarget.setBackground(target == null ? EnumIconBackground.OFF : EnumIconBackground.ON);

            //Area Icon
            LocationArea area = areaTE.getAreaToMove();
            List<String> tooltipArea = new ArrayList<>();
            tooltipArea.add(TextFormatting.GOLD + "Area To Move:");
            if(area == null)
                tooltipArea.add(TextFormatting.RED + "Area not set!");
            else
            {
                tooltipArea.add(TextFormatting.YELLOW + "Dimension ID: " + TextFormatting.GRAY + area.dimensionId);
                tooltipArea.add(TextFormatting.YELLOW + "Position 1: " + CommonUtils.posToString(area.pos1));
                tooltipArea.add(TextFormatting.YELLOW + "Position 2: " + CommonUtils.posToString(area.pos2));
                tooltipArea.add(TextFormatting.YELLOW + "Size: " + TextFormatting.GRAY + area.getSizeString());
            }
            iconArea.setTooltip(tooltipArea);
            iconArea.setBackground(areaTE.getAreaToMove() == null ? EnumIconBackground.OFF : EnumIconBackground.ON);

            //Status Icon
            EnumIconBackground status = areaTE.isActive() ? areaTE.hasEnoughEnergy() ? EnumIconBackground.ON : EnumIconBackground.RED : EnumIconBackground.OFF;
            List<String> tooltipStatus = new ArrayList<>();
            tooltipStatus.add(TextFormatting.GOLD + "Status:");
            switch(status)
            {
                case OFF:
                    tooltipStatus.add(TextFormatting.BLUE + "Inactive");
                    break;
                case ON:
                    tooltipStatus.add(TextFormatting.GREEN + "Active");
                    break;
                case RED:
                    tooltipStatus.add(TextFormatting.RED + "Out of energy!");
            }
            iconStatus.setTooltip(tooltipStatus);
            iconStatus.setBackground(status);
        }
        else
        {
            TileSingleTeleporter singleTE = (TileSingleTeleporter) teleporter;

            //Target Icon
            Location target = singleTE.getTarget();
            List<String> tooltipTarget = new ArrayList<>();
            tooltipTarget.add(TextFormatting.GOLD + "Destination Target:");
            if(target == null)
                tooltipTarget.add(TextFormatting.RED + "Target not set!");
            else
            {
                tooltipTarget.add(TextFormatting.YELLOW + "Dimension ID: " + TextFormatting.GRAY + target.dimensionId);
                tooltipTarget.add(TextFormatting.YELLOW + "Block Position: " + CommonUtils.posToString(target.position));
            }
            iconTarget.setTooltip(tooltipTarget);
            iconTarget.setBackground(singleTE.getTarget() == null ? EnumIconBackground.OFF : EnumIconBackground.ON);

            //Status Icon
            boolean hasEnoughEnergy = singleTE.hasEnoughEnergy();
            List<String> tooltipStatus = new ArrayList<>();
            tooltipStatus.add(TextFormatting.GOLD + "Status:");
            if(target == null)
                tooltipStatus.add(TextFormatting.RED + "Target not set!");
            else
            {
                if(hasEnoughEnergy)
                    tooltipStatus.add(TextFormatting.BLUE + "Inactive");
                else
                    tooltipStatus.add(TextFormatting.RED + "Out of energy!");
            }
            iconStatus.setTooltip(tooltipStatus);
            iconStatus.setBackground(target != null && hasEnoughEnergy ? EnumIconBackground.OFF : EnumIconBackground.RED);
        }
    }

    public void updateButtons()
    {
        boolean isActive = isAreaTeleporter && ((TileAreaTeleporter) teleporter).isActive();
        boolean hasEnergy = teleporter.hasEnoughEnergy();
        boolean hasTargets = iconTarget.getBackground() == EnumIconBackground.ON && (!isAreaTeleporter || iconArea.getBackground() == EnumIconBackground.ON);
        for(GuiButton button : buttonList)
        {
            switch(button.id)
            {
                case 0: //Teleport
                    button.enabled = !isActive && hasEnergy && hasTargets;
                    break;
                case 1: //Copy
                    button.enabled = mc.player.capabilities.isCreativeMode && !isActive && hasEnergy && hasTargets;
                    break;
                case 2: //Stop
                    button.enabled = isActive;
            }
        }
    }

    @Override
    protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY)
    {
        //Draw gui
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.getTextureManager().bindTexture(guiImage);
        drawTexturedModalRect(guiLeft, guiTop, 0, 0, xSize, ySize);

        //Draw energy bar
        int pixelsHigh = Math.round(energyBar.height * teleporter.getEnergyPercent());
        int correctYPos = energyBar.height - pixelsHigh;
        drawTexturedModalRect(guiLeft + energyBar.x, guiTop + energyBar.y + correctYPos, 176, correctYPos, energyBar.width, pixelsHigh);

        //Needs to be done here, before they're drawn
        updateButtons();
    }

    @Override
    protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY)
    {
        updateIcons();

        //Draw icons
        for(Icon icon : iconList)
            icon.drawIcon(mc);

        drawText();

        mouseX -= guiLeft;
        mouseY -= guiTop;
        List<String> tooltip = new ArrayList<String>();
        drawTooltips(tooltip, mouseX, mouseY);
        if(!tooltip.isEmpty())
            drawHoveringText(tooltip, mouseX, mouseY);
    }

    protected void drawText()
    {
        fontRenderer.drawString(I18n.format(teleporter.getBlockType().getUnlocalizedName() + ".name"), 8, 6, textColour);
        fontRenderer.drawString(I18n.format("container.inventory"), 8, 82, textColour);

        //TODO: Draw any extra text
    }

    protected void drawTooltips(List<String> tooltip, int mouseX, int mouseY)
    {
        //Draw energy bar tooltip
        if(energyBar.contains(mouseX, mouseY))
        {
            tooltip.add("Energy: " + CommonUtils.addDigitGrouping(teleporter.getEnergyStored()) + " RF");
            tooltip.add("Max: " + CommonUtils.addDigitGrouping(teleporter.getMaxEnergyStored()) + " RF");
        }

        //Draw tooltips for icons
        for(Icon icon : iconList)
            if(icon.isMouseOver(mouseX, mouseY))
                tooltip.addAll(icon.getTooltip());
    }

    /**
     * Called by the controls from the buttonList when activated. (Mouse pressed for buttons)
     */
    @Override
    protected void actionPerformed(GuiButton button) throws IOException
    {
        CommonUtils.NETWORK.sendToServer(new MessageGuiTeleport(button.id, mc.player, teleporter.getPos()));
    }

    public void drawCenteredString(FontRenderer fontRendererIn, String text, int y, int color)
    {
        fontRendererIn.drawString(text, (xSize / 2) - (fontRendererIn.getStringWidth(text) / 2), y, color);
    }

    @Override
    public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int color)
    {
        fontRendererIn.drawString(text, x - (fontRendererIn.getStringWidth(text) / 2), y, color);
    }

    private int buttonId = 0;

    protected class Button extends GuiButton
    {
        private final int iconX, iconY;

        public Button(int x, int y, String buttonText)
        {
            super(buttonId++, guiLeft + x, guiTop + y, 46, 15, buttonText);
            iconX = 186;
            iconY = 0;
        }

        public Button(int x, int y, String buttonText, boolean enabled)
        {
            this(x, y, buttonText);
            this.enabled = enabled;
        }

        @Override
        public void drawCenteredString(FontRenderer fontRendererIn, String text, int x, int y, int colour)
        {
            fontRendererIn.drawString(text, (float)(x - fontRendererIn.getStringWidth(text) / 2), (float)y, colour, false);
        }

        @Override
        public void drawButton(Minecraft mc, int mouseX, int mouseY, float partialTicks)
        {
            if(!visible) return;
            FontRenderer fontrenderer = mc.fontRenderer;
            mc.getTextureManager().bindTexture(guiImage);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            this.hovered = mouseX >= x && mouseY >= y && mouseX < x + width && mouseY < y + height;
            //Select appropriate button icon for button state
            int iy = iconY;
            int textColour = 0x0094FF;
            if(!enabled)
            {
                iy += height; //Changes to "off" icon
                textColour = 0x002B49; //Changes to darker text
            }
            //Draw icon
            drawTexturedModalRect(x, y, iconX, iy, width, height);
            //Draw text
            if(!displayString.equals(""))
                drawCenteredString(fontrenderer, displayString, x + (width / 2), y + (height - 7) / 2, textColour);
        }
    }

    public enum EnumIconBackground
    {
        ON,
        OFF,
        RED
    }

    public class Icon extends Gui
    {
        private int posX, posY, iconId;
        private int size = 20;
        private int iconX = 186;
        private int iconY = 30;
        private EnumIconBackground bg = EnumIconBackground.ON;
        private List<String> tooltip = new ArrayList<String>();

        public Icon(int posX, int posY, int iconId)
        {
            this.posX = posX;
            this.posY = posY;
            this.iconId = iconId;
        }

        public Icon(int posX, int posY, int iconId, EnumIconBackground background)
        {
            this(posX, posY, iconId);
            bg = background;
        }

        public void setBackground(EnumIconBackground background)
        {
            bg = background;
        }

        public EnumIconBackground getBackground()
        {
            return bg;
        }

        public void setTooltip(List<String> text)
        {
            tooltip = text;
        }

        public void drawIcon(Minecraft mc)
        {
            mc.getTextureManager().bindTexture(guiImage);
            //Draw background
            drawTexturedModalRect(posX, posY, iconX + (bg.ordinal() * size), iconY, size, size);
            //Draw icon
            drawTexturedModalRect(posX, posY, iconX + (iconId * size), iconY + size, size, size);
        }

        public boolean isMouseOver(int mouseX, int mouseY)
        {
            return mouseX >= posX && mouseY >= posY && mouseX < posX + size && mouseY < posY + size;
        }

        public List<String> getTooltip()
        {
            return tooltip;
        }
    }
}

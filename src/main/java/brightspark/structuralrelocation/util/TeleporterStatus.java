package brightspark.structuralrelocation.util;

import net.minecraft.util.text.TextFormatting;

public enum TeleporterStatus
{
    ON(0),
    OFF(1),
    ENERGY(2),
    WAITING(2),
    TARGETS(2);

    private final byte iconId;

    TeleporterStatus(int iconId)
    {
        this.iconId = (byte) iconId;
    }

    public byte getIconId()
    {
        return iconId;
    }

    private TextFormatting getColour()
    {
        switch(iconId)
        {
            case 0:     return TextFormatting.GREEN;
            case 2:     return TextFormatting.RED;
            case 1:
            default:    return TextFormatting.BLUE;
        }
    }

    public String applyColour(String text)
    {
        return getColour() + text;
    }
}

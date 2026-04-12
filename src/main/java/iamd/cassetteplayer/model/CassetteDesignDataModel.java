package iamd.cassetteplayer.model;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

import iamd.cassetteplayer.CassetteDesign;
import iamd.cassetteplayer.CassetteSide;

public class CassetteDesignDataModel
{
    private CassetteDesign design = CassetteDesign.RAINBOW;
    protected Font detailsFont = new Font("Arial", Font.BOLD, 12);
    protected Font sideFont = new Font("Arial", Font.BOLD, 64);
    protected Font headerFont = new Font("Arial", Font.BOLD, 20);
    protected Font bodyTextFont = new Font("Arial", Font.PLAIN, 18);

    protected Color caseColor = Color.darkGray;
    protected Color spoolColor = Color.LIGHT_GRAY;
    protected Color labelColor[] = new Color[] { Color.white, Color.white };
    protected Color textColor = Color.black;
    
    public void paintLabel(Graphics2D g2, AffineTransform tx2, CassetteSide cassetteSide, double w, double h)
    {
        this.design.algorithm.paintLabel(g2, tx2, w, h, cassetteSide, this);
    }

    public Color getLateralColor()
    {
        return this.caseColor.darker();
    }

    public CassetteDesign getDesign()
    {
        return this.design;
    }

    public void setDesign(CassetteDesign design)
    {
        this.design = design;
    }

    public Color getCaseColor()
    {
        return this.caseColor;
    }

    public void setCaseColor(Color color)
    {
        this.caseColor = color;
    }

    public Color getTrapezoidColor()
    {
        return this.caseColor.darker();
    }

    public Color getSpoolColor()
    {
        return this.spoolColor;
    }

    public void setSpoolColor(Color color)
    {
        this.spoolColor = color;
    }

    public Color[] getLabelColor()
    {
        return this.labelColor;
    }

    public void setLabelColor(Color[] color)
    {
        this.labelColor = color;
    }

    public Color getTextColor()
    {
        return this.textColor;
    }

    public void setTextColor(Color color)
    {
        this.textColor = color;
    }

    public String getHeaderFontFamily()
    {
        return this.headerFont.getFamily();
    }

    public void setHeaderFontFamily(String fontFamily)
    {
        this.headerFont = new Font(fontFamily, this.headerFont.getStyle(), this.headerFont.getSize());
    }

    public String getBodyTextFontFamily()
    {
        return this.bodyTextFont.getFamily();
    }
    
    public void setBodyTextFontFamily(String fontFamily)
    {
        this.bodyTextFont = new Font(fontFamily, this.bodyTextFont.getStyle(), this.bodyTextFont.getSize());
    }
    
    public Font getBodyTextFont()
    {
        return this.bodyTextFont;
    }

    public Font getHeaderFont()
    {
        return this.headerFont;
    }

    public Font getSideFont()
    {
        return this.sideFont;
    }

    public Font getDetailsFont()
    {
        return this.detailsFont;
    }

}

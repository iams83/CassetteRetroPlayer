package iamd.cassetteplayer;

import java.awt.Color;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.util.ArrayList;

import iamd.cassetteplayer.model.CassetteDesignDataModel;
import iamd.ui.ScopedAffineTransform;

public enum CassetteDesign
{
    BASIC(1, new PaintingAlgorithmIfz()
    {
        @Override
        public void paintLabel(Graphics2D g2, AffineTransform tx2, double w,
                double h, CassetteSide cassetteSide, CassetteDesignDataModel dataModel)
        {
            g2.setColor(dataModel.getLabelColor()[0]);
            g2.fill(tx2.createTransformedShape(new RoundRectangle2D.Double(0, 0, w, h, 32, 32)));
            
            g2.setColor(dataModel.getLabelColor()[0].darker().darker());
            g2.fill(tx2.createTransformedShape(new RoundRectangle2D.Double(5, 5, w - 10, h - 10, 32, 32)));

            g2.setColor(dataModel.getLabelColor()[0]);
            g2.fill(tx2.createTransformedShape(new Rectangle2D.Double(0, 20, w, 80)));

            int lateralDecentering = 8;

            paintSideLetter(g2, tx2, (100 - lateralDecentering) * w / 100, 63 * h / 100, cassetteSide, dataModel, null);

            g2.setColor(dataModel.getTextColor());
            
            paintQuality(g2, tx2, lateralDecentering * w / 100, 52 * h / 100, cassetteSide, dataModel);

            paintDuration(g2, tx2, w / 2, 90 * h / 100, cassetteSide, dataModel);
            
            paintHeaderAndBodyText(g2, tx2, w, cassetteSide, dataModel);
        }
    }),
    
    RAINBOW(2, new PaintingAlgorithmIfz()
    {
        @Override
        public void paintLabel(Graphics2D g2, AffineTransform tx2, double w,
                double h, CassetteSide cassetteSide, CassetteDesignDataModel dataModel)
        {
            g2.setColor(dataModel.getLabelColor()[0]);
            g2.fill(tx2.createTransformedShape(new RoundRectangle2D.Double(0, 0, w, h, 32, 32)));
            
            g2.setColor(dataModel.getLabelColor()[1]);
            g2.fill(tx2.createTransformedShape(new RoundRectangle2D.Double(5, 5, w - 10, h - 10, 32, 32)));

            double bandSize = 128;
            
            Color[] colors = new Color[] 
            { 
                new Color(234, 83, 112), 
                new Color(238, 122, 81), 
                new Color(239, 234, 110), 
                new Color(37, 128, 67), 
                new Color(32, 139, 202)
            };
            
            for (int i = 0; i < 5; i ++)
            {
                g2.setColor(colors[i]);
                
                g2.fill(tx2.createTransformedShape(new Rectangle2D.Double(
                        5, 120 + 1. * i * bandSize / colors.length, 
                        w - 10, bandSize / colors.length)));
            }
            
            g2.setColor(dataModel.getLabelColor()[0]);
            g2.fill(tx2.createTransformedShape(new Rectangle2D.Double(0, 20, w, 80)));
            
            int lateralDecentering = 8;

            paintSideLetter(g2, tx2, (100 - lateralDecentering) * w / 100, 63 * h / 100, cassetteSide, dataModel, dataModel.getLabelColor()[1]);

            paintQuality(g2, tx2, lateralDecentering * w / 100, 85 * h / 100, cassetteSide, dataModel);

            g2.setColor(dataModel.getTextColor());
            
            paintDuration(g2, tx2, (90 - lateralDecentering) * w / 100, 90 * h / 100, cassetteSide, dataModel);
            
            paintHeaderAndBodyText(g2, tx2, w, cassetteSide, dataModel);
        }
    }),
    
    PLAIN(2, new PaintingAlgorithmIfz()
    {
        @Override
        public void paintLabel(Graphics2D g2, AffineTransform tx2, double w,
                double h, CassetteSide cassetteSide, CassetteDesignDataModel dataModel)
        {
            g2.setColor(dataModel.getLabelColor()[0]);
            g2.fill(tx2.createTransformedShape(new RoundRectangle2D.Double(0, 0, w, h, 32, 32)));
            
            g2.setColor(dataModel.getLabelColor()[1]);
            g2.fill(tx2.createTransformedShape(new RoundRectangle2D.Double(5, 5, w - 10, h - 10, 32, 32)));

            g2.setColor(dataModel.getLabelColor()[0]);
            g2.fill(tx2.createTransformedShape(new Rectangle2D.Double(0, 120, w, 128)));

            int lateralDecentering = 8;

            paintSideLetter(g2, tx2, (100 - lateralDecentering) * w / 100, 63 * h / 100, cassetteSide, dataModel, dataModel.getLabelColor()[0]);

            paintQuality(g2, tx2, lateralDecentering * w / 100, 52 * h / 100, cassetteSide, dataModel);

            paintDuration(g2, tx2, w / 2, 90 * h / 100, cassetteSide, dataModel);
            
            paintHeaderAndBodyText(g2, tx2, w, cassetteSide, dataModel);
        }
    }),
    
    ;
    
    public interface PaintingAlgorithmIfz
    {
        public void paintLabel(Graphics2D g2, AffineTransform tx2, double w, double h, CassetteSide cassetteSide, CassetteDesignDataModel dataModel);
    }

    final public int numColors;
    
    final public PaintingAlgorithmIfz algorithm;
    
    CassetteDesign(int numColors, PaintingAlgorithmIfz algorithm)
    {
        this.numColors = numColors;
        
        this.algorithm = algorithm;
    }

    static private void paintHeaderAndBodyText(Graphics2D g2, AffineTransform tx2, double w,
            CassetteSide cassetteSide, CassetteDesignDataModel dataModel)
    {
        try (ScopedAffineTransform sat = new ScopedAffineTransform(g2))
        {
            g2.transform(tx2);
            g2.translate(w / 2, 0);
            
            int maxStringWidth = (int) (.95 * w);
            
            if (cassetteSide.getBodyText() != null)
            {
                g2.setFont(dataModel.getBodyTextFont());
                
                FontMetrics fm = g2.getFontMetrics();
                
                int row = 0;
                
                ArrayList<String> lines = trimStringWidth(maxStringWidth, 2, fm, cassetteSide.getBodyText());
                
                for (String line : lines)
                {
                    g2.drawString(line, (int) - fm.stringWidth(line) / 2, 65 + (lines.size() == 1 ? 10 : 0) + 25 * row);
                    
                    row ++;
                }
            }
            
            if (cassetteSide.getHeaderLine() != null)
            {
                g2.setFont(dataModel.getHeaderFont());
                
                FontMetrics fm = g2.getFontMetrics(dataModel.getHeaderFont());
                
                String printLine = trimStringWidth(maxStringWidth, 1, fm, cassetteSide.getHeaderLine()).get(0);
                
                if (printLine != null)
                    g2.drawString(printLine, (int) - fm.stringWidth(printLine) / 2, 40);
            }
        }
    }
    
    static private void paintSideLetter(Graphics2D g2, AffineTransform tx2,
            double x, double y, CassetteSide cassetteSide, CassetteDesignDataModel dataModel, Color innerColor)
    {
        try (ScopedAffineTransform sat = new ScopedAffineTransform(g2))
        {
            g2.transform(tx2);
            g2.translate(x, y);
            g2.setFont(dataModel.getSideFont());
            
            FontMetrics fm = g2.getFontMetrics();
            
            g2.translate(- fm.stringWidth(cassetteSide.getSide().name()) / 2, 0);

            if (innerColor != null)
            {
                GlyphVector letterGlyph = dataModel.getSideFont().createGlyphVector(g2.getFontRenderContext(), cassetteSide.getSide().name());
                
                g2.setColor(dataModel.getCaseColor());
                g2.fill(letterGlyph.getOutline());
    
                g2.setColor(dataModel.getTextColor());
                g2.draw(letterGlyph.getOutline());
            }
            else
            {
                g2.setColor(dataModel.getTextColor());
                g2.drawString(cassetteSide.getSide().name(), 0, 0);
            }
        }
    }        

    static private void paintQuality(Graphics2D g2, AffineTransform tx2,
            double x, double y, CassetteSide cassetteSide, CassetteDesignDataModel dataModel)
    {
    	g2.setColor(dataModel.getTextColor());
    	
        try (ScopedAffineTransform sat = new ScopedAffineTransform(g2))
        {
            g2.transform(tx2);
            g2.translate(x, y);
            g2.setFont(dataModel.getDetailsFont());
            
            FontMetrics fm = g2.getFontMetrics(dataModel.getDetailsFont());
            
            String[] messages = new String[] { cassetteSide.getChannelQuality(), cassetteSide.getQualityBitRate() };
            
            int i = 0;
            
            for (String line : messages)
            {
                if (line != null)
                    g2.drawString(line, - fm.stringWidth(line) / 2, 20 * i);
                
                i ++;
            }
        }
    }     

    static private void paintDuration(Graphics2D g2, AffineTransform tx2,
            double x, double y, CassetteSide cassetteSide, CassetteDesignDataModel dataModel)
    {
        g2.setColor(dataModel.getTextColor());
        
        try (ScopedAffineTransform sat = new ScopedAffineTransform(g2))
        {
            g2.transform(tx2);
            g2.translate(x, y);
            g2.setFont(dataModel.getDetailsFont().deriveFont(20.f));
            
            FontMetrics fm = g2.getFontMetrics();
            
            String[] messages = new String[] { "2 x " + (cassetteSide.getDurationAsSecs() / 60) + " min." };
            
            int i = 0;
            
            for (String line : messages)
            {
                if (line != null)
                    g2.drawString(line, - fm.stringWidth(line) / 2, 20 * i);
                
                i ++;
            }
        }
    }
    
    static private ArrayList<String> trimStringWidth(int maxWidth, int numRows, FontMetrics fm, String text)
    {
        String firstLine = text;
        
        if (fm.stringWidth(firstLine) > maxWidth)
        {
            if (numRows <= 1)
            {
                for (int i = firstLine.length() - 1; 
                    canBeBroken(firstLine.charAt(i)) || fm.stringWidth(firstLine.trim() + "...") > maxWidth; i --)
                {
                    firstLine = firstLine.substring(0, i);
                }
                
                firstLine = firstLine.trim() + "...";
            }
            else
            {
                String remainingText = "";
                
                for (int i = firstLine.length() - 1; 
                    canBeBroken(firstLine.charAt(i)) || fm.stringWidth(firstLine.trim()) > maxWidth; 
                    i --)
                {
                    remainingText = firstLine.substring(i) + remainingText;

                    firstLine = firstLine.substring(0, i);
                }
                
                firstLine = firstLine.trim();
                ArrayList<String> restOfLines = trimStringWidth(maxWidth, numRows - 1, fm, remainingText);
                restOfLines.add(0, firstLine);
                return restOfLines;
            }
        }
        
        ArrayList<String> lines = new ArrayList<String>();
        lines.add(firstLine);
        return lines;
    }

    private static boolean canBeBroken(char c)
    {
        return Character.isAlphabetic(c) || c == '\'';
    }
}

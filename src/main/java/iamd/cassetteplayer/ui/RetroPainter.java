package iamd.cassetteplayer.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;

import iamd.ui.ScopedAffineTransform;

public class RetroPainter
{
    static final Color RETRO_COLORS[] = new Color[]
    {
        new Color(37, 159, 164), 
        new Color(124, 238, 228), 
        new Color(234, 85, 117), 
        new Color(86, 206, 158), 
        new Color(234, 217, 99)
    };

    static public void paintBackground(Graphics2D g2, Dimension size)
    {
        g2.setColor(RETRO_COLORS[0]);
        g2.fillRect(0, 0, size.width, size.height);
        
        try (ScopedAffineTransform sat = new ScopedAffineTransform(g2))
        {
            g2.translate(size.width / 2, size.height / 2);
            g2.rotate(-Math.PI / 10);
            g2.translate(-size.width / 2, -size.height / 2);

            final int borderW = 100;
            final int borderH = 200;
            
            int bandWidth = (size.width + 2 * borderW) / (RETRO_COLORS.length + 1);
            
            for (int i = 1; i < RETRO_COLORS.length; i ++)
            {
                g2.setColor(RETRO_COLORS[i]);
                g2.fillRect(i * bandWidth - borderW, -borderH, bandWidth, size.height + 2 * borderH);
            }
        }
    }
}

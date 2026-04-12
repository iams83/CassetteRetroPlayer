package iamd.ui;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;

public class ScopedAffineTransform implements AutoCloseable
{
    final private Graphics2D g2;
    final private AffineTransform at0, at;

    public ScopedAffineTransform(Graphics2D g2)
    {
        this.g2 = g2;
        
        this.at0 = null;
        
        this.at = new AffineTransform(g2.getTransform());
    }

    public ScopedAffineTransform(AffineTransform at0)
    {
        this.g2 = null;
        
        this.at0 = at0;
        
        this.at = new AffineTransform(at0);
    }

    @Override
    public void close()
    {
        if (this.g2 != null)
            this.g2.setTransform(this.at);
        else
            this.at0.setTransform(this.at);
    }
}

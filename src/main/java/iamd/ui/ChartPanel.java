package iamd.ui;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.SwingUtilities;

@SuppressWarnings("serial")
public class ChartPanel<T extends ChartArc> extends GraphicsPanel
{
    private ArrayList<ChartPanelListener<T>> chartPanelListeners = new ArrayList<ChartPanelListener<T>>();
    
    private ArrayList<T> arcs = new ArrayList<T>();
    
    public ChartPanel()
    {
        super(PanelMovement.PANNING_AND_SCALING, Reverse.YES);
        
        this.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseClicked(MouseEvent e)
            {
                Point2D point = ChartPanel.this.mapToScene(e.getPoint());
                
                for (T arc : new ArrayList<T>(ChartPanel.this.arcs))
                {
                    if (arc.containsPoint(point))
                    {
                        for (ChartPanelListener<T> listener : ChartPanel.this.chartPanelListeners)
                            listener.mouseClicked(e, arc);
                    }
                }
            }
        });

        this.addMouseMotionListener(new MouseMotionAdapter()
        {
            private ArrayList<ChartArc> hoveredArcs = new ArrayList<ChartArc>();
            
            @Override
            public void mouseMoved(MouseEvent e)
            {
                Point2D point = ChartPanel.this.mapToScene(e.getPoint());
                
                for (T arc : new ArrayList<T>(ChartPanel.this.arcs))
                {
                    if (arc.containsPoint(point))
                    {
                        if (!this.hoveredArcs.contains(arc))
                        {
                            for (ChartPanelListener<T> listener : ChartPanel.this.chartPanelListeners)
                                listener.mouseEntered(e, arc);
                            
                            this.hoveredArcs.add(arc);
                        }
                        
                        for (ChartPanelListener<T> listener : ChartPanel.this.chartPanelListeners)
                            listener.mouseMoved(e, arc);
                    }
                    else if (this.hoveredArcs.contains(arc))
                    {
                        for (ChartPanelListener<T> listener : ChartPanel.this.chartPanelListeners)
                            listener.mouseExited(e, arc);
                        
                        this.hoveredArcs.remove(arc);
                    }
                }
            }
        });
        
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentResized(ComponentEvent e)
            {
                ChartPanel.this.repaint();

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ChartPanel.this.repaint();
                    }
                });
            }
            
            @Override
            public void componentMoved(ComponentEvent e)
            {
                ChartPanel.this.repaint();

                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        ChartPanel.this.repaint();
                    }
                });
            }
        });
    }

    public T getArcAt(Point mousePoint)
    {
        Point2D point = ChartPanel.this.mapToScene(mousePoint);
        
        for (T arc : new ArrayList<T>(ChartPanel.this.arcs))
        {
            if (arc.containsPoint(point))
                return arc;
        }
        
        return null;
    }

    public void addChartPanelListener(ChartPanelListener<T> listener)
    {
        this.chartPanelListeners.add(listener);
    }
    
    double prevRadius = 0;

    public void initializeBoundingBox()
    {
        double maxRadius = 0;
        
        for (T arc : this.arcs)
        {
            if (arc.getMaxRadius() > maxRadius)
                maxRadius = arc.getMaxRadius();
        }
       
        maxRadius += 50;
        
        if (Math.abs(this.prevRadius - maxRadius) > 10)
        {
            this.prevRadius = maxRadius;
            
            this.initializeBoundingBox(new Rectangle2D.Double(-maxRadius, -maxRadius, 2 * maxRadius, 2 * maxRadius));
        }
    }

    public void addArc(T arc)
    {
        this.arcs.add(arc);
    }
    
    public void clearArcs()
    {
        if (this.arcs != null)
            this.arcs.clear();
    }
    
    public Rectangle2D getBounds2D()
    {
        Rectangle2D bounds = null;
        
        for (ChartArc arc : new ArrayList<ChartArc>(this.arcs))
        {
            if (bounds == null)
                bounds = arc.getBounds2D();
            else
                bounds = bounds.createUnion(arc.getBounds2D());
        }
        
        return bounds;
    }

    @Override
    public void paint(Graphics2D g2, AffineTransform tx2, Dimension size)
    {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        
        for (ChartArc arc : new ArrayList<ChartArc>(this.arcs))
            arc.paint(g2, tx2);
    }

}

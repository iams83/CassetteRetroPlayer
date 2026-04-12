package iamd.ui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

abstract public class GraphicsPanel extends JPanel
{
    public enum Reverse { YES, NO }
    
    public enum PanelMovement { PANNING, PANNING_AND_SCALING, NO }
    
	private static final long serialVersionUID = 1L;

	private AffineTransform tx = new AffineTransform();

	private Point draggedPoint;
	
	public Reverse reverse;

    public GraphicsPanel(PanelMovement movement, Reverse reverse)
    {
        this.reverse = reverse;
        
        if (movement != PanelMovement.NO)
        {
            this.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mousePressed(MouseEvent e)
                {
                    GraphicsPanel.this.draggedPoint = e.getPoint();
                }
            });
        
            this.addMouseMotionListener(new MouseMotionAdapter()
            {
                @Override
                public void mouseDragged(MouseEvent e)
                {
                    if (GraphicsPanel.this.draggedPoint == null)
                        return;
                    
                    AffineTransform newTx = new AffineTransform();
                    
                    newTx.translate(
                            e.getX() - GraphicsPanel.this.draggedPoint.x, 
                            e.getY() - GraphicsPanel.this.draggedPoint.y);
    
                    GraphicsPanel.this.draggedPoint = e.getPoint();
                    
                    GraphicsPanel.this.tx.preConcatenate(newTx);
                    
                    repaint();
                }
            });
            
            if (movement == PanelMovement.PANNING_AND_SCALING)
            {
                this.addMouseWheelListener(new MouseWheelListener()
                {
                    @Override
                    public void mouseWheelMoved(MouseWheelEvent e)
                    {
                        AffineTransform newTx = new AffineTransform();
                        
                        double scale = Math.pow(1.1, -e.getWheelRotation());
        
                        newTx.translate(e.getX(), e.getY());
                        newTx.scale(scale, scale);
                        newTx.translate(-e.getX(), -e.getY());
                        
                        GraphicsPanel.this.draggedPoint = e.getPoint();
                        
                        GraphicsPanel.this.tx.preConcatenate(newTx);
                        
                        repaint();
                    }
                });
            }
        }
    }

	public AffineTransform getTransform()
	{
	    return new AffineTransform(this.tx);
	}
	
	public Point2D sceneToMap(Point2D point)
	{
		return this.tx.transform(point, null);
	}

	public Point2D mapToScene(Point point)
	{
		try
		{
			return this.tx.inverseTransform(point, null);
		}
		catch(NoninvertibleTransformException ex)
		{
			return null;
		}
	}
	
	public void initializeBoundingBox(Rectangle2D boundingBox)
	{
		this.tx = initializeTransformation(boundingBox, this.getSize());
		
		this.repaint();
	}

    public AffineTransform initializeTransformation(Rectangle2D boundingBox,
            Dimension dimension)
    {
        AffineTransform newTx = new AffineTransform();
	    
		double scale = Math.min(
				dimension.getWidth() / boundingBox.getWidth(), 
				dimension.getHeight() / boundingBox.getHeight());

		newTx.setToIdentity();

		if (this.reverse == Reverse.YES)
		{
    		newTx.translate(dimension.width / 2, dimension.height / 2);
    		newTx.scale(scale, -scale);
    		newTx.translate(- boundingBox.getMinX() - boundingBox.getWidth() / 2, 
    				 	      - boundingBox.getMinY() - boundingBox.getHeight() / 2);
		}
		else
		{
            newTx.translate(dimension.width / 2, dimension.height / 2);
            newTx.scale(scale, scale);
            newTx.translate(- boundingBox.getMinX() - boundingBox.getWidth() / 2, 
                              - boundingBox.getMinY() - boundingBox.getHeight() / 2);
		}
        return newTx;
    }
	
	private BufferedImage paintBuffer;
	
	@Override
	public void paint(Graphics g)
	{
	    if (this.paintBuffer == null || this.paintBuffer.getWidth() != getWidth() || this.paintBuffer.getHeight() != getHeight())
	        this.paintBuffer = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_RGB);
	    
	    Graphics g1 = this.paintBuffer.createGraphics();

        g1.setColor(this.getBackground());
        g1.fillRect(0, 0, this.getWidth(), this.getHeight());
        
        Graphics2D g2 = (Graphics2D) g1;

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        this.paint(g2, this.tx, this.getSize());
		
		g.drawImage(this.paintBuffer, 0, 0, this);
	}

    public double getScreenFactor()
    {
        return 1.0 / this.tx.deltaTransform(new Point2D.Double(1, 0), null).getX();
    }

	abstract protected void paint(Graphics2D g2, AffineTransform tx2, Dimension size);
}

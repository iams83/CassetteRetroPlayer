package iamd.cassetteplayer;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;

import iamd.cassetteplayer.model.CassetteDesignDataModel;
import iamd.cassetteplayer.ui.CassettePlayerWidget;
import iamd.shapetoobj.ObjFile;
import iamd.shapetoobj.ShapeToObj;
import iamd.shapetoobj.ShapeToObj.ShapeCycle;
import iamd.ui.ScopedAffineTransform;
import utils.Matrix4x4;

public class CassettePainter
{
	static final public Dimension CASSETTE_IMAGE_SIZE = new Dimension(792, 496);

    static final public BufferedImage CASSETTE_IMAGE;

    static 
    {
        try
        {
            CASSETTE_IMAGE = ImageIO.read(CassettePlayerWidget.class.getResourceAsStream("cassette.png"));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    static final private Shape CASSETTE_OUTLINE = createAdvancedSymmetricalShape(385, 0, 483, 52);

    static final private RoundRectangle2D.Double LEFT_LATERAL = new RoundRectangle2D.Double(370, 303, 20, 125, 15, 15);
    static final private RoundRectangle2D.Double RIGHT_LATERAL = new RoundRectangle2D.Double(-391, 303, 20, 125, 15, 15);

    static final private Path2D.Double CASSETTE_TRAPEZOID = new Path2D.Double();
    {
        CASSETTE_TRAPEZOID.moveTo(230, 378);
        CASSETTE_TRAPEZOID.lineTo(260, 483);
        CASSETTE_TRAPEZOID.lineTo(-260, 483);
        CASSETTE_TRAPEZOID.lineTo(-230, 378);
        CASSETTE_TRAPEZOID.lineTo(230, 378);
    }
    
    static final private Shape TRAPEZOID_HOLES[] = new Shape[]
    {
        new Ellipse2D.Double(165, 437, 36, 36),
        new Ellipse2D.Double(-202, 437, 36, 36),
        new RoundRectangle2D.Double(89, 425, 30, 30, 20, 20),
        new RoundRectangle2D.Double(-120, 425, 30, 30, 20, 20)
    };
    
    static final private double SPOOL_X_DECENTERING = 158.5;
    static final private double SPOOL_Y = 221;
    static final private double SPOOL_RADIUS = 44.5;
    static final private double TAPE_MIN_SIZE = 90, TAPE_MAX_SIZE = 160;

    static final private Point2D.Double SPOOL_CENTER = 
            new Point2D.Double(SPOOL_X_DECENTERING, SPOOL_Y);
    
    static final private Ellipse2D.Double SPOOL_CIRCLE = 
            new Ellipse2D.Double(- SPOOL_RADIUS, - SPOOL_RADIUS, 2 * SPOOL_RADIUS, 2 * SPOOL_RADIUS);
    
    static final private Rectangle2D.Double CENTER_RECT_CLIP = new Rectangle2D.Double(- 158 / 2, 182, 158, 81);

    static final private Point2D[] SCREW_CENTERS = new Point2D[] { new Point2D.Double(   0, 408),
			 new Point2D.Double( 360,  23),
			 new Point2D.Double(-360,  23),
			 new Point2D.Double( 360, 460),
			 new Point2D.Double(-360, 460), };

    static final private double SCREW_RADIUS = 12.5;
    
    static final private Shape LABEL_OUTLINE = createAdvancedSymmetricalShape(349, 33, 364, 32);

	static final private Shape LABEL_HOLE = createAdvancedSymmetricalShape(233, 158, 286, 32);

	BufferedImage image;
    
    public BufferedImage createCassette(Dimension screenSize, AffineTransform tx2, CassetteDesignDataModel cassettePainterDataModel, CassetteSide cassetteSide, int rotation)
    {
        if (this.image != null)
            return this.image;
        
        this.image = new BufferedImage(screenSize.width, screenSize.height, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g2 = this.image.createGraphics();

        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_QUALITY);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
        
        g2.setStroke(new BasicStroke());
        
        g2.setColor(cassettePainterDataModel.getLateralColor());
        g2.fill(tx2.createTransformedShape(LEFT_LATERAL));
        g2.fill(tx2.createTransformedShape(RIGHT_LATERAL));

        g2.setColor(cassettePainterDataModel.getCaseColor());
        g2.fill(tx2.createTransformedShape(CASSETTE_OUTLINE));
        
        g2.setColor(Color.white);
        g2.fill(tx2.createTransformedShape(LABEL_OUTLINE));

        g2.setColor(cassettePainterDataModel.getCaseColor());
        g2.fill(tx2.createTransformedShape(createAdvancedSymmetricalShape(350, 32, 362, 32)));

        g2.setColor(cassettePainterDataModel.getTrapezoidColor());
        g2.fill(tx2.createTransformedShape(CASSETTE_TRAPEZOID));

        Shape labelHole = LABEL_HOLE;
        Shape labelOutline = createAdvancedSymmetricalShape(340, 38, 357, 32);
        
        Area area2 = new Area(CASSETTE_OUTLINE);
        area2.subtract(new Area(labelHole));
        g2.setClip(tx2.createTransformedShape(area2));
        g2.clip(tx2.createTransformedShape(labelOutline));

        try (ScopedAffineTransform sat = new ScopedAffineTransform(tx2))
        {
            tx2.translate(-340, 38);
            
            cassettePainterDataModel.paintLabel(g2, tx2, cassetteSide, 680, 320);
        }
        
        g2.setClip(null);

        g2.setStroke(new BasicStroke((int) (2 * tx2.getScaleX())));
        
        g2.setColor(Color.black);
        g2.draw(tx2.createTransformedShape(labelHole));
        g2.draw(tx2.createTransformedShape(labelOutline));

        g2.setStroke(new BasicStroke((int) (11 * tx2.getScaleX())));
        
        g2.setColor(cassettePainterDataModel.getSpoolColor());
        
        g2.setClip(null);

        g2.setStroke(new BasicStroke((int) (2 * tx2.getScaleX())));
        
        // Central screw
        for (Point2D p : SCREW_CENTERS)
        {
	        createScrew(g2, tx2, p.getX(), p.getY(), SCREW_RADIUS);
        }

        g2.setColor(Color.black);
        
        g2.draw(tx2.createTransformedShape(CASSETTE_TRAPEZOID));
        
        for (Shape trapozoidHole : TRAPEZOID_HOLES)
            g2.draw(tx2.createTransformedShape(trapozoidHole));

        g2.setColor(Color.white);
        g2.draw(tx2.createTransformedShape(new Line2D.Double(-230, 378, 230, 378)));
        g2.draw(tx2.createTransformedShape(new Line2D.Double(-230, 378, -260, 483)));

        g2.dispose();
        
        return this.image;
    }
    
    public Shape paintCassette(Graphics2D g2, Dimension screenSize, AffineTransform tx2, CassetteDesignDataModel cassettePainterDataModel, CassetteSide cassetteSide, int currentFrame, int frameCount, int rotation)
    {
        try (ScopedAffineTransform gast = new ScopedAffineTransform(tx2))
        {
            tx2.translate(CASSETTE_IMAGE_SIZE.getWidth() / 2, 7);

            for (int i = 0; i < 2; i ++)
            {
                try (ScopedAffineTransform sat = new ScopedAffineTransform(tx2))
                {
                    tx2.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
    
                    g2.setClip(tx2.createTransformedShape(SPOOL_CIRCLE));
    
                    double radius = 42;
                    Ellipse2D.Double circle = new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius);
                    
                    g2.setColor(Color.black);
                    g2.setStroke(new BasicStroke((int) (13 * tx2.getScaleX())));
                    g2.draw(tx2.createTransformedShape(circle));
    
                    Line2D.Double rect = new Line2D.Double(32, 0, 50, 0);
                    
                    g2.setStroke(new BasicStroke((int) Math.abs(13 * tx2.getScaleX())));

                    for (int r = 0; r < 6; r ++)
                    {
                        try (ScopedAffineTransform sat2 = new ScopedAffineTransform(tx2))
                        {
                            tx2.rotate(2 * Math.PI * (1. * -rotation / 60 + 1. * r / 6));
                            
                            g2.setColor(Color.black);
                            g2.draw(tx2.createTransformedShape(rect));
                        }
                    }
                    
                    g2.setStroke(new BasicStroke((int) Math.abs(10 * tx2.getScaleX())));

                    for (int r = 0; r < 6; r ++)
                    {
                        try (ScopedAffineTransform sat2 = new ScopedAffineTransform(tx2))
                        {
                            tx2.rotate(2 * Math.PI * (1. * -rotation / 60 + 1. * r / 6));
                            
                            g2.setColor(cassettePainterDataModel.getSpoolColor());
                            g2.draw(tx2.createTransformedShape(rect));
                        }
                    }
                    
                    g2.setColor(cassettePainterDataModel.getSpoolColor());
                    g2.setStroke(new BasicStroke((int) Math.abs(11 * tx2.getScaleX())));
                    g2.draw(tx2.createTransformedShape(circle));
                }
            }
            
            double progress = Math.abs(1. * currentFrame / frameCount);

            double tapeSizes[] = new double[] { 
                    TAPE_MIN_SIZE + (TAPE_MAX_SIZE - TAPE_MIN_SIZE) * progress, 
                    TAPE_MAX_SIZE + (TAPE_MIN_SIZE - TAPE_MAX_SIZE) * progress, };
            
            Area area = new Area(new Rectangle2D.Double(
                    -CASSETTE_IMAGE_SIZE.width, 0, 
                    2 * CASSETTE_IMAGE_SIZE.width, 
                    CASSETTE_IMAGE_SIZE.height));
            area.subtract(new Area(new Ellipse2D.Double(SPOOL_X_DECENTERING - SPOOL_RADIUS, SPOOL_Y - SPOOL_RADIUS, 2 * SPOOL_RADIUS, 2 * SPOOL_RADIUS)));
            area.subtract(new Area(new Ellipse2D.Double(-SPOOL_X_DECENTERING - SPOOL_RADIUS, SPOOL_Y - SPOOL_RADIUS, 2 * SPOOL_RADIUS, 2 * SPOOL_RADIUS)));
            g2.setClip(tx2.createTransformedShape(area));

            for (int i = 0; i < 2; i ++)
            {
                AffineTransform sat = new AffineTransform();

                sat.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
                
                g2.setStroke(new BasicStroke());
                
                g2.setColor(Color.DARK_GRAY);
                Ellipse2D.Double circle = new Ellipse2D.Double(- tapeSizes[i], - tapeSizes[i], 2 * tapeSizes[i], 2 * tapeSizes[i]);
                g2.fill(tx2.createTransformedShape(sat.createTransformedShape(circle)));
                
                g2.setColor(cassettePainterDataModel.getSpoolColor());
                Ellipse2D.Double baseCircle = new Ellipse2D.Double(- TAPE_MIN_SIZE, - TAPE_MIN_SIZE, 2 * TAPE_MIN_SIZE, 2 * TAPE_MIN_SIZE);
                g2.fill(tx2.createTransformedShape(sat.createTransformedShape(baseCircle)));

                g2.setColor(Color.BLACK);
                g2.draw(tx2.createTransformedShape(sat.createTransformedShape(baseCircle)));

                Line2D.Double microRect = new Line2D.Double(SPOOL_RADIUS, 0, TAPE_MIN_SIZE - 10, 0);
                Line2D.Double longMicroRect = new Line2D.Double(SPOOL_RADIUS, 0, TAPE_MIN_SIZE - 5, 0);
                
                g2.setStroke(new BasicStroke((int) (11 * tx2.getScaleX())));
                
                double numMicroRects = 23;
                
                for (int r = 0; r < numMicroRects; r ++)
                {
                    AffineTransform satr = new AffineTransform(sat);
        
                    satr.rotate(2 * Math.PI * (1. * -rotation / 80 + 1. * r / numMicroRects));
                    
                    if (r == 0)
                    {
                        g2.setColor(Color.RED);
                        g2.draw(tx2.createTransformedShape(satr.createTransformedShape(longMicroRect)));
                    }
                    else
                    {
                        g2.setColor(cassettePainterDataModel.getSpoolColor().darker().darker());
                        g2.draw(tx2.createTransformedShape(satr.createTransformedShape(microRect)));
                    }
                }
            }

            BufferedImage bufferedImage = createCassette(screenSize, tx2, cassettePainterDataModel, cassetteSide, -rotation);
            
            Area area3 = new Area(CASSETTE_OUTLINE);
            area3.subtract(new Area(new Ellipse2D.Double(SPOOL_X_DECENTERING - SPOOL_RADIUS, SPOOL_Y - SPOOL_RADIUS, 2 * SPOOL_RADIUS, 2 * SPOOL_RADIUS)));
            area3.subtract(new Area(new Ellipse2D.Double(-SPOOL_X_DECENTERING - SPOOL_RADIUS, SPOOL_Y - SPOOL_RADIUS, 2 * SPOOL_RADIUS, 2 * SPOOL_RADIUS)));
            
            for (Shape trapozoidHole : TRAPEZOID_HOLES)
                area3.subtract(new Area(trapozoidHole));

            area3.subtract(new Area(CENTER_RECT_CLIP));
            g2.setClip(tx2.createTransformedShape(area3));

            g2.drawImage(bufferedImage, 0, 0, null);
            
            g2.setClip(null);

            g2.setStroke(new BasicStroke());
            
            g2.setColor(Color.black);

            for (int i = 0; i < 2; i ++)
            {
                try (ScopedAffineTransform sat = new ScopedAffineTransform(tx2))
                {
                    tx2.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
                
                    g2.draw(tx2.createTransformedShape(SPOOL_CIRCLE));
                }
            }

            for (Shape trapozoidHole : TRAPEZOID_HOLES)
                g2.draw(tx2.createTransformedShape(trapozoidHole));

            g2.draw(tx2.createTransformedShape(CENTER_RECT_CLIP));
            g2.draw(tx2.createTransformedShape(CASSETTE_OUTLINE));
            
            return tx2.createTransformedShape(CASSETTE_OUTLINE);
        }
    }
    
    private void createScrew(Graphics2D g2, AffineTransform tx2, double x, double y, double radius)
    {
        try (ScopedAffineTransform sat = new ScopedAffineTransform(tx2))
        {
            tx2.translate(x, y);
            
            tx2.rotate(Math.PI / 6);
            
            Shape outline = new Ellipse2D.Double(- radius, - radius, 2 * radius, 2 * radius);
            
            g2.setColor(Color.darkGray);
            g2.fill(tx2.createTransformedShape(outline));
    
            
            Shape screwLine = new Rectangle2D.Double(
                    -1 * radius, -0.2 * radius, 2 * radius, 0.4 * radius);
            
            g2.setColor(Color.DARK_GRAY.darker());
            g2.fill(tx2.createTransformedShape(screwLine));

            g2.setColor(Color.BLACK);
            g2.draw(tx2.createTransformedShape(outline));
            g2.draw(tx2.createTransformedShape(screwLine));
        }
    }

    static private Shape createAdvancedSymmetricalShape(double x, double y0, double yF, double r)
    {
        return new RoundRectangle2D.Double(-x, y0, 2 * x, yF - y0, r, r);
    }

    public void invalidateCache()
    {
        this.image = null;
    }

	public void getShapeCycle(ObjFile objFile)
	{
		// Case
		objFile.useMaterial("case");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();
			
            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(CASSETTE_OUTLINE)));
	        
            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(LABEL_OUTLINE)));

	        for (Shape shape : TRAPEZOID_HOLES)
	            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(shape)));

	        Ellipse2D.Double screwEllipse = new Ellipse2D.Double(-SCREW_RADIUS, -SCREW_RADIUS, 2 * SCREW_RADIUS, 2 * SCREW_RADIUS);
	        
	        for (Point2D center : SCREW_CENTERS)
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
	        		at.translate(center.getX(), center.getY());
	        		
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse)));
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 32;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}
		
		// Trapezoid
		objFile.useMaterial("trapezoid");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();
			
            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(CASSETTE_TRAPEZOID)));
	        
	        for (Shape shape : TRAPEZOID_HOLES)
	            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(shape)));

	        Ellipse2D.Double screwEllipse = new Ellipse2D.Double(-SCREW_RADIUS, -SCREW_RADIUS, 2 * SCREW_RADIUS, 2 * SCREW_RADIUS);
	        
	        Point2D center = SCREW_CENTERS[0]; 
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
	        		at.translate(center.getX(), center.getY());
	        		
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse)));
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 42;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}
				
		// Label hole
		objFile.useMaterial("label_hole");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();

            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(LABEL_HOLE)));
	        
            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(CENTER_RECT_CLIP)));

	        for (int i = 0; i < 2; i ++)
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
		            at.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(SPOOL_CIRCLE)));
	        	}
	        }

	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 32;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}

		// Label
		objFile.useMaterial("label");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();

            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(LABEL_OUTLINE.getBounds2D())));
	        
            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(CENTER_RECT_CLIP)));

	        for (int i = 0; i < 2; i ++)
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
		            at.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(SPOOL_CIRCLE)));
	        	}
	        }

	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 30;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}

		// Spool internal part
		objFile.useMaterial("spool_internal_part");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();

			final double SPOOL_INNER_RADIUS = 40;
			
			Rectangle2D.Double hiddenSpoolRect = new Rectangle2D.Double(-1.2 * SPOOL_INNER_RADIUS, -1.2 * SPOOL_INNER_RADIUS, 2 * 1.2 * SPOOL_INNER_RADIUS, 2 * 1.2 * SPOOL_INNER_RADIUS);
			
        	Ellipse2D.Double tapeInnerEllipse = new Ellipse2D.Double(-SPOOL_INNER_RADIUS, -SPOOL_INNER_RADIUS, 2 * SPOOL_INNER_RADIUS, 2 * SPOOL_INNER_RADIUS);
        	
	        for (int i = 0; i < 2; i ++)
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
		            at.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(hiddenSpoolRect)));
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(tapeInnerEllipse)));
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 10;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}

		// Spool medium part
		objFile.useMaterial("spool_medium_part");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();

			final double SPOOL_INNER_RADIUS = 40;
			
			Rectangle2D.Double hiddenSpoolRect = new Rectangle2D.Double(-1.2 * SPOOL_INNER_RADIUS, -1.2 * SPOOL_INNER_RADIUS, 2 * 1.2 * SPOOL_INNER_RADIUS, 2 * 1.2 * SPOOL_INNER_RADIUS);
			
        	Ellipse2D.Double tapeInnerEllipse = new Ellipse2D.Double(-TAPE_MIN_SIZE, -TAPE_MIN_SIZE, 2 * TAPE_MIN_SIZE, 2 * TAPE_MIN_SIZE);
        	
	        for (int i = 0; i < 2; i ++)
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
		            at.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(hiddenSpoolRect)));
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(tapeInnerEllipse)));
	        	}
	        }

	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 20;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}

		// Spool outer part
		objFile.useMaterial("spool_outer_part");
		{
			AffineTransform at = new AffineTransform();
			
			for (int i = 0; i < 2; i ++)
	        {
				ArrayList<ShapeCycle> outline = new ArrayList<>();

	        	Ellipse2D.Double tapeInnerEllipse = new Ellipse2D.Double(-TAPE_MIN_SIZE, -TAPE_MIN_SIZE, 2 * TAPE_MIN_SIZE, 2 * TAPE_MIN_SIZE);
	        	
		        try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
		            at.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
		            
		            double tapeMaxSize = i == 0 ? TAPE_MAX_SIZE : 1.01 * TAPE_MIN_SIZE;
		            
		            Ellipse2D.Double tapeOuterEllipse = new Ellipse2D.Double(-tapeMaxSize, -tapeMaxSize, 2 * tapeMaxSize, 2 * tapeMaxSize);
					
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(tapeOuterEllipse)));
		            
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(tapeInnerEllipse)));
	        	}

		        ShapeToObj.sortCyclePaths(outline);
				
		        double thickness = 20;
		        
				ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
	        }
		}

		// Screw holes
        objFile.useMaterial("screw_holes");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();
			
			Rectangle2D.Double screwEllipse = new Rectangle2D.Double(-SCREW_RADIUS, -SCREW_RADIUS, 2 * SCREW_RADIUS, 2 * SCREW_RADIUS);
	        
			boolean first = true;
	        
	        for (Point2D center : SCREW_CENTERS)
	        {
	        	if (first)
	        	{
	        		first = false;
	        		continue;
	        	}
	        	
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
	        		at.translate(center.getX(), center.getY());
	        		
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse)));
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 24;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}
		
		// Screws
        objFile.useMaterial("screws");
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();
			
			Rectangle2D.Double screwEllipse1 = new Rectangle2D.Double(-SCREW_RADIUS, -SCREW_RADIUS, 2 * SCREW_RADIUS, 2 * SCREW_RADIUS);

			Rectangle2D.Double screwEllipse2 = new Rectangle2D.Double(-0.9 * SCREW_RADIUS, -0.2 * SCREW_RADIUS, 2 * 0.9 * SCREW_RADIUS, 2 * 0.2 * SCREW_RADIUS);
			
			boolean first = true;
	        
	        for (Point2D center : SCREW_CENTERS)
	        {
	        	if (first)
	        	{
	        		first = false;
	        		continue;
	        	}
	        	
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
	        		at.translate(center.getX(), center.getY());
	        		
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse1)));
		        	
		        	try (ScopedAffineTransform sat1 = new ScopedAffineTransform(at))
		        	{
		        		at.rotate(Math.PI / 4);
		        		
			            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse2)));
		        	}
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 28;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}

		// Central screw hole
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();
			
			Rectangle2D.Double screwEllipse = new Rectangle2D.Double(-SCREW_RADIUS, -SCREW_RADIUS, 2 * SCREW_RADIUS, 2 * SCREW_RADIUS);
	        
			Point2D center = SCREW_CENTERS[0];
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
	        		at.translate(center.getX(), center.getY());
	        		
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse)));
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 34;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}
		
		// Central screw
		{
			AffineTransform at = new AffineTransform();
			
			ArrayList<ShapeCycle> outline = new ArrayList<>();
			
			Rectangle2D.Double screwEllipse1 = new Rectangle2D.Double(-SCREW_RADIUS, -SCREW_RADIUS, 2 * SCREW_RADIUS, 2 * SCREW_RADIUS);

			Rectangle2D.Double screwEllipse2 = new Rectangle2D.Double(-0.9 * SCREW_RADIUS, -0.2 * SCREW_RADIUS, 2 * 0.9 * SCREW_RADIUS, 2 * 0.2 * SCREW_RADIUS);
	        
	        Point2D center = SCREW_CENTERS[0];
	        {
	        	try (ScopedAffineTransform sat = new ScopedAffineTransform(at))
	        	{
	        		at.translate(center.getX(), center.getY());
	        		
		            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse1)));
		        	
		        	try (ScopedAffineTransform sat1 = new ScopedAffineTransform(at))
		        	{
		        		at.rotate(Math.PI / 4);
		        		
			            outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(screwEllipse2)));
		        	}
	        	}
	        }
	        
	        ShapeToObj.sortCyclePaths(outline);
			
	        double thickness = 38;
	        
			ShapeToObj.writeOBJ(objFile, outline, thickness, -thickness);
		}
		
		objFile.useMaterial("spool_sticks");
		
		for (int r = 0; r < 6; r ++)
		{
	        for (int i = 0; i < 2; i ++)
	        {
	        	Matrix4x4 m = new Matrix4x4();
			        
				AffineTransform at = new AffineTransform();
	
				ArrayList<ShapeCycle> outline = new ArrayList<>();
				
				double radius = 5;
				
				Ellipse2D.Double spoolStick = new Ellipse2D.Double(-radius, -radius, 2 * radius, 2 * radius);
				
		        outline.addAll(ShapeToObj.createShapeCycle(at.createTransformedShape(spoolStick)));
				
		        ShapeToObj.sortCyclePaths(outline);
		        
		        m.rotateX(Math.PI / 2);
		        
		        m.rotateZ(r * Math.PI / 3);

	            m.translate((i == 0 ? 1 : -1) * SPOOL_CENTER.getX(), SPOOL_CENTER.getY());
	            
				ShapeToObj.writeOBJ(objFile, outline, 42, 30, m);
	        }
		}
	}
}

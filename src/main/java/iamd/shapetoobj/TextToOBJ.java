package iamd.shapetoobj;

import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

import iamd.shapetoobj.ShapeToObj.ShapeCycle;
import utils.Matrix4x4;

public class TextToOBJ
{
    static public void main(String[] args) throws IOException
    {
        Font font = new Font("Arial Unicode MS", Font.PLAIN, 64);
        
        File objOutputFile = new File("C:\\tmp\\1.obj");
        
        BufferedImage image = new BufferedImage(800, 100, BufferedImage.TYPE_INT_RGB);
        
        Graphics2D g = (Graphics2D) image.getGraphics();
        
        Shape shape1 = new TextLayout("\u265E Check mate!", font, g.getFontRenderContext()).getOutline(new AffineTransform());
        
        AffineTransform at1 = new AffineTransform();
        
        at1.translate(-shape1.getBounds2D().getWidth() / 2, -shape1.getBounds2D().getHeight() / 2);
        
        shape1 = at1.createTransformedShape(shape1);
        
        Shape shape2 = new TextLayout("Hello world", font, g.getFontRenderContext()).getOutline(new AffineTransform());
        
        AffineTransform at2 = new AffineTransform();
        
        at2.translate(shape2.getBounds2D().getWidth() / 2, 250);
        
        shape2 = at2.createTransformedShape(shape2);
        
        Matrix4x4 at = new Matrix4x4();
        
        try (PrintStream out = objOutputFile == null ? System.out : 
            new PrintStream(new FileOutputStream(objOutputFile)))
        {
            ObjFile objFile = new ObjFile();

            at.rotateX(Math.PI / 4);
            
            ArrayList<ShapeCycle> shapeCycle1 = ShapeToObj.createShapeCycle(shape1);
            
            shapeCycle1.addAll(ShapeToObj.createShapeCycle(new Rectangle2D.Double(-300, -100, 600, 200)));
            
            shapeCycle1.addAll(ShapeToObj.createShapeCycle(new Rectangle2D.Double(-400, -150, 800, 300)));

            shapeCycle1.addAll(ShapeToObj.createShapeCycle(new Rectangle2D.Double(-500, -200, 1000, 400)));

            ShapeToObj.sortCyclePaths(shapeCycle1);

            ShapeToObj.writeOBJ(objFile, shapeCycle1, 10, -10, at);
            
            ArrayList<ShapeCycle> shapeCycle2 = ShapeToObj.createShapeCycle(shape2);
            
            at.rotateX(Math.PI);
            
            ShapeToObj.writeOBJ(objFile, shapeCycle2, 10, -10, at);
            
            objFile.write(out);
        }
    }


}

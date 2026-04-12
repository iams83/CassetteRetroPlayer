package iamd.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class ChartArc
{
    private static final double EXTERN_BORDER = 0;
    private static final double TEXT_BORDER = 0;

    private double minRadius, maxRadius, centerAngle, maxAngleExtent;
    
    private String text1, text2; 
    
    private Font font1 = null, 
                 font2 = null;
    
    private Color fillingColor = Color.white, 
                  borderColor  = Color.black, 
                  textColor    = Color.black;

    private Shape fillingShape, borderShape1, borderShape2;
    
    public ChartArc(double minRadius, double maxRadius, 
            double centerAngle, double maxAngleExtent, 
            String text1, String text2)
    {
        this.text1 = text1;
        this.text2 = text2;
        
        this.setArc(minRadius, maxRadius, centerAngle, maxAngleExtent);
    }
    
    public Rectangle2D getBounds2D()
    {
        if (this.borderShape2 != null)
            return this.borderShape1.getBounds2D().createUnion(this.borderShape2.getBounds2D());
        
        return this.borderShape1.getBounds2D();
    }

    public String getText1()
    {
        return this.text1;
    }
    
    public String getText2()
    {
        return this.text2;
    }
    
    public double getMaxRadius()
    {
        return this.maxRadius;
    }
    
    public void setText1(String text)
    {
        this.text1 = text;
    }
    
    public void setText2(String text)
    {
        this.text2 = text;
    }
    
    public void setFont1(Font font1)
    {
        this.font1 = font1;
    }
    
    public void setFont2(Font font2)
    {
        this.font2 = font2;
    }
    
    public void setFillingColor(Color color)
    {
        this.fillingColor = color;
    }
    
    public void setBorderColor(Color color)
    {
        this.borderColor = color;
    }
    
    public void setTextColor(Color color)
    {
        this.textColor = color;
    }
    
    public void setArc(double minRadius, double maxRadius, double centerAngle, double angleExtent)
    {
        this.minRadius = minRadius;
        this.maxRadius = maxRadius;
        this.centerAngle = centerAngle;
        this.maxAngleExtent = angleExtent;

        Path2D.Double borderPath = new Path2D.Double();
        
        int maxArcPoints = (int) (maxRadius * angleExtent / 5);
        int minArcPoints = (int) (minRadius * angleExtent / 5);
        
        double startingAngle = centerAngle - angleExtent / 2 + EXTERN_BORDER;
        
        double x0 = 0, y0 = 0;
        
        for (int r = 0; r <= maxArcPoints; r ++)
        {
            double angle = startingAngle + r * (angleExtent - 2 * EXTERN_BORDER) / maxArcPoints;
            
            double x = maxRadius * Math.cos(angle);
            double y = - maxRadius * Math.sin(angle);
            
            if (r == 0)
                borderPath.moveTo(x0 = x, y0 = y);
            else
                borderPath.lineTo(x, y);
        }
        
        for (int r = 0; r <= minArcPoints; r ++)
        {
            double angle = startingAngle + (minArcPoints - r) * (angleExtent - 2 * EXTERN_BORDER) / minArcPoints;
            
            double x = minRadius * Math.cos(angle);
            double y = - minRadius * Math.sin(angle);
            
            borderPath.lineTo(x, y);
        }
        
        borderPath.lineTo(x0, y0);
        
        this.fillingShape = borderPath;
        
        if (Math.abs(this.maxAngleExtent - 2 * Math.PI) < 0.001)
        {
            this.borderShape1 = new Ellipse2D.Double(- maxRadius, - maxRadius, 2 * maxRadius, 2 * maxRadius);
            
            this.borderShape2 = new Ellipse2D.Double(- minRadius, - minRadius, 2 * minRadius, 2 * minRadius);
        }
        else
        {
            this.borderShape1 = this.fillingShape;
        }
    }

    public void paint(Graphics2D g2, AffineTransform tx2)
    {
        g2.setColor(this.fillingColor);
        g2.fill(tx2.createTransformedShape(this.fillingShape));

        g2.setColor(this.borderColor);
        g2.draw(tx2.createTransformedShape(this.borderShape1));
        
        g2.draw(tx2.createTransformedShape(this.borderShape1));
        
        if (this.borderShape2 != null)
            g2.draw(tx2.createTransformedShape(this.borderShape2));
        
        g2.setColor(this.textColor);
        
        boolean reverse = Math.sin(this.centerAngle) > 0;

        if (this.font1 != null)
            g2.setFont(this.font1);

        if (this.text2 == null)
        {
            drawCircularText(g2, tx2, 
                    this.minRadius + 0.5 * (this.maxRadius - minRadius), 
                    this.centerAngle, 
                    this.maxAngleExtent - 2 * TEXT_BORDER, 
                    this.text1, reverse);
        }
        else
        {
            drawCircularText(g2, tx2, 
                    this.minRadius + (reverse ? 0.25 : 0.75) * (this.maxRadius - minRadius), 
                    this.centerAngle, 
                    this.maxAngleExtent - 2 * TEXT_BORDER, 
                    this.text1, reverse);
    
            if (this.font2 != null)
                g2.setFont(this.font2);
    
            drawCircularText(g2, tx2, 
                    this.minRadius + (reverse ? 0.75 : 0.25) * (this.maxRadius - this.minRadius), 
                    this.centerAngle, 
                    this.maxAngleExtent - 2 * TEXT_BORDER, 
                    this.text2, reverse);
        }
    }

    static private void drawCircularText(Graphics2D g2, AffineTransform tx2, double radius, double centerAngle, double maxExtent, String text, boolean reverse)
    {
        FontMetrics fm = g2.getFontMetrics();
        
        text = text.trim();
        
        double textAngularExtent = fm.stringWidth(text) / tx2.getScaleX() / radius;
        
        if (textAngularExtent > maxExtent)
        {
            while (textAngularExtent > maxExtent && text.length() > 0)
            {
                text = text.substring(0, text.length() - 1).trim();
                
                textAngularExtent = fm.stringWidth(text + "...") / tx2.getScaleX() / radius;
            }
            
            text += "...";
        }
                
        double textOffset = - textAngularExtent * radius / 2;
        double letterFactor = 1. / tx2.getScaleX();
        double textRotation = 0;
         
        if (reverse)
        {
            textOffset = -textOffset;
            letterFactor = -letterFactor;
            textRotation = Math.PI;
            radius -= (fm.getDescent() - fm.getAscent()) / 2 / tx2.getScaleX();
        }
        else
        {
            radius += (fm.getDescent() - fm.getAscent()) / 2 / tx2.getScaleX();
        }
            
            
        AffineTransform preAt = g2.getTransform();
        
        for (char c : text.toCharArray())
        {
            String s = Character.toString(c);
            
            double angle = Math.PI / 2 + centerAngle + textOffset / radius;
            
            double x0 = radius * Math.sin(angle);
            double y0 = radius * Math.cos(angle);
            
            Point2D p = tx2.transform(new Point2D.Double(x0, y0), null);

            AffineTransform at = new AffineTransform();
            at.translate(p.getX(), p.getY());
            at.rotate(textRotation + angle);
            g2.setTransform(at);
            g2.drawString(s, 0, 0);
            
            textOffset += letterFactor * fm.stringWidth(s);

            g2.setTransform(preAt);
        }        
    }

    public boolean containsPoint(Point2D point)
    {
        return this.fillingShape.contains(point);
    }
}

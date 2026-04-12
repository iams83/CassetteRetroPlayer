package iamd.shapetoobj;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import earcut4j.Earcut;
import utils.Arrays;
import utils.Matrix4x4;
import utils.Vector4;

public class ShapeToObj
{
    public static class ShapeCycle
    {
        final private ArrayList<Point2D> movements = new ArrayList<>();
        
        final private ArrayList<ShapeCycle> innerCycles = new ArrayList<>();

        public ShapeCycle(double startX, double startY)
        {
            this.movements.add(new Point2D.Double(startX, startY));
        }
        
        private Point2D getLastPoint()
        {
            return this.movements.get(this.movements.size() - 1);
        }

        public void addLineTo(double x, double y)
        {
            this.movements.add(new Point2D.Double(x, y));
        }
    
        public void addCubicTo(double x2, double y2, double tx1, double ty1, double tx2, double ty2)
        {
            Point2D lastPoint = getLastPoint();
            
            double x1 = lastPoint.getX();
            double y1 = lastPoint.getY();
            
            ArrayList<Point2D> tentativePath = getCubicPath(x1, y1, x2, y2, tx1, ty1, tx2, ty2, 10);
            
            tentativePath.add(0, lastPoint);
            
            double pathLength = getPathLength(tentativePath);
            
            this.movements.addAll(getCubicPath(x1, y1, x2, y2, tx1, ty1, tx2, ty2, (int) (pathLength / 3)));
        }
    
        public void addQuadTo(double x2, double y2, double tx, double ty)
        {
            Point2D lastPoint = getLastPoint();
            
            double x1 = lastPoint.getX();
            double y1 = lastPoint.getY();
            
            ArrayList<Point2D> tentativePath = getQuadPath(x1, y1, x2, y2, tx, ty, 10);
            
            tentativePath.add(0, lastPoint);
            
            double pathLength = getPathLength(tentativePath);
            
            this.movements.addAll(getQuadPath(x1, y1, x2, y2, tx, ty, (int) (pathLength / 3)));
        }

        private ArrayList<Point2D> getCubicPath(double x1, double y1, double x2, double y2, 
                double tx1, double ty1, double tx2, double ty2, int chunks)
        {
            ArrayList<Point2D> points = new ArrayList<Point2D>();
            
            for (int i = 1; i <= chunks; i ++)
            {
                double d = 1. * i / chunks;
                
                double px1 = x1 + (tx1 - x1) * d;
                double py1 = y1 + (ty1 - y1) * d;

                double px2 = tx1 + (tx2 - tx1) * d;
                double py2 = ty1 + (ty2 - ty1) * d;
                
                double px3 = tx2 + (x2 - tx2) * d;
                double py3 = ty2 + (y2 - ty2) * d;
                
                double qx1 = px1 + (px2 - px1) * d;
                double qy1 = py1 + (py2 - py1) * d;

                double qx2 = px2 + (px3 - px2) * d;
                double qy2 = py2 + (py3 - py2) * d;
                
                points.add(new Point2D.Double(
                        qx1 + (qx2 - qx1) * d, 
                        qy1 + (qy2 - qy1) * d));
            }
                
            return points;
        }
        
        private ArrayList<Point2D> getQuadPath(double x1, double y1, double x2,
                double y2, double tx, double ty, int chunks)
        {
            ArrayList<Point2D> points = new ArrayList<>();
            
            for (int i = 1; i <= chunks; i ++)
            {
                double d = 1. * i / chunks;
                
                double px1 = x1 + (tx - x1) * d;
                double py1 = y1 + (ty - y1) * d;

                double px2 = tx + (x2 - tx) * d;
                double py2 = ty + (y2 - ty) * d;
                
                points.add(new Point2D.Double(
                        px1 + (px2 - px1) * d, 
                        py1 + (py2 - py1) * d));
            }
            return points;
        }

        private double getPathLength(ArrayList<Point2D> path)
        {
            double d = 0;
            
            for (int i = path.size() - 1, j = 0; j < path.size(); i = j ++)
                d += path.get(i).distance(path.get(j));
            
            return d;
        }

        public ArrayList<ShapeCycle> getInnerCycles()
		{
			return this.innerCycles;
		}

		public void addInnerCycle(ShapeCycle other)
		{
			this.innerCycles.add(other);
		}
		
        public Path2D getPath2D()
        {
            Path2D.Double path = new Path2D.Double();
            
            boolean first = true;
            
            for (Point2D p : this.movements)
            {
            	if (first)
            	{
                    path.moveTo(p.getX(), p.getY());
                    
                    first = false;
            	}
            	else            		
            		path.lineTo(p.getX(), p.getY());
            }
            
            path.closePath();
            
            return path;
        }

    	private double[] getEarcutCycleFromShape()
    	{
    		double[] cycle = new double[2 * this.movements.size()];
    		
    		for (int i = 0; i < this.movements.size(); i ++)
    		{
    			Point2D p = this.movements.get(i);

    			cycle[2 * i] = p.getX();
    			cycle[2 * i + 1] = p.getY();
    		}
    		
    		return cycle;
    	}

		public Collection<Point2D> getMovements()
		{
			return this.movements;
		}

		public double area()
		{
			if (this.movements.size() < 3)
				return 0;
			
			double sum = 0;
	    	
			for (int i = 0; i < this.movements.size() ; i++)
		    {
				if (i == 0)
				{
					sum += this.movements.get(i).getX() * (this.movements.get(i + 1).getY() - this.movements.get(this.movements.size() - 1).getY());
				}
				else if (i == this.movements.size() - 1)
				{
					sum += this.movements.get(i).getX() * (this.movements.get(0).getY() - this.movements.get(i - 1).getY());
				}
				else
				{
					sum += this.movements.get(i).getX() * (this.movements.get(i + 1).getY() - this.movements.get(i - 1).getY());
				}
		    }
			
	    	return 0.5 * sum;
		}
		
		public void reverse()
		{
			for (int i = 0; i < this.movements.size() / 2; i ++)
			{
				Point2D swap = this.movements.get(i);
				
				this.movements.set(i, this.movements.get(this.movements.size() - 1 - i));
				
				this.movements.set(this.movements.size() - 1 - i, swap);
			}
		}
    }

    public static ArrayList<ShapeCycle> createShapeCycle(Shape shape)
    {
        double jmpX = 0, jmpY = 0;
        
        boolean jump = false;
        
        AffineTransform at = new AffineTransform();
        
        ArrayList<ShapeCycle> turtlePaths = new ArrayList<ShapeCycle>();
        
        ShapeCycle main = new ShapeCycle(0, 0);
        
        turtlePaths.add(main);
        
        for (PathIterator iterator = shape.getPathIterator(at); !iterator.isDone(); iterator.next())
        {
            double coords[] = new double[8];
            
            int type = iterator.currentSegment(coords);
            
            switch(type)
            {
                case PathIterator.SEG_MOVETO:
                case PathIterator.SEG_CLOSE:
                {
                    jmpX = coords[0];
                    jmpY = coords[1];
                    jump = true;

                    break;
                }

                case PathIterator.SEG_CUBICTO:
                {
                    if (jump)
                    {
                        main = new ShapeCycle(jmpX, jmpY);

                        turtlePaths.add(main);
                        
                        jump = false;
                    }
                    
                    main.addCubicTo(coords[4], coords[5], coords[0], coords[1], coords[2], coords[3]);
                    break;
                }
                
                case PathIterator.SEG_QUADTO:
                {
                    if (jump)
                    {
                        main = new ShapeCycle(jmpX, jmpY);

                        turtlePaths.add(main);
                        
                        jump = false;
                    }
                    
                    main.addQuadTo(coords[2], coords[3], coords[0], coords[1]);
                    break;
                }
                
                case PathIterator.SEG_LINETO:
                {
                    if (jump)
                    {
                        main = new ShapeCycle(jmpX, jmpY);

                        turtlePaths.add(main);
                        
                        jump = false;
                    }
                    
                    main.addLineTo(coords[0], coords[1]);
                    break;
                }
            }
        }

        sortCyclePaths(turtlePaths);
        
        return turtlePaths;
    }
    
    public static void sortCyclePaths(ArrayList<ShapeCycle> cycles)
    {
        for (int j = 0; j < cycles.size(); j ++)
        {
            ShapeCycle cyclej = cycles.get(j);
            
            for (int i = j + 1; i < cycles.size(); i ++)
            {
                ShapeCycle cyclei = cycles.get(i);
                
                Path2D pathi = cyclei.getPath2D();
                
                if (pathi.contains(cyclej.getLastPoint()))
                {
            		cyclei.addInnerCycle(cyclej);
                	
                	cycles.remove(j);
                	
                    sortCyclePaths(cycles);
                    
                    return;
                }
            }
        }

        for (int j = 0; j < cycles.size(); j ++)
        {
            ShapeCycle cyclej = cycles.get(j);
            
            for (int i = 0; i < cycles.size(); i ++)
            {
                if (i == j)
                    continue;
                
                ShapeCycle cyclei = cycles.get(i);
                
                Path2D pathi = cyclei.getPath2D();
                
                if (pathi.contains(cyclej.getLastPoint()))
                {
                	cyclei.addInnerCycle(cyclej);
                	
                	cycles.remove(j);
                	
                	sortCyclePaths(cycles);
                	
                	return;
                }
            }
        }
        
        for (int i = 0; i < cycles.size(); i ++)
        {
        	ShapeCycle turtlePath0 = cycles.get(i);
        	
        	for (ShapeCycle turtlePath1 : turtlePath0.innerCycles)
        	{
        		if (!turtlePath1.innerCycles.isEmpty())
        		{
        			cycles.addAll(turtlePath1.innerCycles);
        		
        			turtlePath1.innerCycles.clear();
        		}
        	}
        }

        for (ShapeCycle cycle : cycles)
        {
        	if (cycle.area() < 0)
        		cycle.reverse();
        
        	for (ShapeCycle turtlePath1 : cycle.innerCycles)
        	{
            	if (turtlePath1.area() > 0)
            		turtlePath1.reverse();
        	}
        }
    }

    private static class VertexPair
    {
		final public int i, j;

    	public VertexPair(int i, int j)
    	{
			this.i = i;
			this.j = j;
		}
    }

	static public class Triangle
	{
		final public int a, b, c;
		
		public Triangle(int a, int b, int c)
		{
			this.a = a;
			this.b = b;
			this.c = c;
		}
	}
	
	public static void writeOBJ(ObjFile objFile, ArrayList<ShapeCycle> shapes, double z1, double z2)
	{
		writeOBJ(objFile, shapes, z1, z2, new Matrix4x4());
	}
	
    public static void writeOBJ(ObjFile objFile, ArrayList<ShapeCycle> shapes, double z1, double z2, Matrix4x4 at)
    {
    	ArrayList<Point2D> objVertexes = new ArrayList<Point2D>();
    	
    	ArrayList<Triangle> objTriangles = new ArrayList<Triangle>();
    	
    	ArrayList<VertexPair> objVertexPairs = new ArrayList<>();
    	
    	int prevObjVertexCount = objFile.vertexes.size();
    	
        for (ShapeCycle contour : shapes)
        {
            if (contour.movements.size() >= 3)
            {
            	int objVertexesIndex = objVertexes.size();
            	
        		objVertexes.addAll(contour.getMovements());
        		
                double[] shellEarcutCycle = contour.getEarcutCycleFromShape();

            	for (int p1 = objVertexesIndex, p2 = objVertexes.size() - 1; p1 < objVertexes.size(); p2 = p1 ++)
            		objVertexPairs.add(new VertexPair(p1, p2));
                	
                int[] earcutHoleCycles = new int[contour.getInnerCycles().size()];
                
                for (int i = 0; i < contour.getInnerCycles().size(); i ++)
                {
                	earcutHoleCycles[i] = shellEarcutCycle.length / 2;
                	
                	int objHoleVertexesIndex = objVertexes.size();
                	
                	ShapeCycle innerHole = contour.getInnerCycles().get(i);
                	
            		objVertexes.addAll(innerHole.getMovements());
            		
                	shellEarcutCycle = Arrays.concatenate(shellEarcutCycle, innerHole.getEarcutCycleFromShape());

                	for (int p1 = objHoleVertexesIndex, p2 = objVertexes.size() - 1; p1 < objVertexes.size(); p2 = p1 ++)
                		objVertexPairs.add(new VertexPair(p1, p2));
                }
                
            	List<Integer> triangles = Earcut.earcut(shellEarcutCycle, earcutHoleCycles, 2);
            	
            	for (int i = 0; i< triangles.size(); i += 3)
            	{
            		objTriangles.add(new Triangle(
            				objVertexesIndex + triangles.get(i + 0), 
            				objVertexesIndex + triangles.get(i + 1), 
            				objVertexesIndex + triangles.get(i + 2)));
            	}
            }
        }
        
        for (Point2D v : objVertexes)
        	objFile.add(at.transformVector4(new Vector4(v.getX(), v.getY(), z2, 1))); 
        
        for (Point2D v : objVertexes)
        	objFile.add(at.transformVector4(new Vector4(v.getX(), v.getY(), z1, 1))); 
        
        for (Triangle t : objTriangles)
        {
        	objFile.add(new ObjFile.Triangle(
        			prevObjVertexCount + t.a + 1, 
        			prevObjVertexCount + t.b + 1, 
        			prevObjVertexCount + t.c + 1));
    	}
    
        for (Triangle t : objTriangles)
        {
        	objFile.add(new ObjFile.Triangle(
        			prevObjVertexCount + objVertexes.size() + t.c + 1, 
        			prevObjVertexCount + objVertexes.size() + t.b + 1, 
        			prevObjVertexCount + objVertexes.size() + t.a + 1));
        }
        
        for (VertexPair p : objVertexPairs)
        {
        	objFile.add(new ObjFile.Triangle(
        			prevObjVertexCount + p.i + 1, 
        			prevObjVertexCount + p.j + 1, 
        			prevObjVertexCount + objVertexes.size() + p.i + 1));
        	
        	objFile.add(new ObjFile.Triangle(
        			prevObjVertexCount + p.j + 1, 
        			prevObjVertexCount + objVertexes.size() + p.j + 1, 
        			prevObjVertexCount + objVertexes.size() + p.i + 1));
        }
    }
}

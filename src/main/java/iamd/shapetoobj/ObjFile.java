package iamd.shapetoobj;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.TreeMap;

import utils.Vector4;

public class ObjFile
{
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
	
	public ArrayList<Vector4> vertexes = new ArrayList<>();

	public ArrayList<Triangle> triangles = new ArrayList<>();
	
	public TreeMap<Integer,String> materials = new TreeMap<Integer,String>();

	public void add(Vector4 vertex)
	{
		this.vertexes.add(vertex);
	}

	public void add(Triangle triangle)
	{
		this.triangles.add(triangle);
	}
	
    public void write(PrintStream out)
	{
		int i = 0;
		
		for (Vector4 v : this.vertexes)
		{
			String material = this.materials.get(i);
			
			if (material != null)
				out.println("usemtl " + material);
			
			out.println("v " + v.getElement(0) + " " + -v.getElement(1) + " " + v.getElement(2));
			
			i ++;
		}
		
		for (Triangle t : this.triangles)
		{
			out.println("f " + t.a + " " + t.b + " " + t.c);
		}
	}

	public void useMaterial(String material)
	{
		this.materials.put(this.vertexes.size(), material);
	}
}

package iamd.cassetteplayer;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import iamd.shapetoobj.ObjFile;

public class ObjMain
{
	static public void main(String[] args) throws IOException
	{
        File objOutputFile = new File("C:\\tmp\\1.obj");
        
        try (PrintStream out = objOutputFile == null ? System.out : 
            new PrintStream(new FileOutputStream(objOutputFile)))
        {
            ObjFile objFile = new ObjFile();

            CassettePainter painter = new CassettePainter();
            
            painter.getShapeCycle(objFile);
            
            objFile.write(out);
        }
        
        Desktop.getDesktop().open(objOutputFile);
	}
}

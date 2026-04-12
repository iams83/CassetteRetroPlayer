package iamd.rsrc;

import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Resources
{
    public static final ImageIcon EditIcon, EmptyIcon;

    public static final ImageIcon LinkIcon, LinkDisabledIcon;
    
    public static final ImageIcon ArrowTopIcon, ArrowTopDisabledIcon;
    
    public static final ImageIcon ArrowBottomIcon, ArrowBottomDisabledIcon;

    public static final ImageIcon DeleteIcon, DeleteDisabledIcon;
    
    public static final ImageIcon AddIcon, AddDisabledIcon;
    
    public static final ImageIcon ErrorIcon;

    static
    {
        ImageIcon editIcon0 = null, emptyIcon0 = null;
        
        ImageIcon linkIcon0 = null, linkDisabledIcon0 = null;
        
        ImageIcon arrowTopIcon0 = null, arrowTopDisabledIcon0 = null;
        
        ImageIcon arrowBottomIcon0 = null, arrowBottomDisabledIcon0 = null;
        
        ImageIcon deleteIcon0 = null, deleteDisabledIcon0 = null;
        
        ImageIcon addIcon0 = null, addDisabledIcon0 = null;
        
        ImageIcon errorIcon0 = null;

        try
        {
            editIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("EditIcon.png")));
            
            emptyIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("EmptyIcon.png")));
            
            linkIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("link.png"))); //$NON-NLS-1$
            
            linkDisabledIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("link_disabled.png"))); //$NON-NLS-1$
            
            arrowTopIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("arrow_top.png"))); //$NON-NLS-1$
            
            arrowTopDisabledIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("arrow_top_disabled.png"))); //$NON-NLS-1$
            
            arrowBottomIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("arrow_bottom.png"))); //$NON-NLS-1$
            
            arrowBottomDisabledIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("arrow_bottom_disabled.png"))); //$NON-NLS-1$
            
            deleteIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("delete.png"))); //$NON-NLS-1$
            
            deleteDisabledIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("delete_disabled.png"))); //$NON-NLS-1$

            addIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("add.png"))); //$NON-NLS-1$
            
            addDisabledIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("add_disabled.png"))); //$NON-NLS-1$
            
            errorIcon0 = new ImageIcon(ImageIO.read(Resources.class.getResourceAsStream("error.png"))); //$NON-NLS-1$
        }
        catch(IOException | IllegalArgumentException e)
        {
            e.printStackTrace(System.err);
            
            System.exit(-1);
        }
        finally
        {
            EditIcon = editIcon0;
            
            EmptyIcon = emptyIcon0;
            
            LinkIcon = linkIcon0;
            
            LinkDisabledIcon = linkDisabledIcon0;
            
            ArrowTopIcon = arrowTopIcon0;
            
            ArrowTopDisabledIcon = arrowTopDisabledIcon0;
            
            ArrowBottomIcon = arrowBottomIcon0;
            
            ArrowBottomDisabledIcon = arrowBottomDisabledIcon0;
            
            DeleteIcon = deleteIcon0;
            
            DeleteDisabledIcon = deleteDisabledIcon0;
            
            AddIcon = addIcon0;
            
            AddDisabledIcon = addDisabledIcon0;
            
            ErrorIcon = errorIcon0;
        }
    }

}

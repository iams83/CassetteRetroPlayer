package iamd.ui;

import java.awt.event.MouseEvent;

public interface ChartPanelListener<T extends ChartArc>
{
    public void mouseEntered(MouseEvent e, T arc);
    
    public void mouseExited(MouseEvent e, T arc);
    
    public void mouseMoved(MouseEvent e, T arc);
    
    public void mouseClicked(MouseEvent e, T text);
}

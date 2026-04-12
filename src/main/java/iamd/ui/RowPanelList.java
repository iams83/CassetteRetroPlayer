package iamd.ui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import iamd.rsrc.Resources;

public class RowPanelList<T extends ObjectRowPanel<? extends JPanel>> extends JPanel
{
    final private ArrayList<RowPanelListListener<T>> listeners = new ArrayList<RowPanelListListener<T>>();
    
    public RowPanelList(String tag, Collection<T> individuals, String addButtonTooltip)
    {
        super(new BorderLayout());
        
        BorderListPanelGenerator childrenPanel = new BorderListPanelGenerator(BorderLayout.NORTH);
        
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(new EmptyBorder(4, 0, 2, 0));
        topPanel.add(new JLabel(tag + ":"));
        
        if (addButtonTooltip != null)
        {
            JLabel addIcon = new JLabel(Resources.AddDisabledIcon);
            
            addIcon.setToolTipText(addButtonTooltip);
            
            addIcon.setBorder(new EmptyBorder(1, 1, 1, 1));
    
            topPanel.add(addIcon, BorderLayout.EAST);

            addIcon.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    for (RowPanelListListener<T> listener : RowPanelList.this.listeners)
                        listener.rowPanelNew();
                }
                
                @Override
                public void mouseExited(MouseEvent e)
                {
                    addIcon.setIcon(Resources.AddDisabledIcon);
                }
                
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    addIcon.setIcon(Resources.AddIcon);
                }
            });
        }
        
        childrenPanel.add(topPanel);

        for (T childPanel : individuals)
        {/*
            childPanel.addRowPanelListListener(new RowPanelListListener<ObjectRowPanel<? extends JPanel>>()
            {
                @Override
                public void rowPanelClicked(ObjectRowPanel<? extends JPanel> rowPanel)
                {
                    for (RowPanelListListener<T> listener : RowPanelList.this.listeners)
                        listener.rowPanelClicked(childPanel);
                }

                @Override
                public void rowPanelMovedUp(ObjectRowPanel<? extends JPanel> rowPanel)
                {
                    for (RowPanelListListener<T> listener : RowPanelList.this.listeners)
                        listener.rowPanelMovedUp(childPanel);
                }

                @Override
                public void rowPanelMovedDown(ObjectRowPanel<? extends JPanel> rowPanel)
                {
                    for (RowPanelListListener<T> listener : RowPanelList.this.listeners)
                        listener.rowPanelMovedDown(childPanel);
                }

                @Override
                public void rowPanelDeleted(ObjectRowPanel<? extends JPanel> rowPanel)
                {
                    for (RowPanelListListener<T> listener : RowPanelList.this.listeners)
                        listener.rowPanelDeleted(childPanel);
                }

                @Override
                public void rowPanelNew()
                {
                    for (RowPanelListListener<T> listener : RowPanelList.this.listeners)
                        listener.rowPanelNew();
                }
            });*/
            
            JPanel gapPanel = new JPanel(new BorderLayout());
            gapPanel.add(childPanel);
            gapPanel.setBorder(new EmptyBorder(1, 0, 1, 0));
            
            childrenPanel.add(gapPanel);
        }
        
        this.add(childrenPanel.extractPanel());
    }

    public void addRowPanelListListener(RowPanelListListener<T> listener)
    {
        this.listeners.add(listener);
    }
    
}

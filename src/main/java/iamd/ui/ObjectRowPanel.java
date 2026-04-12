package iamd.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.HashSet;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EtchedBorder;

import iamd.rsrc.Resources;

@SuppressWarnings("serial")
public class ObjectRowPanel<T extends ObjectRowPanel<?>> extends JPanel
{
    class HighlightableIconPanel extends JLabel
    {
        private boolean mouseOver = false;
        
        final private Icon idleIcon, hoverIcon;
        
        public HighlightableIconPanel(Icon idleIcon, Icon hoverIcon)
        {
            super(Resources.EmptyIcon);
            
            this.idleIcon = idleIcon;
            this.hoverIcon = hoverIcon;
            
            this.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseEntered(MouseEvent e)
                {
                    HighlightableIconPanel.this.mouseOver = true;
                    
                    mouseMovedOverPanels();
                }

                @Override
                public void mouseExited(MouseEvent e)
                {
                    mouseOver = false;
                    
                    mouseMovedOverPanels();
                }
            });
        }
        
        public void display(boolean highlight)
        {
            this.setIcon(highlight ? this.hoverIcon : this.idleIcon);
        }
        
        public void conceal()
        {
            this.setIcon(Resources.EmptyIcon);
        }
    }
    
    final private JLabel textLabel = new JLabel();
    
    JPanel showDetailsPanel = new JPanel(new BorderLayout());
    
    private HighlightableIconPanel linkIconLabel = 
            new HighlightableIconPanel(Resources.LinkDisabledIcon, Resources.LinkIcon);
    
    private HighlightableIconPanel arrowTopPanel = 
            new HighlightableIconPanel(Resources.ArrowTopDisabledIcon, Resources.ArrowTopIcon);

    private HighlightableIconPanel arrowBottomPanel = 
            new HighlightableIconPanel(Resources.ArrowBottomDisabledIcon, Resources.ArrowBottomIcon);

    private HighlightableIconPanel deletePanel = 
            new HighlightableIconPanel(Resources.DeleteDisabledIcon, Resources.DeleteIcon);

    JPanel buttonPanel = new JPanel(new GridLayout(1, 3));
    
    JPanel buttonTopPanel = new JPanel(new BorderLayout());
    
    JPanel rightPanel = new JPanel(new BorderLayout());
    
    HashSet<Object> mouseOverPanel = new HashSet<>();
    
    Color defaultColor;

    private ArrayList<RowPanelListener<T>> rowPanelListeners = new ArrayList<>();
    
    private ArrayList<RowPanelListListener<T>> rowPanelListListeners = new ArrayList<>();
    
    public ObjectRowPanel(boolean reorderButtons, boolean deleteButton)
    {
        super(new BorderLayout());
        
        this.setBorder(new EtchedBorder());
        
        this.showDetailsPanel.add(this.textLabel);
        
        this.rightPanel.add(this.linkIconLabel, BorderLayout.NORTH);
        
        this.showDetailsPanel.add(this.rightPanel, BorderLayout.EAST);
        
        this.addMouseListener(new MouseAdapter()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void mouseReleased(MouseEvent e)
            {
                for (RowPanelListener<T> listener : ObjectRowPanel.this.rowPanelListeners)
                    listener.rowPanelClicked((T) ObjectRowPanel.this);

                for (RowPanelListListener<T> listener : ObjectRowPanel.this.rowPanelListListeners)
                    listener.rowPanelClicked((T) ObjectRowPanel.this);
            }
        });

        this.showDetailsPanel.addMouseListener(new MouseAdapter()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void mouseReleased(MouseEvent e)
            {
                for (RowPanelListener<T> listener : rowPanelListeners)
                    listener.rowPanelClicked((T) ObjectRowPanel.this);

                for (RowPanelListListener<T> listener : ObjectRowPanel.this.rowPanelListListeners)
                    listener.rowPanelClicked((T) ObjectRowPanel.this);
            }
        });
        
        this.linkIconLabel.addMouseListener(new MouseAdapter()
        {
            @SuppressWarnings("unchecked")
            @Override
            public void mouseReleased(MouseEvent e)
            {
                for (RowPanelListener<T> listener : rowPanelListeners)
                    listener.rowPanelClicked((T) ObjectRowPanel.this);

                for (RowPanelListListener<T> listener : ObjectRowPanel.this.rowPanelListListeners)
                    listener.rowPanelClicked((T) ObjectRowPanel.this);
            }
        });

        if (reorderButtons)
        {
            this.arrowTopPanel.addMouseListener(new MouseAdapter()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    for (RowPanelListListener<T> listener : rowPanelListListeners)
                        listener.rowPanelMovedUp((T) ObjectRowPanel.this);
                }
            });
            
            this.arrowBottomPanel.addMouseListener(new MouseAdapter()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    for (RowPanelListListener<T> listener : rowPanelListListeners)
                        listener.rowPanelMovedDown((T) ObjectRowPanel.this);
                }
            });
            
            this.buttonPanel.add(this.arrowTopPanel);
            this.buttonPanel.add(this.arrowBottomPanel);
        }

        if (deleteButton)
        {
            this.deletePanel.addMouseListener(new MouseAdapter()
            {
                @SuppressWarnings("unchecked")
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    for (RowPanelListListener<T> listener : rowPanelListListeners)
                        listener.rowPanelDeleted((T) ObjectRowPanel.this);
                }
            });
            
            this.buttonPanel.add(this.deletePanel);
        }
        
        this.buttonTopPanel.add(buttonPanel, BorderLayout.NORTH);
        
        this.add(this.showDetailsPanel);
        
        this.add(buttonTopPanel, BorderLayout.EAST);

        this.defaultColor = linkIconLabel.getBackground();

        this.showDetailsPanel.setBackground(defaultColor);
        this.rightPanel.setBackground(defaultColor);
        this.buttonPanel.setBackground(defaultColor);
        this.buttonTopPanel.setBackground(defaultColor);
        this.textLabel.setBackground(defaultColor);
        this.setBackground(defaultColor);
        
        MouseListener highlightBackgroundListener = new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                mouseOverPanel.add(e.getSource());
                
                mouseMovedOverPanels();
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                mouseOverPanel.remove(e.getSource());
                
                mouseMovedOverPanels();
            }
        };

        this.showDetailsPanel.addMouseListener(highlightBackgroundListener);
        
        this.buttonTopPanel.addMouseListener(highlightBackgroundListener);
        
        this.rightPanel.addMouseListener(highlightBackgroundListener);
        
        this.buttonPanel.addMouseListener(highlightBackgroundListener);
        
        this.buttonTopPanel.addMouseListener(highlightBackgroundListener);
    }

    public void addRowPanelListener(RowPanelListener<T> rowPanelListener)
    {
        this.rowPanelListeners.add(rowPanelListener);
    }

    public void addRowPanelListListener(RowPanelListListener<T> rowPanelListener)
    {
        this.rowPanelListListeners.add(rowPanelListener);
    }

    private void mouseMovedOverPanels()
    {
        Color defaultColor = linkIconLabel.getBackground();

        if (!this.mouseOverPanel.isEmpty() ||
            this.linkIconLabel.mouseOver ||
            this.arrowTopPanel.mouseOver ||
            this.arrowBottomPanel.mouseOver ||
            this.deletePanel.mouseOver)
        {
            ObjectRowPanel.this.showDetailsPanel.setBackground(Color.white);
            rightPanel.setBackground(Color.white);
            buttonPanel.setBackground(Color.white);
            buttonTopPanel.setBackground(Color.white);
            ObjectRowPanel.this.textLabel.setBackground(Color.white);
            ObjectRowPanel.this.setBackground(Color.white);

            linkIconLabel   .display(!this.mouseOverPanel.isEmpty() || this.linkIconLabel.mouseOver);
            arrowTopPanel   .display(this.arrowTopPanel.mouseOver);
            arrowBottomPanel.display(this.arrowBottomPanel.mouseOver);
            deletePanel     .display(this.deletePanel.mouseOver);
        }
        else
        {        
            ObjectRowPanel.this.showDetailsPanel.setBackground(defaultColor);
            rightPanel.setBackground(defaultColor);
            buttonPanel.setBackground(defaultColor);
            buttonTopPanel.setBackground(defaultColor);
            ObjectRowPanel.this.textLabel.setBackground(defaultColor);
            ObjectRowPanel.this.setBackground(defaultColor);
    
            linkIconLabel   .conceal();
            arrowTopPanel   .conceal();
            arrowBottomPanel.conceal();
            deletePanel     .conceal();
        }
    }
    
    protected void setText(String myString)
    {
        this.textLabel.setText(myString);
    }
}

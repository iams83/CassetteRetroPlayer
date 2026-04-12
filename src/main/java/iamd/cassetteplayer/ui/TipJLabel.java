package iamd.cassetteplayer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class TipJLabel extends JPanel
{
    static ArrayList<TipJLabel> all = new ArrayList<>();
    
    public TipJLabel(String message, boolean addToRing)
    {
        this(message, new Color(0xffffbf), addToRing);
    }

    public TipJLabel(String message, Color background, boolean addToRing)
    {
        JLabel dismissLabel = new JLabel("<HTML><U>Dismiss</U></HTML>");
        dismissLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        dismissLabel.setForeground(Color.blue);
        dismissLabel.setBackground(background);
        dismissLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        dismissLabel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                TipJLabel.this.setVisible(false);
            }
        });
        
        JPanel subPanel = new JPanel(new BorderLayout());
        subPanel.add(new JLabel(message));
        subPanel.add(dismissLabel, BorderLayout.EAST);
        subPanel.setBorder(new EmptyBorder(2, 2, 2, 2));
        subPanel.setBackground(background);
        
        this.setLayout(new BorderLayout());
        this.add(subPanel);

        if (addToRing)
        {
            JLabel dismissAllLabel = new JLabel("<HTML><U>Dismiss all</U></HTML>");
            dismissAllLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            dismissAllLabel.setForeground(Color.blue);
            dismissAllLabel.setBackground(background);
            dismissAllLabel.setBorder(new EmptyBorder(2, 4, 2, 2));
            dismissAllLabel.addMouseListener(new MouseAdapter()
            {
                @Override
                public void mouseReleased(MouseEvent e)
                {
                    for (TipJLabel label : all)
                        label.setVisible(false);
                }
            });
            this.add(dismissAllLabel, BorderLayout.EAST);
        }
        
        this.setBackground(background);
        
        if (addToRing)
            all.add(this);
    }
}

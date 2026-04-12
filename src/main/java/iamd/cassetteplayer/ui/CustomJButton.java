package iamd.cassetteplayer.ui;

import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;

@SuppressWarnings("serial")
public class CustomJButton extends JButton
{
    public CustomJButton(ImageIcon imageIcon)
    {
        super(imageIcon);
        
        initialize();
    }

    public CustomJButton()
    {
        initialize();
    }
    
    boolean mousePressed = false;

    private void initialize()
    {
        this.addMouseListener(new MouseListener()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                mousePressed = false;
            }
            
            @Override
            public void mousePressed(MouseEvent e)
            {
                mousePressed = true;
            }
            
            @Override
            public void mouseExited(MouseEvent e)
            {
                mousePressed = false;
            }
            
            @Override
            public void mouseEntered(MouseEvent e)
            {
            }
            
            @Override
            public void mouseClicked(MouseEvent e)
            {
            }
        });
    }

    @Override
    public void paint(Graphics g)
    {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        BufferedImage buttonImage = this.isSelected() || mousePressed ? CustomJToggleButton.BUTTON_PRESSED_IMAGE : CustomJToggleButton.BUTTON_IMAGE;
        
        CustomJToggleButton.paintButton(this, g, buttonImage);
        
        Icon icon = getIcon();
        
        icon.paintIcon(this, g, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2);
    }

}

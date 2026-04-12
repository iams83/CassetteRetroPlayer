package iamd.cassetteplayer.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JToggleButton;

@SuppressWarnings("serial")
public class CustomJToggleButton extends JToggleButton
{
    static final public BufferedImage BUTTON_IMAGE, BUTTON_PRESSED_IMAGE;

    static 
    {
        try
        {
            BUTTON_IMAGE = ImageIO.read(CassettePlayerWidget.class.getResourceAsStream("button.png"));
            
            BUTTON_PRESSED_IMAGE = ImageIO.read(CassettePlayerWidget.class.getResourceAsStream("button-pressed.png"));
        }
        catch(IOException e)
        {
            throw new RuntimeException(e);
        }
    }
    
    
    public CustomJToggleButton(ImageIcon imageIcon)
    {
        super(imageIcon);
        
        this.initialize();
    }

    public CustomJToggleButton()
    {
        this.initialize();
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

    public void paint(Graphics g)
    {
        g.clearRect(0, 0, getWidth(), getHeight());
        
        BufferedImage buttonImage = this.isSelected() || mousePressed ? BUTTON_PRESSED_IMAGE : BUTTON_IMAGE;
        
        paintButton(this, g, buttonImage);
        
        Icon icon = getIcon();
        
        icon.paintIcon(this, g, (getWidth() - icon.getIconWidth()) / 2, (getHeight() - icon.getIconHeight()) / 2);
    }

    static public void paintButton(Component c, Graphics g, BufferedImage buttonImage)
    {
        int buttonWidth = buttonImage.getWidth();
        int buttonHeight = buttonImage.getHeight();
        int buttonWidth_2 = buttonImage.getWidth() / 2;
        int buttonHeight_2 = buttonImage.getHeight() / 2 + 5;
        
        g.drawImage(buttonImage, 0, 0, buttonWidth_2, buttonHeight_2, 0, 0, buttonWidth_2, buttonHeight_2, c);
        g.drawImage(buttonImage, c.getWidth() - buttonWidth_2, 0, c.getWidth(), buttonHeight_2, buttonWidth_2, 0, buttonWidth, buttonHeight_2, c);
        g.drawImage(buttonImage, 0, c.getHeight() - buttonHeight + buttonHeight_2, buttonWidth_2, c.getHeight(), 0, buttonHeight_2, buttonWidth_2, buttonHeight, c);
        g.drawImage(buttonImage, c.getWidth() - buttonWidth + buttonWidth_2, c.getHeight() - buttonHeight + buttonHeight_2, c.getWidth(), c.getHeight(), buttonWidth_2, buttonHeight_2, buttonWidth, buttonHeight, c);
        
        for (int x = buttonWidth_2; x < c.getWidth() - buttonWidth + buttonWidth_2; x ++)
        {
            g.drawImage(buttonImage, x, 0, x + 1, buttonHeight_2, buttonWidth_2, 0, buttonWidth_2 + 1, buttonHeight_2, c);
            g.drawImage(buttonImage, x, c.getHeight() - buttonHeight + buttonHeight_2, x + 1, c.getHeight(), buttonWidth_2, buttonHeight_2, buttonWidth_2 + 1, buttonHeight, c);
        }
        
        for (int y = buttonHeight_2; y < c.getHeight() - buttonHeight + buttonHeight_2; y ++)
        {
            g.drawImage(buttonImage, 0, y, buttonWidth_2, y + 1, 0, buttonHeight_2, buttonWidth_2, buttonHeight_2 + 1, c);
            g.drawImage(buttonImage, c.getWidth() - buttonWidth + buttonWidth_2, y, c.getWidth(), y + 1, buttonWidth_2, buttonHeight_2, buttonWidth, buttonHeight_2 + 1, c);
        }

        int center = buttonImage.getRGB(buttonWidth_2, buttonHeight_2);

        g.setColor(new Color(center));
        g.fillRect(buttonWidth_2, buttonHeight_2, c.getWidth() - buttonWidth + 1, c.getHeight() - buttonHeight + 1);
    }
}

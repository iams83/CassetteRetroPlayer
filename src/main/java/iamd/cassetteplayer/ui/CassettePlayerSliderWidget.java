package iamd.cassetteplayer.ui;

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.EmptyBorder;

@SuppressWarnings("serial")
public class CassettePlayerSliderWidget extends JPanel
{
    final public JSlider slider;

    public CassettePlayerSliderWidget(BufferedImage icon) throws IOException
    {
        this.slider = new JSlider(JSlider.HORIZONTAL, 0, 100, 75);
        
        JPanel sliderPanel = new JPanel(new BorderLayout());
        sliderPanel.add(new JLabel(" + ", JLabel.CENTER), BorderLayout.EAST);
        sliderPanel.add(this.slider);
        sliderPanel.add(new JLabel(" - ", JLabel.CENTER), BorderLayout.WEST);
        
        JLabel iconLabel = new JLabel(new ImageIcon(icon.getScaledInstance(16, 16, Image.SCALE_SMOOTH)));
        iconLabel.setBorder(new EmptyBorder(4, 4, 4, 4));
        
        this.setLayout(new BorderLayout());
        this.add(iconLabel, BorderLayout.WEST);
        this.add(sliderPanel);
    }
}

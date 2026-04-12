package iamd.ui;

import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Window;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.io.Serializable;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.colorchooser.AbstractColorChooserPanel;


@SuppressWarnings("serial")
public class ColorEditor extends AttributeEditor<ColorEditor.ColorField, Color>
{
    static class ColorField extends JPanel
    {
        private Color selectedColor;
        
        private ColorEditor colorEditor;

        public ColorField(Container parent)
        {
            this.setBorder(new EtchedBorder());
            
            this.addFocusListener(new FocusAdapter()
            {
                class ColorChooserDialogDisposeOnClose extends ComponentAdapter implements Serializable
                {
                    @Override
                    public void componentHidden(ComponentEvent e)
                    {
                        Window w = (Window)e.getComponent();
                        w.dispose();
                    }
                }

                @Override
                public void focusGained(FocusEvent e)
                {
                    try
                    {
                        final JColorChooser pane = new JColorChooser(selectedColor != null? selectedColor : Color.white);

                        pane.setChooserPanels(new AbstractColorChooserPanel[] { pane.getChooserPanels()[0] });
                        
                        JDialog dialog = JColorChooser.createDialog(parent, "Choose color...", true, pane, null, null);
                        
                        dialog.addComponentListener(new ColorChooserDialogDisposeOnClose());
                        
                        dialog.setVisible(true);
                        
                        Color newColor = pane.getColor();

                        if (newColor != null)
                        {
                            selectedColor = newColor;
                            
                            repaint();
                            
                            colorEditor.initializeValue(newColor);
                        }
                    }
                    catch (IllegalArgumentException | SecurityException e1)
                    {
                        ErrorMessage.showErrorMessage(e1);
                    }
                }
            });
            
            this.setPreferredSize(new JTextField("#XXXXXXX").getPreferredSize());
        }

        @Override
        public void paint(Graphics g)
        {
            try
            {
                g.setColor(this.selectedColor);
                g.fillRect(0, 0, this.getWidth(), this.getHeight());
            }
            catch (IllegalArgumentException | SecurityException e1)
            {
                ErrorMessage.showErrorMessage(e1);
            }
        }

        public void registerColorEditor(ColorEditor colorEditor)
        {
            this.colorEditor = colorEditor;
        }
    }

    public ColorEditor(Container parent)
    {
        super(new ColorField(parent), false, false);
        
        this.getComponent().registerColorEditor(this);
    }

    @Override
    protected void setValue(Color value)
    {
        this.getComponent().selectedColor = value;
        
        this.getComponent().repaint();
    }

    @Override
    protected Color getValue()
    {
        return this.getComponent().selectedColor;
    }

    @Override
    final protected boolean validateCurrentValue()
    {
        return validateCurrentValue(this.getComponent().selectedColor);
    }

    protected boolean validateCurrentValue(Color color)
    {
        return true;
    }

    @Override
    protected void valueTypeToComponent(JLabel label, Color color)
    {
        this.setDefaultColor(color);
        
        label.setText(String.format("#%02X%02X%02X", color.getRed(), color.getGreen(), color.getBlue()));
        
        int grey = (int) Math.round(0.3 * color.getRed() + .59 * color.getGreen() + .11 * color.getBlue());
        
        label.setForeground(grey < 128 ? Color.white : Color.black);
        label.setBackground(color);
    }
}

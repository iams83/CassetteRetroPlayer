package iamd.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

import iamd.rsrc.Resources;

@SuppressWarnings("serial")
public abstract class AttributeEditor<JEditorComponent extends JComponent, ValueType> extends JPanel
{
    final private AttributeBinder<ValueType> attributeBinder = new AttributeBinder<ValueType>()
    {
        @Override
        protected void initializeValue(ValueType value)
        {
            AttributeEditor.this.initializeValue(value);
        }
    };

    private static final String DISPLAY_CARD = "DISPLAY";
    private static final String EDIT_CARD = "EDIT";
    
    private ValueType previousValue;

    final private JEditorComponent component;
    
    final private JLabel valueLabel = new JLabel();
    
    final private CardLayout editPanelCardLayout = new CardLayout();
    
    final private JPanel valuePanel = new JPanel(this.editPanelCardLayout);

    final private KeyAdapter keyEventListener;
    
    final private JPanel displayValuePanel = new JPanel(new BorderLayout());

    private Color manualBackgroundColor;

    public void setDefaultColor(Color color)
    {
        this.manualBackgroundColor = color;
    }
    
    protected AttributeEditor(JEditorComponent component)
    {
        this(component, true, false);
    }
    
    protected AttributeEditor(JEditorComponent component, boolean overrideEnter, boolean scrollable)
    {
        this.keyEventListener = new KeyAdapter()
        {
            @Override
            public void keyReleased(KeyEvent e)
            {
                if (overrideEnter && e.getKeyCode() == KeyEvent.VK_ENTER)
                {
                    AttributeEditor.this.getComponent().setFocusable(false);
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            AttributeEditor.this.getComponent().setFocusable(true);
                        }
                    });
                }
                else if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
                {
                    AttributeEditor.this.discardEditingValue();
                }
            }
        };
        
        this.component = component;
        
        this.setLayout(new BorderLayout());
        
        this.valueLabel.setBorder(new EmptyBorder(2, 2, 2, 2));
        this.valueLabel.setHorizontalAlignment(JLabel.LEFT);
        
        Font font = this.valueLabel.getFont().deriveFont(Font.BOLD);
        
        this.valueLabel.setFont(font);
        this.component.setFont(font);
        
        JLabel editIcon = new JLabel(Resources.EmptyIcon);
        
        displayValuePanel.add(this.valueLabel);
        displayValuePanel.add(editIcon, BorderLayout.EAST);

        this.valueLabel.setPreferredSize(this.component.getPreferredSize());
        
        if (scrollable)
        {
            valuePanel.add(new JScrollPane(displayValuePanel), DISPLAY_CARD);
            valuePanel.add(new JScrollPane(this.component), EDIT_CARD);
        }
        else
        {
            JPanel displayValuePanelContainer = new JPanel();
            displayValuePanelContainer.setLayout(new BorderLayout());
            displayValuePanelContainer.setBorder(new EtchedBorder(EtchedBorder.LOWERED));
            displayValuePanelContainer.add(displayValuePanel);
            valuePanel.add(displayValuePanelContainer, DISPLAY_CARD);
            valuePanel.add(this.component, EDIT_CARD);
        }
        valuePanel.setMinimumSize(new Dimension(valuePanel.getMinimumSize().width, 16));

        this.add(valuePanel);
        
        Color autoBackgroundColor = displayValuePanel.getBackground();
        
        displayValuePanel.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseEntered(MouseEvent e)
            {
                if (manualBackgroundColor != null)
                    displayValuePanel.setBackground(manualBackgroundColor);
                else
                    displayValuePanel.setBackground(Color.WHITE);
                
                editIcon.setIcon(Resources.EditIcon);
            }

            @Override
            public void mouseExited(MouseEvent e)
            {
                if (manualBackgroundColor != null)
                    displayValuePanel.setBackground(manualBackgroundColor);
                else
                    displayValuePanel.setBackground(autoBackgroundColor);
                
                editIcon.setIcon(Resources.EmptyIcon);
            }
            
            public void mouseReleased(MouseEvent e)
            {
                AttributeEditor.this.setValue(AttributeEditor.this.previousValue);
                
                editPanelCardLayout.show(valuePanel, EDIT_CARD);

                AttributeEditor.this.getComponent().requestFocus();
            }
        });
        
        this.getComponent().addKeyListener(keyEventListener);
        
        this.getComponent().addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusLost(FocusEvent e)
            {
                AttributeEditor.this.confirmNewValue();
            }
        });
    }
    
    public void addAttributeEditionListener(AttributeEditorListener<ValueType> attributeEditorListener)
    {
        this.attributeBinder.addAttributeEditionListener(attributeEditorListener);
    }

    private void confirmNewValue()
    {
        if (!this.validateCurrentValue())
        {
            String message = this.valueNotValidErrorMessage();
            
            if (message == null)
            {
                this.discardEditingValue();
            }
            else
            {
                this.getComponent().removeKeyListener(this.keyEventListener);
                
                JOptionPane.showMessageDialog(this, message, "Question", JOptionPane.WARNING_MESSAGE);
                
                Timer t = new Timer(500, new ActionListener()
                {
                    public void actionPerformed(ActionEvent e)
                    {
                        AttributeEditor.this.getComponent().addKeyListener(AttributeEditor.this.keyEventListener);
                        
                        AttributeEditor.this.getComponent().setFocusable(true);
    
                        AttributeEditor.this.getComponent().requestFocus();
                    }
                });
                
                t.setRepeats(false);
                t.start();
            }
        }
        else
        {
            ValueType newValue = AttributeEditor.this.getValue();
            
            if ((newValue == null && this.previousValue != null) ||
                (newValue != null && (this.previousValue == null || !newValue.equals(AttributeEditor.this.previousValue))))
            {
                this.previousValue = newValue;
                
                AttributeEditor.this.valueTypeToComponent(AttributeEditor.this.valueLabel, newValue);
                
                if (this.manualBackgroundColor != null)
                    this.displayValuePanel.setBackground(this.manualBackgroundColor);

                this.setValue(newValue);
                
                this.attributeBinder.setBindedValue(newValue);
            }
            
            this.editPanelCardLayout.show(this.valuePanel, DISPLAY_CARD);
        }
    }
    
    protected String valueNotValidErrorMessage()
    {
        return null;
    }

    public JEditorComponent getComponent()
    {
        return this.component;
    }

    public void alignValueRight()
    {
        this.valueLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    }
    
    public void bindValue(Object editingObject, String editingField)
    {
        this.attributeBinder.bindValue(editingObject, editingField);
        
        this.discardEditingValue();
    }

    public void initializeValue(ValueType value)
    {
        AttributeEditor.this.previousValue = value;
        
        this.valueTypeToComponent(this.valueLabel, value);
        
        this.attributeBinder.setBindedValue(value);
        
        if (this.manualBackgroundColor != null)
            this.displayValuePanel.setBackground(this.manualBackgroundColor);
    }

    protected boolean validateCurrentValue()
    {
        return true;
    }

    abstract protected void setValue(ValueType value);

    abstract protected ValueType getValue();
    
    abstract protected void valueTypeToComponent(JLabel label, ValueType value);

    private void discardEditingValue()
    {
        this.setValue(AttributeEditor.this.previousValue);
        
        this.editPanelCardLayout.show(this.valuePanel, DISPLAY_CARD);
    }
}

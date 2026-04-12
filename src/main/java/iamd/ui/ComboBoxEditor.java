package iamd.ui;

import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.FocusAdapter;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

class ValueWithString<T>
{
    final private ComboBoxEditor<T> editor;
    
    final T value;
    
    public ValueWithString(ComboBoxEditor<T> editor, T value)
    {
        this.editor = editor;
        
        this.value = value;
    }
    
    @Override
    public String toString()
    {
        return this.editor.valueTypeToString(this.value);
    }
}

@SuppressWarnings("serial")
public class ComboBoxEditor<T> extends AttributeEditor<JComboBox<ValueWithString<T>>, T>
{
    final private HashMap<T, ValueWithString<T>> values = new HashMap<T, ValueWithString<T>>();
    
    public ComboBoxEditor(T[] values)
    {
        super(new JComboBox<ValueWithString<T>>());
        
        for (T value : values)
        {
            ValueWithString<T> item = new ValueWithString<T>(this, value);
            
            this.values.put(value, item);
            
            this.getComponent().addItem(item);
        }
        
        this.getComponent().addFocusListener(new FocusAdapter()
        {
            @Override
            public void focusGained(FocusEvent e)
            {
                ComboBoxEditor.this.getComponent().showPopup();
            }
        });
        
        this.getComponent().addItemListener(new ItemListener()
        {
            
            @Override
            public void itemStateChanged(ItemEvent e)
            {
                getComponent().setFocusable(false);
                
                SwingUtilities.invokeLater(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        getComponent().setFocusable(true);
                    }
                });
            }
        });
    }

    @Override
    protected void setValue(T value)
    {
        if (value != null)
        {
            for (ValueWithString<T> item : values.values())
            {
                if (item.value == value ||
                    item.value != null && item.value.toString().equals(value.toString()))
                    this.getComponent().setSelectedItem(item);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public T getValue()
    {
        return ((ValueWithString<T>) this.getComponent().getSelectedItem()).value;
    }

    protected String valueTypeToString(T s)
    {
        if (s == null)
            return "";  
        
        return s.toString();
    }
    
    @Override
    protected void valueTypeToComponent(JLabel label, T value)
    {
        label.setText(valueTypeToString(value));
        
        this.setValue(value);
    }
}

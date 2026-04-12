package iamd.ui;

import javax.swing.JLabel;
import javax.swing.JTextArea;

@SuppressWarnings("serial")
public class TextAreaEditor extends AttributeEditor<JTextArea, String>
{
    static private JTextArea createTextArea(int numRows)
    {
        JTextArea component = new JTextArea();
        
        component.setWrapStyleWord(true);
        component.setLineWrap(true);
        component.setRows(numRows);

        return component;
    }
    
    public TextAreaEditor(int numRows)
    {
        super(createTextArea(numRows), false, true);
    }

    @Override
    protected void setValue(String value)
    {
        this.getComponent().setText(value);
    }

    @Override
    protected String getValue()
    {
        return this.getComponent().getText();
    }


    @Override
    protected void valueTypeToComponent(JLabel label, String value)
    {
        if (value == null)
            label.setText("");
        else
            label.setText("<html>" + value.trim().replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\n", "<br/>") + "</html>");
    }
    
    @Override
    final protected boolean validateCurrentValue()
    {
        return validateCurrentValue(this.getComponent().getText());
    }

    protected boolean validateCurrentValue(String text)
    {
        return true;
    }
}

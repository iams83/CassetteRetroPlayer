package iamd.ui;

import javax.swing.JLabel;
import javax.swing.JTextField;

@SuppressWarnings("serial")
public class FilePathEditor extends AttributeEditor<JTextField, String>
{
    public FilePathEditor()
    {
        super(new JTextField());
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
    final protected boolean validateCurrentValue()
    {
        return validateCurrentValue(this.getComponent().getText());
    }

    protected boolean validateCurrentValue(String text)
    {
        return true;
    }

    @Override
    protected void valueTypeToComponent(JLabel label, String value)
    {
        label.setText(value == null ? "" : value);
    }
}

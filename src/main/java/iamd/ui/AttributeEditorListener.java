package iamd.ui;

import java.lang.reflect.Field;

public interface AttributeEditorListener<ValueType>
{
    public void attributeModified(Object editingObject, Field editingField, ValueType value);
}

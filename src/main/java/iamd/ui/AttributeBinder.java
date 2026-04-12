package iamd.ui;

import java.lang.reflect.Field;
import java.util.ArrayList;

public abstract class AttributeBinder<ValueType>
{
    ArrayList<AttributeEditorListener<ValueType>> attributeEditorListeners = new ArrayList<AttributeEditorListener<ValueType>>();

    public void addAttributeEditionListener(AttributeEditorListener<ValueType> attributeEditorListener)
    {
        this.attributeEditorListeners.add(attributeEditorListener);
    }

    private Object editingObject;
    
    private Field editingField;
    
    public void setBindedValue(ValueType value)
    {
        try
        {
            if (this.editingObject != null && this.editingField != null)
                this.editingField.set(this.editingObject, value);
        }
        catch (IllegalArgumentException | IllegalAccessException e)
        {
            throw new AssertionError(e);
        }
        
        for (AttributeEditorListener<ValueType> listener : this.attributeEditorListeners)
            listener.attributeModified(this.editingObject, this.editingField, value);
    }

    public void bindValue(Object editingObject, String attribute)
    {
        this.editingObject = editingObject;
        
        try
        {
            this.editingField = this.editingObject.getClass().getDeclaredField(attribute);
            
            this.editingField.setAccessible(true);
            
            @SuppressWarnings("unchecked")
            ValueType value = (ValueType) this.editingField.get(this.editingObject);
            
            this.initializeValue(value);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e)
        {
            throw new AssertionError(e);
        }
    }
    
    abstract protected void initializeValue(ValueType value);

}

package iamd.ui;

public interface RowPanelListListener<T extends ObjectRowPanel<?>>
{
    void rowPanelClicked(T rowPanel);
    
    void rowPanelMovedUp(T rowPanel);
    
    void rowPanelMovedDown(T rowPanel);
    
    void rowPanelDeleted(T rowPanel);

    void rowPanelNew();
}

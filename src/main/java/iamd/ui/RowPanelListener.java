package iamd.ui;


public interface RowPanelListener<T extends ObjectRowPanel<?>>
{
    void rowPanelClicked(T objectRowPanel);
}

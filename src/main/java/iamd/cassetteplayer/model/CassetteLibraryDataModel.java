package iamd.cassetteplayer.model;

import java.awt.event.MouseEvent;
import java.util.ArrayList;

import iamd.cassetteplayer.Cassette;

public class CassetteLibraryDataModel
{
    public interface Listener
    {
        public void onCasseteListUpdated();

        public void onCasseteClicked(MouseEvent event, Cassette cassette);
    }
    
    final private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }
    
    final private ArrayList<Cassette> cassettes = new ArrayList<Cassette>();

    private int currentCassetteIndex = -1;
    
    public Cassette[] getCassettes()
    {
        return this.cassettes.toArray(new Cassette[0]);
    }

    public void addCassette(Cassette cassette)
    {
        this.cassettes.add(cassette);
        
        for (Listener listener : this.listeners)
            listener.onCasseteListUpdated();
    }

    public void removeCassette(Cassette cassetteToRemove)
    {
        int indexOfCassetteToRemove = this.cassettes.indexOf(cassetteToRemove);
        
        this.cassettes.remove(indexOfCassetteToRemove);
        
        if (indexOfCassetteToRemove == this.cassettes.size())
            this.currentCassetteIndex = -1;

        for (Listener listener : this.listeners)
            listener.onCasseteListUpdated();
    }

    public Cassette getCurrentCassette()
    {
        if (this.cassettes.isEmpty() || this.currentCassetteIndex == -1)
            return null;
        
        return this.cassettes.get(this.currentCassetteIndex);
    }

    public void setCurrentCassetteIndex(MouseEvent event, int index)
    {
        this.currentCassetteIndex = index;
        
        for (Listener listener : this.listeners)
            listener.onCasseteClicked(event, this.getCurrentCassette());
    }
}

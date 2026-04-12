package iamd.cassetteplayer.model;

import java.util.ArrayList;

public class CassettePlayerDataModel
{
    public interface Listener
    {
        public void onMasterGainChanged(int masterGainPercentage);
    }
    
    final private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }

    public void removeListener(Listener listener)
    {
        this.listeners.remove(listener);
    }
    
    public enum Status
    {
        MENU,
    	STOP,
        RWND, 
        PLAY, 
        FFWD;
    }
    
    private Status status;
    
    private int masterGainPercentage;

    public Status getStatus()
    {
        return this.status;
    }

    public void setStatus(Status status)
    {
        this.status = status;
    }

    public int getMasterGain()
    {
        return this.masterGainPercentage;
    }

    public boolean setMasterGain(int value)
    {
        if (this.masterGainPercentage == -1)
            return false;
        
        if (this.masterGainPercentage != value)
        {
            this.masterGainPercentage = value;
            
            for (Listener listener : this.listeners)
                listener.onMasterGainChanged(this.masterGainPercentage);
        }
        
        return true;
    }
}

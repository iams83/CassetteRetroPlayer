package iamd.cassetteplayer.audio;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import javax.sound.sampled.FloatControl;
import javax.swing.SwingUtilities;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;

import iamd.cassetteplayer.model.CassettePlayerDataModel;
import iamd.ui.ErrorMessage;
import javazoom.jl.decoder.JavaLayerException;

public class MP3FilePlayer
{
    public interface Listener
    {
        public void onStop();
    }
    
    final private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }
    
    final private CassettePlayerDataModel playerDataModel;
    
    final public File file;
    final public String artist, album, title, year;
    final public double frameTime;
    final public String bitrate;
    final public String channels;

    private byte[] mediaContent = null;
    private AudioPlayer player = null;
    private int currentFrame = 0;
    private int frameCount;
    
    private Thread thread;

    public MP3FilePlayer(CassettePlayerDataModel playerDataModel, File file, InputStream fis, double frameTime) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        this.playerDataModel = playerDataModel;
        
        if (file != null)
        {
        	System.out.println("Creating MP3FilePlayer instance for " + file);
        	
            AudioFile f = AudioFileIO.read(file);
            Tag tag = f.getTag();
            this.file = file;
            this.artist = tag.getFirst(FieldKey.ARTIST);
            this.album = tag.getFirst(FieldKey.ALBUM);
            this.title = tag.getFirst(FieldKey.TITLE);
            this.year = tag.getFirst(FieldKey.YEAR);
            
            if (frameTime > 0)
            {
                this.frameTime = frameTime;
                this.bitrate = null;
                this.channels = null;
            }
            else
            {
                AudioHeader ah = f.getAudioHeader();
                
                String bitrate;
                try
                {
                    bitrate = Integer.parseInt(ah.getBitRate()) + " Kbps";
                }
                catch(NumberFormatException e)
                {
                    bitrate = ah.getBitRate();
                }

                this.bitrate = bitrate;
                this.channels = ah.getChannels();

                // TODO: This is not working correctly
                this.frameTime = ah.getTrackLength() / ah.getSampleRateAsNumber();
            }
        }
        else
        {
            this.file = null;
            this.artist = this.album = this.title = this.year = null;
            this.frameTime = frameTime;
            this.bitrate = null;
            this.channels = null;
        }
            
        byte[] buffer = new byte[1024 * 1024];
        
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream())
        {
            int read = 0;
            
            while ((read = fis.read(buffer)) != -1)
                baos.write(buffer, 0, read);
            
            this.mediaContent = baos.toByteArray();
        }
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(this.mediaContent))
        {
            AudioPlayer audioPlayer = new AudioPlayer(bais);
            
            while (audioPlayer.skipFrame()) { /* Skip frames until the end */ }
            
            this.frameCount = audioPlayer.getCurrentFrame();
        }
    }

    public boolean play() throws IOException, JavaLayerException
    {
        if (this.currentFrame == this.frameCount)
            return false;
        
        if (this.thread == null)
        {
            InputStream inputStream = new ByteArrayInputStream(this.mediaContent);

            this.player = new AudioPlayer(inputStream);
            
            for (int i = 0; i < this.currentFrame - 1; i ++)
                this.player.skipFrame();
            
            this.thread = new Thread()
            {
                public void run()
                {
                    CassettePlayerDataModel.Listener playerDataModelListener = null;
                    
                    try
                    {
                        player.play(1);
                        
                        FloatControl masterGainControl = player.getControl(FloatControl.Type.MASTER_GAIN);

                        playerDataModelListener = new CassettePlayerDataModel.Listener()
                        {
                            @Override
                            public void onMasterGainChanged(int masterGainPercentage)
                            {
                                if (masterGainControl == null)
                                {
                                    playerDataModel.setMasterGain(-1);
                                }
                                else
                                {
                                    float value = masterGainControl.getMinimum() + 
                                            (masterGainControl.getMaximum() - masterGainControl.getMinimum()) * masterGainPercentage / 100;
                                    
                                    masterGainControl.setValue(value);
                                }
                            }
                        };
                        
                        playerDataModel.addListener(playerDataModelListener);
                        
                        int masterGain = playerDataModel.getMasterGain();
                        
                        if (masterGain != -2)
                        	playerDataModelListener.onMasterGainChanged(masterGain);
                        
                        MP3FilePlayer.this.player.play(MP3FilePlayer.this.frameCount - MP3FilePlayer.this.currentFrame);
                        
                        inputStream.close();
                        
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                try
                                {
                                    MP3FilePlayer.this.stop();
                                }
                                catch (IOException | InterruptedException e1)
                                {
                                    ErrorMessage.showErrorMessage(e1);
                                }
                                
                                if (MP3FilePlayer.this.getCurrentFrame() == MP3FilePlayer.this.frameCount)
                                {
                                    for (Listener listener : MP3FilePlayer.this.listeners)
                                        listener.onStop();
                                }
                            }
                        });
                    }
                    catch (JavaLayerException | IOException e1)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                ErrorMessage.showErrorMessage(e1);
                            }
                        });
                    }
                    finally
                    {
                        if (playerDataModelListener != null)
                            playerDataModel.removeListener(playerDataModelListener);
                    }
                }
            };
            
            this.thread.start();
        }
        
        return true;
    }

    public void stop() throws IOException, InterruptedException
    {
        if (this.thread != null)
        {
            this.player.close();
            
            this.thread.join();

            this.currentFrame = this.player.getCurrentFrame();

            this.thread = null;
            
            this.player = null;
        }
    }

    void setTotalFrameCount(int frameCount)
    {
        this.frameCount = frameCount;
    }
    
    public int getTotalFrameCount()
    {
        return this.frameCount;
    }

    public int getCurrentFrame()
    {
        if (this.thread != null)
            return this.player.getCurrentFrame();
        else
            return this.currentFrame;
    }

    public boolean ffwd() throws JavaLayerException, IOException, InterruptedException
    {
        if (this.thread != null)
            this.stop();
            
        if (this.currentFrame < this.frameCount)
        {
            this.currentFrame ++;
            return true;
        }
        
        return false;
    }

    public boolean rwnd() throws IOException, InterruptedException
    {
        if (this.thread != null)
            this.stop();
            
        if (this.currentFrame > 0)
        {
            this.currentFrame --;
            return true;
        }
        
        return false;
    }

    public void setCurrentFrame(int frame) throws IOException, InterruptedException
    {
        if (this.thread != null)
            this.stop();
            
        this.currentFrame = frame;
    }
    
    public void debug()
    {
        System.out.println(this.getCurrentFrame() + "/" + this.getTotalFrameCount() + " " + this.artist + " " + this.album + " " + this.title);
    }

    public double getTrackLength()
    {
        return this.getTotalFrameCount() * this.frameTime;
    }

    public String getTrackLengthAsString()
    {
        return MP3FilePlayer.getTrackLengthAsString(this.getTrackLength());
    }
    
    static public String getTrackLengthAsString(double trackLengthDbl)
    {
        int trackLength = (int) Math.round(trackLengthDbl);

        int durationSec = trackLength % 60;
        int durationMin = trackLength / 60;
        
        if (durationMin >= 60)
        {
            int durationHour = durationMin / 60;

            durationMin %= durationMin;

            return String.format("%d:%02d:%02d", durationHour, durationMin, durationSec);
        }
        else
            return String.format("%d:%02d", durationMin, durationSec);
    }
}
package iamd.cassetteplayer;

import java.io.IOException;
import java.util.ArrayList;

import iamd.cassetteplayer.Cassette.Side;
import iamd.cassetteplayer.audio.MP3FilePlayerWrapper;
import iamd.ui.ErrorMessage;
import javazoom.jl.decoder.JavaLayerException;

public class CassetteSide
{
    final private ArrayList<MP3FilePlayerWrapper> audioPlayers = new ArrayList<>();
    
    final public Side side;
    protected String headerLine, bodyText;
    protected String qualityBitRate;
    protected String qualityChannel;

    private int currentAudioPlayer = 0;
    
    public CassetteSide(Side side)
    {
        this.side = side;
    }
    
    public void initialize()
    {
        MP3FilePlayerWrapper.Listener audioPlayerListener = new MP3FilePlayerWrapper.Listener()
        {
            @Override
            public void onStop()
            {
                try
                {
                    if (CassetteSide.this.currentAudioPlayer < CassetteSide.this.audioPlayers.size() - 1)
                    {
                        CassetteSide.this.currentAudioPlayer ++;
                        
                        CassetteSide.this.play();
                    }
                }
                catch(IOException | JavaLayerException e)
                {
                    ErrorMessage.showErrorMessage(e);
                }
            }
        };
        
        for (int i = 0; i < audioPlayers.size() - 1; i ++)
            this.audioPlayers.get(i).addListener(audioPlayerListener);
        
        this.autoWritePrintedText();
    }

    public ArrayList<MP3FilePlayerWrapper> getAudioPlayers()
    {
        return this.audioPlayers;
    }

    public ArrayList<MP3FilePlayerWrapper> getAudioPlayersWithNoNoise()
    {
        ArrayList<MP3FilePlayerWrapper> audioPlayersCopy = new ArrayList<>();

        for (MP3FilePlayerWrapper audioPlayer : this.audioPlayers)
        {
            if (audioPlayer.file != null)
                audioPlayersCopy.add(audioPlayer);
        }
        
        return audioPlayersCopy;
    }

    public void autoWritePrintedText()
    {
        setPrintedText(null, null);

        if (!this.audioPlayers.isEmpty())
        {
            String commonArtist = this.audioPlayers.get(0).getArtist();
            String commonAlbum = this.audioPlayers.get(0).getAlbum();
            String commonYear = this.audioPlayers.get(0).getYear();
            
            if (commonYear != null && commonYear.length() <= 1)
            	commonYear = null;
            
            String bodyText = null;
            
            for (MP3FilePlayerWrapper audioPlayer : this.audioPlayers)
            {
                if (audioPlayer.getArtist() != null && commonArtist != null && !audioPlayer.getArtist().equals(commonArtist))
                    commonArtist = null;

                if (audioPlayer.getAlbum() != null && commonAlbum != null && !audioPlayer.getAlbum().equals(commonAlbum))
                    commonAlbum = null;
                
                if (audioPlayer.getYear() != null && audioPlayer.getYear().length() > 1 && commonYear != null && !audioPlayer.getYear().equals(commonYear))
                	commonYear = null;
                
                if (audioPlayer.getTitle() != null)
                {
                    if (bodyText == null)
                        bodyText = audioPlayer.getTitle();
                    else
                        bodyText += " - " + audioPlayer.getTitle();
                }
            }
            
            String headerLine;
            
            if (commonArtist != null && commonAlbum != null)
                headerLine = commonArtist + " - " + commonAlbum;
            else if (commonArtist != null)
                headerLine = commonArtist;
            else
                headerLine = "Various Artists";
            
            if (commonYear != null)
            	headerLine += " (" + commonYear + ")";
            
            this.qualityBitRate = this.audioPlayers.get(0).getBitRate();
            this.qualityChannel = this.audioPlayers.get(0).getChannel();
            this.headerLine = headerLine;
            this.bodyText = bodyText;
        }
    }

    public void setPrintedText(String headerLine, String bodyText)
    {
        this.headerLine = headerLine;
        this.bodyText = bodyText;
    }

    public Side getSide()
    {
        return this.side;
    }

    public String getBodyText()
    {
        return this.bodyText;
    }

    public String getHeaderLine()
    {
        return this.headerLine;
    }

    public String getChannelQuality()
    {
        return this.qualityChannel;
    }

    public String getQualityBitRate()
    {
        return this.qualityBitRate;
    }

    public void setQualityBitRate(String bitrate)
    {
        this.qualityBitRate = bitrate;
    }

    public void setQualityChannel(String channels)
    {
        this.qualityChannel = channels;
    }

    public void setHeaderLine(String headerLine)
    {
        this.headerLine = headerLine;
    }

    public void setBodyText(String bodyText)
    {
        this.bodyText = bodyText;
    }

    public MP3FilePlayerWrapper[] getAllAudioPlayers()
    {
        return this.audioPlayers.toArray(new MP3FilePlayerWrapper[0]);
    }
    
    private MP3FilePlayerWrapper getAudioPlayer()
    {
        if (this.currentAudioPlayer == this.audioPlayers.size())
            return null;
        
        return this.audioPlayers.get(this.currentAudioPlayer);
    }

    public double getProgress()
    {
        return 1. * this.getCurrentFrame() / this.getTotalFrameCount();
    }

    public boolean ffwd() throws JavaLayerException, IOException, InterruptedException
    {
        MP3FilePlayerWrapper audioPlayer = this.getAudioPlayer();
        
        if (audioPlayer == null)
            return false;
        
        if (!audioPlayer.ffwd())
        {
            if (this.currentAudioPlayer < this.audioPlayers.size() - 1)
            {
                this.currentAudioPlayer ++;
                
                return this.ffwd();
            }
            
            return false;
        }
        
        return true;
    }

    public boolean rwnd() throws IOException, InterruptedException
    {
        MP3FilePlayerWrapper audioPlayer = this.getAudioPlayer();
        
        if (audioPlayer == null)
            return false;
        
        if (!audioPlayer.rwnd())
        {
            if (this.currentAudioPlayer > 0)
            {
                this.currentAudioPlayer --;
                return this.rwnd();
            }
            
            return false;
        }
        
        return true;
    }

    public boolean play() throws IOException, JavaLayerException
    {
        MP3FilePlayerWrapper audioPlayer = this.getAudioPlayer();
        
        if (audioPlayer == null)
            return false;
        
        if (!audioPlayer.play())
        {
            if (this.currentAudioPlayer < this.audioPlayers.size() - 1)
            {
                this.currentAudioPlayer ++;
                
                return this.play();
            }
            
            return false;
        }
        
        return true;
    }

    public void stop() throws IOException, InterruptedException
    {
        MP3FilePlayerWrapper audioPlayer = this.getAudioPlayer();
        
        if (audioPlayer == null)
            return;
        
        audioPlayer.stop();
    }

    public void debug()
    {
        MP3FilePlayerWrapper audioPlayer = this.getAudioPlayer();
        
        if (audioPlayer == null)
            return;
        
        audioPlayer.debug();
    }

    public int getTotalFrameCount()
    {
        int allPreviousFrameCounts = 0;
        
        for (MP3FilePlayerWrapper audioPlayer : this.audioPlayers)
            allPreviousFrameCounts += audioPlayer.getTotalFrameCount();
        
        return allPreviousFrameCounts;
    }

    public int getCurrentFrame()
    {
        int allPreviousFrameCounts = 0;
        
        for (int i = 0; i < this.currentAudioPlayer; i ++)
            allPreviousFrameCounts += this.audioPlayers.get(i).getTotalFrameCount();
            
        MP3FilePlayerWrapper audioPlayer = this.getAudioPlayer();
        
        if (audioPlayer != null)
            allPreviousFrameCounts += audioPlayer.getCurrentFrame();
        
        return allPreviousFrameCounts;
    }

    public void setCurrentFrame(int frame) throws IOException, InterruptedException
    {
        this.currentAudioPlayer = 0;
        
        for (MP3FilePlayerWrapper audioPlayer : this.audioPlayers)
        {
            if (frame > audioPlayer.getTotalFrameCount())
            {
                audioPlayer.setCurrentFrame(audioPlayer.getTotalFrameCount());
                
                this.currentAudioPlayer ++;
            }
            else if (frame > 0)
                audioPlayer.setCurrentFrame(frame);
            else
                audioPlayer.setCurrentFrame(0);
            
            frame -= audioPlayer.getTotalFrameCount();
        }
    }

    public int getDurationAsSecs()
    {
        double totalLength = 0;
        
        for (MP3FilePlayerWrapper player : this.getAllAudioPlayers())
            totalLength += player.getTrackLength();
        
        return (int) Math.round(totalLength);
    }
}

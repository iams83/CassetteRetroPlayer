package iamd.cassetteplayer;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.function.Predicate;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import iamd.cassetteplayer.audio.MP3FilePlayerWrapper;
import iamd.cassetteplayer.audio.MP3FilePlayerWrapper.MP3CachedData;
import iamd.cassetteplayer.model.CassetteDesignDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel;
import javazoom.jl.decoder.JavaLayerException;

public class Cassette
{
    public enum Side
    {
        A, B;

        public Side reverse()
        {
            if (this == A)
                return B;
            else
                return A;
        }
    }
    
    final private CassettePlayerDataModel playerDataModel;
    
    final private CassetteDesignDataModel designDataModel = new CassetteDesignDataModel();
    
    final private CassettePainter cassettePainter = new CassettePainter();
    
    final private CassetteSide sides[] = new CassetteSide[2];

    private Side currentSide = Side.A;
    
    public Cassette(CassettePlayerDataModel playerDataModel, File ... audioFiles) throws FileNotFoundException, IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {        
        this.playerDataModel = playerDataModel;
        
        LinkedList<File> audioFilesList = new LinkedList<File>(Arrays.asList(audioFiles));
        
        CassetteSide sideA = this.sides[Side.A.ordinal()] = new CassetteSide(Side.A);
        CassetteSide sideB = this.sides[Side.B.ordinal()] = new CassetteSide(Side.B);

        int sideASideBFrameDiff = sideA.getTotalFrameCount() - sideB.getTotalFrameCount();
        
        while (!audioFilesList.isEmpty())
        {
            if (sideASideBFrameDiff <= 0)
                sideA.getAudioPlayers().add(MP3FilePlayerWrapper.createFromFile(playerDataModel, audioFilesList.removeFirst()));
            else
                sideB.getAudioPlayers().add(0, MP3FilePlayerWrapper.createFromFile(playerDataModel, audioFilesList.removeLast()));
            
            sideASideBFrameDiff = sideA.getTotalFrameCount() - sideB.getTotalFrameCount();
        }
        
        this.initialize();
    }

    public Cassette(CassettePlayerDataModel playerDataModel, ArrayList<MP3CachedData> audioFilesA, ArrayList<MP3CachedData> audioFilesB) throws FileNotFoundException, IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {        
        this.playerDataModel = playerDataModel;
        
        CassetteSide sideA = this.sides[Side.A.ordinal()] = new CassetteSide(Side.A);
        CassetteSide sideB = this.sides[Side.B.ordinal()] = new CassetteSide(Side.B);

        for (MP3CachedData file : audioFilesA)
            sideA.getAudioPlayers().add(MP3FilePlayerWrapper.createFromFile(playerDataModel, file));

        for (MP3CachedData file : audioFilesB)
            sideB.getAudioPlayers().add(MP3FilePlayerWrapper.createFromFile(playerDataModel, file));

        this.initialize();
    }

    void initialize() throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        CassetteSide sideA = this.getCassetteSide(Side.A);
        CassetteSide sideB = this.getCassetteSide(Side.B);

        Predicate<MP3FilePlayerWrapper> noiseSelector = new Predicate<MP3FilePlayerWrapper>()
        {
            @Override
            public boolean test(MP3FilePlayerWrapper t)
            {
                return t.file == null;
            }
        };
        
        sideA.getAudioPlayers().removeIf(noiseSelector);
        sideB.getAudioPlayers().removeIf(noiseSelector);
        
        int sideASideBFrameDiff = sideA.getTotalFrameCount() - sideB.getTotalFrameCount();
        
        while (sideASideBFrameDiff < 0)
        {
            sideA.getAudioPlayers().add(MP3FilePlayerWrapper.createNoise(this.playerDataModel, -sideASideBFrameDiff));
            
            sideASideBFrameDiff = sideA.getTotalFrameCount() - sideB.getTotalFrameCount();
        }
        
        while (sideASideBFrameDiff > 0)
        {
            sideB.getAudioPlayers().add(MP3FilePlayerWrapper.createNoise(this.playerDataModel, sideASideBFrameDiff));
            
            sideASideBFrameDiff = sideA.getTotalFrameCount() - sideB.getTotalFrameCount();
        }
        
        sideA.initialize();
        sideB.initialize();
    }

    public CassetteSide getCassetteSide(Side side)
    {
        return this.sides[side.ordinal()];
    }
    
    public Side getCurrentSide()
    {
        return this.currentSide;
    }
    
    public CassetteSide getCurrentCassetteSide()
    {
        return this.getCassetteSide(this.currentSide);
    }
    
    public int getTotalFrameCount()
    {
        return this.getCurrentCassetteSide().getTotalFrameCount();
    }

    public int getCurrentFrame()
    {
        return this.getCurrentCassetteSide().getCurrentFrame();
    }

    public boolean ffwd() throws JavaLayerException, IOException, InterruptedException
    {
        return this.getCurrentCassetteSide().ffwd();
    }

    public boolean rwnd() throws IOException, InterruptedException
    {
        return this.getCurrentCassetteSide().rwnd();
    }

    public void debug()
    {
        this.getCurrentCassetteSide().debug();
    }

    public boolean play() throws IOException, JavaLayerException
    {
        return this.getCurrentCassetteSide().play();
    }

    public void stop() throws IOException, InterruptedException
    {
        this.getCurrentCassetteSide().stop();
    }

    public void setCurrentFrame(int frame) throws IOException, InterruptedException
    {
        this.getCurrentCassetteSide().setCurrentFrame(frame);
    }

    public double getProgress()
    {
        return 1. * this.getCurrentFrame() / this.getTotalFrameCount();
    }

    public void setProgress(double progress) throws IOException, InterruptedException
    {
        int frame = (int) Math.round(progress * this.getTotalFrameCount());
        
        if (frame < 0)
            frame = 0;
        
        if (frame >= this.getTotalFrameCount())
            frame = this.getTotalFrameCount();
        
        this.setCurrentFrame(frame);
    }

    public void setCurrentSide(Side side) throws IOException, InterruptedException
    {
        if (this.currentSide != side)
            reverse();
    }

    public void reverse() throws IOException, InterruptedException
    {
        int currentFrame = this.getCurrentFrame();
        
        this.currentSide = this.currentSide.reverse();

        this.setCurrentFrame(this.getTotalFrameCount() - currentFrame);
    }

    public CassetteDesignDataModel getDesignDataModel()
    {
        return this.designDataModel;
    }

    public Shape paint(Graphics2D g2, Dimension size, AffineTransform tx2, Side side, int rotation)
    {
        return this.cassettePainter.paintCassette(g2, size, tx2, this.designDataModel, 
                this.getCassetteSide(side), this.getCurrentFrame(), this.getTotalFrameCount(), rotation);
    }

    public void invalidateCache()
    {
        this.cassettePainter.invalidateCache();
    }

    public void moveTrackUp(Side side, int index) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        if (index <= 0)
        {
            ArrayList<MP3FilePlayerWrapper> audioPlayersBWithNoNoise = this.getCassetteSide(side.reverse()).getAudioPlayersWithNoNoise();
            
            ArrayList<MP3FilePlayerWrapper> audioPlayersA = this.getCassetteSide(side).getAudioPlayers();
            ArrayList<MP3FilePlayerWrapper> audioPlayersB = this.getCassetteSide(side.reverse()).getAudioPlayers();

            audioPlayersB.add(audioPlayersBWithNoNoise.size() - 1, audioPlayersA.remove(0));

            this.initialize();
        }
        else
        {
            ArrayList<MP3FilePlayerWrapper> audioPlayersAWithNoNoise = this.getCassetteSide(side).getAudioPlayersWithNoNoise();

            if (index < audioPlayersAWithNoNoise.size())
            {
                ArrayList<MP3FilePlayerWrapper> audioPlayersA = this.getCassetteSide(side).getAudioPlayers();
                
                audioPlayersA.add(index - 1, audioPlayersA.remove(index));
            }
        }
    }

    public void moveTrackDown(Side side, int index) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        ArrayList<MP3FilePlayerWrapper> audioPlayersAWithNoNoise = this.getCassetteSide(side).getAudioPlayersWithNoNoise();

        if (index >= audioPlayersAWithNoNoise.size())
        {
            ArrayList<MP3FilePlayerWrapper> audioPlayersA = this.getCassetteSide(side).getAudioPlayers();
            ArrayList<MP3FilePlayerWrapper> audioPlayersB = this.getCassetteSide(side.reverse()).getAudioPlayers();

            audioPlayersB.add(0, audioPlayersA.remove(audioPlayersAWithNoNoise.size() - 1));
            
            this.initialize();
        }
        else if (index >= 0)
        {
            moveTrackUp(side, index + 1);
        } 
    }

    public void addTracks(Side side, File[] files) throws FileNotFoundException, IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        ArrayList<MP3FilePlayerWrapper> audioPlayersA = this.getCassetteSide(side).getAudioPlayers();
        
        for (File file : files)
        {
            audioPlayersA.add(MP3FilePlayerWrapper.createFromFile(this.playerDataModel, file));
        }
        
        this.initialize();
    }

    public void removeTrack(Side side, int index) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        if (index >= 0 && index < this.getCassetteSide(side).getAudioPlayers().size())
        {
            this.getCassetteSide(side).getAudioPlayers().remove(index);
            
            this.initialize();
        }
    }
}

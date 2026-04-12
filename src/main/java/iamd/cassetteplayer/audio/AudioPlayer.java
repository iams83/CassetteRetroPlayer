package iamd.cassetteplayer.audio;

import java.io.InputStream;
import java.lang.reflect.Field;

import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;

import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.BitstreamException;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.decoder.SampleBuffer;
import javazoom.jl.player.AudioDevice;
import javazoom.jl.player.FactoryRegistry;
    
/**
 * The <code>Player</code> class implements a simple player for playback
 * of an MPEG audio stream. 
 * 
 * @author  Mat McGowan
 * @since   0.0.8
 */

// REVIEW: the audio device should not be opened until the
// first MPEG audio frame has been decoded. 
public class AudioPlayer
{       
    /**
     * The MPEG audio bitstream. 
     */
    // javac blank final bug. 
    /*final*/ private Bitstream     bitstream;
    
    /**
     * The MPEG audio decoder. 
     */
    /*final*/ private Decoder       decoder; 
    
    /**
     * The AudioDevice the audio samples are written to. 
     */
    private AudioDevice audio;
    
    /**
     * Has the player been closed?
     */
    private boolean     closed = false;
    
    /**
     * Has the player played back all frames from the stream?
     */
    private boolean     complete = false;

    private int         lastPosition = 0;
    
    private Object      currentFrameSynchronizer = new Object();
    
    private int         currentFrame = 0;
    
    /**
     * Creates a new <code>Player</code> instance. 
     */
    public AudioPlayer(InputStream stream) throws JavaLayerException
    {
        this(stream, null); 
    }
    
    public AudioPlayer(InputStream stream, AudioDevice device) throws JavaLayerException
    {
        bitstream = new Bitstream(stream);      
        decoder = new Decoder();

        if (device!=null)
            audio = device;
        else
            audio = FactoryRegistry.systemRegistry().createAudioDevice();

        audio.open(decoder);
    }

    public FloatControl getControl(Control.Type control) throws JavaLayerException
    {
        try
        {
            Class<? extends AudioDevice> audioClass = this.audio.getClass();
            
            Field sourceField = audioClass.getDeclaredField("source");
            
            sourceField.setAccessible(true);
            
            javax.sound.sampled.SourceDataLine source = (javax.sound.sampled.SourceDataLine) sourceField.get(this.audio);
            
            if (!source.isControlSupported(control))
                return null;
            
            return (FloatControl) source.getControl(control);
        }
        catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e)
        {
            throw new AssertionError(e);
        }
    }
    
    public void play() throws JavaLayerException
    {
        play(Integer.MAX_VALUE);
    }

    /**
     * Plays a number of MPEG audio frames. 
     * 
     * @param frames    The number of frames to play. 
     * @return  true if the last frame was played, or false if there are
     *          more frames. 
     */
    public boolean play(int frames) throws JavaLayerException
    {
        boolean ret = true;
            
        while (frames-- > 0 && ret)
        {
            ret = decodeFrame();            
        }
        
        if (!ret)
        {
            // last frame, ensure all data flushed to the audio device. 
            AudioDevice out = audio;
            if (out!=null)
            {               
                out.flush();
                synchronized (this)
                {
                    complete = (!closed);
                    close();
                }               
            }
        }
        return ret;
    }
        
    /**
     * Cloases this player. Any audio currently playing is stopped
     * immediately. 
     */
    public synchronized void close()
    {       
        AudioDevice out = audio;
        if (out!=null)
        { 
            closed = true;
            audio = null;   
            // this may fail, so ensure object state is set up before
            // calling this method. 
            out.close();
            lastPosition = out.getPosition();
            try
            {
                bitstream.close();
            }
            catch (BitstreamException ex)
            {
            }
        }
    }
    
    /**
     * Returns the completed status of this player.
     * 
     * @return  true if all available MPEG audio frames have been
     *          decoded, or false otherwise. 
     */
    public synchronized boolean isComplete()
    {
        return complete;    
    }
                
    /**
     * Retrieves the position in milliseconds of the current audio
     * sample being played. This method delegates to the <code>
     * AudioDevice</code> that is used by this player to sound
     * the decoded audio samples. 
     */
    public int getPosition()
    {
        int position = lastPosition;
        
        AudioDevice out = audio;        
        if (out!=null)
        {
            position = out.getPosition();   
        }
        return position;
    }       
    
    /**
     * Decodes a single frame.
     * 
     * @return true if there are no more frames to decode, false otherwise.
     */
    protected boolean decodeFrame() throws JavaLayerException
    {       
        try
        {
            AudioDevice out = audio;
            if (out==null)
                return false;

            Header h = bitstream.readFrame();   
            
            if (h==null)
                return false;
               
            synchronized(currentFrameSynchronizer)
            {
                currentFrame ++;
            }
            
            // sample buffer set when decoder constructed
            SampleBuffer output = (SampleBuffer)decoder.decodeFrame(h, bitstream);
                                                                                                                                                    
            synchronized (this)
            {
                out = audio;
                if (out!=null)
                {                   
                    out.write(output.getBuffer(), 0, output.getBufferLength());
                }               
            }
                                                                            
            bitstream.closeFrame();
        }       
        catch (RuntimeException ex)
        {
            throw new JavaLayerException("Exception decoding audio frame", ex);
        }
/*
        catch (IOException ex)
        {
            System.out.println("exception decoding audio frame: "+ex);
            return false;   
        }
        catch (BitstreamException bitex)
        {
            System.out.println("exception decoding audio frame: "+bitex);
            return false;   
        }
        catch (DecoderException decex)
        {
            System.out.println("exception decoding audio frame: "+decex);
            return false;               
        }
*/      
        return true;
    }
    
    /**
     * skips over a single frame
     * @return false    if there are no more frames to decode, true otherwise.
     */
    protected boolean skipFrame() throws JavaLayerException
    {
        Header h = bitstream.readFrame();
        
        if (h == null)
            return false;
        
        synchronized(currentFrameSynchronizer)
        {
            this.currentFrame ++;
        }
        
        bitstream.closeFrame();
        
        return true;
    }

    public int getCurrentFrame()
    {
        synchronized(currentFrameSynchronizer)
        {
            return this.currentFrame;
        }
    }

    public Header getHeader() throws BitstreamException
    {
        return bitstream.readFrame();
    }
}

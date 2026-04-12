package iamd.cassetteplayer.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;
import org.json.JSONObject;

import iamd.cassetteplayer.model.CassettePlayerDataModel;
import javazoom.jl.decoder.JavaLayerException;

public class MP3FilePlayerWrapper
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
    
	final private MP3FilePlayer.Listener defaultListener = new MP3FilePlayer.Listener()
	{
		@Override
		public void onStop()
		{
			for (Listener listener : listeners)
				listener.onStop();
		}
	};

	static public class MP3CachedData
	{
		final public File file;
		final public String artist;
		final public String album;
		final public String year;
		final public String title;
		final public String bitrate;
		final public String channels;
		final public double trackLength;

		private boolean isTotalFrameCountSet = false;
		private int totalFrameCount;
		
		private boolean isCurrentFrameSet = false;
		private int currentFrame;
		
		public MP3CachedData(MP3FilePlayer mp3FilePlayer)
		{
			this.file = mp3FilePlayer.file;
			this.artist = mp3FilePlayer.artist;
			this.album = mp3FilePlayer.album;
			this.year = mp3FilePlayer.year;
			this.title = mp3FilePlayer.title;
			this.bitrate = mp3FilePlayer.bitrate;
			this.channels = mp3FilePlayer.channels;
			this.trackLength = mp3FilePlayer.getTrackLength();
			this.totalFrameCount = mp3FilePlayer.getTotalFrameCount();
		}

		public MP3CachedData(JSONObject track)
		{
			this.file = new File(track.getString("file"));
			this.artist = track.getString("artist");
			this.album = track.getString("album");
			this.year = track.getString("year");
			this.title = track.getString("title");
			this.bitrate = track.getString("bitrate");
			this.channels = track.getString("channels");
			this.trackLength = track.getDouble("trackLength");
			this.totalFrameCount = track.getInt("totalFrameCount");
			
		}

		public JSONObject toJSONObject()
		{
	    	JSONObject track = new JSONObject();
	    	
	    	track.put("file", this.file.getAbsolutePath());
	    	track.put("artist", this.artist);
	    	track.put("album", this.album);
	    	track.put("year", this.year);
	    	track.put("title", this.title);
	    	track.put("bitrate", this.bitrate);
	    	track.put("channels", this.channels);
	    	track.put("trackLength", this.trackLength);
	    	track.put("totalFrameCount", this.totalFrameCount);
	    	
			return track;
		}

		public int getTotalFrameCount()
		{
			return this.totalFrameCount;
		}

		public void setTotalFrameCount(int maxTotalFrame)
		{
			this.totalFrameCount = maxTotalFrame;
			
			this.isTotalFrameCountSet = true;
		}
		
		public boolean isTotalFrameCountSet()
		{
			return this.isTotalFrameCountSet;
		}

		public void setCurrentFrame(int frame)
		{
			this.currentFrame = frame;
			
			this.isCurrentFrameSet = true;
		}
		
		public int getCurrentFrame()
		{
			return this.currentFrame;
		}
		
		public boolean isCurrentFrameSet()
		{
			return this.isCurrentFrameSet;
		}
	}
	
	final private CassettePlayerDataModel playerDataModel;
	final public File file;
	final private double frameTime;
	
	private MP3FilePlayer actualMp3FilePlayer;
	private MP3CachedData cachedData;

    public MP3FilePlayerWrapper(CassettePlayerDataModel playerDataModel, File file, InputStream is, double frameTime) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
		this.playerDataModel = playerDataModel;
		this.file = file;
		this.frameTime = frameTime;
		
		if (is != null)
		{
			try
			{
				this.buildActualMp3FilePlayer(is);
	        }
	        catch (IOException | JavaLayerException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException | InterruptedException e)
	        {
				e.printStackTrace();
			}
		}
	}

	public MP3FilePlayerWrapper(CassettePlayerDataModel playerDataModel, MP3CachedData cachedData) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
	{
		this.playerDataModel = playerDataModel;
		this.file = cachedData.file;
		this.frameTime = -1;
		this.cachedData = cachedData;
	}

	private MP3FilePlayer getActualMP3FilePlayer()
	{
		if (this.actualMp3FilePlayer == null)
		{
	        try (FileInputStream fis = new FileInputStream(this.file))
	        {
	        	this.buildActualMp3FilePlayer(fis);
	        }
	        catch (IOException | JavaLayerException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException | InterruptedException e)
	        {
				e.printStackTrace();
			}
		}
		
		return this.actualMp3FilePlayer;
	}

	private void buildActualMp3FilePlayer(InputStream fis) throws IOException, JavaLayerException,
			CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException, InterruptedException
	{
		this.actualMp3FilePlayer = new MP3FilePlayer(this.playerDataModel, this.file, fis, this.frameTime);

		if (this.cachedData != null)
		{
			if (this.cachedData.isCurrentFrameSet())
				this.actualMp3FilePlayer.setCurrentFrame(this.cachedData.getCurrentFrame());

			if (this.cachedData.isTotalFrameCountSet())
				this.actualMp3FilePlayer.setTotalFrameCount(this.cachedData.getTotalFrameCount());
		}

		this.cachedData = new MP3CachedData(this.actualMp3FilePlayer);
		this.actualMp3FilePlayer.addListener(this.defaultListener);
	}

	private MP3CachedData getCachedData()
	{
		if (this.cachedData == null)
	        this.getActualMP3FilePlayer();
		
		return this.cachedData;
	}

	public static MP3FilePlayerWrapper createFromFile(CassettePlayerDataModel playerDataModel, File file) throws FileNotFoundException, IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
		return new MP3FilePlayerWrapper(playerDataModel, file, null, -1);
    }

	public static MP3FilePlayerWrapper createFromFile(CassettePlayerDataModel playerDataModel, MP3CachedData file) throws FileNotFoundException, IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
		return new MP3FilePlayerWrapper(playerDataModel, file);
    }

    public static MP3FilePlayerWrapper createNoise(CassettePlayerDataModel playerDataModel, int maxTotalFrame) throws IOException, JavaLayerException, CannotReadException, TagException, ReadOnlyFileException, InvalidAudioFrameException
    {
        try (InputStream is = MP3FilePlayerWrapper.class.getResourceAsStream("brownnoise.mp3"))
        {
            double frameTime = 0.026122448979591834;
            
            MP3FilePlayerWrapper mp3FilePlayer = new MP3FilePlayerWrapper(playerDataModel, null, is, frameTime);
    
            if (mp3FilePlayer.getTotalFrameCount() > maxTotalFrame)
                mp3FilePlayer.setTotalFrameCount(maxTotalFrame);
            
            return mp3FilePlayer;
        }
    }

	public String getArtist()
	{
		return this.getCachedData().artist;
	}

	public String getAlbum()
	{
		return this.getCachedData().album;
	}

	public String getYear()
	{
		return this.getCachedData().year;
	}

	public String getTitle()
	{
		return this.getCachedData().title;
	}

	public String getBitRate()
	{
		return this.getCachedData().bitrate;
	}

	public String getChannel()
	{
		return this.getCachedData().channels;
	}

	public boolean ffwd() throws JavaLayerException, IOException, InterruptedException
	{
		return this.getActualMP3FilePlayer().ffwd();
	}

	public boolean rwnd() throws IOException, InterruptedException
	{
		return this.getActualMP3FilePlayer().rwnd();
	}

	public boolean play() throws IOException, JavaLayerException
	{
		return this.getActualMP3FilePlayer().play();
	}

	public void stop() throws IOException, InterruptedException 
	{
		this.getActualMP3FilePlayer().stop();
	}

	public int getCurrentFrame() 
	{
		return this.getActualMP3FilePlayer().getCurrentFrame();
	}

	public void setCurrentFrame(int frame) throws IOException, InterruptedException
	{
		this.getCachedData().setCurrentFrame(frame);
		
		if (this.actualMp3FilePlayer != null)
			this.actualMp3FilePlayer.setCurrentFrame(frame);
	}

	private void setTotalFrameCount(int maxTotalFrame)
	{
		this.getCachedData().setTotalFrameCount(maxTotalFrame);
		
		if (this.actualMp3FilePlayer != null)
			this.actualMp3FilePlayer.setTotalFrameCount(maxTotalFrame);
	}

	public int getTotalFrameCount() 
	{
		return this.getCachedData().getTotalFrameCount();
	}

	public double getTrackLength() 
	{
		return this.getCachedData().trackLength;
	}

	public String getTrackLengthAsString()
	{
		return MP3FilePlayer.getTrackLengthAsString(this.getTrackLength());
	}

	public void debug()
	{
		this.getActualMP3FilePlayer().debug();
	}

	public MP3CachedData getKnownFile()
	{
		return this.cachedData;
	}
}

package iamd.cassetteplayer.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.TreeMap;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

import iamd.cassetteplayer.Main;
import iamd.cassetteplayer.model.CassettePlayerDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel.Status;

@SuppressWarnings("serial")
public class CassettePlayerButtonWidget extends JPanel
{
    public interface Listener
    {
        public void onButtonClicked(Status requestedStatus);

        public void onReverseClicked();

        public void onEjectClicked();

        public void onPauseClicked();
    }
    
    final private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }

    final private JToggleButton invisibleStopButton = new CustomJToggleButton();

    final private TreeMap<Status,JToggleButton> toggleButtons = new TreeMap<>();
    
    final private JToggleButton pauseButton;
    
    final private CustomJButton ejectButton;
    
    private Clip clip = null;
    {
        try
        {
            this.clip = AudioSystem.getClip();
            
            this.clip.open(AudioSystem.getAudioInputStream(Main.class.getResourceAsStream("buttons/button14.wav")));
        }
        catch (Exception exc)
        {
            exc.printStackTrace(System.out);
        }
    }    
    
    final private CassettePlayerDataModel playerDataModel;

    public CassettePlayerButtonWidget(CassettePlayerDataModel playerDataModel) throws IOException
    {
        this.playerDataModel = playerDataModel;
        
        this.setLayout(new GridLayout(1, 7));
        
        this.setPreferredSize(new Dimension(7 * CustomJToggleButton.BUTTON_IMAGE.getWidth(), 
                CustomJToggleButton.BUTTON_IMAGE.getHeight()));
        
        ButtonGroup buttonGroup = new ButtonGroup();
        
        buttonGroup.add(this.invisibleStopButton);
        
        BufferedImage ejectIcon = ImageIO.read(Main.class.getResourceAsStream("buttons/eject.png"));
        ejectButton = new CustomJButton(new ImageIcon(ejectIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        ejectButton.setToolTipText("EJECT - Go to library");
        ejectButton.setFocusable(false);
        this.add(ejectButton);

        for (CassettePlayerDataModel.Status status : CassettePlayerDataModel.Status.values())
        {
        	if (status == Status.MENU)
        		continue;
        	
            BufferedImage icon = ImageIO.read(Main.class.getResourceAsStream("buttons/" + status.name().toLowerCase() + ".png"));
            JToggleButton button = new CustomJToggleButton(new ImageIcon(icon.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
            button.setToolTipText(status.name());
            button.setFocusable(false);
            this.toggleButtons.put(status, button);
            this.add(button);
            buttonGroup.add(button);
            
            button.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    if (status != playerDataModel.getStatus())
                        playClick();
                    
                    for (Listener listener : listeners)
                        listener.onButtonClicked(status);
                }
            });
        }

        BufferedImage pauseIcon = ImageIO.read(Main.class.getResourceAsStream("buttons/pause.png"));
        this.pauseButton = new CustomJToggleButton(new ImageIcon(pauseIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        pauseButton.setToolTipText("PAUSE");
        pauseButton.setFocusable(false);
        this.add(pauseButton);
        
        pauseButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                playClick();

                for (Listener listener : listeners)
                    listener.onPauseClicked();
            }
        });
        
        BufferedImage reverseIcon = ImageIO.read(Main.class.getResourceAsStream("buttons/reverse.png"));
        JButton reverseButton = new CustomJButton(new ImageIcon(reverseIcon.getScaledInstance(32, 32, Image.SCALE_SMOOTH)));
        reverseButton.setToolTipText("REVERSE");
        reverseButton.setFocusable(false);
        this.add(reverseButton);
        buttonGroup.add(reverseButton);

        ejectButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                playClick();

                for (Listener listener : listeners)
                    listener.onEjectClicked();
            }
        });
        
        reverseButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                playClick();

                for (Listener listener : listeners)
                    listener.onReverseClicked();
            }
        });
    }

    public JToggleButton getPressedButton()
    {
        for (Map.Entry<Status,JToggleButton> entry : this.toggleButtons.entrySet())
        {
            if (entry.getValue().isSelected())
                return entry.getValue();
        }
        
        return null;
    }

    public void playClick()
    {
        if (this.clip != null)
        {
            this.clip.setFramePosition(0);
            
            if (this.clip.isControlSupported(FloatControl.Type.MASTER_GAIN) && playerDataModel.getMasterGain() >= 0)
            {
                FloatControl masterGainControl = (FloatControl) this.clip.getControl(FloatControl.Type.MASTER_GAIN);
                
                float value = masterGainControl.getMinimum() + 
                        (masterGainControl.getMaximum() - masterGainControl.getMinimum()) * playerDataModel.getMasterGain() / 100;
                
                masterGainControl.setValue(value);
            }
            
            this.clip.start();
        }
    }
    
    public void popAllButtons()
    {
        this.invisibleStopButton.doClick();
    }

    public boolean isPaused()
    {
        return this.pauseButton.isSelected();
    }

	public void togglePause()
	{
		this.pauseButton.doClick();
	}
	
	public void doClick(Status status)
	{
		toggleButtons.get(status).doClick();
	}

	public boolean isButtonPressed(Status status)
	{
		return toggleButtons.get(status).isSelected();
	}

	public void eject()
	{
		ejectButton.doClick();
	}
}

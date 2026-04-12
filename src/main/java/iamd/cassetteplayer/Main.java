package iamd.cassetteplayer;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

import javax.swing.JFrame;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel.Status;
import iamd.cassetteplayer.serialize.CassetteLibrarySerialization;
import iamd.cassetteplayer.ui.CassetteContentEditorWidget;
import iamd.cassetteplayer.ui.CassetteDesignEditorWidget;
import iamd.cassetteplayer.ui.CassettePlayerButtonWidget;
import iamd.cassetteplayer.ui.CassettePlayerWidget;
import iamd.cassetteplayer.ui.CustomJToggleButton;
import iamd.cassetteplayer.ui.MainWindow;
import iamd.cassetteplayer.ui.MainWindow.CardPanel;
import iamd.ui.ErrorMessage;
import javazoom.jl.decoder.JavaLayerException;
import net.iharder.dnd.FileDrop;

public class Main
{
	private static final String VERSION = "0.1.2";

	private static final String[] AUDIO_FORMAT_EXT = new String[] { "mp3", "aac", "ogg" };

    static public void main(String[] args) throws IOException
    {
        try
        {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        }
        catch (ClassNotFoundException | InstantiationException
                | IllegalAccessException | UnsupportedLookAndFeelException e1)
        {
            e1.printStackTrace();
        }

        CassetteLibraryDataModel libraryDataModel = new CassetteLibraryDataModel();

        CassettePlayerDataModel playerDataModel = new CassettePlayerDataModel();
        
        MainWindow main = new MainWindow(libraryDataModel, playerDataModel);

        FileDrop.Listener fileDropListener = new FileDrop.Listener()
        {
            @Override
            public void filesDropped(File[] audioFiles)
            {
                try
                {
                    playerDataModel.setStatus(CassettePlayerDataModel.Status.STOP);
                    
                    Cassette currentCassette = libraryDataModel.getCurrentCassette();
                    
                    if (currentCassette != null)
                    {
                        main.playerButtonWidget.playClick();
                        currentCassette.stop();
                    }

                    ArrayList<File> audioFileList = new ArrayList<File>();
                    
                    for (File file : audioFiles)
                        this.processDroppedFile(audioFileList, file);
                    
                    Cassette cassette = new Cassette(playerDataModel, audioFileList.toArray(new File[0]));
                    
                    libraryDataModel.addCassette(cassette);
                }
                catch (IOException | InterruptedException | JavaLayerException | CannotReadException | TagException | ReadOnlyFileException | InvalidAudioFrameException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
            }
            
            final private Collection<String> AUDIO_FORMAT_EXT_LIST = Arrays.asList(AUDIO_FORMAT_EXT);

            private void processDroppedFile(ArrayList<File> audioFileList, File file)
            {
                if (file.isDirectory())
                {
                    for (File subFile : file.listFiles())
                        processDroppedFile(audioFileList, subFile);
                }
                else
                {
                    String fileName = file.toString();
                    
                    int indexOfExt = fileName.lastIndexOf('.');
                    
                    if (indexOfExt != -1)
                    {
                        String ext = fileName.substring(indexOfExt + 1).toLowerCase();
                        
                        if (this.AUDIO_FORMAT_EXT_LIST.contains(ext))
                            audioFileList.add(file);
                    }
                }
            }
        };
        
		new FileDrop(main.libraryWidget, fileDropListener);

        libraryDataModel.addListener(new CassetteLibraryDataModel.Listener()
        {
            @Override
            public void onCasseteListUpdated()
            {
                // Do nothing
            }

            @Override
            public void onCasseteClicked(MouseEvent e, Cassette cassette)
            {
                if (e != null && e.isPopupTrigger())
                {
                    JPopupMenu menu = new JPopupMenu();
                    
                    JMenuItem editDesignMenuItem = new JMenuItem("Edit case design...");
                    JMenuItem editContentMenuItem = new JMenuItem("Edit content...");
                    JMenuItem deleteMenuItem = new JMenuItem("Delete");
                    
                    menu.add(editDesignMenuItem);
                    
                    editDesignMenuItem.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            main.designEditorWidget.setCassette(cassette);
                            
                            main.selectPanel(CardPanel.DESIGN_EDITOR);
                        }
                    });

                    menu.add(editContentMenuItem);
                    
                    editContentMenuItem.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            main.contentEditorWidget.setCassette(cassette);
                            
                            main.selectPanel(CardPanel.CONTENT_EDITOR);
                        }
                    });
                    
                    menu.add(deleteMenuItem);
                    
                    deleteMenuItem.addActionListener(new ActionListener()
                    {
                        @Override
                        public void actionPerformed(ActionEvent e)
                        {
                            libraryDataModel.removeCassette(cassette);
                        }
                    });
                    
                    menu.show(e.getComponent(), e.getX(), e.getY());
                }
                else
                {
                	playerDataModel.setStatus(Status.STOP);
                	
                    main.playerWidget.setCassete(cassette);
                    
                    if (!playerDataModel.setMasterGain(main.playerWidget.masterGainWidget.slider.getValue()))
                    {
                        main.playerWidget.masterGainWidget.slider.setValue(100);

                        main.masterVolumeError.setVisible(true);

                    }
                    
                    main.selectPanel(CardPanel.PLAYER);
                }
            }
        });
        
        Timer rewinder = new Timer(30, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                try
                {
                    Cassette currentCassette = libraryDataModel.getCurrentCassette();
                    
                    if (currentCassette == null)
                        return;
                    
                    if (playerDataModel.getStatus() == CassettePlayerDataModel.Status.FFWD)
                    {
                        for (int i = 0; i < 20; i ++)
                        {
                            if (!currentCassette.ffwd())
                            {
                                if (i != 0)
                                    main.playerButtonWidget.playClick();
                                break;
                            }
                        }
                    }
                    else if (playerDataModel.getStatus() == CassettePlayerDataModel.Status.RWND)
                    {
                        for (int i = 0; i < 20; i ++)
                        {
                            if (!currentCassette.rwnd())
                            {
                                if (i != 0)
                                    main.playerButtonWidget.playClick();
                                break;
                            }
                        }
                    }
                }
                catch (JavaLayerException | IOException | InterruptedException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
            }
        });
        
        main.playerWidget.addListener(new CassettePlayerWidget.Listener()
        {
            @Override
            public void autoStop()
            {
                main.playerButtonWidget.playClick();
                
                playerDataModel.setStatus(Status.STOP);
                
                main.playerButtonWidget.popAllButtons();
            }

			@Override
			public void onCassetteClicked(MouseEvent e)
			{
				if (e.isPopupTrigger())
				{
					JPopupMenu menu = new JPopupMenu();
	                
	                JMenuItem editDesignMenuItem = new JMenuItem("Edit case design...");
	                
	                menu.add(editDesignMenuItem);
	                
	                editDesignMenuItem.addActionListener(new ActionListener()
	                {
	                    @Override
	                    public void actionPerformed(ActionEvent e)
	                    {
	                        main.designEditorWidget.setCassette(libraryDataModel.getCurrentCassette());
	                        
	                        main.selectPanel(CardPanel.DESIGN_EDITOR);
	                    }
	                });
	                
	                menu.show(e.getComponent(), e.getX(), e.getY());
				}
            }
        });
        
        main.playerWidget.masterGainWidget.slider.addChangeListener(new ChangeListener()
        {
            @Override
            public void stateChanged(ChangeEvent e)
            {
                if (!playerDataModel.setMasterGain(main.playerWidget.masterGainWidget.slider.getValue()))
                {
                    main.playerWidget.masterGainWidget.slider.setValue(100);

                    main.masterVolumeError.setVisible(true);

                }
            }
        });
        
        main.playerButtonWidget.addListener(new CassettePlayerButtonWidget.Listener()
        {
            @Override
            public void onButtonClicked(Status status)
            {
                try
                {
                    if (!main.playerButtonWidget.isPaused())
                    {
                        Cassette currentCassette = libraryDataModel.getCurrentCassette();
                        
                        currentCassette.debug();
                        
                        playerDataModel.setStatus(status);
                        
                        if (status == CassettePlayerDataModel.Status.PLAY)
                            currentCassette.play();
                        else
                            currentCassette.stop();
                        
                        if (status == CassettePlayerDataModel.Status.RWND || status == CassettePlayerDataModel.Status.FFWD)
                            rewinder.start();
                        else
                            rewinder.stop();
                    }

                    if (status == CassettePlayerDataModel.Status.STOP)
                    {
                        SwingUtilities.invokeLater(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                main.playerButtonWidget.popAllButtons();
                            }
                        });
                    }
                }
                catch (IOException | JavaLayerException | InterruptedException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
            }

            @Override
            public void onPauseClicked()
            {
                try
                {
                    Cassette currentCassette = libraryDataModel.getCurrentCassette();
                    
                    if (playerDataModel.getStatus() == Status.STOP)
                    {
                        JToggleButton button = main.playerButtonWidget.getPressedButton();
                        
                        main.playerButtonWidget.popAllButtons();
                        
                        if (button != null)
                            button.doClick();
                    }
                    else
                    {
                        playerDataModel.setStatus(Status.STOP);
                        
                        currentCassette.stop();
                        
                        rewinder.stop();
                    }
                }
                catch (IOException | InterruptedException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
            }
            
            @Override
            public void onEjectClicked()
            {
                try
                {
                    Cassette currentCassette = libraryDataModel.getCurrentCassette();
                    
                    playerDataModel.setStatus(Status.MENU);
                    
                    currentCassette.stop();
                    
                    rewinder.stop();
                    
                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            main.playerButtonWidget.popAllButtons();
                        }
                    });
                    
                    main.playerWidget.setCassete(null);
                    
                    main.selectPanel(CardPanel.LIBRARY);
                }
                catch (IOException | InterruptedException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
            }

            @Override
            public void onReverseClicked()
            {
                try
                {
                    Cassette currentCassette = libraryDataModel.getCurrentCassette();
                    
                    playerDataModel.setStatus(Status.STOP);
                    
                    currentCassette.stop();
                    
                    rewinder.stop();

                    SwingUtilities.invokeLater(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            main.playerButtonWidget.popAllButtons();
                        }
                    });
                    
                    currentCassette.reverse();
                    
                    main.playerWidget.setCassete(currentCassette);
                }
                catch (IOException | InterruptedException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
            }

        });
        
        main.designEditorWidget.addListener(new CassetteDesignEditorWidget.Listener()
        {
            @Override
            public void onLeave()
            {
            	if (playerDataModel.getStatus() == Status.MENU)
            		main.selectPanel(CardPanel.LIBRARY);
            	else
            		main.selectPanel(CardPanel.PLAYER);
            }
        });

        main.contentEditorWidget.addListener(new CassetteContentEditorWidget.Listener()
        {
            @Override
            public void onLeave()
            {
            	if (playerDataModel.getStatus() == Status.MENU)
            		main.selectPanel(CardPanel.LIBRARY);
            	else
            		main.selectPanel(CardPanel.PLAYER);
            }
        });
        
        CassetteLibrarySerialization.open(playerDataModel, libraryDataModel);
        
        main.selectPanel(CardPanel.LIBRARY);
        
        JFrame frame = new JFrame("Cassette Retro Player " + VERSION);
        frame.setIconImage(CassettePainter.CASSETTE_IMAGE);
        frame.setSize(new Dimension(800, 800));
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLocationByPlatform(true);
        frame.add(main);
        frame.setVisible(true);
        
        frame.addWindowListener(new WindowAdapter()
        {
            @Override
            public void windowClosing(WindowEvent e)
            {
                CassetteLibrarySerialization.save(libraryDataModel);
            }
        });
        
        frame.addKeyListener(new KeyAdapter()
        {
			@Override
			public void keyReleased(KeyEvent e)
			{
				if (main.getSelectedPanel() == CardPanel.PLAYER)
				{
					if (e.getKeyCode() == KeyEvent.VK_ESCAPE)
					{
						if (main.playerButtonWidget.isButtonPressed(Status.PLAY))
							main.playerButtonWidget.doClick(Status.STOP);
						else
							main.playerButtonWidget.eject();
					}
					if (e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
					{
						if (!main.playerButtonWidget.isButtonPressed(Status.PLAY))
							main.playerButtonWidget.eject();
					}
					if (e.getKeyCode() == KeyEvent.VK_SPACE)
					{
						if (main.playerButtonWidget.isButtonPressed(Status.PLAY))
							main.playerButtonWidget.doClick(Status.STOP);
						else
							main.playerButtonWidget.doClick(Status.PLAY);
	
						if (main.playerButtonWidget.isPaused())
							main.playerButtonWidget.togglePause();
					}
					else if (e.getKeyCode() == KeyEvent.VK_LEFT)
					{
						main.playerButtonWidget.doClick(Status.RWND);
						
						if (main.playerButtonWidget.isPaused())
							main.playerButtonWidget.togglePause();
					}
					else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
					{
						main.playerButtonWidget.doClick(Status.FFWD);
						
						if (main.playerButtonWidget.isPaused())
							main.playerButtonWidget.togglePause();
					}
				}
				else if (main.getSelectedPanel() == CardPanel.LIBRARY)
				{
					if (e.getKeyCode() == KeyEvent.VK_ENTER)
					{
						main.libraryWidget.openCurrentCassette();
					}
					else if (e.getKeyCode() == KeyEvent.VK_LEFT)
					{
						main.libraryWidget.selectAnotherCassette(-1, 0);
					}
					else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
					{
						main.libraryWidget.selectAnotherCassette(1, 0);
					}
					else if (e.getKeyCode() == KeyEvent.VK_UP)
					{
						main.libraryWidget.selectAnotherCassette(0, -1);
					}
					else if (e.getKeyCode() == KeyEvent.VK_DOWN)
					{
						main.libraryWidget.selectAnotherCassette(0, 1);
					}

					// Press insert to view properties
				}
			}
		});
        
        if (args.length == 1)
    	{
        	File file = new File(args[0]);
        	
        	if (file.isDirectory())
        	{
        		if (!main.libraryWidget.selectCassetteFromFolder(file))
        		{
        			fileDropListener.filesDropped(new File[] { file });

        			main.libraryWidget.selectLastCassette();
        		}
        		
        		main.libraryWidget.openCurrentCassette();
        	}
    	}
        
        frame.setMinimumSize(new Dimension(
                7 * CustomJToggleButton.BUTTON_IMAGE.getWidth(),
                7 * CustomJToggleButton.BUTTON_IMAGE.getHeight()));
    }
}

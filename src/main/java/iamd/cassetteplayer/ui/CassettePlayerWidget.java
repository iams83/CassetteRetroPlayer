package iamd.cassetteplayer.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.Timer;
import iamd.cassetteplayer.Cassette;
import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel.Status;
import iamd.cassetteplayer.CassettePainter;
import iamd.cassetteplayer.Main;
import iamd.ui.GraphicsPanel;

@SuppressWarnings("serial")
public class CassettePlayerWidget extends GraphicsPanel
{
    public interface Listener
    {
        public void autoStop();
        
        public void onCassetteClicked(MouseEvent e);
    }
    
    final private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }

    private int rotation;
    
    final private CassetteLibraryDataModel libraryDataModel;
    
    final private CassettePlayerDataModel cassettePlayerDataModel;

    final public CassettePlayerSliderWidget masterGainWidget;
    
    public CassettePlayerWidget(CassetteLibraryDataModel libraryDataModel, CassettePlayerDataModel cassettePlayerDataModel) throws IOException
    {
        super(PanelMovement.NO, Reverse.NO);
        
        this.setLayout(new BorderLayout());
        
        this.libraryDataModel = libraryDataModel;
        
        this.cassettePlayerDataModel = cassettePlayerDataModel;

        BufferedImage volumeIcon = ImageIO.read(Main.class.getResourceAsStream("buttons/volume.png"));
        
        this.masterGainWidget = new CassettePlayerSliderWidget(volumeIcon);
        
        this.add(this.masterGainWidget, BorderLayout.NORTH);
        
        this.masterGainWidget.setVisible(false);
        
        this.addMouseWheelListener(new MouseWheelListener()
        {
            Timer timer = new Timer(3000, new ActionListener()
            {
                @Override
                public void actionPerformed(ActionEvent e)
                {
                    masterGainWidget.setVisible(false);
                }
            });
            
            {
                timer.setRepeats(false);
            }
            
            @Override
            public void mouseWheelMoved(MouseWheelEvent e)
            {
                masterGainWidget.setVisible(true);
                
                masterGainWidget.slider.setValue(
                		masterGainWidget.slider.getValue() - (int) (2 * e.getPreciseWheelRotation()));
                
                timer.restart();
            }
        });
        
        this.addMouseListener(new MouseAdapter()
        {
			@Override
			public void mouseReleased(MouseEvent e) 
			{
				for (Listener listener : CassettePlayerWidget.this.listeners)
					listener.onCassetteClicked(e);
			}
		});
        
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                this.componentResized(e);
            }
            
            @Override
            public void componentResized(ComponentEvent e)
            {
            	final int BORDER_SIZE_INV = 20;
            	
                CassettePlayerWidget.this.initializeBoundingBox(
                        new Rectangle2D.Double(
                        		-CassettePainter.CASSETTE_IMAGE_SIZE.width / BORDER_SIZE_INV, 
                                -CassettePainter.CASSETTE_IMAGE_SIZE.height / BORDER_SIZE_INV, 
                                (BORDER_SIZE_INV + 2) * CassettePainter.CASSETTE_IMAGE_SIZE.width / BORDER_SIZE_INV, 
                                (BORDER_SIZE_INV + 2) * CassettePainter.CASSETTE_IMAGE_SIZE.height / BORDER_SIZE_INV));

                if (libraryDataModel.getCurrentCassette() != null)
                    libraryDataModel.getCurrentCassette().invalidateCache();
            }
        });
        
        Timer timer = new Timer(30, new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                CassettePlayerWidget.this.tick();
            }
        });
        
        timer.setRepeats(true);
        timer.start();
    }
    
    public void setCassete(Cassette cassette)
    {
        if (this.libraryDataModel.getCurrentCassette() != null)
            this.libraryDataModel.getCurrentCassette().invalidateCache();
    }
    
    protected void tick()
    {
        Cassette currentCassette = this.libraryDataModel.getCurrentCassette();
        
        if (currentCassette != null)
        {
            int currentFrame = currentCassette.getCurrentFrame();
            
            int frameCount = currentCassette.getTotalFrameCount();
            
            Status status = this.cassettePlayerDataModel.getStatus();
            
            double factor = 0;
            
            if (status == Status.PLAY)
                factor = 1;
            
            if (status == Status.RWND)
                factor = -2.5;
    
            if (status == Status.FFWD)
                factor = 2.5;
    
            if (currentFrame >= frameCount && factor > 0)
            {
                for (Listener listener : listeners)
                    listener.autoStop();
    
                factor = 0;
            }
            
            if (currentFrame == 0 && factor < 0)
            {
                for (Listener listener : listeners)
                    listener.autoStop();
    
                factor = 0;
            }
            
            this.rotation += factor;
            
            repaint();
        }
    }

    @Override
    protected void paint(Graphics2D g2, AffineTransform tx2, Dimension size)
    {
        RetroPainter.paintBackground(g2, size);
        
        Cassette currentCassette = this.libraryDataModel.getCurrentCassette();

        currentCassette.paint(g2, getSize(), tx2, currentCassette.getCurrentSide(), this.rotation);
        
        if (this.masterGainWidget.isVisible())
            this.masterGainWidget.paint(g2);
    }
}

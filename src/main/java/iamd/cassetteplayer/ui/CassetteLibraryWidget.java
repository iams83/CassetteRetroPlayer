package iamd.cassetteplayer.ui;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Shape;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Area;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.io.File;

import iamd.cassetteplayer.Cassette;
import iamd.cassetteplayer.Cassette.Side;
import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.CassettePainter;
import iamd.cassetteplayer.audio.MP3FilePlayerWrapper;
import iamd.ui.GraphicsPanel;
import iamd.ui.ScopedAffineTransform;

@SuppressWarnings("serial")
public class CassetteLibraryWidget extends GraphicsPanel
{
    Font font = new Font("Arial", Font.BOLD, 40);
    
    CassetteLibraryDataModel dataModel;
    protected Point currentMouseLocation;
    private int hoverCassette, forceHoverCassette;
    
    public CassetteLibraryWidget(CassetteLibraryDataModel dataModel)
    {
        super(PanelMovement.NO, Reverse.NO);
        
        this.dataModel = dataModel;
        
        this.dataModel.addListener(new CassetteLibraryDataModel.Listener()
        {
            @Override
            public void onCasseteListUpdated()
            {
                refresh();
            }

            @Override
            public void onCasseteClicked(MouseEvent event, Cassette cassette)
            {
                // Do nothing
            }
        });
        
        this.addComponentListener(new ComponentAdapter()
        {
            @Override
            public void componentShown(ComponentEvent e)
            {
                refresh();
            }
            
            @Override
            public void componentResized(ComponentEvent e)
            {
                refresh();
            }
        });
        
        this.addMouseMotionListener(new MouseMotionAdapter()
        {
            @Override
            public void mouseMoved(MouseEvent e)
            {
                currentMouseLocation = e.getPoint();
                
                forceHoverCassette = -1;
                
                repaint();
            }
        });
        
        this.addMouseListener(new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (hoverCassette != -1)
                {
                    dataModel.setCurrentCassetteIndex(e, hoverCassette);
                }
            }
        });
    }
    
    public void refresh()
    {
        Cassette[] cassettes = this.dataModel.getCassettes();
        
		int rows = (int) Math.ceil(Math.sqrt(cassettes.length + 1));
        int cols = rows;

        if (rows == 0)
        {
        	rows = cols = 1;
        }
        else if (cassettes.length == rows * rows)
        {
        	rows = cols = rows - 1;
        }
        
        this.initializeBoundingBox(
                new Rectangle2D.Double(0, 0, 
                        rows * CassettePainter.CASSETTE_IMAGE_SIZE.getWidth(), 
                        cols * CassettePainter.CASSETTE_IMAGE_SIZE.getHeight()));

        for (Cassette cassette : cassettes)
            cassette.invalidateCache();
        
        this.repaint();
    }

    @Override
    protected void paint(Graphics2D g2, AffineTransform tx2, Dimension size)
    {
        RetroPainter.paintBackground(g2, size);
        
        this.hoverCassette = -1;
        
        Shape currentCassetteOutline = null;
        
        Cassette[] cassettes = dataModel.getCassettes();
        
        int rows = (int) Math.ceil(Math.sqrt(cassettes.length + 1));
        int cols = rows;
        
        if (rows == 0)
        {
        	rows = cols = 1;
        }
        
        for (int i = 0, r = 0; r < rows && i <= cassettes.length; r ++)
        {
            for (int c = 0; c < cols && i <= cassettes.length; c ++)
            {
                try (ScopedAffineTransform gast = new ScopedAffineTransform(tx2))
                {
                    tx2.translate(
                            c * CassettePainter.CASSETTE_IMAGE_SIZE.getWidth(),
                            r * CassettePainter.CASSETTE_IMAGE_SIZE.getHeight());
    
                    if (i < cassettes.length)
                    {
                        Shape outline = cassettes[i].paint(g2, size, tx2, cassettes[i].getCurrentSide(), 0);
                        
                        if (this.forceHoverCassette == i ||
                        	(this.forceHoverCassette != i && this.currentMouseLocation != null && outline.contains(this.currentMouseLocation)))
                        {
                            this.hoverCassette = i;
                            
                            String sideAHeaderLine = cassettes[i].getCassetteSide(Side.A).getHeaderLine();
                            String sideBHeaderLine = cassettes[i].getCassetteSide(Side.B).getHeaderLine();

                            if (sideAHeaderLine.equals(sideBHeaderLine))
                                this.setToolTipText(sideAHeaderLine);
                            else
                                this.setToolTipText("A:" + sideAHeaderLine + " - B: " + sideBHeaderLine);
                            
                            currentCassetteOutline = outline;
                        }
                    }
                    else
                    {
                        g2.setColor(Color.BLACK);
                        
                        RoundRectangle2D.Double border = new RoundRectangle2D.Double(50, 50, 
                                CassettePainter.CASSETTE_IMAGE_SIZE.getWidth() - 100,
                                CassettePainter.CASSETTE_IMAGE_SIZE.getHeight() - 100, 50, 50);
                        
                        String message1 = "Drag music files";
                        String message2 = "to create a new cassette.";
                        
                        g2.setFont(font);
                        
                        try (ScopedAffineTransform sat = new ScopedAffineTransform(g2))
                        {
                            g2.transform(tx2);
                            g2.translate(CassettePainter.CASSETTE_IMAGE_SIZE.getWidth() / 2,
                                    CassettePainter.CASSETTE_IMAGE_SIZE.getHeight() / 2);
                            
                            g2.drawString(message1, - g2.getFontMetrics().stringWidth(message1) / 2, -50);
                            g2.drawString(message2, - g2.getFontMetrics().stringWidth(message2) / 2, 50);
                        }
                        
                        float scale = Math.max(.001f, (float) tx2.getScaleX());
                        
                        g2.setStroke(new BasicStroke(10f * scale, BasicStroke.CAP_SQUARE, BasicStroke.JOIN_MITER, 10.0f, 
                                new float[] { 50.f * scale, 50.f * scale }, 20.0f));
                        g2.draw(tx2.createTransformedShape(border));
                    }

                    i ++;
                }
            }
        }
        
        if (currentCassetteOutline != null)
        {
            Area area = new Area(new Rectangle2D.Double(0, 0, getWidth(), getHeight()));
            area.subtract(new Area(currentCassetteOutline));
            
            g2.setClip(area);
            g2.setStroke(new BasicStroke(10));
            g2.setColor(Color.YELLOW);
            g2.draw(currentCassetteOutline);
    
            g2.setClip(null);
            g2.setStroke(new BasicStroke(1));
            g2.setColor(Color.BLACK);
            g2.draw(currentCassetteOutline);
        }
    }

	public void openCurrentCassette()
	{
		if (this.hoverCassette != -1)
        {
			this.dataModel.setCurrentCassetteIndex(null, this.hoverCassette);
        }
	}
	
	public void selectAnotherCassette(int x, int y)
	{
		Cassette[] cassettes = dataModel.getCassettes();
		
		int numCassettes = cassettes.length;
		
		int rows = (int) Math.ceil(Math.sqrt(cassettes.length + 1));
		
		if (this.hoverCassette == -1)
        {
			if (numCassettes > 0)
				this.hoverCassette = 0;
        }
		else
		{
			int newHoverCassette = this.hoverCassette + x + rows * y;
			
			if (newHoverCassette >= 0 && newHoverCassette < numCassettes)
				this.forceHoverCassette = newHoverCassette;
        }
		
		this.repaint();
	}

	public boolean selectCassetteFromFolder(File folder)
	{
		Cassette[] cassettes = dataModel.getCassettes();
		
		for (int i = 0; i < cassettes.length; i ++)
		{
			Cassette cassette = cassettes[i];
			
			int eq = 0, df = 0;
			
			for (Side side : Side.values())
			{
				for (MP3FilePlayerWrapper audioPlayer : cassette.getCassetteSide(side).getAllAudioPlayers())
				{
					if (audioPlayer.file != null)
					{
						if (audioPlayer.file.getParentFile().equals(folder))
							eq ++;
						else
							df ++;
					}
				}
			}
			
			if (eq > 0 && df == 0)
			{
				this.hoverCassette = i;
				return true;
			}
		}
		
		return false;
	}

	public void selectLastCassette()
	{
		this.hoverCassette = dataModel.getCassettes().length - 1;
	}
}

package iamd.cassetteplayer.ui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.io.IOException;

import javax.swing.JPanel;

import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.model.CassettePlayerDataModel;
import iamd.ui.BorderListPanelGenerator;

@SuppressWarnings("serial")
public class MainWindow extends JPanel
{
    public static enum CardPanel
    {
        LIBRARY, PLAYER, DESIGN_EDITOR, CONTENT_EDITOR
    }

    private CardPanel currentCardPanel;
    
    final public CassetteLibraryWidget libraryWidget;
    final public CassettePlayerWidget playerWidget;
    final public CassetteDesignEditorWidget designEditorWidget;
    final public CassetteContentEditorWidget contentEditorWidget;
    final public CassettePlayerButtonWidget playerButtonWidget;
    final public TipJLabel masterVolumeError;

    public MainWindow(CassetteLibraryDataModel libraryDataModel, CassettePlayerDataModel playerDataModel) throws IOException
    {
        super(new CardLayout());

        this.libraryWidget = new CassetteLibraryWidget(libraryDataModel);
        this.playerWidget = new CassettePlayerWidget(libraryDataModel, playerDataModel);
        this.designEditorWidget = new CassetteDesignEditorWidget(libraryDataModel);
        this.contentEditorWidget = new CassetteContentEditorWidget(libraryDataModel);
        this.playerButtonWidget = new CassettePlayerButtonWidget(playerDataModel);

        JPanel libraryPanelContainerGenerator = new JPanel(new BorderLayout());
        libraryPanelContainerGenerator.add(this.libraryWidget);
        libraryPanelContainerGenerator.add(new TipJLabel("Tip: Left-click on cassette to play it. Right-click for additional options", true), BorderLayout.NORTH);
        
        BorderListPanelGenerator playerPanelContainerGenerator = new BorderListPanelGenerator(BorderLayout.NORTH);
        playerPanelContainerGenerator.add(this.masterVolumeError = new TipJLabel("Error: master volume adjustment is not available.", new Color(0xfdc1c5), false));
        playerPanelContainerGenerator.add(new TipJLabel("Tip: Use mouse wheel to adjust master volume.", true));

        JPanel playerPanel = new JPanel(new BorderLayout());
        playerPanel.add(playerPanelContainerGenerator.extractPanel(this.playerWidget));
        playerPanel.add(this.playerButtonWidget, BorderLayout.SOUTH);
        
        this.masterVolumeError.setVisible(false);
        
        JPanel contentEditorPanel = new JPanel(new BorderLayout());
        contentEditorPanel.add(this.contentEditorWidget);
        contentEditorPanel.add(new TipJLabel("Tip: Right-click on tracks to change tracklist. Drag MP3 files into tracklists to add new tracks.", true), BorderLayout.NORTH);

        this.currentCardPanel = CardPanel.LIBRARY;
        
        this.add(libraryPanelContainerGenerator, CardPanel.LIBRARY.name());
        this.add(playerPanel, CardPanel.PLAYER.name());
        this.add(this.designEditorWidget, CardPanel.DESIGN_EDITOR.name());
        this.add(contentEditorPanel, CardPanel.CONTENT_EDITOR.name());
    }

    public void selectPanel(CardPanel panel)
    {
    	this.currentCardPanel = panel;
    	
        ((CardLayout) this.getLayout()).show(this, panel.name());
    }

	public CardPanel getSelectedPanel()
	{
		return this.currentCardPanel;
	}
}

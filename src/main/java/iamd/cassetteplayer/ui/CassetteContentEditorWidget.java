package iamd.cassetteplayer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableModel;

import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.TagException;

import iamd.cassetteplayer.Cassette;
import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.Cassette.Side;
import iamd.cassetteplayer.audio.MP3FilePlayerWrapper;
import iamd.ui.AttributeEditorListener;
import iamd.ui.BorderListPanelGenerator;
import iamd.ui.ErrorMessage;
import iamd.ui.TextLineEditor;
import javazoom.jl.decoder.JavaLayerException;
import net.iharder.dnd.FileDrop;

@SuppressWarnings("serial")
public class CassetteContentEditorWidget extends JPanel
{
    public interface Listener
    {
        public void onLeave();
    }
    
    final private ArrayList<Listener> listeners = new ArrayList<Listener>();

    public void addListener(Listener listener)
    {
        this.listeners.add(listener);
    }

    class CassetteTrackObject
    {
        final private MP3FilePlayerWrapper file;
        
        public CassetteTrackObject(MP3FilePlayerWrapper file)
        {
            this.file = file;
        }
        
        public String toString()
        {
            return this.file.getTitle();
        }
    }
    
    final private DefaultTableModel tableModelA = new DefaultTableModel()
    {
        @Override
        public boolean isCellEditable(int row, int column)
        {
           return false;
        }
    };

    final private DefaultTableModel tableModelB = new DefaultTableModel()
    {
        @Override
        public boolean isCellEditable(int row, int column)
        {
           return false;
        }
    };

    public String headerLineA = "", bodyTextA = "";
    public String headerLineB = "", bodyTextB = "";
    
    private TextLineEditor headerLineEditorA = new TextLineEditor();
    private TextLineEditor headerLineEditorB = new TextLineEditor();
    private TextLineEditor bodyTextEditorA = new TextLineEditor();
    private TextLineEditor bodyTextEditorB = new TextLineEditor();
    
    private Cassette currentCassette;

    public CassetteContentEditorWidget(CassetteLibraryDataModel libraryDataModel)
    {
        this.setLayout(new BorderLayout());
        
        this.tableModelA.setColumnIdentifiers(new Object[] { "Title", "Artist", "Album", "Duration"});
        this.tableModelB.setColumnIdentifiers(new Object[] { "Title", "Artist", "Album", "Duration"});

        JTable tableA = new JTable();
        tableA.setAutoCreateColumnsFromModel(true);
        tableA.setModel(this.tableModelA);
        
        JTable tableB = new JTable();
        tableB.setAutoCreateColumnsFromModel(true);
        tableB.setModel(this.tableModelB);

        BorderListPanelGenerator generatorA = new BorderListPanelGenerator(BorderLayout.NORTH);
        generatorA.add(createTextLineEditor(headerLineEditorA, "Header line:", this, "headerLineA"));
        generatorA.add(createTextLineEditor(bodyTextEditorA,   "Body text:",   this, "bodyTextA"));

        BorderListPanelGenerator tableAGenerator = new BorderListPanelGenerator(BorderLayout.NORTH);
        tableAGenerator.add(tableA.getTableHeader());
        tableAGenerator.add(tableA);
        generatorA.add(componentWithBorder(componentWithBorder(tableAGenerator.extractPanel(), new LineBorder(Color.LIGHT_GRAY)), new EmptyBorder(2, 2, 2, 2)));
        
        BorderListPanelGenerator generatorB = new BorderListPanelGenerator(BorderLayout.NORTH);
        generatorB.add(createTextLineEditor(headerLineEditorB, "Header line:", this, "headerLineB"));
        generatorB.add(createTextLineEditor(bodyTextEditorB,   "Body text:",   this, "bodyTextB"));

        BorderListPanelGenerator tableBGenerator = new BorderListPanelGenerator(BorderLayout.NORTH);
        tableBGenerator.add(tableB.getTableHeader());
        tableBGenerator.add(tableB);
        generatorB.add(componentWithBorder(componentWithBorder(tableBGenerator.extractPanel(), new LineBorder(Color.LIGHT_GRAY)), new EmptyBorder(2, 2, 2, 2)));
        
        TitledBorder painterBorder = new TitledBorder("Cassette");
        painterBorder.setTitleFont(painterBorder.getTitleFont().deriveFont(14.f).deriveFont(Font.BOLD));
        
        TitledBorder sideABorder = new TitledBorder("Side A");
        sideABorder.setTitleFont(painterBorder.getTitleFont());
        
        TitledBorder sideBBorder = new TitledBorder("Side B");
        sideBBorder.setTitleFont(sideABorder.getTitleFont());
        
        JPanel generator = new JPanel(new GridLayout(2, 1));
        generator.add(componentWithBorder(componentWithBorder(generatorA.extractPanel(), sideABorder), new EmptyBorder(15, 15, 15, 15)));
        generator.add(componentWithBorder(componentWithBorder(generatorB.extractPanel(), sideBBorder), new EmptyBorder(15, 15, 15, 15)));

        MouseListener tableMouseListener = new MouseAdapter()
        {
            @Override
            public void mouseReleased(MouseEvent e)
            {
                JTable table = ((JTable) e.getSource());
                
                int row = table.rowAtPoint(e.getPoint());
                
                if (row != -1)
                {
                    table.setRowSelectionInterval(row, row);
                    
                    CassetteTrackObject track = (CassetteTrackObject) table.getModel().getValueAt(row, 0);
                    
                    if (e.isPopupTrigger())
                    {
                        JPopupMenu menu = new JPopupMenu();
                        
                        JMenuItem moveUpMenuItem = new JMenuItem("Move up");
                        moveUpMenuItem.setEnabled(row > 0 || table == tableB);
                        moveUpMenuItem.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                try
                                {
                                    currentCassette.moveTrackUp(table == tableA ? Side.A : Side.B, row);
                                }
                                catch (IOException | JavaLayerException
                                        | CannotReadException | TagException
                                        | ReadOnlyFileException
                                        | InvalidAudioFrameException e1)
                                {
                                    ErrorMessage.showErrorMessage(e1);
                                }
                                
                                CassetteContentEditorWidget.this.setCassette(currentCassette);
                            }
                        });
                        menu.add(moveUpMenuItem);

                        JMenuItem moveDownMenuItem = new JMenuItem("Move down");
                        moveDownMenuItem.setEnabled(row < table.getRowCount() || table == tableA);
                        moveDownMenuItem.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                try
                                {
                                    currentCassette.moveTrackDown(table == tableA ? Side.A : Side.B, row);
                                }
                                catch (IOException | JavaLayerException
                                        | CannotReadException | TagException
                                        | ReadOnlyFileException
                                        | InvalidAudioFrameException e1)
                                {
                                    ErrorMessage.showErrorMessage(e1);
                                }
                                
                                CassetteContentEditorWidget.this.setCassette(currentCassette);
                            }
                        });
                        menu.add(moveDownMenuItem);
                        
                        JMenuItem removeMenuItem = new JMenuItem("Remove");
                        removeMenuItem.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                try
                                {
                                    currentCassette.removeTrack(table == tableA ? Side.A : Side.B, row);
                                }
                                catch (IOException | JavaLayerException
                                        | CannotReadException | TagException
                                        | ReadOnlyFileException
                                        | InvalidAudioFrameException e1)
                                {
                                    ErrorMessage.showErrorMessage(e1);
                                }
                                
                                CassetteContentEditorWidget.this.setCassette(currentCassette);
                            }
                        });
                        menu.add(removeMenuItem);
                        
                        JMenuItem openFileLocationMenuItem = new JMenuItem("Open file location...");
                        openFileLocationMenuItem.addActionListener(new ActionListener()
                        {
                            @Override
                            public void actionPerformed(ActionEvent e)
                            {
                                try
                                {
                                    System.out.println("EXPLORE \"" + track.file.file + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                                    
                                    Runtime.getRuntime().exec("explorer.exe /select,\"" + track.file.file + "\""); //$NON-NLS-1$ //$NON-NLS-2$
                                }
                                catch(IOException e1)
                                {
                                    ErrorMessage.showErrorMessage(e1);
                                }
                            }
                        });
                        menu.add(openFileLocationMenuItem);

                        menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }
        };
        
        tableA.addMouseListener(tableMouseListener);
        tableB.addMouseListener(tableMouseListener);
        
        new FileDrop(tableA, new FileDrop.Listener()
        {
            @Override
            public void filesDropped(File[] files)
            {
                try
                {
                    currentCassette.addTracks(Side.A, files);
                }
                catch (IOException | JavaLayerException | CannotReadException
                        | TagException | ReadOnlyFileException
                        | InvalidAudioFrameException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
                
                setCassette(currentCassette);
            }
        });
        
        new FileDrop(tableB, new FileDrop.Listener()
        {
            @Override
            public void filesDropped(File[] files)
            {
                try
                {
                    currentCassette.addTracks(Side.B, files);
                }
                catch (IOException | JavaLayerException | CannotReadException
                        | TagException | ReadOnlyFileException
                        | InvalidAudioFrameException e1)
                {
                    ErrorMessage.showErrorMessage(e1);
                }
                
                setCassette(currentCassette);
            }
        });
        
        JButton returnToLibraryButton = new JButton("Return");
        returnToLibraryButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (currentCassette != null)
                    updateCassette();
                
                currentCassette = null;
                
                for (Listener listener : CassetteContentEditorWidget.this.listeners)
                    listener.onLeave();
            }
        });
        
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.add(returnToLibraryButton);
        
        this.add(generator);
        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private Component createTextLineEditor(TextLineEditor lineEditor, String label, Object object, String fieldName)
    {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(componentWithBorder(new JLabel(label), new EmptyBorder(2, 5, 2, 5)), BorderLayout.WEST);
        lineEditor.bindValue(object, fieldName);
        lineEditor.addAttributeEditionListener(new AttributeEditorListener<String>()
        {
            @Override
            public void attributeModified(Object editingObject, Field editingField, String value)
            {
                updateCassette();
            }
        });
        messagePanel.add(lineEditor);
        return componentWithBorder(messagePanel, new EmptyBorder(2, 2, 2, 2));
    }

    static private JComponent componentWithBorder(JComponent component, Border border)
    {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(border);
        panel.add(component);
        return panel;
    }

    protected void updateCassette()
    {
        this.currentCassette.getCassetteSide(Side.A).setPrintedText(this.headerLineA, this.bodyTextA);
        this.currentCassette.getCassetteSide(Side.B).setPrintedText(this.headerLineB, this.bodyTextB);
        
        this.currentCassette.invalidateCache();
    }

    public void setCassette(Cassette cassette)
    {
        this.currentCassette = cassette;
        
        this.headerLineA = cassette.getCassetteSide(Side.A).getHeaderLine();
        this.headerLineB = cassette.getCassetteSide(Side.B).getHeaderLine();
        this.bodyTextA = cassette.getCassetteSide(Side.A).getBodyText();
        this.bodyTextB = cassette.getCassetteSide(Side.B).getBodyText();
        
        this.headerLineEditorA.initializeValue(this.headerLineA);
        this.headerLineEditorB.initializeValue(this.headerLineB);
        this.bodyTextEditorA.initializeValue(this.bodyTextA);
        this.bodyTextEditorB.initializeValue(this.bodyTextB);
        
        while (this.tableModelA.getRowCount() > 0)
            this.tableModelA.removeRow(0);
    
        while (this.tableModelB.getRowCount() > 0)
            this.tableModelB.removeRow(0);
        
        double totalTrackLengthA = 0;
        double totalNoiseLengthA = 0;
        
        for (MP3FilePlayerWrapper file : Arrays.asList(cassette.getCassetteSide(Side.A).getAllAudioPlayers()))
        {
            if (file.file == null)
            {
                totalNoiseLengthA += file.getTrackLength();
            }
            else
            {
                totalTrackLengthA += file.getTrackLength();
                
                if (totalNoiseLengthA > 0)
                    throw new AssertionError();
                
                this.tableModelA.addRow(
                        new Object[] { new CassetteTrackObject(file), file.getArtist(), file.getAlbum(), file.getTrackLengthAsString() });
            }
        }
        
        double totalNoiseLengthB = 0;
        double totalTrackLengthB = 0;
        
        for (MP3FilePlayerWrapper file : Arrays.asList(cassette.getCassetteSide(Side.B).getAllAudioPlayers()))
        {
            if (file.file == null)
            {
                totalNoiseLengthB += file.getTrackLength();
            }
            else
            {
                totalTrackLengthB += file.getTrackLength();
                
                if (totalNoiseLengthB > 0)
                    throw new AssertionError();
                
                this.tableModelB.addRow(
                        new Object[] { new CassetteTrackObject(file), file.getArtist(), file.getAlbum(), file.getTrackLengthAsString() });
            }
        }
        
        System.out.println(totalNoiseLengthA + ":" + totalNoiseLengthB + " / " + totalTrackLengthA + ":" + totalTrackLengthB + " / " + 
                (totalNoiseLengthA + totalTrackLengthA) + " / " +
                ((totalNoiseLengthA + totalTrackLengthA) - (totalNoiseLengthB + totalTrackLengthB)));
        
        if (totalNoiseLengthA > 0 && totalNoiseLengthB > 0)
            throw new AssertionError();
        
        if (this.currentCassette.getCassetteSide(Side.A).getTotalFrameCount() != this.currentCassette.getCassetteSide(Side.B).getTotalFrameCount())
            throw new AssertionError();

        this.currentCassette.invalidateCache();
    }
}

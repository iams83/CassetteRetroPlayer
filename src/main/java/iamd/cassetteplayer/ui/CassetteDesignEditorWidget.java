package iamd.cassetteplayer.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Field;
import java.util.ArrayList;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;
import iamd.cassetteplayer.Cassette;
import iamd.cassetteplayer.CassetteDesign;
import iamd.cassetteplayer.model.CassetteLibraryDataModel;
import iamd.cassetteplayer.model.CassetteDesignDataModel;
import iamd.cassetteplayer.CassettePainter;
import iamd.ui.AttributeEditorListener;
import iamd.ui.BorderListPanelGenerator;
import iamd.ui.ColorEditor;
import iamd.ui.ComboBoxEditor;
import iamd.ui.GraphicsPanel;
import iamd.ui.GraphicsPanel.PanelMovement;
import iamd.ui.GraphicsPanel.Reverse;

@SuppressWarnings("serial")
public class CassetteDesignEditorWidget extends JPanel
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

    public CassetteDesign design = CassetteDesign.BASIC;
    public Color caseColor = Color.white;
    public Color spoolColor = Color.white;
    public Color labelColor0 = Color.white;
    public Color labelColor1 = Color.white;
    public Color textColor  = Color.white;
    public String headerFontFamily = "Arial";
    public String bodyTextFontFamily = "Arial";
    
    private ColorEditor caseColorEditor = new ColorEditor(this.getParent());
    private ColorEditor labelColorEditor[] = new ColorEditor[] {
        new ColorEditor(this.getParent()),
        new ColorEditor(this.getParent())
    };
    private ColorEditor textColorEditor = new ColorEditor(this.getParent());
    private ColorEditor spoolColorEditor = new ColorEditor(this.getParent());

    private String[] fontFamilyNames = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();
    private ComboBoxEditor<CassetteDesign> designComboEditor = new ComboBoxEditor<>(CassetteDesign.values());
    private ComboBoxEditor<String> headerFontFamilyComboEditor = new ComboBoxEditor<>(fontFamilyNames);
    private ComboBoxEditor<String> bodyTextFontFamilyComboEditor = new ComboBoxEditor<>(fontFamilyNames);

    private Cassette currentCassette;

    JPanel cassettePreviewPanel = new GraphicsPanel(PanelMovement.NO, Reverse.NO)
    {
        {
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
                    initializeBoundingBox(
                            new Rectangle2D.Double(0, 0, 
                                    CassettePainter.CASSETTE_IMAGE_SIZE.width, 
                                    CassettePainter.CASSETTE_IMAGE_SIZE.height));

                    if (currentCassette != null)
                        currentCassette.invalidateCache();
                }
            });
        }

        @Override
        protected void paint(Graphics2D g2, AffineTransform tx2, Dimension size)
        {
            if (currentCassette != null)
                currentCassette.paint(g2, getSize(), tx2, currentCassette.getCurrentSide(), 0);
        }
    };

    public CassetteDesignEditorWidget(CassetteLibraryDataModel libraryDataModel)
    {
        this.setLayout(new BorderLayout());
        
        BorderListPanelGenerator generatorPainter = new BorderListPanelGenerator(BorderLayout.NORTH);
        
        generatorPainter.add(createComboBoxEditor(this.designComboEditor, "Design:", this, "design"));
        generatorPainter.add(createColorEditor("Case color:", this,  "caseColor", this.caseColorEditor));
        generatorPainter.add(createColorEditor("Label color:", this, new String[] { "labelColor0", "labelColor1" }, this.labelColorEditor));
        generatorPainter.add(createColorEditor("Text color:", this,  "textColor", this.textColorEditor));
        generatorPainter.add(createColorEditor("Spool color:", this, "spoolColor", this.spoolColorEditor));
        generatorPainter.add(createComboBoxEditor(this.headerFontFamilyComboEditor, "Header font:", this, "headerFontFamily"));
        generatorPainter.add(createComboBoxEditor(this.bodyTextFontFamilyComboEditor, "Text font:", this, "bodyTextFontFamily"));
        
        this.designComboEditor.addAttributeEditionListener(new AttributeEditorListener<CassetteDesign>()
        {
            @Override
            public void attributeModified(Object editingObject, Field editingField, CassetteDesign value)
            {
                for (int i = 0; i < labelColorEditor.length; i ++)
                    labelColorEditor[i].setVisible(i < design.numColors);
            }
        });
        
        TitledBorder painterBorder = new TitledBorder("Cassette");
        painterBorder.setTitleFont(painterBorder.getTitleFont().deriveFont(14.f).deriveFont(Font.BOLD));
        
        TitledBorder sideABorder = new TitledBorder("Side A");
        sideABorder.setTitleFont(painterBorder.getTitleFont());
        
        TitledBorder sideBBorder = new TitledBorder("Side B");
        sideBBorder.setTitleFont(sideABorder.getTitleFont());
        
        BorderListPanelGenerator generator = new BorderListPanelGenerator(BorderLayout.NORTH);
        generator.add(componentWithBorder(componentWithBorder(generatorPainter.extractPanel(), painterBorder), new EmptyBorder(15, 15, 15, 15)));

        JButton returnToLibraryButton = new JButton("Return");
        returnToLibraryButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                if (currentCassette != null)
                    updateCassette();
                
                currentCassette = null;
                
                for (Listener listener : CassetteDesignEditorWidget.this.listeners)
                    listener.onLeave();
            }
        });
        
        JPanel bottomPanel = new JPanel(new GridLayout(1, 2));
        bottomPanel.add(returnToLibraryButton);
        
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(generator.extractPanel(), BorderLayout.NORTH);
        
        panel.add(componentWithBorder(this.cassettePreviewPanel, new EmptyBorder(0, 5, 30, 5)));
        this.add(panel);
        this.add(bottomPanel, BorderLayout.SOUTH);
    }

    private Component createColorEditor(String label, Object o, String fieldName, ColorEditor colorEditor)
    {
        colorEditor.bindValue(o, fieldName);
        colorEditor.addAttributeEditionListener(new AttributeEditorListener<Color>()
        {
            @Override
            public void attributeModified(Object editingObject, Field editingField, Color value)
            {
                updateCassette();
            }
        });
        BorderListPanelGenerator colorPanel = new BorderListPanelGenerator(BorderLayout.WEST);
        colorPanel.add(componentWithBorder(new JLabel(label), new EmptyBorder(2, 5, 2, 5)));
        colorPanel.add(colorEditor);
        return componentWithBorder(colorPanel.extractPanel(), new EmptyBorder(2, 2, 2, 2));
    }

    private Component createColorEditor(String label, Object o, String[] fieldNames, ColorEditor[] colorEditors)
    {
        JPanel gridPanel = new JPanel(new GridLayout(1, fieldNames.length));

        for (int i = 0; i < fieldNames.length; i ++)
        {
            colorEditors[i].bindValue(o, fieldNames[i]);
            colorEditors[i].addAttributeEditionListener(new AttributeEditorListener<Color>()
            {
                @Override
                public void attributeModified(Object editingObject, Field editingField, Color value)
                {
                    updateCassette();
                }
            });
            gridPanel.add(colorEditors[i]);
        }
        BorderListPanelGenerator colorPanel = new BorderListPanelGenerator(BorderLayout.WEST);
        colorPanel.add(componentWithBorder(new JLabel(label), new EmptyBorder(2, 5, 2, 5)));
        colorPanel.add(gridPanel);
        return componentWithBorder(colorPanel.extractPanel(), new EmptyBorder(2, 2, 2, 2));
    }

    private <T> Component createComboBoxEditor(ComboBoxEditor<T> comboEditor, String label, Object object, String fieldName)
    {
        JPanel messagePanel = new JPanel(new BorderLayout());
        messagePanel.add(componentWithBorder(new JLabel(label), new EmptyBorder(2, 5, 2, 5)), BorderLayout.WEST);
        comboEditor.bindValue(object, fieldName);
        comboEditor.addAttributeEditionListener(new AttributeEditorListener<T>()
        {
            @Override
            public void attributeModified(Object editingObject, Field editingField, T value)
            {
                updateCassette();
            }
        });
        messagePanel.add(comboEditor);
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
        CassetteDesignDataModel designDataModel = this.currentCassette.getDesignDataModel();
        
        designDataModel.setDesign    (this.design);
        designDataModel.setCaseColor (this.caseColor);
        designDataModel.setLabelColor(new Color[] { this.labelColor0, this.labelColor1 });
        designDataModel.setTextColor (this.textColor);
        designDataModel.setSpoolColor(this.spoolColor);
        designDataModel.setHeaderFontFamily(this.headerFontFamily);
        designDataModel.setBodyTextFontFamily(this.bodyTextFontFamily);
        
        this.currentCassette.invalidateCache();
        
        this.cassettePreviewPanel.repaint();
    }

    public void setCassette(Cassette cassette)
    {
        this.currentCassette = cassette;
        
        CassetteDesignDataModel cassettePainterModel = this.currentCassette.getDesignDataModel();

        this.design = cassettePainterModel.getDesign();
        this.caseColor = cassettePainterModel.getCaseColor();
        this.labelColor0 = cassettePainterModel.getLabelColor()[0];
        this.labelColor1 = cassettePainterModel.getLabelColor()[1];
        this.textColor = cassettePainterModel.getTextColor();
        this.spoolColor = cassettePainterModel.getSpoolColor();
        this.headerFontFamily = cassettePainterModel.getHeaderFontFamily();
        this.bodyTextFontFamily = cassettePainterModel.getBodyTextFontFamily();
        
        this.designComboEditor.initializeValue(this.design);
        this.headerFontFamilyComboEditor.initializeValue(this.headerFontFamily);
        this.bodyTextFontFamilyComboEditor.initializeValue(this.bodyTextFontFamily);

        this.caseColorEditor.initializeValue(this.caseColor);
        this.labelColorEditor[0].initializeValue(this.labelColor0);
        this.labelColorEditor[1].initializeValue(this.labelColor1);
        this.textColorEditor.initializeValue(this.textColor);
        this.spoolColorEditor.initializeValue(this.spoolColor);
        
        this.currentCassette.invalidateCache();
    }
}

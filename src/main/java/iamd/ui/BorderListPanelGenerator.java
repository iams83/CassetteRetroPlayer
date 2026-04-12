package iamd.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.Collection;
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.JPanel;

public class BorderListPanelGenerator
{
	final private LinkedList<JPanel> internalPanelsList = new LinkedList<JPanel>();
	final private String borderLayoutPosition;
	
	public BorderListPanelGenerator(String borderLayoutPosition)
	{
		this.borderLayoutPosition = borderLayoutPosition;
		
		JPanel firstPanel = new JPanel(new BorderLayout());
		firstPanel.setBackground(new Color(0, 0, 0, 0));
		
		this.internalPanelsList.add(firstPanel);
	}
	
	public void add(Component component)
	{
		JPanel p = this.internalPanelsList.getLast();
		
        p.add(component, this.borderLayoutPosition);
        
        JPanel newP = new JPanel(new BorderLayout());
        newP.setBackground(new Color(0, 0, 0, 0));
        
        p.add(newP);
        
        this.internalPanelsList.add(newP);
	}
	
	public void addAll(Collection<? extends JComponent> components)
	{
		for (JComponent component : components)
			add(component);
	}
	
	public void addAll(JComponent ... components)
	{
		for (JComponent component : components)
			add(component);
	}
	
	public void setBackground(Color color)
	{
		for (JPanel p : this.internalPanelsList)
			p.setBackground(color);
	}
	
	public JPanel extractPanel()
	{
		return extractPanel(null);
	}

	public JPanel extractPanel(JComponent mainComponent)
	{
	    this.internalPanelsList.getLast().add(mainComponent != null ? mainComponent : new JPanel());
		
		return this.internalPanelsList.getFirst();
	}

}

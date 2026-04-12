package iamd.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.EmptyBorder;

import iamd.rsrc.Resources;

public class ErrorMessage
{
    static public void showErrorMessage(JFrame parent, Throwable e1, String title)
    {
        e1.printStackTrace();
        
        StringWriter errors = new StringWriter();
        e1.printStackTrace(new PrintWriter(errors));

        showErrorMessage(parent, e1.getClass().getSimpleName() + ": " + e1.getMessage(), errors.toString(), title);
    }

    static public void showErrorMessage(JFrame parent, Throwable e1)
    {
        e1.printStackTrace();
        
        StringWriter errors = new StringWriter();
        e1.printStackTrace(new PrintWriter(errors));

        showErrorMessage(parent, e1.getClass().getSimpleName() + ": " + e1.getMessage(), errors.toString(), "Error");
    }

    static public void showErrorMessage(Throwable e1)
    {
        e1.printStackTrace();
        
        StringWriter errors = new StringWriter();
        e1.printStackTrace(new PrintWriter(errors));

        showErrorMessage(null, e1.getClass().getSimpleName() + ": " + e1.getMessage(), errors.toString(), "Error");
    }

    public static void showErrorMessage(JFrame parent, String errorLine, String errorDetails, String title)
    {
        JDialog dialog = new JDialog(parent);
        
        JLabel errorIcon = new JLabel(Resources.ErrorIcon);
        errorIcon.setBorder(new EmptyBorder(0, 10, 0, 0));
        
        JLabel label = new JLabel(errorLine);
        label.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JTextArea detailsArea = new JTextArea();
        detailsArea.setEditable(false);
        
        detailsArea.setText(errorDetails);
        detailsArea.setFont(new Font("Courier new", Font.PLAIN, 11));
        detailsArea.setCaretPosition(0);
        
        JPanel detailsPanel = new JPanel(new BorderLayout());
        detailsPanel.setBorder(new EmptyBorder(0, 10, 10, 10));
        detailsPanel.add(new JScrollPane(detailsArea));
        
        JPanel buttonsPanel = new JPanel(new BorderLayout());
        buttonsPanel.setBorder(new EmptyBorder(10, 10, 10, 10));
        
        JButton closeButton = new JButton("OK");
        buttonsPanel.add(closeButton, BorderLayout.EAST);
        closeButton.addActionListener(new ActionListener()
        {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                dialog.setVisible(false);
            }
        });
        
        dialog.setLayout(new BorderLayout());
        dialog.add(label, BorderLayout.NORTH);
        dialog.add(detailsPanel);
        dialog.add(errorIcon, BorderLayout.WEST);
        dialog.add(buttonsPanel, BorderLayout.SOUTH);
        
        dialog.setTitle(title);
        dialog.setModal(true);
        dialog.setSize(new Dimension(600, 400));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

}

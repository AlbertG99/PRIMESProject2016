package primesproject;
import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.border.EmptyBorder;

public class ProgressBar {
	private JPanel panel;
	private JFrame frame;
	private JLabel label;
	private JLabel note;
	private JProgressBar progressBar;

	public ProgressBar () {
		panel = new JPanel();
		panel.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		label = new JLabel("Running...");
		note = new JLabel("                                                                        ");
		c.fill = GridBagConstraints.HORIZONTAL;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.ipadx = 10;
		c.gridwidth = 1;
		c.gridy = 0;
		c.gridx = 0;
		panel.add(label, c);
		c.gridx = 1;
		frame = new JFrame();
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		panel.add(progressBar, c);
		panel.setBorder(new EmptyBorder(5, 10, 5, 10));
		panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressBar.setStringPainted(true);
		c.gridy = 1;
		c.gridwidth = 2;
		c.gridx = 0;
		panel.add(note, c);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setTitle("");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setTitle (String title) {
		frame.setTitle(title);
	}
	
	public void setVisible (boolean bool) {
		frame.setVisible(bool);
	}
	
	public void setLabel (String text) {
		label.setText(text);
		System.out.println(text);
	}
	
	public void setNote (String text) {
		note.setText(text);
		System.out.println("Note: " + text);
	}
	
	public void clearNote () {
		note.setText("                                                                        ");
	}
	
	public void setContinuous (boolean bool) {
		progressBar.setIndeterminate(bool);
	}
	
	public void setPercent (int percent) {
		progressBar.setIndeterminate(false);
		progressBar.setValue(percent);
	}
}

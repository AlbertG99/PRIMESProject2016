import java.awt.Cursor;
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
	private JProgressBar progressBar;

	public ProgressBar () {
		panel = new JPanel();
		panel.setLayout(new GridLayout(1, 0));
		label = new JLabel("Running...");
		panel.add(label);
		frame = new JFrame();
		progressBar = new JProgressBar(0, 100);
		progressBar.setValue(0);
		progressBar.setIndeterminate(true);
		progressBar.setStringPainted(true);
		panel.add(progressBar);
		panel.setBorder(new EmptyBorder(5, 10, 5, 10));
		panel.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		progressBar.setStringPainted(true);
		frame.add(panel);
		frame.pack();
		frame.setLocationRelativeTo(null);
		frame.setResizable(false);
		frame.setVisible(true);
		frame.setTitle("Running...");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}
	
	public void setVisible (boolean bool) {
		frame.setVisible(bool);
	}
	
	public void setLabel (String text) {
		label.setText(text);
	}
	
	public void setContinuous (boolean bool) {
		progressBar.setIndeterminate(bool);
	}
	
	public void setPercent (int percent) {
		progressBar.setIndeterminate(false);
		progressBar.setValue(percent);
	}
}

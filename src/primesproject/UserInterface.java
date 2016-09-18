package primesproject;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

public class UserInterface {
	public static ArrayList<Object> getInput () {
		ArrayList<Object> inputs = new ArrayList<Object>();

		final JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.ipadx = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.WEST;
//		panel.setPreferredSize(new Dimension(513, 200));
		
		final JPanel pixelErrorPanel = new JPanel(new GridBagLayout());
		GridBagLayout pixelLayout = new GridBagLayout();
		GridBagConstraints pixelC = new GridBagConstraints();
		pixelLayout.setConstraints(pixelErrorPanel, pixelC);
		pixelC.ipadx = 0;
		pixelC.fill = GridBagConstraints.NONE;
		pixelC.anchor = GridBagConstraints.WEST;
		TitledBorder title = BorderFactory.createTitledBorder("Visualization Options");
		pixelErrorPanel.setBorder(title);

		final StringStorage file1Path = new StringStorage("/Users/Albert/Google Drive/Boyden PRIMES/Example/Bandy-1.tif");
		final JButton file1Button = new JButton("                              Desktop/ground-truth.tif                              ");
		file1Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				//System.out.println(graphFileButton.getText());
				fileChooser.setCurrentDirectory(new File(file1Path.toString()));
				FileFilter filter = new FileNameExtensionFilter("TIF file", new String[] {"tif", "TIF", "tiff", "TIFF"});
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
				int result = fileChooser.showOpenDialog(new JFrame());
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String path = selectedFile.getAbsolutePath();
					//System.out.println(path);
					if (!path.substring(path.length() - 4).toLowerCase().equals(".tif")) {
						path += ".tif";
					}

					file1Path.set(path);

					while (true) {
						int index = path.indexOf('/');
						//System.out.println(index);
						if (path.length() - index < 30) {
							path = "..." + path.substring(index);
							break;
						}
						else if (index == -1) {
							path = "..." + path.substring(path.length() - 15);
							break;
						}
						else {
							path = path.substring(index + 1);
						}
					}

					file1Button.setText(path);
				}
			}
		});
		
		final StringStorage file2Path = new StringStorage("/Users/Albert/Google Drive/Boyden PRIMES/Example/Uygar-1.tif");
		final JButton file2Button = new JButton("Desktop/segmentation.tif");
		file2Button.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				//System.out.println(graphFileButton.getText());
				fileChooser.setCurrentDirectory(new File(file2Path.toString()));
				FileFilter filter = new FileNameExtensionFilter("TIF file", new String[] {"tif", "TIF", "tiff", "TIFF"});
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
				int result = fileChooser.showOpenDialog(new JFrame());
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String path = selectedFile.getAbsolutePath();
					//System.out.println(path);
					if (!path.substring(path.length() - 4).toLowerCase().equals(".tif")) {
						path += ".tif";
					}

					file2Path.set(path);

					while (true) {
						int index = path.indexOf('/');
						//System.out.println(index);
						if (path.length() - index < 30) {
							path = "..." + path.substring(index);
							break;
						}
						else if (index == -1) {
							path = "..." + path.substring(path.length() - 15);
							break;
						}
						else {
							path = path.substring(index + 1);
						}
					}

					file2Button.setText(path);
				}
			}
		});
		
		final StringStorage graphPath = new StringStorage("/Users/Albert/Desktop/image_test.tif");
		final JButton graphButton = new JButton("                            Desktop/image_test.tif                            ");
		graphButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				JFileChooser fileChooser = new JFileChooser();
				//System.out.println(graphFileButton.getText());
				fileChooser.setCurrentDirectory(new File(graphButton.getText()));
				FileFilter filter = new FileNameExtensionFilter("TIF file", new String[] {"tif", "TIF", "tiff", "TIFF"});
				fileChooser.addChoosableFileFilter(filter);
				fileChooser.setFileFilter(filter);
				int result = fileChooser.showSaveDialog(new JFrame());
				if (result == JFileChooser.APPROVE_OPTION) {
					File selectedFile = fileChooser.getSelectedFile();
					String path = selectedFile.getAbsolutePath();
					if (!path.substring(path.length() - 4).toLowerCase().equals(".tif")) {
						path += ".tif";
					}
//					System.out.println(path);

					graphPath.set(path);

					while (true) {
						int index = path.indexOf('/');
						//System.out.println(index);
						if (path.length() - index < 30) {
							path = "..." + path.substring(index);
							break;
						}
						else if (index == -1) {
							path = "..." + path.substring(path.length() - 15);
							break;
						}
						else {
							path = path.substring(index + 1);
						}
					}

					graphButton.setText(path);
				}
			}
		});
		graphButton.setEnabled(false);
		
		final JCheckBox createPixelImage = new JCheckBox();
		createPixelImage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (createPixelImage.isSelected()) {
					graphButton.setEnabled(true);
				}
				else {
					graphButton.setEnabled(false);
				}
			}
		});
		
		final String[] errors = {"Pixel Error", "Rand Error", "Warping Error"};
		final JComboBox<String> errorChoice = new JComboBox<String>(errors);
		errorChoice.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				if (errorChoice.getSelectedItem() != "Rand Error") {
					createPixelImage.setEnabled(true);
				}
				else {
					createPixelImage.setSelected(false);
					createPixelImage.setEnabled(false);
					graphButton.setEnabled(false);
				}
			}
		});
		
		c.ipady = 0;
		c.gridy = 0;
		c.gridx = 0;
		panel.add(new JLabel("Ground Truth:"), c);
		c.gridx = 1;
		panel.add(file1Button, c);
		c.gridy += 1;
		c.gridx = 0;
		panel.add(new JLabel("Segmentation:"), c);
		c.gridx = 1;
		panel.add(file2Button, c);
		c.gridy += 1;
		c.gridx = 0;
		panel.add(new JLabel("Error:"), c);
		c.gridx = 1;
		panel.add(errorChoice, c);
		c.gridy += 1;
		c.gridx = 0;
		c.gridwidth = 2;
		panel.add(pixelErrorPanel, c);
		c.gridy += 1;
		c.gridx = 1;
		panel.add(new JLabel("                                                                                                                      "), c);
		
		pixelC.gridy = 0;
		pixelC.gridx = 0;
		pixelErrorPanel.add(new JLabel("Create image?   "), pixelC);
		pixelC.gridx = 1;
		pixelErrorPanel.add(createPixelImage, pixelC);
		pixelC.gridy += 1;
		pixelC.gridx = 0;
		pixelErrorPanel.add(new JLabel("Image:"), pixelC);
		pixelC.gridx = 1;
		pixelErrorPanel.add(graphButton, pixelC);
		
		JFrame frame = new JFrame();
		frame.setResizable(false);
		int result = JOptionPane.showConfirmDialog(frame, panel, "Configuration", JOptionPane.OK_CANCEL_OPTION);
		if (result != JOptionPane.OK_OPTION) {
			System.exit(0);
		}
		
		inputs.add(file1Path);
		inputs.add(file2Path);
		inputs.add(errorChoice.getSelectedItem());
		inputs.add(createPixelImage.isSelected());
		inputs.add(graphPath);
		
		return inputs;
	}
	
	public static void showPopupText (String message, boolean copyable) {
		if (!copyable) {
			JOptionPane.showMessageDialog(new JFrame(), message);
		}
		else {
			JFrame frame = new JFrame();
			JPanel panel = new JPanel();
			JTextField text = new JTextField(message);
			text.setEditable(false);
			text.setBackground(new Color(238, 238, 238));
			text.setBorder(BorderFactory.createLineBorder(new Color(238, 238, 238), 0));
			panel.add(text);
			int result = JOptionPane.showConfirmDialog(frame, panel, "Result", JOptionPane.OK_CANCEL_OPTION);
			if (result != JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		}
	}
}























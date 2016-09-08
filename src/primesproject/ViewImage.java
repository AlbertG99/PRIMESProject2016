package primesproject;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.idrsolutions.image.tiff.TiffDecoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import ij.ImagePlus;
import ij.ImageStack;
import ij.process.ImageProcessor;

public class ViewImage {
	// Create mask
	
	public static BufferedImage createBlackImage (int width, int height) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < width * height; i++) {
			bufferedImage.setRGB(i % width, i / width, 1);
		}
		return bufferedImage;
	}
	
	public static BufferedImage createColorImage (int width, int height, Color color) {
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		for (int i = 0; i < width * height; i++) {
			bufferedImage.setRGB(i % width, i / width, color.getRGB());
		}
		return bufferedImage;
	}
	
	public static ImagePlus create3DBlackImage (int width, int height, int pages) {
		ImageStack stack = new ImageStack(width, height, pages);
		ImagePlus implus = new ImagePlus ("Mask", createBlackImage(width, height));
		
		for (int i = 0; i < pages; i++) {
			stack.addSlice("Mask " + i, (ImageProcessor)implus.getChannelProcessor().clone(), i);
//			System.out.println(i);
		}
		
		return new ImagePlus("Mask", stack);
	}
	
	// Create images
	
	public static BufferedImage createBufferedImage (byte[][] pixels, int width, int height, String filename, int page) throws FileNotFoundException {
		byte[] pagePixels = pixels[page];
		
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < pagePixels.length; i++) {
			int color = pagePixels[i];
			if (pagePixels[i] == 0) {
				color = new Color(105, 105, 105).getRGB();
			}
			else if (pagePixels[i] == 3) {
				color = -1;
			}
			else if (pagePixels[i] == 1) {
				color = new Color(0, 0, 255).getRGB();
			}
			else if (pagePixels[i] == 2) {
				color = new Color(0, 255, 0).getRGB();
			}
			bufferedImage.setRGB(i % width, i / width, color);
		}
		
		return bufferedImage;
	}
	
	public static BufferedImage createBufferedImage (float[][] pixels) throws FileNotFoundException {		
		BufferedImage bufferedImage = new BufferedImage(pixels.length, pixels[0].length, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < pixels.length; i++) {
			for (int j = 0; j < pixels[0].length; j++) {
				bufferedImage.setRGB(i, i, (int)pixels[i][j]);
			}
		}
		
		return bufferedImage;
	}
	
	public static BufferedImage[] create3DBufferedImage (byte[][] pixels, int width, int height, String filename, ProgressBar pBar) throws IOException {
		BufferedImage[] image = new BufferedImage[pixels.length];
		for (int i = 0; i < pixels.length; i++) {
			pBar.setPercent((i * 100) / pixels.length);
			image[i] = createBufferedImage(pixels, width, height, filename, i);
		}
		pBar.setContinuous(true);
		pBar.setLabel("Saving image...");
		TIFFEncodeParam params = new TIFFEncodeParam();
		OutputStream out = new FileOutputStream(filename); 
		ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
		Vector<BufferedImage> vector = new Vector<BufferedImage>();   
//		System.out.println(image.length);
		for (int i = 1; i < pixels.length; i++) {
		    vector.add(image[i]);
		}
		params.setExtraImages(vector.iterator()); 
		encoder.encode(image[0]);
		out.close();
		
		return image;
	}
	
	// Display images
	
	public static void viewImage (Image image, String label) { // Display image
		Image imageScaled = image.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
		final JPanel panel = new JPanel(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		panel.add(new JLabel(label), c);
		c.gridy += 1;
		panel.add(new JLabel(new ImageIcon(imageScaled)), c);
		JOptionPane.showMessageDialog(null, panel);
	}
	
	public static void view3DImage (String filepath, String label) throws Exception {
		RandomAccessFile raf = new RandomAccessFile(filepath,"r");
		TiffDecoder decoder = new TiffDecoder(raf);
		int numPages = decoder.getPageCount();
		BufferedImage image = decoder.read(1);
		Image imageScaled = image.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setFocusable(true);
		panel.requestFocusInWindow();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.ipady = 3;
		c.gridwidth = 4;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		JTextField labelField = new JTextField(label);
		labelField.setEditable(false);
		labelField.setBackground(new Color(238, 238, 238));
		labelField.setBorder(BorderFactory.createLineBorder(new Color(238, 238, 238), 0));
		panel.add(labelField, c);
		c.gridy += 1;
		c.ipady = 0;
		ImageIcon imageIcon = new ImageIcon(imageScaled);
		JLabel imageLabel = new JLabel(imageIcon);
		panel.add(imageLabel, c);
		c.gridy += 1;
		Scrollbar scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, numPages + 1);
		panel.add(scrollbar, c);
		c.gridy += 1;
		JTextField currLayer = new JTextField("1", 10);
		currLayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				BufferedImage image2 = null;
				int val = 1;
				try {
					val = Integer.parseInt(currLayer.getText());
					if (val > 0 && val <= numPages) {
						image2 = decoder.read(val);
						Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
						imageIcon.setImage(imageScaled2);
						imageLabel.setIcon(imageIcon);
						scrollbar.setValue(val);
						panel.repaint();
					}
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		c.gridwidth = 1;
		c.gridx = 0;
		JTextField white = new JTextField("Both / Correct", 10);
		white.setEditable(false);
		white.setBackground(new Color(255, 255, 255));
		panel.add(white, c);
		c.gridx += 1;
		JTextField blue = new JTextField("Only Truth", 10);
		blue.setEditable(false);
		blue.setBackground(new Color(0, 0, 255));
		panel.add(blue, c);
		c.gridx += 1;
		JTextField green = new JTextField("Only Segmentation", 10);
		green.setEditable(false);
		green.setBackground(new Color(0, 255, 0));
		panel.add(green, c);
		c.gridx += 1;
		JTextField black = new JTextField("Neither", 10);
		black.setEditable(false);
		black.setBackground(new Color(105, 105, 105));
		panel.add(black, c);
		c.gridy += 1;
		c.gridx = 0;
		panel.add(new JLabel("Layer:"), c);
		c.gridx = 1;
		panel.add(currLayer, c);
		c.gridwidth = 4;
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				BufferedImage image2 = null;
				int val = 1;
				try {
					val = scrollbar.getValue();
					image2 = decoder.read(val);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
				imageIcon.setImage(imageScaled2);
				imageLabel.setIcon(imageIcon);
				currLayer.setText("" + val);
				panel.repaint();
			}
		});
		imageLabel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					int scrollAmt = e.getWheelRotation();
					int currVal = scrollbar.getValue();
					int newVal = currVal + scrollAmt;
					if (newVal > numPages) {
						newVal = numPages;
					}
					else if (newVal < 1) {
						newVal = 1;
					}
					
					BufferedImage image2 = null;
					int val = 1;
					try {
						val = newVal;
						image2 = decoder.read(val);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
					imageIcon.setImage(imageScaled2);
					imageLabel.setIcon(imageIcon);
					currLayer.setText("" + val);
					scrollbar.setValue(val);
					panel.repaint();
				}
			}
		});
		panel.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent e) {
			}
			@Override
			public void keyPressed(KeyEvent e) {
//				System.out.println(e.getKeyCode());
				int moveAmt = 0;
				if (e.getKeyCode()==39) { // Right arrow
					moveAmt = 1;
				}
				else if (e.getKeyCode()==37) { // Left arrow
					moveAmt = -1;
				}
				int currVal = scrollbar.getValue();
				int newVal = currVal + moveAmt;
				if (newVal > numPages) {
					newVal = numPages;
				}
				else if (newVal < 1) {
					newVal = 1;
				}

				BufferedImage image2 = null;
				int val = 1;
				try {
					val = newVal;
					image2 = decoder.read(val);
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
				imageIcon.setImage(imageScaled2);
				imageLabel.setIcon(imageIcon);
				currLayer.setText("" + val);
				scrollbar.setValue(val);
				panel.repaint();
			}
			@Override
			public void keyReleased(KeyEvent e) {

			}
		});
		JOptionPane.showMessageDialog(null, panel);
	}
	
	public static void view3DImage (BufferedImage[] imageList, String label) {
		BufferedImage image = imageList[0];
		int numPages = imageList.length;
		Image imageScaled = image.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
		final JPanel panel = new JPanel(new GridBagLayout());
		panel.setFocusable(true);
		panel.requestFocusInWindow();
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		JTextField labelField = new JTextField(label);
		labelField.setEditable(false);
		panel.add(labelField, c);
		c.gridy += 1;
		ImageIcon imageIcon = new ImageIcon(imageScaled);
		JLabel imageLabel = new JLabel(imageIcon);
		panel.add(imageLabel, c);
		c.gridy += 1;
		Scrollbar scrollbar = new Scrollbar(Scrollbar.HORIZONTAL, 1, 1, 1, numPages + 1);
		panel.add(scrollbar, c);
		c.gridy += 1;
		JTextField currLayer = new JTextField("1", 10);
		currLayer.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				BufferedImage image2 = null;
				int val = 1;
				try {
					val = Integer.parseInt(currLayer.getText());
					image2 = imageList[val];
					Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
					imageIcon.setImage(imageScaled2);
					imageLabel.setIcon(imageIcon);
					scrollbar.setValue(val);
					panel.repaint();
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				
			}
		});
		panel.add(currLayer, c);
		scrollbar.addAdjustmentListener(new AdjustmentListener() {
			public void adjustmentValueChanged(AdjustmentEvent e) {
				BufferedImage image2 = null;
				int val = 1;
				try {
					val = scrollbar.getValue();
					image2 = imageList[val];
				} catch (Exception e1) {
					e1.printStackTrace();
				}
				Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
				imageIcon.setImage(imageScaled2);
				imageLabel.setIcon(imageIcon);
				currLayer.setText("" + val);
				panel.repaint();
			}
		});
		imageLabel.addMouseWheelListener(new MouseWheelListener() {
			public void mouseWheelMoved(MouseWheelEvent e) {
				if (e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
					int scrollAmt = e.getWheelRotation();
					int currVal = scrollbar.getValue();
					int newVal = currVal + scrollAmt;
					if (newVal > numPages) {
						newVal = numPages;
					}
					else if (newVal < 1) {
						newVal = 1;
					}
					
					BufferedImage image2 = null;
					int val = 1;
					try {
						val = newVal;
						image2 = imageList[val];
					} catch (Exception e1) {
						e1.printStackTrace();
					}
					Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
					imageIcon.setImage(imageScaled2);
					imageLabel.setIcon(imageIcon);
					currLayer.setText("" + val);
					scrollbar.setValue(val);
					panel.repaint();
				}
			}
		});
//		imageLabel.addKeyListener(new KeyListener() {
//			@Override
//			public void keyTyped(KeyEvent e) {
//				
//			}
//			@Override
//			public void keyPressed(KeyEvent e) {
//				System.out.println(e.getKeyCode());
//				int moveAmt = 0;
//				if (e.getKeyCode()==39) { // Right arrow
//					moveAmt = -1;
//				}
//				else if (e.getKeyCode()==37) { // Left arrow
//					moveAmt = 1;
//				}
//				int currVal = scrollbar.getValue();
//				int newVal = currVal + moveAmt;
//				if (newVal > numPages) {
//					newVal = numPages;
//				}
//				else if (newVal < 1) {
//					newVal = 1;
//				}
//				
//				BufferedImage image2 = null;
//				int val = 1;
//				try {
//					val = newVal;
//					image2 = imageList[val];
//				} catch (Exception e1) {
//					e1.printStackTrace();
//				}
//				Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
//				imageIcon.setImage(imageScaled2);
//				imageLabel.setIcon(imageIcon);
//				currLayer.setText("" + val);
//				scrollbar.setValue(val);
//				panel.repaint();
//			}
//			@Override
//			public void keyReleased(KeyEvent e) {
//				
//			}
//        });
		JOptionPane.showMessageDialog(null, panel);
	}
}

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Image;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.idrsolutions.image.tiff.TiffDecoder;
import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

public class ViewImage {
	// Create images
	
	public static BufferedImage createBufferedImage (byte[][] pixels, int width, int height, String filename, int page) throws FileNotFoundException {
		byte[] pagePixels = pixels[page];
		
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		
		for (int i = 0; i < pagePixels.length; i++) {
			int color = pagePixels[i];
			if (pagePixels[i] == 0 || pagePixels[i] == 3) {
				color = 0;
			}
			else {
				color = -1;
			}
			bufferedImage.setRGB(i % width, i / width, color);
		}
		
		return bufferedImage;
	}
	
	public static void create3DBufferedImage (byte[][] pixels, int width, int height, String filename, ProgressBar pBar) throws IOException {
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
		for (int i = 0; i < pixels.length; i++) {
		    vector.add(image[i]); 
		}
		params.setExtraImages(vector.iterator()); 
		encoder.encode(image[0]); 
		out.close(); 
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
		GridBagConstraints c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.ipadx = 0;
		c.ipady = 0;
		c.fill = GridBagConstraints.BOTH;
		c.anchor = GridBagConstraints.CENTER;
		c.gridy = 0;
		panel.add(new JLabel(label), c);
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
					image2 = decoder.read(val);
					Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
					imageIcon.setImage(imageScaled2);
					imageLabel.setIcon(imageIcon);
					scrollbar.setValue(val);
					panel.repaint();
				} catch (Exception e1) {
					// TODO Auto-generated catch block
//					e1.printStackTrace();
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
					image2 = decoder.read(val);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				Image imageScaled2 = image2.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
				imageIcon.setImage(imageScaled2);
				imageLabel.setIcon(imageIcon);
				currLayer.setText("" + val);
				panel.repaint();
			}
		});
		JOptionPane.showMessageDialog(null, panel);
	}
}

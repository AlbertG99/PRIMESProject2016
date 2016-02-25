import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.Image;
import java.io.RandomAccessFile;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.idrsolutions.image.tiff.TiffDecoder;

public class Tiff {
	private String filepath;
	private BufferedImage[] pages;
	private int numPages;
	
	public Tiff (String filepath) throws Exception {
		this.filepath = filepath;
		RandomAccessFile raf = new RandomAccessFile(filepath,"r");
		TiffDecoder decoder = new TiffDecoder(raf);
		this.numPages = decoder.getPageCount();
		this.pages = new BufferedImage[numPages];
		for (int i = 1; i <= numPages; i++) {
			BufferedImage decodedImage = decoder.read(i);
			pages[i - 1] = decodedImage;
		}
	}
	
	public int getHeight () {
		return pages[0].getHeight();
	}
	
	public int getWidth () {
		return pages[0].getWidth();
	}
	
	public byte[] getPixels (int page) throws Exception {
		if (page > numPages) {
			throw new Exception("Page number out of bounds.");
		}
		byte[] pixels = ((DataBufferByte) pages[page].getRaster().getDataBuffer()).getData();
		return pixels;
	}
	
	public int getNumPages () {
		return numPages;
	}
	
	public BufferedImage getPage (int page) throws Exception {
		if (page > numPages) {
			throw new Exception("Page number out of bounds.");
		}
		return pages[page];
	}
	
	public static void viewImage (Image image) {
		Image imageScaled = image.getScaledInstance(500, -1,  Image.SCALE_SMOOTH);
		JOptionPane.showMessageDialog(null, new JLabel(new ImageIcon(imageScaled)));
	}
}
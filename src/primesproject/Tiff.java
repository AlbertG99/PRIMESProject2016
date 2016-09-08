package primesproject;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

import javax.imageio.ImageIO;

import com.idrsolutions.image.tiff.TiffDecoder;

public class Tiff {
//	private String filepath;
	private BufferedImage[] pages;
	private int numPages;
	
	public Tiff (String filepath) throws Exception { // Create new Tiff object
//		this.filepath = filepath;
		RandomAccessFile raf = new RandomAccessFile(filepath,"r");
		TiffDecoder decoder = new TiffDecoder(raf);
		this.numPages = decoder.getPageCount();
		this.pages = new BufferedImage[numPages];
		for (int i = 1; i <= numPages; i++) { // Add all pages to pages array
			BufferedImage decodedImage = decoder.read(i);
			pages[i - 1] = decodedImage;
		}
//		System.out.println(Arrays.toString(getPixels(0)));
	}
	
	public Tiff (byte[][] byteArray) throws IOException {
		this.numPages = byteArray.length;
		this.pages = new BufferedImage[numPages];
		for (int i = 0; i < numPages; i++) {
			pages[i] = createImageFromBytes(byteArray[i]);
		}
		
		ImageIO.write(pages[0], "tif", new File("/Users/Albert/Desktop/Test.TIF"));
	}
	
	private BufferedImage createImageFromBytes(byte[] imageData) {
	    ByteArrayInputStream bais = new ByteArrayInputStream(imageData);
	    try {
	        return ImageIO.read(bais);
	    } catch (IOException e) {
	        throw new RuntimeException(e);
	    }
	}
	
	public int getHeight () {
		return pages[0].getHeight();
	}
	
	public int getWidth () {
		return pages[0].getWidth();
	}
	
	public int getNumPixels () {
		return getHeight() * getWidth();
	}
		
	public byte[] getPixels (int pageIndex) throws Exception { // Get array of all pixels of given page (0 = black, -1 = white)
		if (pageIndex - 1 > numPages) {
			throw new Exception("Page number out of bounds.");
		}
		byte[] pixels = ((DataBufferByte) pages[pageIndex].getRaster().getDataBuffer()).getData();
		return pixels;
	}
	
	public int getNumPages () {
		return numPages;
	}
	
	public BufferedImage getPage (int pageIndex) throws Exception { // Get page from given index
		if (pageIndex - 1 > numPages) {
			throw new Exception("Page number out of bounds.");
		}
		return pages[pageIndex];
	}
}
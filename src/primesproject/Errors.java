package primesproject;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Vector;

import org.scijava.vecmath.Point3f;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import ij.ImagePlus;
import ij.ImageStack;
import trainableSegmentation.metrics.AdjustedRandError;
import trainableSegmentation.metrics.PixelError;
import trainableSegmentation.metrics.RandError;
import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.WarpingResults;

public class Errors {
	public static double pixelError (ImagePlus originalLabels, ImagePlus proposedLabels) {
		// Seems to be working, returns 0.0 when same image and correct value when different images. For some reason seems to work in 3D even though description says 2D only.
		
		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		PixelError metric = new PixelError(originalLabels, proposedLabels);
		
		System.gc();
		double pixelError = metric.getMetricValue(threshold);
		
		return pixelError;
		
//		IJ.log("Pixel error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + pixelError);
	}
	
	public static double randError (ImagePlus originalLabels, ImagePlus proposedLabels) {
		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		RandError metric = new RandError(originalLabels, proposedLabels);
		
		System.gc();
		double randError = metric.getMetricValue(threshold);
		
//		double randError = metric.randError(originalLabels.getChannelProcessor(), proposedLabels.getChannelProcessor(), threshold);
		
		return randError;
		
//		IJ.log("Rand error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + randError);
	}
	
	public static double adjustedRandError (ImagePlus originalLabels, ImagePlus proposedLabels) {
		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		AdjustedRandError metric = new AdjustedRandError(originalLabels, proposedLabels);
		
		System.gc();
		double randError = metric.getMetricValue(threshold);
		
		return randError;

//		IJ.log("Rand error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + randError);
	}

	public static WarpingResults[] warpingError (ImagePlus originalLabels, ImagePlus proposedLabels, ImagePlus mask, ProgressBar pBar) { //Source: http://fiji.sc/Topology_preserving_warping_error
		// mask with geometric constraints
//		ImagePlus mask = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF", 1); // What is mask?

		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		WarpingError metric = new WarpingError(originalLabels, proposedLabels, mask);
		
		return metric.getWRS(threshold, pBar, 0);
		
//		System.gc();
//		double warpingError = metric.getMetricValue(threshold, pBar);
//		
//		return warpingError;

//		IJ.log("Warping error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + warpingError);
	}
	
	public static byte[][] pixelErrorArray (Tiff tiff1, Tiff tiff2) throws Exception {
		int numPages = tiff1.getNumPages();
		int numPixels = tiff1.getNumPixels();

		byte[][] fullOutput = new byte[numPages][numPixels];

		for (int page = 0; page < numPages; page++) {
			try {
				fullOutput[page] = comparePage(tiff1.getPixels(page), tiff2.getPixels(page));
			}
			catch (OutOfMemoryError e){
				page--;
				System.gc();
			}
		}
		
		return fullOutput;
	}
	
	public static double pixelError (int numPages, int numPixels, byte[][] fullOutput) {
		double count = 0;
		
		for (int i = 0; i < numPages; i++) {
			for (int j = 0; j < numPixels; j++) {
				if (fullOutput[i][j] == 1 || fullOutput[i][j] == 2) {
					count++;
				}
			}
		}
		
		return count/(numPages*numPixels);
	}

	public static byte[] comparePage (byte[] page1, byte[] page2) {
		/*
		0 = neither
		1 = page1 only
		2 = page2 only
		3 = both
		 */
		int numPixels = page1.length;
		byte[] comparison = new byte[numPixels];

		int page1Pix;
		int page2Pix;

		for (int i = 0; i < numPixels; i++) {
			page1Pix = page1[i];
			page2Pix = page2[i];
			if (page1Pix == -1 && page2Pix == 0) {
				comparison[i] = 1;
			}
			else if (page1Pix == 0 && page2Pix == -1) {
				comparison[i] = 2;
			}
			else if (page1Pix == -1 && page2Pix == -1) {
				comparison[i] = 3;
			}
			else { //if (page1Pix == 0 && page2Pix == 0)
				comparison[i] = 0;
			}
		}

		return comparison;
	}
	
	// Warping error visualization
	public static BufferedImage createWarpingErrorImage (int width, int height, ArrayList<Point3f> mismatches, BufferedImage originalImage, BufferedImage proposedImage) {
		BufferedImage bufferedImage = ViewImage.createColorImage(width, height, new Color(105, 105, 105));
//		BufferedImage bufferedImage = originalImage;
		for (int i = 0; i < mismatches.size(); i++) {
			bufferedImage.setRGB((int)mismatches.get(i).x, (int)mismatches.get(i).y, new Color(255, 0, 0).getRGB());
		}
		for (int i = 0; i < originalImage.getWidth(); i++) {
			for (int j = 0; j < originalImage.getHeight(); j++) {
				int origColor = originalImage.getRGB(i, j);
				int propColor = proposedImage.getRGB(i, j);
				if (origColor == new Color(255, 255, 255).getRGB() && propColor == new Color(255, 255, 255).getRGB()) {
					bufferedImage.setRGB(i, j, new Color(255, 255, 255).getRGB());
				}
				else if (origColor == new Color(255, 255, 255).getRGB() && bufferedImage.getRGB(i, j) == new Color(255, 0, 0).getRGB()) {
					bufferedImage.setRGB(i, j, new Color(0, 0, 255).getRGB());
				}
				else if (propColor == new Color(255, 255, 255).getRGB() && bufferedImage.getRGB(i, j) == new Color(255, 0, 0).getRGB()) {
					bufferedImage.setRGB(i, j, new Color(0, 255, 0).getRGB());
				}
				else {
					bufferedImage.setRGB(i, j, new Color(105, 105, 105).getRGB());
				}
			}
		}
		return bufferedImage;
	}
	
	public static BufferedImage[] create3DWarpingErrorImage (ImagePlus originalLabels, ImagePlus proposedLabels, WarpingResults[] wrs, String filename, ProgressBar pBar) throws IOException {
		int width = originalLabels.getWidth();
		int height = originalLabels.getHeight();
		ImageStack originalStack = originalLabels.getStack();
		ImageStack proposedStack = proposedLabels.getStack();
		BufferedImage[] image = new BufferedImage[wrs.length];
		for (int i = 0; i < wrs.length; i++) {
			pBar.setPercent((i * 100) / wrs.length);
			image[i] = createWarpingErrorImage(width, height, wrs[i].mismatches, originalStack.getProcessor(i + 1).getBufferedImage(), proposedStack.getProcessor(i + 1).getBufferedImage());
		}
		pBar.setContinuous(true);
		pBar.setLabel("Saving image...");
		TIFFEncodeParam params = new TIFFEncodeParam();
		OutputStream out = new FileOutputStream(filename); 
		ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
		Vector<BufferedImage> vector = new Vector<BufferedImage>();   
		for (int i = 0; i < wrs.length; i++) {
		    vector.add(image[i]);
		}
		params.setExtraImages(vector.iterator());
		encoder.encode(image[0]); 
		out.close();
		
		return image;
	}
}

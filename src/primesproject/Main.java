package primesproject;
import java.util.ArrayList;

import ij.IJ;
import ij.ImagePlus;
import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.WarpingResults;

public class Main {
	public static void main(String[] args) throws Exception {
//		ViewImage.view3DImage("/Users/Albert/Google Drive/Boyden PRIMES/Example/Testing/Compared-Unfilled.tif", "The warping error is: 0.019187266791044776");
		
		System.gc();
		
		// Get inputs
		ArrayList<Object> inputs = UserInterface.getInput();
		String originalLabelsPath = ((StringStorage)inputs.get(0)).toString();
		String proposedLabelsPath = ((StringStorage)inputs.get(1)).toString();
		String error = (String)inputs.get(2);
		boolean createPixelImage = (boolean)inputs.get(3);
		String graphPath = ((StringStorage)inputs.get(4)).toString();
		
//		originalLabelsPath = "/Users/Albert/Google Drive/Boyden PRIMES/Example/Bandy-1.tif";
//		proposedLabelsPath = "/Users/Albert/Google Drive/Boyden PRIMES/Data/Simulated Data/simulation2 segs/simulation2_sig500_HMINTH0.008_sUB31_cUB0.1_detTh5e-11_subdivTh10_minVox10_cc30.tif";
//		proposedLabelsPath = originalLabelsPath;
//		proposedLabelsPath = "/Users/Albert/Google Drive/Boyden PRIMES/Example/Uygar-1.tif";
		
		originalLabelsPath = "/Users/Albert/Google Drive/Boyden PRIMES/Data/simulation2 results/simulation2_gt_fixed.tif";
		proposedLabelsPath = "/Users/Albert/Google Drive/Boyden PRIMES/Data/simulation2 results/seg_filled.tif";
		
		// Create progress bar
		ProgressBar pBar = new ProgressBar();
		
		double errorVal = 0.0;
		if (error.equals("Pixel Error")) { // Run pixel error
			pBar.setTitle("Running pixel error...");
			pBar.setLabel("Getting TIFFs...");
//			ViewImage.view3DImage(originalLabelsPath, "Test");
			Tiff originalLabels = new Tiff(originalLabelsPath);
			Tiff proposedLabels = new Tiff(proposedLabelsPath);
			checkCompatibility(originalLabels, proposedLabels);
			byte[][] fullOutput = Errors.pixelErrorArray(originalLabels, proposedLabels);
			pBar.setLabel("Calculating error...");
			errorVal = Errors.pixelError(originalLabels.getNumPages(), originalLabels.getNumPixels(), fullOutput);
			if (createPixelImage) {
				String filename = graphPath;
				pBar.setLabel("Creating image...");
				ViewImage.create3DBufferedImage(fullOutput, originalLabels.getWidth(), originalLabels.getHeight(), filename, pBar);
				pBar.setVisible(false);
				ViewImage.view3DImage(filename, "The " + error.toLowerCase() + " is: " + errorVal);
			}
			else {
				pBar.setVisible(false);
				UserInterface.showPopupText("The " + error.toLowerCase() + " is: " + errorVal, true);
			}
		}
		else if (error.equals("Rand Error")) { // Run rand error
			pBar.setTitle("Running rand error...");
			pBar.setLabel("Getting TIFFs...");
			ImagePlus originalLabels = IJ.openImage(originalLabelsPath);
			ImagePlus proposedLabels = IJ.openImage(proposedLabelsPath);
			checkCompatibility(originalLabels, proposedLabels);
			pBar.setLabel("Calculating error...");
			errorVal = Errors.adjustedRandError(originalLabels, proposedLabels);
			pBar.setVisible(false);
			UserInterface.showPopupText("The " + error.toLowerCase() + " is: " + errorVal, true);
		}
		else if (error.equals("Warping Error")) { // Run warping error
			pBar.setTitle("Running warping error...");
			pBar.setLabel("Getting TIFFs...");
			ImagePlus originalLabels = IJ.openImage(originalLabelsPath);
			ImagePlus proposedLabels = IJ.openImage(proposedLabelsPath);
			checkCompatibility(originalLabels, proposedLabels);
			pBar.setLabel("Creating mask...");
			ImagePlus mask = ViewImage.create3DBlackImage(1000, 1000, originalLabels.getImageStackSize());
			pBar.setLabel("Calculating error...");
			WarpingResults[] wrs = Errors.warpingError(originalLabels, proposedLabels, mask, pBar);
			errorVal = WarpingError.getMetricValue(wrs);
			if (createPixelImage) {
				String filename = graphPath;
				pBar.setLabel("Creating image...");
				Errors.create3DWarpingErrorImage(originalLabels, proposedLabels, wrs, filename, pBar);
				pBar.setVisible(false);
				ViewImage.view3DImage(filename, "The " + error.toLowerCase() + " is: " + errorVal);
			}
			else {
				pBar.setVisible(false);
				UserInterface.showPopupText("The " + error.toLowerCase() + " is: " + errorVal, true);
			}
		}
		
		System.exit(0); // Exit application		
	}

	public static void checkCompatibility (Tiff tiff1, Tiff tiff2) throws Exception { // Checks if two TIFF files are compatible and can be further worked with
		// Check if number of pages matches
		int numPages = tiff1.getNumPages();
		if (numPages != tiff2.getNumPages()) {
			throw new Exception("TIFF number of pages do not match.");
		}
		// Check if heights match
		int tiff1Height = tiff1.getHeight(); int tiff2Height = tiff2.getHeight();
		if (tiff1Height != tiff2Height) {
			throw new Exception("TIFF heights do not match.");
		}
		// Check if widths match
		int tiff1Width = tiff1.getWidth(); int tiff2Width = tiff2.getWidth();
		if (tiff1Width != tiff2Width) {
			throw new Exception("TIFF widths do not match.");
		}
	}
	
	public static void checkCompatibility (ImagePlus tiff1, ImagePlus tiff2) throws Exception { // Checks if two TIFF files are compatible and can be further worked with
		// Check if number of pages matches
		int numPages = tiff1.getImageStackSize();
		if (numPages != tiff2.getImageStackSize()) {
			throw new Exception("TIFF number of pages do not match.");
		}
		// Check if heights match
		int tiff1Height = tiff1.getHeight(); int tiff2Height = tiff2.getHeight();
		if (tiff1Height != tiff2Height) {
			throw new Exception("TIFF heights do not match.");
		}
		// Check if widths match
		int tiff1Width = tiff1.getWidth(); int tiff2Width = tiff2.getWidth();
		if (tiff1Width != tiff2Width) {
			throw new Exception("TIFF widths do not match.");
		}
	}
}












































import ij.IJ;
import ij.ImagePlus;
import trainableSegmentation.metrics.AdjustedRandError;
import trainableSegmentation.metrics.PixelError;
import trainableSegmentation.metrics.RandError;
import trainableSegmentation.metrics.WarpingError;

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

	public static double warpingError (ImagePlus originalLabels, ImagePlus proposedLabels) { //Source: http://fiji.sc/Topology_preserving_warping_error
		// mask with geometric constraints
		ImagePlus mask = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF", 1); // What is mask?

		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		WarpingError metric = new WarpingError(originalLabels, proposedLabels, mask);
		
		System.gc();
		double warpingError = metric.getMetricValue(threshold);
		
		return warpingError;

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
}

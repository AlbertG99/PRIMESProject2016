import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.PixelError;
import trainableSegmentation.metrics.RandError;
import ij.IJ;
import ij.ImagePlus;

public class Main {
	public static void main(String[] args) throws Exception {
		System.gc();
//		Tiff tiff1 = new Tiff ("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Uygar.TIF");
//		Tiff tiff2 = new Tiff ("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF");
//		checkCompatibility(tiff1, tiff2);
		// original labels
		ImagePlus originalLabels = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Uygar.TIF");
		// proposed (new) labels
		ImagePlus proposedLabels = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF");
		
		randError(originalLabels, proposedLabels);
		pixelError(originalLabels, proposedLabels);
		//System.out.println(pixelError(tiff1, tiff2));
	}
	
	public static void pixelError (ImagePlus originalLabels, ImagePlus proposedLabels) {
		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		PixelError metric = new PixelError(originalLabels, proposedLabels);
		
		System.gc();
		double pixelError = metric.getMetricValue( threshold );

		IJ.log("Pixel error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + pixelError);
	}
	
	public static void randError (ImagePlus originalLabels, ImagePlus proposedLabels) {
		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		RandError metric = new RandError(originalLabels, proposedLabels);
		
		System.gc();
		double randError = metric.getMetricValue( threshold );

		IJ.log("Rand error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + randError);
	}

	public static void warpingError (ImagePlus originalLabels, ImagePlus proposedLabels) { //Source: http://fiji.sc/Topology_preserving_warping_error
		// mask with geometric constraints
		ImagePlus mask = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF"); // What is mask?

		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		WarpingError metric = new WarpingError( originalLabels, proposedLabels, mask );
		
		System.gc();
		double warpingError = metric.getMetricValue( threshold );

		IJ.log("Warping error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + warpingError);
	}

//	public static byte[][] pixelError (Tiff tiff1, Tiff tiff2) throws Exception {
//		int numPages = tiff1.getNumPages();
//		int numPixels = tiff1.getPixels(0).length;
//
//		byte[][] fullOutput = new byte[numPages][numPixels];
//
//		for (int page = 0; page < numPages; page++) {
//			try {
//				fullOutput[page] = comparePage(tiff1.getPixels(page), tiff2.getPixels(page));
//			}
//			catch (OutOfMemoryError e){
//				page--;
//				System.gc();
//			}
//		}
//
//		return fullOutput;
//	}
	
	public static double pixelError (Tiff tiff1, Tiff tiff2) throws Exception {
		int numPages = tiff1.getNumPages();
		int numPixels = tiff1.getPixels(0).length;

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
}
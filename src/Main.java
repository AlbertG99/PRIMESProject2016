import trainableSegmentation.metrics.WarpingError;
import ij.IJ;
import ij.ImagePlus;

public class Main {
	public static void main(String[] args) throws Exception {
		//		Tiff tiff1 = new Tiff ("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Uygar.TIF");
		//		Tiff tiff2 = new Tiff ("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF");
		//		checkCompatibility(tiff1, tiff2);
		
		System.gc();
		warpingError();
	}

	public static void warpingError () { //Source: http://fiji.sc/Topology_preserving_warping_error
		// original labels
		ImagePlus originalLabels = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Uygar.TIF");

		// proposed (new) labels
		ImagePlus proposedLabels = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF");
		
		// mask with geometric constraints
		ImagePlus mask = IJ.openImage("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Bandy.TIF");

		// threshold to binarize labels (just in case they are not binary)
		double threshold = 0.5;

		WarpingError metric = new WarpingError( originalLabels, proposedLabels, mask );
		
		System.gc();
		double warpingError = metric.getMetricValue( threshold );

		IJ.log("Warping error between source image " + originalLabels.getTitle() + " and target image " + proposedLabels.getTitle() + " = " + warpingError);
	}

	public static byte[][] pixelError (Tiff tiff1, Tiff tiff2) throws Exception {
		int numPages = tiff1.getNumPages();
		int numPixels = tiff1.getPixels(0).length;

		byte[][] fullOutput = new byte[numPages][numPixels];

		for (int page = 0; page < numPages; page++) {
			System.out.println(page);
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
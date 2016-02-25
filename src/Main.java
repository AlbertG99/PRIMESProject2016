public class Main {
	public static void main(String[] args) throws Exception {
		Tiff uygar = new Tiff ("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Uygar.TIF");
		Tiff bandy = new Tiff ("/Users/Albert/Dropbox/Google Drive/Boyden PRIMES/Example/Brandy.TIF");
		checkCompatibility(uygar, bandy);
				
//		int numPages = uygar.getNumPages();
		byte[] uygarPixels = uygar.getPixels(0);
		byte[] bandyPixels = bandy.getPixels(0);
		
		int numPixels = uygarPixels.length;
		int uygarPix;
		int bandyPix;
		
		for (int i = 0; i < numPixels; i++) {
			uygarPix = uygarPixels[i];
			bandyPix = bandyPixels[i];
			if (uygarPix != bandyPix) {
				System.out.println(i + " | " + uygarPix + " | " + bandyPix);
			}
		}
	}
	
	public static void checkCompatibility (Tiff tiff1, Tiff tiff2) throws Exception {
		int numPages = tiff1.getNumPages();
		if (numPages != tiff2.getNumPages()) {
			throw new Exception("TIFF number of pages do not match.");
		}
		int tiff1Height = tiff1.getHeight(); int tiff2Height = tiff2.getHeight();
		if (tiff1Height != tiff2Height) {
			throw new Exception("TIFF heights do not match.");
		}
		int tiff1Width = tiff1.getWidth(); int tiff2Width = tiff2.getWidth();
		if (tiff1Width != tiff2Width) {
			throw new Exception("TIFF widths do not match.");
		}
	}
}
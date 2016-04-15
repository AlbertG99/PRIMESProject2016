package primesproject;
import ij.ImagePlus;
import ij.ImageStack;
import ij.process.StackProcessor;

public class HelperMethods {

	// ArrayToString

	public static String arrayToString (byte[] array) { // Converts array of bytes to String
		String output = "" + array[0];

		for (int i = 1; i < array.length; i++) {
			if (i % 10000 == 0) {
				System.out.println(i);
			}
			output = output + array[i];
		}

		return output;
	}

	public static String arrayToString (byte[] array, int n) { // Converts array of bytes to String with n terms
		String output = "" + array[0];

		for (int i = 1; i < n; i++) {
			output = output + ", " + array[i];
		}

		return output;
	}

	public static String arrayToString (String[] array) { // Converts array of Strings to String
		String output = "" + array[0];

		for (int i = 1; i < array.length; i++) {
			output = output + array[i];
		}

		return output;
	}
	
	// Rotate ImagePlus
	
	public static ImagePlus rotate (ImagePlus original) {
		StackProcessor stackProcessor = new StackProcessor(original.getStack());
		ImageStack stack = stackProcessor.rotateLeft();
		ImagePlus toReturn = new ImagePlus("Final", stack);
		
		return toReturn;
	}
}
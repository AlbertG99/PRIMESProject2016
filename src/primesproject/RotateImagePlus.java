package primesproject;
import java.awt.Rectangle;

import ij.ImagePlus;
import ij.ImageStack;
import ij.gui.Roi;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageProcessor;
import ij.process.ShortProcessor;

public class RotateImagePlus {

	// Rotate ImageStack
	public void rotateAll(ImagePlus imp, Roi roi, double theta) {
		Roi all = new Roi(0, 0, imp.getWidth(), imp.getHeight());	
		imp.setRoi(all);

		int interpolationMethod=ImageProcessor.BICUBIC;

		int n=imp.getStackSize();
		if (n==1) {

			ImageProcessor ip = imp.getProcessor();
			ip.setInterpolationMethod(interpolationMethod);

			rotate(theta, ip, roi);
		} else {
			ImageStack is=imp.getStack();
			int width=is.getWidth();
			int height=is.getHeight();
			ImageStack isnew=new ImageStack(width, height);
			for (int i=0; i<n; i++) {

				ImageProcessor ip =is.getProcessor(i+1);
				ip.setInterpolationMethod(interpolationMethod);

				rotate(theta, ip, roi);

				isnew.addSlice(""+1, ip);
			}
			imp.setStack(isnew);
			is=null;
		}
		imp.updateAndDraw();		
		//imp.setRoi(roi, true);
	}

	public void rotate(double angle, ImageProcessor ip, Roi roi) {	
		Rectangle r=roi.getBounds();
		if (ip instanceof FloatProcessor)
			rotateF(angle, (FloatProcessor)ip, r);
		if (ip instanceof ByteProcessor)
			rotateB(angle, (ByteProcessor)ip, r);
		if (ip instanceof ShortProcessor)
			rotateS(angle, (ShortProcessor)ip, r);
	}

	/** Rotates the image or ROI 'angle' degrees clockwise.
	 */
	public void rotateF(double angle, FloatProcessor fp, Rectangle r) {
		System.out.println("rotating float...");
		float[] pixels = (float[])fp.getPixelsCopy();	 

		double centerX = r.x + (r.width)/2.0;
		double centerY = r.y + (r.height)/2.0;


		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index=0;
		int width=fp.getWidth();
		int height=fp.getHeight();

		for (int y=0; y<height; y++) {

			tmp3 = tmp1 - y*sa + centerX;
			tmp4 = tmp2 + y*ca + centerY;
			for (int x=0; x<width; x++) {
				index = y*width + x;
				xs = x*ca + tmp3;
				ys = x*sa + tmp4;
				pixels[index++] = (float)fp.getInterpolatedPixel(xs, ys);
			}			 
		}

		fp.setPixels(pixels);
	}

	/** Rotates the image or ROI 'angle' degrees clockwise.
	 */
	public void rotateB(double angle, ByteProcessor bp, Rectangle r) {
		System.out.println("rotating byte...");
		byte[] pixels = (byte[])bp.getPixelsCopy();


		double centerX = r.x + (r.width)/2.0;
		double centerY = r.y + (r.height)/2.0;


		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index=0;
		int width=bp.getWidth();
		int height=bp.getHeight();

		for (int y=0; y<height; y++) {

			tmp3 = tmp1 - y*sa + centerX;
			tmp4 = tmp2 + y*ca + centerY;
			for (int x=0; x<width; x++) {
				index = y*width + x;
				xs = x*ca + tmp3;
				ys = x*sa + tmp4;
				pixels[index++] = (byte)bp.getInterpolatedPixel(xs, ys);
			}			 
		}

		bp.setPixels(pixels);

	}

	/** Rotates the image or ROI 'angle' degrees clockwise.
	@see ImageProcessor#setInterpolate
	 */
	public void rotateS(double angle, ShortProcessor sp, Rectangle r) {
		System.out.println("rotating float...");
		short[] pixels = (short[])sp.getPixelsCopy();


		double centerX = r.x + (r.width)/2.0;
		double centerY = r.y + (r.height)/2.0;


		double angleRadians = -angle/(180.0/Math.PI);
		double ca = Math.cos(angleRadians);
		double sa = Math.sin(angleRadians);
		double tmp1 = centerY*sa-centerX*ca;
		double tmp2 = -centerX*sa-centerY*ca;
		double tmp3, tmp4, xs, ys;
		int index=0;
		int width=sp.getWidth();
		int height=sp.getHeight();

		for (int y=0; y<height; y++) {

			tmp3 = tmp1 - y*sa + centerX;
			tmp4 = tmp2 + y*ca + centerY;
			for (int x=0; x<width; x++) {
				index = y*width + x;
				xs = x*ca + tmp3;
				ys = x*sa + tmp4;
				pixels[index++] = (short)sp.getInterpolatedPixel(xs, ys);
			}			 
		}

		sp.setPixels(pixels);
	}
}
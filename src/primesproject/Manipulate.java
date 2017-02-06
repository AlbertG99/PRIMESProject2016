package primesproject;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.apache.commons.io.FileUtils;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.NewImage;
import ij.io.FileSaver;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;
import ij.process.Blitter;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.WarpingResults;

public class Manipulate {
	public static void main(String[] args) throws Exception {
		// Initial parameters
		String dataRoot = "/Users/Albert/Google Drive/Boyden PRIMES/Data/simulation7 results/";
		String segRoot = "/Users/Albert/Google Drive/Boyden PRIMES/Others' Programs/working core/";
		String error = "Warping Error";
		Integer[] sigma = {500, 1000, 1500, 2000, 2500};
		Integer[] spatialDistanceUpperBound = {25};
		String xVal = "sigma";
		String graphFilename = "LineChart";
		
		// Generate filepaths
		String rawPath = dataRoot + "simulation2.tif";
		String rawPathOut = dataRoot + "simulation2_fixed.tif";
		String gtPath = dataRoot + "simulation2_gt.tif";
		String gtPathOut = dataRoot + "simulation2_gt_fixed.tif";
		String segmentationOut = segRoot + "simulation2_fixed_Segmentation_/simulation2_fixed_sig500_HMINTH0.01_sUB31_cUB0.1_detTh5e-11_subdivTh10_minVox10_cc30.tiff";
		String segmentationFilled = dataRoot + "segmentation.tif";
				
		// Create progress bar
		ProgressBar pBar = new ProgressBar();
		pBar.setTitle("Running SEV-3D...");
		
		// Manipulate TIFF files
		pBar.setLabel("Manipulating TIFFs...");
		manipGT(gtPath, gtPathOut);
		manipRaw(rawPath, rawPathOut);
		
		// Set up variables
		DefaultCategoryDataset dataset = new DefaultCategoryDataset();
		double lowestError = 1.0;
		int optimalSigma = sigma[0];
		int optimalsUB = spatialDistanceUpperBound[0];
		
		// Run each parameter combination
		MatlabProxy proxy;
		for (int sigmaI : sigma) {
			editLine(60, segRoot, "    sigma                                                    = " + sigmaI + ";        % 2000 TRY THIS ONE, between 1000-3000, increments of 1000, try 500, 1500, 2500");
			for (int spatialDistanceUpperBoundI : spatialDistanceUpperBound) {				
				editLine(63, segRoot, "    superVoxelOpts.spatialDistanceUpperBound                 = " + spatialDistanceUpperBoundI + ";         % orig at 31, lower=faster, try 10-15, big impact on runtime (in calculate_sAff) TRY THIS ONE, 10-15");
				
				// Run algorithm
				pBar.setLabel("Running algorithm...");
				pBar.setNote("sigma = " + sigmaI + ", spatialDistanceUpperBound = " + spatialDistanceUpperBoundI);
				
				proxy = getProxy();
				try {
					runAlgorithm(proxy, rawPathOut, segRoot);
				}
				catch (Exception e) {
					System.out.println("Ran into an error with current parameters. Skipping this combination. (" + "sigma = " + sigmaI + ", spatialDistanceUpperBound = " + spatialDistanceUpperBoundI + ")");
					continue;
				}
				
				// Update filenames to current parameters
				String segmentationOutI = segmentationOut.replace("sig500", "sig" + sigmaI).replace("sUB31", "sUB" + spatialDistanceUpperBoundI);
				String segmentationFilledI = segmentationFilled.substring(0, segmentationFilled.length() - 4) + "_" + sigmaI + "_" + spatialDistanceUpperBoundI + ".tif";
				
				// Fix segmentation if necessary
				if (!isCorrect(segmentationOutI)) {
					Scanner sc = new Scanner(System.in);
					System.out.print("File is incorrect. Fix it and then press Enter.");
					String str = sc.nextLine();
				}
				
				// Fill segmentation
				pBar.setLabel("Filling TIFF...");
				pBar.clearNote();
				fillTiff(proxy, segmentationOutI, segmentationFilledI);
				
				// Find error value
				double errorVal = run(pBar, gtPathOut, segmentationFilledI, error, dataRoot, true);
				System.out.println("errorVal (sigma = " + sigmaI + ", spatialDistanceUpperBound = " + spatialDistanceUpperBoundI + "): " + errorVal);
				
				// Add value to graph dataset
				if (xVal.equals("sigma"))
					dataset.addValue(errorVal, xVal, Double.toString(sigmaI));
				else if (xVal.equals("sUB"))
					dataset.addValue(errorVal, xVal, Double.toString(spatialDistanceUpperBoundI));
				
				// Update lowest error value
				if (errorVal < lowestError) {
					optimalSigma = sigmaI;
					optimalsUB = spatialDistanceUpperBoundI;
					lowestError = errorVal;
				}
				
				// Reset for next round
				FileUtils.deleteDirectory(new File(segRoot + "simulation2_fixed_Segmentation_"));
//				proxy.exit();
			}
		}
		
		// Create graph
		pBar.setLabel("Creating chart...");
		JFreeChart lineChart = ChartFactory.createLineChart(
		         "Error Value vs. " + xVal,
		         xVal, "Error Value",
		         dataset,
		         PlotOrientation.VERTICAL,
		         true, true, false);
		int width = 1080; /* Width of the image */
	    int height = 810; /* Height of the image */ 
	    File lineChartFile = new File(dataRoot + graphFilename + ".jpeg"); 
	    ChartUtilities.saveChartAsJPEG(lineChartFile,lineChart, width ,height);

	    // Display optimal segmentation
		ViewImage.view3DImage(segmentationFilled.substring(0, segmentationFilled.length() - 4) + "_" + optimalSigma + "_" + optimalsUB + ".tif", "Optimal Error: " + String.format("%3.3E", lowestError) + " (sigma = " + optimalSigma + ", spatialDistanceUpperBound = " + optimalsUB + ")");
		
		System.exit(0); // Exit application
	}
	
	public static void manipGT (String filepath, String outputFilepath) throws Exception {
		ImagePlus groundTruth = IJ.openImage(filepath);
		ImageConverter converter = new ImageConverter(groundTruth);
		converter.convertToGray8();
		fixCompression(groundTruth, outputFilepath);
	}
	
	public static void rename (String origName, String newName) {
		ImagePlus rawData = IJ.openImage(origName);
		saveImage(rawData, newName);
	}
	
	public static void manipRaw (String filepath, String outputFilepath) {
		ImagePlus rawData = IJ.openImage(filepath);
		ChannelSplitter splitter = new ij.plugin.ChannelSplitter();
		ImagePlus[] rawDataSplit = splitter.split(rawData);
		RGBStackMerge merger = new ij.plugin.RGBStackMerge();
		ImagePlus rawDataMerged = merger.mergeChannels(rawDataSplit, true);
		saveImage(rawDataMerged, outputFilepath);
	}
	
	public static boolean isCorrect (String filepath) throws IOException {
		double black = 0;
		double white = 0;
		BufferedImage image = ImageIO.read(new File (filepath));
		for (int y = 0; y < image.getHeight(); y++) {
		    for (int x = 0; x < image.getWidth(); x++) {
		          int  clr   = image.getRGB(x, y); 
		          int  red   = (clr & 0x00ff0000) >> 16;
		          int  green = (clr & 0x0000ff00) >> 8;
		          int  blue  =  clr & 0x000000ff;
		          if (red == 255) {
		        	  white++;
		          }
		          else {
		        	  black++;
		          }
		    }
		}
		System.out.println("White/black ratio: " + white/black);
		if (white/black > 1) {
			return false;
		}
		return true;
	}
	
	public static void fixImage (String filepath, String outputFilepath) throws IOException {
		if (!isCorrect(filepath)) {
			ImagePlus imageP = IJ.openImage(filepath);
			invertColors(imageP.getProcessor(), outputFilepath);
		}
	}
	
	public static void invertColors(ImageProcessor ip, String filepath) {

		// get width, height and the region of interest
		int w = ip.getWidth();     
		int h = ip.getHeight();
		int l = ip.getSliceNumber();
		Rectangle roi = ip.getRoi();

		// create a new image with the same size and copy the pixels of the original image
		ImagePlus inverted = NewImage.createRGBImage ("Inverted image", w, h, l, NewImage.FILL_BLACK);
		ImageProcessor inv_ip = inverted.getProcessor();
		inv_ip.copyBits(ip,0,0,Blitter.COPY);
		int[] pixels = (int[]) inv_ip.getPixels();

		// invert the pixels in the ROI
		for (int i=roi.y; i<roi.y+roi.height; i++) {
			int offset =i*w; 
			for (int j=roi.x; j<roi.x+roi.width; j++) {
				int pos = offset+j;
				int c = pixels[pos];
				int r = (c&0xff0000)>>16;
			int g = (c&0x00ff00)>>8;
			int b = (c&0x0000ff);
			r = 255-r;
			g=255-g;
			b=255-b;
			pixels[pos] = ((r & 0xff) << 16) + ((g & 0xff) << 8) + (b & 0xff);		
			}
		}
		saveImage(inverted, filepath);
	}
	
	public static void saveImage (ImagePlus image, String filepath) {
		FileSaver fileSaver = new ij.io.FileSaver(image);
		fileSaver.saveAsTiffStack(filepath);
	}
	
	public static void fixCompression (ImagePlus origImage, String outputFilepath) throws Exception {
		TIFFEncodeParam params = new TIFFEncodeParam();
		params.setCompression(TIFFEncodeParam.COMPRESSION_DEFLATE);
		
		BufferedImage[] pages = new BufferedImage[origImage.getNSlices()];
		for (int i = 0; i < pages.length; i++) {
//			System.out.println(origImage.getCurrentSlice());
			origImage.setPosition(i + 1);
			BufferedImage in = origImage.getBufferedImage();
			
			pages[i] = in;
		}
		
		OutputStream out = new FileOutputStream(outputFilepath); 
		ImageEncoder encoder = ImageCodec.createImageEncoder("tiff", out, params);
		Vector<BufferedImage> vector = new Vector<BufferedImage>();   
		for (int i = 1; i < pages.length; i++) {
		    vector.add(pages[i]);
		}
		params.setExtraImages(vector.iterator()); 
		encoder.encode(pages[0]);
		out.close();
	}
	
	public static MatlabProxy getProxy () throws MatlabConnectionException {
		MatlabProxyFactory factory = new MatlabProxyFactory();
		MatlabProxy proxy = factory.getProxy();
		return proxy;
	}
	
	public static void editLine (int lineNum, String segRoot, String newLine) throws IOException {
		String segFilepath = segRoot + "superVoxelize1.m";
		List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(segFilepath), StandardCharsets.UTF_8));
		newLine = newLine.replaceAll("'", "''");
		fileContent.set(lineNum, newLine);
		Files.write(Paths.get(segFilepath), fileContent, StandardCharsets.UTF_8);
	}
	
	public static void editImgFilePath (String rawDataPath, String segRoot) throws IOException {
		String segFilepath = segRoot + "superVoxelize1.m";
		List<String> fileContent = new ArrayList<>(Files.readAllLines(Paths.get(segFilepath), StandardCharsets.UTF_8));
		rawDataPath = rawDataPath.replaceAll("'", "''");
		fileContent.set(38, "imgFilePath = '" + rawDataPath + "';");
		Files.write(Paths.get(segFilepath), fileContent, StandardCharsets.UTF_8);
	}
	
	public static void runAlgorithm (MatlabProxy proxy, String rawDataPath, String segRoot) throws Exception {
		editImgFilePath(rawDataPath, segRoot);
		
		proxy.eval("cd ../");
		proxy.eval("cd 'Others'' Programs/working core'");
		try {
			proxy.eval("superVoxelize1");
		}
		catch (Exception e) {
			proxy.eval("addpath(genpath(pwd))");
			proxy.eval("superVoxelize1");
		}
	}
	
	public static void fillTiff (MatlabProxy proxy, String segmentationOut, String segmentationFilled) throws Exception {
		segmentationOut = segmentationOut.replaceAll("'", "''");
		segmentationFilled = segmentationFilled.replaceAll("'", "''");
		proxy.eval("filename = '" + segmentationOut + "';");
		proxy.eval("outputFilename = '" + segmentationFilled + "';");
		proxy.eval("im = imread(filename);");
		proxy.eval("info = imfinfo(filename);");
		proxy.eval("numImages = numel(info);");
		proxy.eval("A = cell(numImages);");
		proxy.eval("for k = 1:(numImages);page = imread(filename, k);st = strel('disk', 5);closed = imclose(page, st);filled = imfill(closed, 'holes');imwrite(filled, outputFilename, 'WriteMode', 'append', 'Compression', 'none');end;");
	}
	
	public static double run (ProgressBar pBar, String originalLabelsPath, String proposedLabelsPath, String error, String outPath, boolean justReturn) throws Exception {
		boolean createPixelImage = true;
		String graphPath = outPath + error.toLowerCase().replaceAll(" ", "_") + ".tif";
		
		double errorVal = 0.0;
		if (error.equals("Pixel Error")) { // Run pixel error
//			pBar.setTitle("Running pixel error...");
			pBar.setLabel("Getting TIFFs...");
			Tiff originalLabels = new Tiff(originalLabelsPath);
			Tiff proposedLabels = new Tiff(proposedLabelsPath);
			byte[][] fullOutput = Errors.pixelErrorArray(originalLabels, proposedLabels);
			pBar.setLabel("Calculating error...");
			errorVal = Errors.pixelError(originalLabels.getNumPages(), originalLabels.getNumPixels(), fullOutput);
			if (justReturn) {
				return errorVal;
			}
			else if (createPixelImage) {
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
//			pBar.setTitle("Running rand error...");
			pBar.setLabel("Getting TIFFs...");
			ImagePlus originalLabels = IJ.openImage(originalLabelsPath);
			ImagePlus proposedLabels = IJ.openImage(proposedLabelsPath);
			pBar.setLabel("Calculating error...");
			errorVal = Errors.adjustedRandError(originalLabels, proposedLabels);
			pBar.setVisible(false);
			UserInterface.showPopupText("The " + error.toLowerCase() + " is: " + errorVal, true);
		}
		else if (error.equals("Warping Error")) { // Run warping error
//			pBar.setTitle("Running warping error...");
			pBar.setLabel("Getting TIFFs...");
			ImagePlus originalLabels = IJ.openImage(originalLabelsPath);
			ImagePlus proposedLabels = IJ.openImage(proposedLabelsPath);
			pBar.setLabel("Creating mask...");
			ImagePlus mask = ViewImage.create3DBlackImage(1000, 1000, originalLabels.getImageStackSize());
			pBar.setLabel("Calculating error...");
			WarpingResults[] wrs = Errors.warpingError(originalLabels, proposedLabels, mask, pBar);
			errorVal = WarpingError.getMetricValue(wrs);
			if (justReturn) {
				return errorVal;
			}
			else if (createPixelImage) {
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
		return errorVal;		
	}
}
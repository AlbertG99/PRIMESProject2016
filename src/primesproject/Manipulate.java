package primesproject;

import java.awt.image.BufferedImage;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import com.sun.media.jai.codec.ImageCodec;
import com.sun.media.jai.codec.ImageEncoder;
import com.sun.media.jai.codec.TIFFEncodeParam;

import ij.IJ;
import ij.ImagePlus;
import ij.io.FileSaver;
import ij.plugin.ChannelSplitter;
import ij.plugin.RGBStackMerge;
import ij.process.ImageConverter;
import matlabcontrol.MatlabConnectionException;
import matlabcontrol.MatlabProxy;
import matlabcontrol.MatlabProxyFactory;
import trainableSegmentation.metrics.WarpingError;
import trainableSegmentation.metrics.WarpingResults;

public class Manipulate {
	public static void main(String[] args) throws Exception {
		String dataRoot = "/Users/Albert/Google Drive/Boyden PRIMES/Data/simulation6 results/";
		String segRoot = "/Users/Albert/Google Drive/Boyden PRIMES/Others' Programs/working core/";
		String gtPath = dataRoot + "simulation2_gt.tif";
		String gtPathOut = dataRoot + "simulation2_gt_fixed.tif";
		String rawPath = dataRoot + "simulation2.tif";
		String rawPathOut = dataRoot + "simulation2_fixed.tif";
		String segmentationOut = segRoot + "simulation2_fixed_Segmentation_/simulation2_fixed_sig500_HMINTH0.01_sUB31_cUB0.1_detTh5e-11_subdivTh10_minVox10_cc30.tiff";
		String segmentationFilled = dataRoot + "segmentation.tif";
		
		String[] errors = {"Pixel Error", "Warping Error"};
		
		System.out.println("Manipulating original TIFFs...");
		manipGT(gtPath, gtPathOut);
		manipRaw(rawPath, rawPathOut);
		
		System.out.println("Running algorithm...");
		MatlabProxy proxy = getProxy();
		runAlgorithm(proxy, rawPathOut, segRoot);
		
		System.out.println("Filling TIFF...");
		fillTiff(proxy, segmentationOut, segmentationFilled);
		
		System.out.println("Running metric...");
		for (String error : errors) {
			run(gtPathOut, segmentationFilled, error, dataRoot);
		}
		
		proxy.disconnect();
		
		System.exit(0); // Exit application
	}
	
	public static void manipGT (String filepath, String outputFilepath) throws Exception {
		ImagePlus groundTruth = IJ.openImage(filepath);
		ImageConverter converter = new ImageConverter(groundTruth);
		converter.convertToGray8();
		fixCompression(groundTruth, outputFilepath);
	}
	
	public static void manipRaw (String filepath, String outputFilepath) {
		ImagePlus rawData = IJ.openImage(filepath);
		ChannelSplitter splitter = new ij.plugin.ChannelSplitter();
		ImagePlus[] rawDataSplit = splitter.split(rawData);
		RGBStackMerge merger = new ij.plugin.RGBStackMerge();
		ImagePlus rawDataMerged = merger.mergeChannels(rawDataSplit, true);
		saveImage(rawDataMerged, outputFilepath);
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
	
	public static void run (String originalLabelsPath, String proposedLabelsPath, String error, String outPath) throws Exception {
		boolean createPixelImage = true;
		String graphPath = outPath + error.toLowerCase().replaceAll(" ", "_") + ".tif";
		
		// Create progress bar
		ProgressBar pBar = new ProgressBar();
		
		double errorVal = 0.0;
		if (error.equals("Pixel Error")) { // Run pixel error
			pBar.setTitle("Running pixel error...");
			pBar.setLabel("Getting TIFFs...");
			Tiff originalLabels = new Tiff(originalLabelsPath);
			Tiff proposedLabels = new Tiff(proposedLabelsPath);
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
	}
}
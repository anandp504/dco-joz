package com.tumri.joz.utils;

import com.tumri.joz.index.creator.JozIndexUpdater;
import com.tumri.joz.products.JozIndexHelper;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

/**
 *  Overview:
 *  This program takes in commandLine args that specify possibly 5 things.
 *
 *  You can specify the path location of those bin files.
 *  To specify the directory where the bin files are located use the following format: -binLoc /bin/bin2
 *  If no bin directory is given the default directory will be /opt/Tumri/joz/data/caa/current/jozindex
 *
 *  You can enter 0 or more *.bin files that you wish to include in the data collection.
 *  To specify a *.bin file use the following format: -binFile hello.bin
 *  If no bin Files are specified, data will be colledted across all *.bin files located in binLoc
 *
 *  You can also enter 0 or more product ids that you wish to include in the data collection
 *  To specify a productId number use the following format: prodId 123456
 *  If no productIds are specified, data will be collected across all productIDs
 *
 *  You can also specify what directory to save the collected data to
 *  To specify the save directory use the following format: -saveDir /tmp/tmp2/tmp3/...etc
 *  If no saveDir is specified, data will be saved to /tmp
 *
 *  You can specify what file you want to save the collected data to.
 *  To specify the save file use the following format: -saveFile myDebugFile.txt
 *  If no saveFile is specified, data will be saved to jozIndexDebugFile.txt
 *
 *  There is no paticular order that needs to be followed.
 *
 *  The default save directory is /tmp and the default save file is jozIndexDebugFile.txt
 *
 *
 *  SAMPLE COMMANDLINE:
 *      -saveDir /tmp/tmp2 -saveFile tmp.txt -binFile hello.bin -binFile bye.bin -binLoc /bin/bin2 -prodId 123456 -prodId 654321
 *
 *  *Note: in the above example the order should of the args should not matter, as long as there is a
 *  space in between each argument passed through the commandLine
 *
 * @author: scbraun
 * Date: 07/03/08
 */

public class IndexDebugUtils {
	protected static Logger log = Logger.getLogger(JozIndexHelper.class);
	protected static StringBuffer myDebugBuff = new StringBuffer();


	public IndexDebugUtils(){
		System.out.println("Initiating IndexDebugUtils");

	}

	/**
	 * given a set of command line arguments executes the data collection and saves it to a file
	 * @param args CommandLine set of arguments
	 */
	public void execute(String args[]){

		String writeFile = new String();
		String writeDir = new String();
		String binLoc = new String();
		/**
		 *  myPids is an ArrayList of product ids of which we wish to collect data.
		 *  If zero ids are inputed through the command Line then we include all product ids
		 *  in our data collection process.
		 */
		ArrayList<Long> myPids = new ArrayList<Long>();
		/**
		 *  myBinFiles is an ArrayList of *.bin files from which we wish to generate data.
		 *  If zero files are inputed through the command Line then we include all *.bin files
		 *  in our data collection process.
		 */
		ArrayList<String> myBinFiles = new ArrayList<String>();
		String saveDirFlag = "-saveDir";
		String saveFileFlag = "-saveFile";
		String binFilesFlag = "-binFile";
		String binLocationFlag = "-binLoc";
		String prodIdsFlag = "-prodId";

		for(int i = 0; i < args.length; i++){
			if(saveDirFlag.equals(args[i])){
				writeDir = args[++i];
			} else if(saveFileFlag.equals(args[i])){
				writeFile = args[++i];
			} else if(binFilesFlag.equals(args[i])){
				myBinFiles.add(args[++i]);
			} else if(binLocationFlag.equals(args[i])){
				binLoc = args[++i];
			} else if(prodIdsFlag.equals(args[i])){
				myPids.add(new Long(args[++i]));
			} else {
				System.out.println("-saveDir /tmp/tmp2 -saveFile tmp.txt -binFile hello.bin -binFile bye.bin -binLoc /bin/bin2 -prodId 123456 -prodId 654321");
				return ;
			}
		}


		JozIndexUpdater.setInstance(true, true, myPids);

		collect(writeDir, writeFile, myBinFiles, binLoc, myPids);
	}

	/**
	 * given a command line set of arguments returns the collected StringBuffer
	 * @param args commandLine set of arguments
	 * @return StringBuffer
	 */
	public StringBuffer returnBuffer(String args[]){

		String writeFile = new String();
		String writeDir = new String();
		String binLoc = new String();
		/**
		 *  myPids is an ArrayList of product ids of which we wish to collect data.
		 *  If zero ids are inputed through the command Line then we include all product ids
		 *  in our data collection process.
		 */
		ArrayList<Long> myPids = new ArrayList<Long>();
		/**
		 *  myBinFiles is an ArrayList of *.bin files from which we wish to generate data.
		 *  If zero files are inputed through the command Line then we include all *.bin files
		 *  in our data collection process.
		 */
		ArrayList<String> myBinFiles = new ArrayList<String>();
		String saveDirFlag = "-saveDir";
		String saveFileFlag = "-saveFile";
		String binFilesFlag = "-binFile";
		String binLocationFlag = "-binLoc";
		String prodIdsFlag = "-prodId";

		for(int i = 0; i < args.length; i++){
			if(saveDirFlag.equals(args[i])){
				writeDir = args[++i];
			} else if(saveFileFlag.equals(args[i])){
				writeFile = args[++i];
			} else if(binFilesFlag.equals(args[i])){
				myBinFiles.add(args[++i]);
			} else if(binLocationFlag.equals(args[i])){
				binLoc = args[++i];
			} else if(prodIdsFlag.equals(args[i])){
				myPids.add(new Long(args[++i]));
			} else {
				System.out.println("-saveDir /tmp/tmp2 -saveFile tmp.txt -binFile hello.bin -binFile bye.bin -binLoc /bin/bin2 -prodId 123456 -prodId 654321");
				return new StringBuffer("Formating Example: -saveDir /tmp/tmp2 -saveFile tmp.txt -binFile hello.bin -binFile bye.bin -binLoc /bin/bin2 -prodId 123456 -prodId 654321");
			}
		}


		JozIndexUpdater.setInstance(true, true, myPids);

		if(binLoc.length()>0){
			if (!new File(binLoc).exists()) {
				boolean success = new File(binLoc).mkdirs();
			}
			JozIndexHelper.loadIndex(binLoc, myBinFiles);
		} else {
			JozIndexHelper.loadIndex("/opt/Tumri/joz/data/caa/current/jozindex", myBinFiles);
		}
		return JozIndexUpdater.getBuffer();

	}

	public static void main(String args[]) {
		System.out.println("Initiating IndexDebugUtils");
		String writeFile = new String();
		String writeDir = new String();
		String binLoc = new String();
		/**
		 *  myPids is an ArrayList of product ids of which we wish to collect data.
		 *  If zero ids are inputed through the command Line then we include all product ids
		 *  in our data collection process.
		 */
		ArrayList<Long> myPids = new ArrayList<Long>();
		/**
		 *  myBinFiles is an ArrayList of *.bin files from which we wish to generate data.
		 *  If zero files are inputed through the command Line then we include all *.bin files
		 *  in our data collection process.
		 */
		ArrayList<String> myBinFiles = new ArrayList<String>();


		String saveDirFlag = "-saveDir";
		String saveFileFlag = "-saveFile";
		String binFilesFlag = "-binFile";
		String binLocationFlag = "-binLoc";
		String prodIdsFlag = "-prodId";

		for(int i = 0; i < args.length; i++){
			if(saveDirFlag.equals(args[i])){
				writeDir = args[++i];
			} else if(saveFileFlag.equals(args[i])){
				writeFile = args[++i];
			} else if(binFilesFlag.equals(args[i])){
				myBinFiles.add(args[++i]);
			} else if(binLocationFlag.equals(args[i])){
				binLoc = args[++i];
			} else if(prodIdsFlag.equals(args[i])){
				myPids.add(new Long(args[++i]));
			} else {
				System.out.println("-saveDir /tmp/tmp2 -saveFile tmp.txt -binFile hello.bin -binFile bye.bin -binLoc /bin/bin2 -prodId 123456 -prodId 654321");
				return;
			}
		}


		JozIndexUpdater.setInstance(true, true, myPids);

		collect(writeDir, writeFile, myBinFiles, binLoc, myPids);

		//TODO: Validate the file.

	}

	/**
	 * Public method to collect data specified by the parameters
	 * @param saveDir the directory to which the collected data will be saved
	 * @param saveFile the file name under which the collected data will be saved
	 * @param binFiles a list of *.bin files to collect data from
	 * @param binLoc the directory where the *.bin files are located
	 * @param prodIds a list of productIds to collect data from
	 */
	public static void collect(String saveDir, String saveFile, ArrayList<String> binFiles, String binLoc, ArrayList<Long> prodIds){
		JozIndexUpdater.setInstance(true, true, prodIds);
		if(binLoc.length()>0){
			if (!new File(binLoc).exists()) {
				boolean success = new File(binLoc).mkdirs();
			}
			JozIndexHelper.loadIndex(binLoc, binFiles);
		} else {
			JozIndexHelper.loadIndex("/opt/Tumri/joz/data/caa/current/jozindex", binFiles);
		}
		myDebugBuff = JozIndexUpdater.getBuffer();
		saveToFileandDir(saveDir, saveFile);
	}

	/**
	 * 	contains logic to determin which dir and file should be used for output
	 */
	private static void saveToFileandDir(String dir, String file){
		if(dir.length() > 0){
			if(file.length() > 0){ //both file and dir given
				writeToFile(dir, "/" + file);
			} else { //only dir given
				writeToFile(dir, "/jozIndexDebugFile.txt");
			}
		} else {
			if (file.length() >0){ //only file given
				writeToFile("/tmp", "/" + file);
			} else { //neither given
				writeToFile("/tmp", "/jozIndexDebugFile.txt");
			}
		}
	}
	/**
	 *contains actual File operations save a specific file at a specific directory
	 */
	private static void writeToFile(String writeDir, String writeFile){
		File debugOutFile = null;
		File debugDir = new File(writeDir);

		if (!debugDir.exists()) {
			boolean success = debugDir.mkdirs();
		}

		debugOutFile = new File(writeDir + writeFile);

		if (debugOutFile.exists()) {
			debugOutFile.delete();
		}

		FileWriter fw = null;

		try{
			fw = new FileWriter(debugOutFile, true);
			fw.write(myDebugBuff.toString());
		} catch (IOException e) {
			log.error("Could not write to debug file", e);
		} finally {
			try {
				fw.close();
			} catch(Exception e) {
				System.err.println("Error: " + e.getMessage());
			}
		}
		assert(new File(writeDir + writeFile).exists());
	}

}

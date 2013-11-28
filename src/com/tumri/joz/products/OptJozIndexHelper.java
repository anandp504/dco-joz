package com.tumri.joz.products;

import com.tumri.content.InvalidConfigException;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.jic.IndexCreationException;
import com.tumri.jic.JICProperties;
import com.tumri.jic.joz.IJozIndexUpdater;
import com.tumri.jic.joz.PersistantProviderIndex;
import com.tumri.joz.campaign.wm.loader.WMLoaderException;
import com.tumri.joz.index.updater.OptJozIndexUpdater;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexLoadingComparator;
import com.tumri.joz.utils.LogUtils;
import com.tumri.utils.FSUtils;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;
import java.util.concurrent.Semaphore;
import com.tumri.joz.OptListingLoaderException;

/**
 * User: scbraun
 * Date: 10/1/13
 *
 * this class should have methods similar to JozIndexHelper
 */
public class OptJozIndexHelper {
	private String JOZ_INDEX_FILE_PATTERN = ".*_.*_optjozindex\\.bin";
	private String indexDirName = "/opt/Tumri/joz/data/caa/opt/current";
	private static Logger log = Logger.getLogger(OptJozIndexHelper.class);
	private static OptJozIndexHelper inst = null;

	public static OptJozIndexHelper getInstance() {
		if (inst == null) {
			synchronized (OptJozIndexHelper.class) {
				if (inst == null) {
					inst = new OptJozIndexHelper();
				}
			}
		}
		return inst;
	}

	private OptJozIndexHelper() {
		init();
	}

	/**
	 * Load the Joz index from the default dir that is set in the joz.properties
	 * This will load all the joz index files for all advertisers
	 */
	public synchronized boolean loadJozIndex(boolean debug) {
        Throwable th=null;
		try {
			log.info("Starting to load the Listing opt Joz indexes for all advertisers.");
			Date start = new Date();
			//Look for any Joz index files
			List<File> indexFiles = getSortedOptJozIndexFileList(indexDirName);
			boolean bErrors = false;
			try {
				readFromSerializedFile(indexFiles, debug, null, true);
			} catch (Exception e) {
				log.error("Exception caught on loading the index files : ", e);
				bErrors = true;
			}

			if (!bErrors) {
				log.info("Finished loading the Joz indexes");
                ListingOptContentProviderStatus.getInstance().lastSuccessfulRefreshTime = System.currentTimeMillis();
			} else {
				log.info("Finished loading the Joz indexes, with errors");
                ListingOptContentProviderStatus.getInstance().lastErrorRunTime = System.currentTimeMillis();
			}
			log.info(((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes");
            return true;
		} catch (Throwable t) {
            th = t;
            log.error("Joz index load failed.", th);
            ListingOptContentProviderStatus.getInstance().lastErrorRunTime = System.currentTimeMillis();
            ListingOptContentProviderStatus.getInstance().lastError = th;
            return false;
        }
	}
	/**
	 * Load the index for a given set of Bin files. This is used by the console utlity
	 *
	 * @param idxDir
	 * @param myBinFiles
	 */
	public StringBuffer loadIndexForDebug(String idxDir, ArrayList<String> myBinFiles, ArrayList<Long> prods) {
		IJozIndexUpdater updater = new OptJozIndexUpdater();
		updater.setProdIds(prods);
		//Set JIC in debug mode
		Properties props = new Properties();
		props.setProperty("com.tumri.jic.debug", "true");
		props.setProperty("com.tumri.jic.hotload", "false");
		try {
			JICProperties.init(props);
		} catch (IndexCreationException e) {
			log.error("Could not initialize the JIC properties", e);
		}
		if (myBinFiles != null && myBinFiles.size() > 0) {
			loadJozIndexFilesForDebug(idxDir, myBinFiles, updater);
		} else {
			loadJozIndexFilesForDebug(idxDir, null, updater);
		}
		return updater.getBuffer();
	}

	/**
	 * Load the specific set of Joz Index Files
	 */
	private void loadJozIndexFilesForDebug(String dirName, List<String> fileNames, IJozIndexUpdater updater) {
		try {
			log.info("Starting to load the specified Joz indexes.");
			List<File> idxFiles = null;
			if (fileNames != null && fileNames.size() > 0) {
				idxFiles = new ArrayList<File>();
				for (String indexFileName : fileNames) {
					File indexFile = new File(dirName + "/" + indexFileName);
					if (indexFile.exists()) {
						idxFiles.add(indexFile);
					} else {
						log.error("Specified file does not exist - cannot load : " + indexFile);
					}
				}
			} else {
				idxFiles = getSortedOptJozIndexFileList(dirName);
			}
			readFromSerializedFile(idxFiles, true, updater, false);
			log.info("Finished loading the Joz indexes");
		} catch (Exception e) {
			log.error("Joz index load failed.", e);
		}
	}
	/**
	 * Gets the current list of Joz index file in the Dir specified in indexDirName
	 *
	 * @return
	 */
	private List<File> getSortedOptJozIndexFileList(String dirName) throws  OptListingLoaderException{

		List<File> indexFiles = new ArrayList<File>();
		File indexDir = new File(dirName);
		if (!indexDir.exists()) {
			log.error("Directory does not exist : " + dirName);
            throw  new OptListingLoaderException("Directory not available: "+dirName);
		}
        Throwable t=null;
		FSUtils.findFiles(indexFiles, indexDir, JOZ_INDEX_FILE_PATTERN);
		if (indexFiles.size() == 0) {
			log.error("No joz index files found in directory: " + indexDir.getAbsolutePath());
            throw new OptListingLoaderException("No opt index files found to load");
		}

		return indexFiles;
	}

	/**
	 * Helper method to read the index from a file.
	 *
	 * @param inFile
	 */
	private void readFromSerializedFile(List<File> inFiles, boolean debugMode, IJozIndexUpdater updater,
	                                    boolean copy) throws IOException, ClassNotFoundException, InvalidConfigException {
		if (updater == null) {
			updater = new OptJozIndexUpdater();
		}

		//todo: get list of all advertisers
		//todo: for each advertiser, get list of all experiences
		//todo: for each advertiser+experience, get list of file names
		//todo: copy (perhaps mv since this would be nearly instantaneous) files to temporary location
		Map<String, Map<Integer,List<File>>> provToFilesMap = getProvidersFromFileNames(inFiles);
		Set<String> tmpProviderSet = provToFilesMap.keySet();

		if(tmpProviderSet!=null && !tmpProviderSet.isEmpty()){
			Set<String> providerSet = new SortedArraySet<String>(tmpProviderSet);
			List<Integer> validExperienceIdList = new ArrayList<Integer>();
			for(String providerName: providerSet){
				if(providerName!=null){
					Map<Integer,List<File>> experienceIdToFiles = provToFilesMap.get(providerName);
					if(experienceIdToFiles != null && !experienceIdToFiles.isEmpty()){
						Set<Integer> experienceIdSet = experienceIdToFiles.keySet();
						List<Integer> expIdList = new ArrayList<Integer>(experienceIdSet);
						Collections.sort(expIdList);
						validExperienceIdList.addAll(expIdList);
						for(Integer experienceId: expIdList){
							ProductDB.getInstance().deleteAllOptIndexesForExperience(experienceId); //delete all indexes for this experience before loading latest
							List<File> provInFiles = experienceIdToFiles.get(experienceId);
							if(provInFiles!=null && !provInFiles.isEmpty()){
								boolean error = false;
								Throwable e = null;
								((OptJozIndexUpdater)updater).setExperienceId(experienceId);
								for(File provInFile: provInFiles){
									log.info("Going to load the index from file : " + provInFile.getAbsolutePath());
									long startTime = System.currentTimeMillis();
									FileInputStream fis = null;
									ObjectInputStream in = null;
									try {
										JICProperties.init(debugMode, false, updater);
										fis = new FileInputStream(provInFile);
										in = new ObjectInputStream(new BufferedInputStream(fis));
										in.readObject(); //readObject uses JICProperties updater rather than reconstructing serialized object
										in.close();
									} catch (IOException ex) {
										error = true;
										e = ex;
										log.error("Could not load index file: " + provInFile.getAbsolutePath());
									} catch (ClassNotFoundException ex) {
										error = true;
										e = ex;
										log.error("Deserialization failed from file: " + provInFile.getAbsolutePath());
									} catch (Throwable t) {
										error = true;
										e = t;
										LogUtils.getFatalLog().error("Index load failed for: " + provInFile.getAbsolutePath(), t);
									} finally {
										try {
											if (in != null) {
												in.close();
											}
										} catch (Throwable t) {
											error = true;
											e = t;
											log.error("Error in closing the file input stream", t);
										}
									}
									if(error){
										throw new InvalidConfigException(e);
									}
									log.info("time taken to load index file(" + provInFile.getAbsolutePath() + ") is: " + (System.currentTimeMillis() - startTime));
								}
							}
						}
					}
				}
			}
			SortedSet<Integer> sortedExperienceIdSet = new SortedArraySet<Integer>(validExperienceIdList);
			ProductDB.getInstance().cleanOptIndex(sortedExperienceIdSet);
		}

	}

	private static Map<String, Map<Integer,List<File>>> getProvidersFromFileNames(List<File> files){
		Map<String, Map<Integer,List<File>>> retMap = new HashMap<String, Map<Integer, List<File>>>();
		if(files!=null && !files.isEmpty()){
			for(File f: files){
				String provider = getProviderFromFileName(f.getName());
				Integer experienceId = getExperienceIdFromFileName(f.getName());
				if(provider!=null && !"".equals(provider.trim()) && experienceId != null){
					Map<Integer,List<File>> experienceIdToFilesMap = retMap.get(provider);
					if(experienceIdToFilesMap == null){
						experienceIdToFilesMap = new HashMap<Integer, List<File>>();
					}
					List<File> provFiles = experienceIdToFilesMap.get(experienceId);
					if(provFiles == null){
						provFiles = new ArrayList<File>();
					}
					provFiles.add(f);
					experienceIdToFilesMap.put(experienceId, provFiles);
					retMap.put(provider, experienceIdToFilesMap);
				}
			}
		}
		return retMap;
	}

	/**
	 * Gets the provider name from a given mup file by tokenizing by _ char
	 * It is assumed that the provider name will be between the first _ char and the 5th _ char from the end
	 * Returns an empty string if the file name was not of correct syntax
	 *
	 * <advName>_<experienceID>_optjozindex.bin
	 *
	 * @param fileName - mup file name
	 * @return - provider name
	 */
	private static String getProviderFromFileName(String fileName) {
		String providerName = "";
		if (fileName != null) {
			String[] parts = fileName.split("_");
			if (parts.length < 3) {
				return "";
			}
			providerName = parts[0];
		}
		return providerName;
	}
	/**
	 * Gets the provider name from a given mup file by tokenizing by _ char
	 * It is assumed that the provider name will be between the first _ char and the 5th _ char from the end
	 * Returns an empty string if the file name was not of correct syntax
	 *
	 * <advName>_<experienceID>_optjozindex.bin
	 *
	 * @param fileName - mup file name
	 * @return - provider name
	 */

	//todo: do this at time of parsing advertiser
	private static Integer getExperienceIdFromFileName(String fileName) {
		Integer experienceId = null;
		if (fileName != null) {
			String[] parts = fileName.split("_");
			if (parts.length < 3) {
				return null;
			}
			experienceId = Integer.valueOf(parts[1]);
		}
		return experienceId;
	}

	private void init() {
		//todo: fill out with correct properties
//		JOZ_INDEX_FILE_PATTERN = AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.indexFileNamePattern");
		indexDirName = AppProperties.getInstance().getProperty("com.tumri.opt.content.file.sourceDir");
//		indexDirName = "/opt/Tumri/joz/data/caa/opt/current";
	}
}

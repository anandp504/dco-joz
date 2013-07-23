package com.tumri.joz.products;

import com.tumri.content.data.ContentProviderStatus;
import com.tumri.jic.IndexCreationException;
import com.tumri.jic.JICProperties;
import com.tumri.jic.joz.IJozIndexUpdater;
import com.tumri.jic.joz.PersistantProviderIndex;
import com.tumri.joz.index.updater.JozIndexUpdater;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.IndexLoadingComparator;
import com.tumri.joz.utils.LogUtils;
import com.tumri.utils.FSUtils;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Class that will load the Joz index files from a given directory
 *
 * @author: nipun
 * Date: Feb 21, 2008
 */

public class JozIndexHelper {

	private String JOZ_INDEX_FILE_PATTERN = ".*_jozindex_.*.bin";
	private String indexDirName = "/data/jozindex";
	private String prevJozindexDirName = "/prevjozindex";
	private static Logger log = Logger.getLogger(JozIndexHelper.class);
	private static JozIndexHelper inst = null;

	public static JozIndexHelper getInstance() {
		if (inst == null) {
			synchronized (JozIndexHelper.class) {
				if (inst == null) {
					inst = new JozIndexHelper();
				}
			}
		}
		return inst;
	}

	private JozIndexHelper() {
		init();
	}


	/**
	 * Load the Joz index from the default dir that is set in the joz.properties
	 * This will load all the joz index files for all advertisers
	 */
	public synchronized void loadJozIndex(boolean hotload, boolean debug) {
		try {
			log.info("Starting to load the Joz indexes for all advertisers. Hot Deploy = " + hotload);
			Date start = new Date();
			//Look for any Joz index files
			List<File> indexFiles = getSortedJozIndexFileList(indexDirName);
			boolean bErrors = false;
				try {
					readFromSerializedFile(indexFiles, debug, hotload, null, true);
				} catch (Exception e) {
					log.error("Exception caught on loading the index files : ", e);
					bErrors = true;
				}

			if (!bErrors) {
				log.info("Finished loading the Joz indexes");
			} else {
				log.info("Finished loading the Joz indexes, with errors");
			}
			log.info(((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes");
		} catch (Exception e) {
			log.error("Joz index load failed.", e);
		}
	}

	/**
	 * Load the joz index for the specific advertiser. This is not synchronized - so parallel loading is permitted
	 *
	 * @param advertiserName
	 */
	public void loadJozIndex(String advertiserName, boolean debug, boolean hotload) {
		try {
			log.info("Starting to load the Joz indexes for advertiser :" + advertiserName);
			//todo: possibly use List<File> indexFiles = getSortedJozIndexFileList(indexDirName + "/" + advertiserName.toUpperCase() + "/jozindex");

			ArrayList<File> indexFiles = new ArrayList<File>();
			File indexDir = new File(indexDirName + "/" + advertiserName.toUpperCase() + "/jozindex");
			FSUtils.findFiles(indexFiles, indexDir, JOZ_INDEX_FILE_PATTERN);
			if (indexFiles.size() == 0) {
				log.error("No " + advertiserName + " joz index files found under directory: " + indexDir.getAbsolutePath());
				return;
			}
			readFromSerializedFile(indexFiles, debug, hotload, null, true);

			log.info("Finished loading the Joz index for advertiser : " + advertiserName);
		} catch (Exception e) {
			log.error("Joz index load failed.", e);
		}
	}

	/**
	 * Delete all the products for a given advertiser - using the current jozindex file
	 *
	 * @param advertiserName
	 */
	public void deleteJozIndex(String advertiserName) {
		try {
			if (advertiserName == null) {
				log.info("Advertiser should be specified for deleting");
				return;
			}
			log.info("Starting to delete the Joz index for advertiser :" + advertiserName);
			ArrayList<File> prevIndexFiles = new ArrayList<File>();
			File prevIndexDir = new File(prevJozindexDirName + "/" + advertiserName.toUpperCase() + "/jozindex");
			if (prevIndexDir.exists()) {
				FSUtils.findFiles(prevIndexFiles, prevIndexDir, JOZ_INDEX_FILE_PATTERN);
			}
			if (!prevIndexFiles.isEmpty()) {
				IJozIndexUpdater updater = new JozIndexUpdater(true);
				readFromSerializedFile(prevIndexFiles, false, true, updater, false);
				log.info("Finished deleting the Joz index for advertiser : " + advertiserName);
				//Delete all the prev joz index files for that advertiser
				if (!prevIndexDir.exists()) {
					FSUtils.removeFiles(prevIndexDir, true);
				}

			} else {
				log.warn("No Joz index files for the advertiser to delete : " + advertiserName);
			}
		} catch (Exception e) {
			log.error("Joz index delete failed.", e);
		}

	}

	/**
	 * Load the index for a given set of Bin files. This is used by the console utlity
	 *
	 * @param idxDir
	 * @param myBinFiles
	 */
	public StringBuffer loadIndexForDebug(String idxDir, ArrayList<String> myBinFiles, ArrayList<Long> prods) {
		IJozIndexUpdater updater = new JozIndexUpdater();
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
			loadJozIndexFiles(idxDir, myBinFiles, updater);
		} else {
			loadJozIndexFiles(idxDir, null, updater);
		}
		return updater.getBuffer();
	}

	private void init() {
		JOZ_INDEX_FILE_PATTERN = AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.indexFileNamePattern");
		indexDirName = AppProperties.getInstance().getProperty("com.tumri.content.file.sourceDir");
		prevJozindexDirName = AppProperties.getInstance().getProperty("com.tumri.content.prevjozindexDir");
	}

	/**
	 * Gets the current list of Joz index file in the Dir specified in indexDirName
	 *
	 * @return
	 */
	private List<File> getSortedJozIndexFileList(String dirName) {

		List<File> indexFiles = new ArrayList<File>();
		File indexDir = new File(dirName);
		if (!indexDir.exists()) {
			log.error("Directory does not exist : " + dirName);
		}

		FSUtils.findFiles(indexFiles, indexDir, JOZ_INDEX_FILE_PATTERN);
		if (indexFiles.size() == 0) {
			log.error("No joz index files found in directory: " + indexDir.getAbsolutePath());
		}

		//Sort the files by Name
		Collections.sort(indexFiles,
				new Comparator<File>() {
					public int compare(File f1, File f2) {
						String s1 = f1.getName();
						String s2 = f2.getName();
						//Strip off the extension
						s1 = s1.substring(0, s1.indexOf(".bin"));
						s2 = s2.substring(0, s2.indexOf(".bin"));
						StringTokenizer st1 = new StringTokenizer(s1, "_");
						StringTokenizer st2 = new StringTokenizer(s2, "_");
						while (st1.hasMoreTokens() && st2.hasMoreTokens()) {
							String t1 = st1.nextToken();
							String t2 = st2.nextToken();

							int c;
							try {
								Integer i1 = new Integer(t1);
								Integer i2 = new Integer(t2);
								c = i1.compareTo(i2);
							}
							catch (NumberFormatException e) {
								c = t1.compareTo(t2);
							}
							if (c != 0) {
								return c;
							}
						}

						return 0;
					}
				});
		return indexFiles;
	}

	/**
	 * Load the specific set of Joz Index Files
	 */
	private void loadJozIndexFiles(String dirName, List<String> fileNames, IJozIndexUpdater updater) {
		try {
			log.info("Starting to load the specified Joz indexes.");
			if (fileNames != null && fileNames.size() > 0) {
				List<File> indexFiles = new ArrayList<File>();
				for (String indexFileName : fileNames) {
					File indexFile = new File(dirName + "/" + indexFileName);
					if (indexFile.exists()) {
						indexFiles.add(indexFile);
					} else {
						log.error("Specified file does not exist - cannot load : " + indexFile);
					}
				}
				readFromSerializedFile(indexFiles, true, false, updater, false);
			} else {
				List<File> idxFiles = getSortedJozIndexFileList(dirName);
				readFromSerializedFile(idxFiles, true, false, updater, false);
			}
			log.info("Finished loading the Joz indexes");
		} catch (Exception e) {
			log.error("Joz index load failed.", e);
		}
	}

	/**
	 * Helper method to read the index from a file.
	 *
	 * @param inFile
	 */
	private void readFromSerializedFile(List<File> inFiles, boolean debugMode, boolean hotLoad, IJozIndexUpdater updater,
	                                    boolean copy) throws IOException, ClassNotFoundException {
		if (updater == null) {
			updater = new JozIndexUpdater();
		}

		Map<String, List<File>> provToFilesMap = getProvidersFromFileNames(inFiles);
		Set<String> tmpProviderSet = provToFilesMap.keySet();

		if(tmpProviderSet!=null && !tmpProviderSet.isEmpty()){
			Set<String> providerSet = new SortedArraySet<String>(tmpProviderSet);
			for(String providerName: providerSet){
				if(providerName!=null){
					List<File> provInFiles = provToFilesMap.get(providerName);
					if(provInFiles!=null && !provInFiles.isEmpty()){
						for(File provInFile: provInFiles){
							log.info("Going to load the index from file : " + provInFile.getAbsolutePath());
							long startTime = System.currentTimeMillis();
							FileInputStream fis = null;
							ObjectInputStream in = null;

							try {
								JICProperties.init(debugMode, hotLoad, updater);
								fis = new FileInputStream(provInFile);
								in = new ObjectInputStream(new BufferedInputStream(fis));
								PersistantProviderIndex pProvIndex = (PersistantProviderIndex) in.readObject();
								in.close();
							} catch (IOException ex) {
								log.error("Could not load index file: " + provInFile.getAbsolutePath());
								throw ex;
							} catch (ClassNotFoundException ex) {
								log.error("Deserialization failed from file: " + provInFile.getAbsolutePath());
								throw ex;
							} catch (Throwable t) {
								LogUtils.getFatalLog().info("Index load failed for: " + provInFile.getAbsolutePath(), t);
							} finally {
								try {
									if (in != null) {
										in.close();
									}
								} catch (Throwable t) {
									log.error("Error in closing the file input stream", t);
								}
							}
							log.info("time taken to load index file(" + provInFile.getAbsolutePath() + ") is: " + (System.currentTimeMillis() - startTime));
						}

						//Copy to the prev folder
						if (copy) {
                            AppProperties appPropertiesInstance = AppProperties.getInstance();
                            IndexLoadingComparator comp = new IndexLoadingComparator();
                            File prevIndexDir = new File(prevJozindexDirName + "/" + providerName.toUpperCase() + "/jozindex");
                            if (!prevIndexDir.exists()) {
                                prevIndexDir.mkdirs();
                            }
                            //this is where we compare MUP vs index to see if there are any discrepancies between the two.
                            //if there are, we want to keep all new joz-indexes in prevjozindex/ so when we do a full-load
                            //we can correctly clean out all the advertisers indexes and re-add them.
                            if(appPropertiesInstance.isIndexValidEnabled()){
                                if (comp.validateForAdvertiser(providerName)) {
                                    //GC all the older indexes
                                    FSUtils.removeFiles(prevIndexDir, true);
                                }else{
                                    log.info("Index verification failed for: " +providerName);

                                }
                            }

							for(File provInFile: provInFiles){
								FSUtils.copyFile(provInFile, new File(prevIndexDir.getAbsolutePath() + "/" + provInFile.getName()));
								ContentProviderStatus.getInstance().jozIndexFileNames.put(providerName, provInFile.getName());
							}

						}
					}
				}
			}
		}

	}

	private static Map<String, List<File>> getProvidersFromFileNames(List<File> files){
		Map<String, List<File>> retMap = new HashMap<String, List<File>>();
		if(files!=null && !files.isEmpty()){
			for(File f: files){
				String provider = getProviderFromFileName(f.getName());
				if(provider!=null && !"".equals(provider.trim())){
					List<File> provFiles = retMap.get(provider);
					if(provFiles == null){
						provFiles = new ArrayList<File>();
					}
					provFiles.add(f);
					retMap.put(provider, provFiles);
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
	 * @param fileName - mup file name
	 * @return - provider name
	 */
	private static String getProviderFromFileName(String fileName) {
		String providerName = "";
		if (fileName != null) {
			String[] parts = fileName.split("_");
			if (parts.length < 4) {
				return "";
			}
			for (int i = 0; i < parts.length - 3; i++) {
				String delim = "";
				if (i > 1) {
					delim = "_";
				}
				providerName = providerName + delim + parts[i];
			}
		}
		return providerName;
	}

	/**
	 * Test method
	 *
	 * @param args
	 */
	public static void main(String[] args) {
		JozIndexHelper jh = JozIndexHelper.getInstance();
		jh.loadJozIndex(false, false);
	}

}

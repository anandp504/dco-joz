/*
 * WMDBLoader.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.campaign.wm.loader;

import com.tumri.joz.campaign.wm.*;
import com.tumri.joz.index.Range;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;

import java.util.*;
import java.io.File;
import java.io.Writer;
import java.io.StringWriter;
import java.io.PrintWriter;

import org.apache.log4j.Logger;

/**
 * Loads the Data from the input file into the AdPodWeightDB
 *
 * @author: nipun
 * Date: Aug 10, 2009
 * Time: 11:37:19 AM
 */
public class WMDBLoader {

	private static final Logger log = Logger.getLogger(WMDBLoader.class);

	/**
	 * Public method to find the wm.xml file and load into the db.
	 *
	 * @throws WMLoaderException
	 */
	public static List<String> loadData() throws WMLoaderException {
		String dir = AppProperties.getInstance().getProperty("com.tumri.campaign.wm.xmlFileDir");
		String pttern = AppProperties.getInstance().getProperty("com.tumri.campaign.wm.xmlFileNamePattern");

		File srcDir = new File(dir);
		ArrayList<File> wmFiles = new ArrayList<File>();
		ArrayList<String> loadedFiles = new ArrayList<String>();
		//Check for campaign files
		if (srcDir.exists() && srcDir.isDirectory()) {
			findFiles(wmFiles, pttern, srcDir);
		} else {
			throw new WMLoaderException("weights matrix source directory is invalid : " + dir);
		}

		if (!wmFiles.isEmpty()) {
			WMXMLParser parserImpl = getParserImpl();
			List<String> failedFilesErrors = new ArrayList<String>();
			boolean error = false;
			for (File xmlFile : wmFiles) {
				log.info("Now loading  :" + xmlFile.getAbsolutePath());
				try {
					parserImpl.process(xmlFile.getAbsolutePath());
					loadedFiles.add(xmlFile.getAbsolutePath());
				} catch (WMLoaderException e) {
					failedFilesErrors.add("Failed to load : " + xmlFile.getAbsolutePath() + ". Reason : " + e.getMessage());
					error = true;
				}
			}
			parserImpl.finalize();
			if (error) {
				StringBuilder sb = new StringBuilder();
				sb.append("weights matrix loading encountered the following errors: ");
				for (String fileMessage : failedFilesErrors) {
					sb.append(fileMessage);
					sb.append('\n');
				}
				throw new WMLoaderException(sb.toString());
			}
		} else {
			throw new WMLoaderException("No WM files found to load");
		}
		return loadedFiles;
	}

	public static void forceLoadData() {
		try {
			log.info("Going to force refresh wm data.");
			long startTime = System.currentTimeMillis();
			List<String> loadedFiles = loadData();
			StringBuffer sb = new StringBuffer();
			if (loadedFiles.size() > 0) {
				sb.append("Files loaded : ");
				for (String f : loadedFiles) {
					sb.append(f);
					sb.append(",");
				}
			}
			WMContentProviderStatus.getInstance().lastSuccessfulRefreshTime = startTime;
			WMContentProviderStatus.getInstance().lastRunStatus = true;
			WMContentProviderStatus.getInstance().addRunHistory(startTime, true, "Force Refresh successful." + sb.toString() +
					" Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
			WMContentProviderStatus.getInstance().lastRefreshTime = startTime;
			log.info("WM data force refreshed successfully. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
		} catch (WMLoaderException e) {
			log.info("WM data force refresh failed", e);
			long errTime = System.currentTimeMillis();
			Writer errorDetails = new StringWriter();
			PrintWriter pw = new PrintWriter(errorDetails);
			e.printStackTrace(pw);
			WMContentProviderStatus.getInstance().addRunHistory(errTime, false, "Refresh Failed. " +
					" Details : " + errorDetails.toString());
			WMContentProviderStatus.getInstance().lastError = e;
			WMContentProviderStatus.getInstance().lastErrorRunTime = errTime;
			WMContentProviderStatus.getInstance().lastRunStatus = false;
			WMContentProviderStatus.getInstance().lastRefreshTime = errTime;
			LogUtils.getFatalLog().fatal("WM data force refresh failed", e);
		}

	}

	public static void updateDb(Integer adPodId, Map<WMAttribute, Integer> requestMap, WMHandle handle) {
		if (requestMap != null && !requestMap.isEmpty()) {
			for (WMAttribute attr : requestMap.keySet()) {
				Integer id = requestMap.get(attr);
				if (id != null) {
					if (WMRangeIndex.getAllowdAttributes().contains(attr)) {
						TreeMap<Range<Integer>, ArrayList<WMHandle>> map = new TreeMap<Range<Integer>, ArrayList<WMHandle>>();
						ArrayList<WMHandle> handles = new ArrayList<WMHandle>();
						handles.add(handle);

						String s = WMUtils.getDictValue(attr, id);
						List<String> list = WMUtils.getParsedUniqueIntRangeString(s);
						if (list != null && list.size() == 2) {
							int min = Integer.parseInt(list.get(0));
							int max = Integer.parseInt(list.get(1));
							Range<Integer> r = new Range<Integer>(min, max);
							map.put(r, handles);
							WMDB.WMIndexCache db = WMDB.getInstance().getWeightDB(adPodId);
							db.updateRangeIndex(attr, map);
						} else {
							//todo
							log.warn("skipping context");
						}

					} else {
						TreeMap<Integer, ArrayList<WMHandle>> map = new TreeMap<Integer, ArrayList<WMHandle>>();
						ArrayList<WMHandle> handles = new ArrayList<WMHandle>();
						handles.add(handle);
						map.put(id, handles);
						WMDB.WMIndexCache db = WMDB.getInstance().getWeightDB(adPodId);
						db.updateIntegerIndex(attr, map);
					}
				}
			}
		}
	}

	private static WMXMLParser getParserImpl() {
		WMXMLParser impl = new WMXMLParserV1();
		return impl;
	}

	/**
	 * Searches the list of campaign files to be loaded based on search directory and filename pattern
	 *
	 * @param matchedFilesAL list of matched files
	 * @param filePattern    pattern to be searched
	 * @param dir            direcory to look for the files specified by the filePattern
	 */
	private static void findFiles(ArrayList<File> matchedFilesAL, String filePattern, File dir) {
		ArrayList<File> directories = new ArrayList<File>();
		if (dir.isDirectory()) {
			String[] children = dir.list();
			Arrays.sort(children);
			for (String child : children) {
				File file = new File(dir.getAbsolutePath() + File.separator + child);
				if (!file.isDirectory()) {
					if (file.getName().matches(filePattern)) {
						matchedFilesAL.add(file);
					}
				} else {
					directories.add(file);
				}
			}
		}
		for (File f : directories) {
			findFiles(matchedFilesAL, filePattern, f);
		}
	}

	public static void main(String[] args) {
		try {
			loadData();
		} catch (WMLoaderException e) {
			e.printStackTrace();
		}
	}

}

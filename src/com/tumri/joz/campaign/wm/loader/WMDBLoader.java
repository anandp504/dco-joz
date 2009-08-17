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
 * @author: nipun
 * Date: Aug 10, 2009
 * Time: 11:37:19 AM
 */
public class WMDBLoader {

    private static final Logger log = Logger.getLogger(WMDBLoader.class);

    /**
     * Public method to find the wm.xml file and load into the db. 
     * @throws WMLoaderException
     */
    public static void loadData() throws WMLoaderException {
        String dir = AppProperties.getInstance().getProperty("com.tumri.campaign.wm.xmlFileDir");
        String pttern =  AppProperties.getInstance().getProperty("com.tumri.campaign.wm.xmlFileNamePattern");

        File srcDir = new File(dir);
        ArrayList<File> wmFiles = new ArrayList<File>();
        //Check for campaign files
        if (srcDir.exists() && srcDir.isDirectory()) {
            findFiles(wmFiles, pttern, srcDir);
        } else {
            throw new WMLoaderException("weights matrix source directory is invalid : " + dir);
        }

        if (!wmFiles.isEmpty()) {
            for (File xmlFile : wmFiles) {
                log.info("Now loading  :" + xmlFile.getAbsolutePath());
                WMXMLParser parserImpl = getParserImpl(xmlFile);
                try {
                    parserImpl.process(xmlFile.getAbsolutePath());
                } catch (WMLoaderException e) {
                    throw new WMLoaderException("weights matrix load failed",e);
                }
            }
        } else {
            log.warn("No WM files found to load");
        }
    }

    public static void forceLoadData() {
        try {
            log.info("Going to force refresh wm data.");
            long startTime = System.currentTimeMillis();
            loadData();
            WMContentProviderStatus.getInstance().lastSuccessfulRefreshTime = startTime;
            WMContentProviderStatus.getInstance().lastRunStatus = true;
            WMContentProviderStatus.getInstance().addRunHistory(startTime, true, "Force Refresh successful." +
                    " Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
            WMContentProviderStatus.getInstance().lastRefreshTime = startTime;
            log.info("WM data force refreshed successfully. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
        } catch (WMLoaderException e) {
            log.info("WM data force refresh failed",e);
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
            LogUtils.getFatalLog().fatal("WM data force refresh failed",e);
        }

    }
    public static void updateDb(Integer adPodId, Map<WMIndex.Attribute, Integer> requestMap, WMHandle handle) {
        if (requestMap!=null && !requestMap.isEmpty()) {
            for (WMIndex.Attribute attr: requestMap.keySet()) {
                Integer id = requestMap.get(attr);
                if (id != null) {
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

    private static WMXMLParser getParserImpl(File xmlFile) {
        String name = xmlFile.getName();
        int idx = name.indexOf("-");
        WMXMLParser impl = new WMXMLParserV1();
        if (idx>0) {
            String version = name.substring(idx+1, name.length()-4);
            //Pick up appropriate version of the impl from here
        }
        return impl;
    }

    /**
     * Searches the list of campaign files to be loaded based on search directory and filename pattern
     * @param matchedFilesAL list of matched files
     * @param filePattern  pattern to be searched
     * @param dir direcory to look for the files specified by the filePattern
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
        for (File f: directories) {
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

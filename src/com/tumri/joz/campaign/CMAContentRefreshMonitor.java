package com.tumri.joz.campaign;

import java.io.File;

import org.apache.log4j.Logger;

import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.PollingUnit;

/**
 * Timer class to force load the CMA content provided a trigger file is present. 
 * This needs to be invoked from the Joz startup routine. 
 * 
 * The logic looks for a file ( name configured in joz.properties ) at a regular interval, and kicks off the CMA data load if the file is present.
 * @author nipun
 *
 */
public class CMAContentRefreshMonitor implements PollingUnit {

	protected static Logger log = Logger.getLogger(CMAContentRefreshMonitor.class);
	protected long refreshInterval = 10;
	protected String g_triggerRefreshFile = "cmaload.txt";
	protected String g_TSpecMappingsSrcPath = "../.";
	
	private static final String CONFIG_LISP_FILE_SRC_PATH = "com.tumri.campaign.file.sourceDir";
	private static final String CONFIG_REFRESH_INTERVAL = "com.tumri.campaign.file.trigger.monitorInterval";
	private static final String CONFIG_TRIGGER_REFRESH_FILENAME = "com.tumri.campaign.file.triggerLoadFileName";

	private static CMAContentRefreshMonitor g_cmaContentPoller = null;
	
	private CMAContentRefreshMonitor() {
		super();
	}
	
	/**
	 * Returns an static reference to the CMAContentPoller
	 * @return
	 */
	public static CMAContentRefreshMonitor getInstance() {
		if (g_cmaContentPoller == null) {
			g_cmaContentPoller =  new CMAContentRefreshMonitor();
		}
		return g_cmaContentPoller;
	}
	
	/**
	 * Implementation that will invoke the CMA loading.
	 */
	public void performTask() {
		loadCampaignData();
	}
	
	/**
	 * Method to load the campaign data
	 * The logic to check if the refresh is needed, can be implemented inside the Campaign Delta Provider impl.
	 *
	 */
	private void loadCampaignData() {
		try {
			if (checkForTriggerFile()) {
				log.info("Going to force refresh campaign data.");
				long startTime = System.currentTimeMillis();
				CampaignDBDataLoader.getInstance().loadData();
				log.info("Campaign data force refreshed successfully. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
			}
		} catch (Exception e) {
			log.error("Campaign data force refresh failed");
		}
	}
	
	/**
	 * Perform the initialization tasks for Campaign Data Force Loading
	 *
	 */
	public void init() {
		
		//Register for the polling
		try {
			refreshInterval = Long.parseLong(AppProperties.getInstance().getProperty(CONFIG_REFRESH_INTERVAL));
		} catch(NumberFormatException e) {
			refreshInterval = 5;
		}
		
        if (refreshInterval > 0) {
        	//Register with the polling unit
            Polling.getInstance().addPolling(this, refreshInterval*60*1000);
        }
        
		String triggerFileName = AppProperties.getInstance().getProperty(CONFIG_TRIGGER_REFRESH_FILENAME);
		if (triggerFileName != null) {
			g_triggerRefreshFile = triggerFileName.trim();
		} 
		
		String lispFileSrcDirs = AppProperties.getInstance().getProperty(CONFIG_LISP_FILE_SRC_PATH);
		if (lispFileSrcDirs != null) {
			g_TSpecMappingsSrcPath = lispFileSrcDirs;
		} 

	}

	/**
	 * Helper method that will check for the presence of a trigger file, and if present will force campaign data load
	 *
	 */
	private boolean checkForTriggerFile() {
		boolean bTriggerLoad = false;
		try {
			File triggerFile = new File(g_TSpecMappingsSrcPath + "/" + g_triggerRefreshFile);
			if (triggerFile!=null && triggerFile.exists()) {
				bTriggerLoad = true;
				//delete the file
				try {
					triggerFile.delete();
				} catch (Exception e){
					log.warn("Could not delete the trigger file, check permissions on the file : " + triggerFile.getAbsolutePath());
				}
			}
		} catch (Exception e) {
			log.error("Unexpected exception caught during the check for the trigger file");
		}
		return bTriggerLoad;
	}
}

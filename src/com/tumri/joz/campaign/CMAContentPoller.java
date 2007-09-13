package com.tumri.joz.campaign;

import org.apache.log4j.Logger;

import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.PollingUnit;

/**
 * Bizlogic to load the CMA content. This needs to be invoked from the Joz startup routine.
 * @author nipun
 *
 */
public class CMAContentPoller implements PollingUnit {

	protected static Logger log = Logger.getLogger(CMAContentPoller.class);
	protected long refreshInterval = 10;

	private static final String CONFIG_REFRESH_INTERVAL = "com.tumri.campaign.file.refreshInterval";
	private static CMAContentPoller g_cmaContentPoller = null;
	
	private CMAContentPoller() {
		super();
	}
	
	/**
	 * Returns an static reference to the CMAContentPoller
	 * @return
	 */
	public static CMAContentPoller getInstance() {
		if (g_cmaContentPoller == null) {
			g_cmaContentPoller =  new CMAContentPoller();
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
			log.info("Going to refresh campaign data.");
			CampaignDBDataLoader.getInstance().loadData();
			log.info("Campaign data refreshed successfully.");
		} catch (Exception e) {
			//TODO: invoke notification framework to indicate something is wrong here
			log.error("Campaign data refresh failed");
		}
	}
	
	/**
	 * Perform the initialization tasks for Campaign Data Loading
	 *
	 */
	public void init() {
		
		loadCampaignData();
		
		//Register for the polling
		try {
			refreshInterval = Long.parseLong(AppProperties.getInstance().getProperty(CONFIG_REFRESH_INTERVAL));
		} catch(NumberFormatException e) {
			refreshInterval = 10;
		}
		
        if (refreshInterval > 0) {
        	//Register with the polling unit
            Polling.getInstance().addPolling(this, refreshInterval*60*1000);
        }		
	}

}

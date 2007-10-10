package com.tumri.joz.campaign;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.log4j.Logger;

import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;

/**
 * Bizlogic to load the CMA content. This needs to be invoked from the Joz startup routine.
 * This class kicks off a timer task that will activate the campaign loading by the wall clock time as configured in the joz.properties
 * The defaults are set to an hourly refresh at 40 min past the hour.
 * @author nipun
 *
 */
public class CMAContentPoller {

	protected static Logger log = Logger.getLogger(CMAContentPoller.class);
	protected int refreshWallClockTimeMins = 40; //40 mins on the hour
	protected static Timer _timer = new Timer();
    protected static int repeatIntervalMins = 60; //every 1 hour

	private static final String CONFIG_WALL_CLOCK_MINUTES = "com.tumri.campaign.file.refresh.time.minutes";
	private static final String CONFIG_CMA_REFRESH_INTERVAL_MINUTES = "com.tumri.campaign.file.refresh.interval.minutes";
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
	public void performTask() throws CampaignDataLoadingException  {
		loadCampaignData();
	}
	
	/**
	 * Method to load the campaign data
	 * The logic to check if the refresh is needed, can be implemented inside the Campaign Delta Provider impl.
	 *
	 */
	private void loadCampaignData() throws CampaignDataLoadingException {
		log.info("Going to refresh campaign data.");
		long startTime = System.currentTimeMillis();
		CampaignDBDataLoader.getInstance().loadData();
		log.info("Campaign data refreshed successfully. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
	}
	
	/**
	 * Perform the initialization tasks for Campaign Data Loading
	 *
	 */
	public void init() throws CampaignDataLoadingException  {
		
		performTask();

		//Register for the polling
		try {
			int minutes = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_WALL_CLOCK_MINUTES));
			if (minutes > 0 && minutes < 60) {
				refreshWallClockTimeMins = minutes;
			}
			int repeat = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_CMA_REFRESH_INTERVAL_MINUTES));
			if (repeat > 0 && minutes < 60) {
				repeatIntervalMins = repeat;
			}
		} catch(NumberFormatException e) {
		}
		
		startTimer();	
	}

	/**
	 * Shutdown the timer task
	 *
	 */
	public void shutdown() {
		_timer.cancel();
	}
	
	/**
	 * Start the timer.
	 */
    private void startTimer()
    {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
    	c.set(Calendar.SECOND, 0);
    	c.set(Calendar.MILLISECOND, 0);
        if (c.get(Calendar.MINUTE) >= refreshWallClockTimeMins) {
        	c.add(Calendar.HOUR, 1);
        	c.set(Calendar.MINUTE, refreshWallClockTimeMins);
        } else {
        	c.set(Calendar.MINUTE, refreshWallClockTimeMins);
        }
        
        _timer.scheduleAtFixedRate(new TimerTask() {
            public void run()
            {
            	try {
            		performTask();
            	} catch (CampaignDataLoadingException e) {
            	    LogUtils.getFatalLog().fatal("Campaign data load failed", e);
            	}
            }
        }, c.getTime(), repeatIntervalMins*60*1000);

    }

}

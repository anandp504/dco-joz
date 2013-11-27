package com.tumri.joz.products;

import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: scbraun
 * Date: 10/1/13
 *
 * very similar to WMContentPoller
 */
public class ListingOptContentPoller {

	private static ListingOptContentPoller gInstance = null;

	protected static Logger log = Logger.getLogger(ListingOptContentPoller.class);
	protected int refreshWallClockTimeMins = 8; // start at 8 min after an hour
	protected static Timer _timer = new Timer();
	protected static int repeatIntervalMins = 90; // repeat every 90 min

	//todo: add new properties to local.properties
	private static final String CONFIG_WM_WALL_CLOCK_MINUTES = "com.tumri.wm.file.refresh.time.minutes";
	private static final String CONFIG_WM_REFRESH_INTERVAL_MINUTES = "com.tumri.wm.file.refresh.interval.minutes";
	private static final String CONFIG_CMA_REFRESH_ENABLED = "com.tumri.campaign.file.refresh.enabled";

	private ListingOptContentPoller() {
		super();

	}

	public static ListingOptContentPoller getInstance(){
		if(gInstance == null){
			gInstance = new ListingOptContentPoller();
		}
		return gInstance;
	}


	/**
	 * Implementation that will invoke the CMA loading.
	 */
	public void performTask() {
		loadListingOptData();
	}

	/**
	 * Method to load the campaign data
	 * The logic to check if the refresh is needed, can be implemented inside the Campaign Delta Provider impl.
	 *
	 */
	private void loadListingOptData(){
		log.info("Going to refresh optimization listing data.");
		long startTime = System.currentTimeMillis();

		boolean successFailFlag= OptJozIndexHelper.getInstance().loadJozIndex(false);

		ListingOptContentProviderStatus.getInstance().lastSuccessfulRefreshTime = startTime;
		ListingOptContentProviderStatus.getInstance().lastRunStatus = successFailFlag;
        String message = (successFailFlag == true ?  "Refresh Successful" : "Refresh Fail");
		ListingOptContentProviderStatus.getInstance().addRunHistory(startTime, successFailFlag, message +
				" Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
		ListingOptContentProviderStatus.getInstance().lastRefreshTime = startTime;
		log.info("Listing opt data refreshed. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");

	}

	/**
	 * Perform the initialization tasks for Campaign Data Loading
	 *
	 */
	public void init()   {

		// Delete any existing campaigns.lock file if it exists
		boolean toLock = false;

		performTask();

		//Register for the polling
		try {
			int minutes = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_WM_WALL_CLOCK_MINUTES));
			if (minutes > 0 && minutes < 60) {
				refreshWallClockTimeMins = minutes;
			}
			int repeat = Integer.parseInt(AppProperties.getInstance().getProperty(CONFIG_WM_REFRESH_INTERVAL_MINUTES));
			if (repeat > 0 && minutes < 60) {
				repeatIntervalMins = repeat;
			}
			ListingOptContentProviderStatus.getInstance().refreshInterval = repeatIntervalMins;
		} catch(NumberFormatException e) {
			log.error("Error when parsing either: " + CONFIG_WM_WALL_CLOCK_MINUTES +" or " + CONFIG_WM_REFRESH_INTERVAL_MINUTES);
		}
		String refreshEnabled = AppProperties.getInstance().getProperty(CONFIG_CMA_REFRESH_ENABLED);
		if (!"false".equalsIgnoreCase(refreshEnabled)) {
			startTimer();
		}
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
		Calendar c = getTimerStartTime();
		_timer.scheduleAtFixedRate(new TimerTask() {
			public void run()
			{
				performTask();
			}
		}, c.getTime(), repeatIntervalMins*60*1000);

	}

	private Calendar getTimerStartTime(){

		Calendar c = Calendar.getInstance();
		c.setTimeInMillis(System.currentTimeMillis());
		c.set(Calendar.SECOND, 0);
		c.set(Calendar.MILLISECOND, 0);
		if(c.get(Calendar.MINUTE) < refreshWallClockTimeMins){
			// start at 5 min to the hour
			c.set(Calendar.MINUTE, refreshWallClockTimeMins);
		}else{
			// start after 15 min slots starting from 5 min to the hour
			int minToAdd = c.get(Calendar.MINUTE) - refreshWallClockTimeMins;
			int multiplyFactor = minToAdd/repeatIntervalMins;
			c.set(Calendar.MINUTE, refreshWallClockTimeMins + (multiplyFactor+1)*repeatIntervalMins);
		}
		return c;
	}

}

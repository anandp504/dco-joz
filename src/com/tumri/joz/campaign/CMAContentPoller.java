package com.tumri.joz.campaign;

import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
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
    protected int refreshWallClockTimeMins = 5; // start at 5 min after an hour
    protected static Timer _timer = new Timer();
    protected static int repeatIntervalMins = 15; // repeat every 15 min
    private static final String CONFIG_XML_FILE_SRC_PATH = "com.tumri.campaign.file.sourceDir";
    private static final String XML_LOCK_FILE = "campaigns.lock";
    private static final String CONFIG_WALL_CLOCK_MINUTES = "com.tumri.campaign.file.refresh.time.minutes";
    private static final String CONFIG_CMA_REFRESH_INTERVAL_MINUTES = "com.tumri.campaign.file.refresh.interval.minutes";
    private static final String CONFIG_CMA_REFRESH_ENABLED = "com.tumri.campaign.file.refresh.enabled";
    private static CMAContentPoller g_cmaContentPoller = null;
    private static String lockFileName = null;
    private CMAContentPoller() {
        super();

        String cmpFileDir = AppProperties.getInstance().getProperty(CONFIG_XML_FILE_SRC_PATH);
        lockFileName = cmpFileDir+File.separator+XML_LOCK_FILE;
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

        try{
            boolean toLock = true;
            // lock the file
            if(manageCampaignLockFile(toLock)){
                try {
                    loadCampaignData();
                    // unlock the file
                } finally {
                    toLock=false;
                    manageCampaignLockFile(toLock);
                }
            }
        }catch(CampaignDataLoadingException cdlEx){
            log.error("Exception in loadCampaignData ");
        }
    }

    /**
     * Method to load the campaign data
     * The logic to check if the refresh is needed, can be implemented inside the Campaign Delta Provider impl.
     *
     */
    private void loadCampaignData() throws CampaignDataLoadingException {
        log.info("Going to refresh campaign data.");
        long startTime = System.currentTimeMillis();
        try {
            CampaignDBDataLoader.getInstance().loadData();
            CMAContentProviderStatus.getInstance().lastSuccessfulRefreshTime = startTime;
            CMAContentProviderStatus.getInstance().lastRunStatus = true;
            CMAContentProviderStatus.getInstance().addRunHistory(startTime, true, "Refresh successful." +
                    " Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
            CMAContentProviderStatus.getInstance().lastRefreshTime = startTime;
            log.info("Campaign data refreshed successfully. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
        } catch (CampaignDataLoadingException e) {
            Writer errorDetails = new StringWriter();
            PrintWriter pw = new PrintWriter(errorDetails);
            e.printStackTrace(pw);
            CMAContentProviderStatus.getInstance().addRunHistory(startTime, false, "Refresh Failed. " +
                    " Details : " + errorDetails.toString());
            CMAContentProviderStatus.getInstance().lastError = e;
            CMAContentProviderStatus.getInstance().lastErrorRunTime = startTime;
            CMAContentProviderStatus.getInstance().lastRunStatus = false;
            CMAContentProviderStatus.getInstance().lastRefreshTime = startTime;
            LogUtils.getFatalLog().fatal("Exception caught during campaign data load", e);
        }

    }

    /**
     * Perform the initialization tasks for Campaign Data Loading
     *
     */
    public void init() throws CampaignDataLoadingException  {

        // Delete any existing campaigns.lock file if it exists
        boolean toLock = false;
        manageCampaignLockFile(toLock);

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
            CMAContentProviderStatus.getInstance().refreshInterval = repeatIntervalMins;
        } catch(NumberFormatException e) {
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
                try {
                    performTask();
                } catch (CampaignDataLoadingException e) {
                    LogUtils.getFatalLog().fatal("Campaign data load failed", e);
                }
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

    private boolean manageCampaignLockFile(boolean lock) throws CampaignDataLoadingException{
        boolean status = false;
        try{
            File lockFile = new File(lockFileName);
            if(lock){
                if(!lockFile.createNewFile()){
                    log.debug("Unable to create campaigns.lock file , previous import in progress, wait");
                }else
                    status= true;
            }else{
                if(lockFile.exists()){
                    if(lockFile.delete()){
                        log.debug("Deleted campaigns.lock file");
                        status = true;
                    }else{
                        log.debug("Unable to delete campaigns.lock file");
                    }
                }
            }
        }catch(IOException ioEx){
            log.error("IOException in loadCampaignData ");
            ioEx.printStackTrace();
            throw new CampaignDataLoadingException("IOException in loadCampaignData ");
        }catch(SecurityException secEx){
            log.error("SecurityException in loadCampaignData, problem with creating and deleting campaigns.lock file");
            secEx.printStackTrace();
            throw new CampaignDataLoadingException("SecurityException in loadCampaignData, problem with creating and deleting campaigns.lock file");
        }
        return status;

        /*
           * Using FileLock is the suggested way of locking files, but somehow this wasn't working as expected
          String lockFileName = cmpFileDir+File.separator+"campaigns.lock";

          try {
              // Get a file channel for the file
              File file = new File(lockFileName);
              if(!file.exists())
                  file.createNewFile();

              FileChannel channel = new RandomAccessFile(file, "r").getChannel();

              // Use the file channel to create a lock on the file.
              // This method blocks until it can retrieve the lock.
              FileLock lock = channel.lock();

              // Acquiring the lock without blocking. null or  exception if the file is already locked.
              try {
                  lock = channel.tryLock();
              } catch (OverlappingFileLockException e) {
                  log.debug("Unable to acquirelock for campaigns.lock file , previous import in progress, wait");
              }

              // Release the lock
              lock.release();

              // Close the file
              channel.close();
          } catch (Exception ex) {
              log.error("Exception in loadCampaignData ");
              ex.printStackTrace();
          }
          */
    }

}

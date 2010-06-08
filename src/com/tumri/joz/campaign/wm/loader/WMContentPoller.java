package com.tumri.joz.campaign.wm.loader;

import java.util.Calendar;
import java.util.List;
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
 * Bizlogic to load the WM content. This needs to be invoked from the Joz startup routine.
 * This class kicks off a timer task that will activate the wm loading by the wall clock time as configured in the joz.properties
 * The defaults are set to an hourly refresh at 40 min past the hour.
 * @author nipun
 *
 */
public class WMContentPoller {

    protected static Logger log = Logger.getLogger(WMContentPoller.class);
    protected int refreshWallClockTimeMins = 5; // start at 5 min after an hour
    protected static Timer _timer = new Timer();
    protected static int repeatIntervalMins = 15; // repeat every 15 min
    private static final String CONFIG_XML_FILE_SRC_PATH = "com.tumri.campaign.file.sourceDir";
    private static final String XML_LOCK_FILE = "wm.lock";
    private static final String CONFIG_WALL_CLOCK_MINUTES = "com.tumri.campaign.file.refresh.time.minutes";
    private static final String CONFIG_CMA_REFRESH_INTERVAL_MINUTES = "com.tumri.campaign.file.refresh.interval.minutes";
    private static final String CONFIG_CMA_REFRESH_ENABLED = "com.tumri.campaign.file.refresh.enabled";
    private static WMContentPoller g_cmaContentPoller = null;
    private static String lockFileName = null;
    private WMContentPoller() {
        super();

        String cmpFileDir = AppProperties.getInstance().getProperty(CONFIG_XML_FILE_SRC_PATH);
        lockFileName = cmpFileDir+File.separator+XML_LOCK_FILE;
    }

    /**
     * Returns an static reference to the CMAContentPoller
     * @return
     */
    public static WMContentPoller getInstance() {
        if (g_cmaContentPoller == null) {
            g_cmaContentPoller =  new WMContentPoller();
        }
        return g_cmaContentPoller;
    }

    /**
     * Implementation that will invoke the CMA loading.
     */
    public void performTask() throws WMLoaderException  {

        try{
            boolean toLock = true;
            // lock the file
            if(manageWMLoadFile(toLock)){
                try {
                    loadWMData();
                    // unlock the file
                } finally {
                    toLock=false;
                    manageWMLoadFile(toLock);
                }
            }
        }catch(WMLoaderException cdlEx){
            log.error("Exception in loadWmData ");
        }
    }

    /**
     * Method to load the campaign data
     * The logic to check if the refresh is needed, can be implemented inside the Campaign Delta Provider impl.
     *
     */
    private void loadWMData() throws WMLoaderException {
        log.info("Going to refresh wm data.");
        long startTime = System.currentTimeMillis();
        try {
            List<String> loadedFiles = WMDBLoader.loadData();
            StringBuffer sb = new StringBuffer();
            if (loadedFiles.size() > 0) {
                sb.append("Files loaded : " );
                for (String f: loadedFiles) {
                    sb.append(f);
                    sb.append(",");
                }
            }
            WMContentProviderStatus.getInstance().lastSuccessfulRefreshTime = startTime;
            WMContentProviderStatus.getInstance().lastRunStatus = true;
            WMContentProviderStatus.getInstance().addRunHistory(startTime, true, "Refresh successful." + sb.toString() +
                    " Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
            WMContentProviderStatus.getInstance().lastRefreshTime = startTime;
            log.info("WM data refreshed successfully. Time Taken = " + (System.currentTimeMillis() - startTime) + " millis.");
        } catch (WMLoaderException e) {
            log.info("WM data refresh failed", e);
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
            LogUtils.getFatalLog().fatal("WM data refresh failed", e);
        }
    }

    /**
     * Perform the initialization tasks for Campaign Data Loading
     *
     */
    public void init() throws WMLoaderException  {

        // Delete any existing campaigns.lock file if it exists
        boolean toLock = false;
        manageWMLoadFile(toLock);

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
            WMContentProviderStatus.getInstance().refreshInterval = repeatIntervalMins;
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
                } catch (WMLoaderException e) {
                    LogUtils.getFatalLog().fatal("WM data load failed", e);
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

    private boolean manageWMLoadFile(boolean lock) throws WMLoaderException{
        boolean status = false;
        try{
            File lockFile = new File(lockFileName);
            if(lock){
                if(!lockFile.createNewFile()){
                    log.debug("Unable to create wm.lock file , previous import in progress, wait");
                }else
                    status= true;
            }else{
                if(lockFile.exists()){
                    if(lockFile.delete()){
                        log.debug("Deleted wm.lock file");
                        status = true;
                    }else{
                        log.debug("Unable to delete wm.lock file");
                    }
                }
            }
        }catch(IOException ioEx){
            log.error("IOException in loadWMData ");
            ioEx.printStackTrace();
            throw new WMLoaderException("IOException in loadWMData ");
        }catch(SecurityException secEx){
            log.error("SecurityException in loadWMData, problem with creating and deleting wm.lock file");
            secEx.printStackTrace();
            throw new WMLoaderException("SecurityException in loadWMData, problem with creating and deleting wm.lock file");
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
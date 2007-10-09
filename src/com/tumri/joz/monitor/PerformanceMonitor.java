package com.tumri.joz.monitor;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.Set;
import java.util.Iterator;
import org.apache.log4j.Logger;

/**
 * Monitor for JoZ Performance statistics by monitoring product requests.
 *
 * @author Ramki
 */
public class PerformanceMonitor extends ComponentMonitor implements Runnable
{
	private static PerformanceMonitor instance=null;
	private long totalRequests=0;
	private long failedRequests=0;
	private long minRequestTime=0;
	private long maxRequestTime=0;
	private long totalTime=0;
	private String minTimedTspecName=null;
	private String maxTimedTspecName=null;
	private Map<String,Long> failedTspecsMap=new HashMap<String,Long>();
	private Date startDate=new Date();
	private ConcurrentLinkedQueue<HashMap<String,Long>> reqQueue=new ConcurrentLinkedQueue<HashMap<String,Long>>();
	private boolean isRunning=false;

    private static Logger log = Logger.getLogger(PerformanceMonitor.class);

    private PerformanceMonitor()
    {
       super("product-requests", new PerformanceMonitorStatus("performance"));
    }

    public static PerformanceMonitor getInstance() {
		if (null == instance) {
			synchronized(PerformanceMonitor.class) {
				if (null == instance) {
					instance=new PerformanceMonitor();
					Thread perThread=new Thread(instance);
					perThread.start();
				}
			}
		}
		return instance;
	}

	public void registerSuccess(String tspecName,long elapsedTime) {
		HashMap<String,Long> req=new HashMap<String,Long>();
		req.put(tspecName,new Long(elapsedTime));
		reqQueue.add(req);
	}

	public void registerFailure(String tspecName) {
		HashMap<String,Long> req=new HashMap<String,Long>();
		req.put(tspecName,new Long(-1));
		reqQueue.add(req);
	}

    public MonitorStatus getStatus(String arg)
    {
		((PerformanceMonitorStatus)status).setMaxTime(maxRequestTime);
		((PerformanceMonitorStatus)status).setMaxTspec(maxTimedTspecName);
		((PerformanceMonitorStatus)status).setMinTime(minRequestTime);
		((PerformanceMonitorStatus)status).setMinTspec(minTimedTspecName);
		((PerformanceMonitorStatus)status).setTotalRequestCount(totalRequests);
		((PerformanceMonitorStatus)status).setFailedRequestCount(failedRequests);
		((PerformanceMonitorStatus)status).setFailedTspecs(failedTspecsMap);
		((PerformanceMonitorStatus)status).setTotalTime(totalTime);
		((PerformanceMonitorStatus)status).setStartDate(startDate);
		return status;
    }

    public void run() {
		try {
			log.error("Joz Monitor : Performance Monitor Thread is getting started...");
			this.isRunning=true;
			while(true) {
				HashMap<String,Long> reqMap=(HashMap<String,Long>)reqQueue.poll();
				if (null == reqMap) {
					Thread.sleep(1000*5);//Wait for 5 seconds.
				}
				else {
					Set keys=reqMap.keySet();
					Iterator it=keys.iterator();
					String tspecName=(String)it.next();
					Long elapsedTime=reqMap.get(tspecName);
					if (-1L == elapsedTime) {
						failedRequests++;
						totalRequests++;
						Long failedCount=failedTspecsMap.get(tspecName);
						if (null == failedCount) {
							failedTspecsMap.put(tspecName,new Long(1));
						}
						else {
							failedTspecsMap.put(tspecName,new Long(failedCount.longValue()+1));
						}
					}
					else {
						if (failedRequests == totalRequests) {
							minRequestTime=elapsedTime;
							minTimedTspecName=tspecName;
							maxRequestTime=elapsedTime;
							maxTimedTspecName=tspecName;
						}
						else
						if (elapsedTime < minRequestTime) {
							minRequestTime=elapsedTime;
							minTimedTspecName=tspecName;
						}
						else if (elapsedTime > maxRequestTime) {
							maxRequestTime=elapsedTime;
							maxTimedTspecName=tspecName;
						}
						totalTime=totalTime+elapsedTime;
						totalRequests++;
					}
				}
			}
		}catch(Exception e) {
			this.isRunning=false;
			log.error("Joz Monitor : Performance Monitor Thread terminated... : "+e.getMessage());
		}
	}
}

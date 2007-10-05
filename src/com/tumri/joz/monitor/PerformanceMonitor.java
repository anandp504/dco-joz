package com.tumri.joz.monitor;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import org.apache.log4j.Logger;

/**
 * Monitor for JoZ Product Requests.
 *
 * @author Ramki
 */
public class PerformanceMonitor extends ComponentMonitor
{
	private static PerformanceMonitor instance=null;
	private static long totalRequests=0;
	private static long failedRequests=0;
	private static long minRequestTime=0;
	private static long maxRequestTime=0;
	private static long totalTime=0;
	private static String minTimedTspecName=null;
	private static String maxTimedTspecName=null;
	private static Map<String,Long> failedTspecsMap=new HashMap<String,Long>();
	private static Date startDate=new Date();

    private static Logger log = Logger.getLogger(PerformanceMonitor.class);

    private PerformanceMonitor()
    {
       super("product-requests", new PerformanceMonitorStatus("performance"));
    }

    public static PerformanceMonitor getInstance() {
		if ( null == instance) {
			synchronized(PerformanceMonitor.class) {
				if (null == instance) {
					instance=new PerformanceMonitor();
				}
			}
		}
		return instance;
	}

	public static void registerSuccess(String tspecName,long elapsedTime) {
		synchronized(PerformanceMonitor.class) {
			if (totalRequests == failedRequests) {
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

	public static void registerFailure(String tspecName) {
		synchronized(PerformanceMonitor.class) {
			failedRequests++;
			totalRequests++;
		}
		Long failedCount=failedTspecsMap.get(tspecName);
		if (null == failedCount) {
			failedTspecsMap.put(tspecName,new Long(1));
		}
		else {
			failedTspecsMap.put(tspecName,new Long(failedCount.longValue()+1));
		}
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
}

package com.tumri.joz.monitor;

import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.ConcurrentHashMap;
import org.apache.log4j.Logger;

/**
  * JoZ Performance statistics monitor.
  *
  * @author Ramki
  */
public class PerformanceMonitor extends ComponentMonitor
{
	private static PerformanceMonitor instance=null;
	private AtomicLong totalRequests=new AtomicLong();
	private AtomicLong failedRequests=new AtomicLong();
	private AtomicLong minRequestTime=new AtomicLong();
	private AtomicLong maxRequestTime=new AtomicLong();
	private AtomicLong totalTime=new AtomicLong();
	private AtomicReference minTimedTspecName=new AtomicReference();
	private AtomicReference maxTimedTspecName=new AtomicReference();
	private ConcurrentHashMap<String,Long> failedTspecsMap=new ConcurrentHashMap<String,Long>();
	private Date startDate=new Date();

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
				}
			}
		}
		return instance;
	}

	public void registerSuccess(String tspecName,long elapsedTime) {
		if (totalRequests.get() == failedRequests.get()) {
			maxRequestTime.set(elapsedTime);
			maxTimedTspecName.set((String)tspecName);
			minRequestTime.set(elapsedTime);
			minTimedTspecName.set((String)tspecName);
		}
		else {
			if (elapsedTime < minRequestTime.get()) {
				minRequestTime.set(elapsedTime);
				minTimedTspecName.set((String)tspecName);
			}
			else if ( elapsedTime > maxRequestTime.get()) {
				maxRequestTime.set(elapsedTime);
				maxTimedTspecName.set((String)tspecName);
			}
		}
		totalTime.addAndGet(elapsedTime);
		totalRequests.getAndIncrement();
	}

	public void registerFailure(String tspecName) {
		failedRequests.getAndIncrement();
		Long failedCount=(Long)failedTspecsMap.get(tspecName);
		if (null == failedCount) {
			failedTspecsMap.put(tspecName,new Long(1));
		}
		else {
			failedTspecsMap.put(tspecName,new Long(failedCount.longValue()+1));
		}
	}

    public MonitorStatus getStatus(String arg)
    {
		((PerformanceMonitorStatus)status).setMaxTime(maxRequestTime.get());
		((PerformanceMonitorStatus)status).setMaxTspec((String)maxTimedTspecName.get());
		((PerformanceMonitorStatus)status).setMinTime(minRequestTime.get());
		((PerformanceMonitorStatus)status).setMinTspec((String)minTimedTspecName.get());
		((PerformanceMonitorStatus)status).setTotalRequestCount(totalRequests.get());
		((PerformanceMonitorStatus)status).setFailedRequestCount(failedRequests.get());
		((PerformanceMonitorStatus)status).setFailedTspecs(failedTspecsMap);
		((PerformanceMonitorStatus)status).setTotalTime(totalTime.get());
		((PerformanceMonitorStatus)status).setStartDate(startDate);
		return status;
    }
}

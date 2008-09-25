package com.tumri.joz.monitor;

import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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
	private static Date startDate=new Date();

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

    @SuppressWarnings("unchecked")
    public void registerSuccess(String tspecName,long elapsedTime) {
		if (totalRequests.get() == failedRequests.get()) {
			maxRequestTime.set(elapsedTime);
			maxTimedTspecName.set(tspecName);
			minRequestTime.set(elapsedTime);
			minTimedTspecName.set(tspecName);
		}
		else {
			if (elapsedTime < minRequestTime.get()) {
				minRequestTime.set(elapsedTime);
				minTimedTspecName.set(tspecName);
			}
			else if ( elapsedTime > maxRequestTime.get()) {
				maxRequestTime.set(elapsedTime);
				maxTimedTspecName.set(tspecName);
			}
		}
		totalTime.addAndGet(elapsedTime);
		totalRequests.getAndIncrement();
	}

	public void registerFailure(String tspecName) {
		failedRequests.getAndIncrement();
		Long failedCount=failedTspecsMap.get(tspecName);
		if (null == failedCount) {
			failedTspecsMap.put(tspecName,1L);
		}
		else {
			failedTspecsMap.put(tspecName, failedCount + 1);
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

    public void reset() {
		totalRequests.set(0L);
		failedRequests.set(0L);
		totalTime.set(0L);
		minRequestTime.set(0L);
		maxRequestTime.set(0L);
		failedTspecsMap.clear();
		startDate=new Date();
	}
}

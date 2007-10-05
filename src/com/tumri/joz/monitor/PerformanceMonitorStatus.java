package com.tumri.joz.monitor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;

/**
 * Status description Class for the performance monitor
 *
 * @author vijay
 */
public class PerformanceMonitorStatus extends MonitorStatus
{
	private String  maxTspec = null;
    private String  minTspec = null;
    private long    maxTime  = 0;
    private long    minTime  = 0;
	private long    totalCount = 0;
	private long    failedCount = 0;
	private long 	totalTime = 0;
	private Date	startDate = null;
	private Map<String, Long>  failedTspecs = null;

    public  PerformanceMonitorStatus(String name)
    {
        super(name);
		failedTspecs = new HashMap<String, Long>();
    }

    public void setMaxTspec(String tspec)
    {
       maxTspec = tspec;
    }

    public void setMinTspec(String tspec)
    {
       minTspec = tspec;
    }

    public String getMaxTspec()
    {
       return maxTspec;
    }

    public String getMinTspec()
    {
       return minTspec;
    }

    public void setMaxTime(long time)
    {
       maxTime = time;
    }

    public void setMinTime(long time)
    {
       minTime = time;
    }

    public long getMaxTime()
    {
       return maxTime;
    }

    public long getMinTime()
    {
       return minTime;
    }

	public void setTotalRequestCount(long count)
	{
		totalCount = count;
	}

	public void setFailedRequestCount(long count)
	{
		failedCount = count;
	}

	public long getTotalRequestCount()
	{
		return totalCount;
	}

	public long getFailedRequestCount()
	{
		return failedCount;
	}

	public void setFailedTspecs(Map<String, Long> tspecs)
	{
		failedTspecs.putAll(tspecs);
	}

    public Map<String, Long> getFailedTspecs()
	{
		return failedTspecs;
	}

	public void setTotalTime(long totalTime) {
		this.totalTime=totalTime;
	}

	public long getTotalTime() {
		return this.totalTime;
	}

	public void setStartDate(Date startDate) {
		this.startDate=startDate;
	}

	public Date getStartDate() {
		return this.startDate;
	}

    public String toHTML()
    {
        StringBuffer sb = new StringBuffer();
        if (totalCount > 0)
        {
           sb.append("<table>");
		   sb.append("<tr> <td>");
		   sb.append("Total requests received: "+totalCount);
		   sb.append("</td> </tr>");
		   sb.append("<tr> <td>");
		   sb.append("Total requests failed: "+failedCount);
		   sb.append("</td> </tr>");
		   sb.append("<tr> <td>");
		   sb.append("Worst performing tspec: "
					  +encode(maxTspec)+" (serviced in "+maxTime/(long)1000000+" msecs)");
		   sb.append("</td> </tr>");
		   sb.append("<tr> <td>");
		   sb.append("Best performing tspec: "
					  +encode(minTspec)+" (serviced in "+minTime/(long)1000000+" msecs)");
		   sb.append("</td> </tr>");
		   if (failedTspecs.size() > 0) {
		   		sb.append("<tr> <td>");
		   		sb.append("Failed Tspecs are: ");
		   		sb.append("</td> </tr>");
				Iterator it = failedTspecs.keySet().iterator();
			    while (it.hasNext()) {
		   			sb.append("<tr> <td>");
					String key = (String)it.next();
					sb.append(encode(key)+ "    ("+failedTspecs.get(key).longValue()+" times)");
		   			sb.append("</td> </tr>");
				}
			}
			sb.append("</table>");
		 }
         return new String(sb);
    }

	private String encode(String s) {
        if (s == null)
            return null;
        StringBuffer sb = new StringBuffer();
        for (int i = 0, len = s.length(); i < len; i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"':
                    sb.append("&#34;");
                    break;
                case '\'':
                    sb.append("&#39;");
                    break;
                default:
                    sb.append(c);
            }
        }
        return sb.toString();
    }

}

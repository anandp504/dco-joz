package com.tumri.joz.monitor;

import org.apache.log4j.Logger;

/**
 * Monitor for JoZ TSpec Component
 *
 * @author vijay
 */
public class TspecMonitor extends ComponentMonitor 
{
    private static Logger log = Logger.getLogger(TspecMonitor.class);

    public TspecMonitor()
    {
       super("tspec", new TspecMonitorStatus("tspec"));
    } 

    public MonitorStatus getStatus(String arg) 
    {
        return status;
    }

}

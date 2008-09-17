package com.tumri.joz.monitor;


/**
 * Abstract Component Monitor for JoZ 
 *
 * @author vijay
 */
public abstract class ComponentMonitor 
{
    private String name = null;
    protected MonitorStatus status = null;

    public ComponentMonitor(String name, MonitorStatus m)
    {
       this.name = name;
       status = m;
    } 

    public String getName()
    {
       return name;
    }

    public abstract MonitorStatus getStatus(String arg);

}

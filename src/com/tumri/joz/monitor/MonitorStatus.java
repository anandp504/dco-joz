package com.tumri.joz.monitor;

import java.util.Date; 
import java.util.List; 
import java.util.Map;
import java.util.ArrayList; 
import java.util.HashMap; 

import org.apache.log4j.Logger;

/**
 * Status description Class for JoZ 
 *
 * @author vijay
 */
public abstract class MonitorStatus 
{
    private static Logger log = Logger.getLogger(MonitorStatus.class);

    protected String name = null;
    protected Date date;
    protected Map<String, List<String>> data = null;

    public  MonitorStatus(String name) 
    {
        this.name = name;
        date = new Date();
        data = new HashMap<String, List<String>>();
    }

    public void setName(String name) 
    {
        this.name = name;
    }

    public MonitorStatus getStatus()
    {
        return this;
    }

    public void setValues(String name, List<String> values, boolean overwrite)
    {
        if (overwrite)
           data.put(name, values);
        else {
            if (data.containsKey(name))
                data.get(name).addAll(values);
            else
                data.put(name, values);
        }
    }

    public void setValues(String name, List<String> values)
    {
        setValues(name, values, true);
    }

    public void setValue(String name, String value, boolean overwrite)
    {
        List<String> tmp = new ArrayList<String>();
        tmp.add(value);
        setValues(name, tmp, overwrite);     
    }

    public void setValue(String name, String value)
    {
        setValue(name, value, true);
    }

    public List<String> getValue(String name)
    {
        return data.get(name);
    }

    public abstract String toHTML();
    
}

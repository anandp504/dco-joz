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
public class TspecMonitorStatus extends MonitorStatus
{
    public  TspecMonitorStatus(String name) 
    {
        super(name);
    }

    public String toHTML()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<span>");
        if (name != null)
            sb.append("<h2><b>"+name+"</b></h2>");
        sb.append("<b>"+date.toString()+"</b>");
        sb.append("<br>");
        for (String s: data.keySet()) {
           sb.append("<b>"+s+"</b>");
           sb.append("<br>");
           sb.append("<ul>");
           for (String s1: data.get(s)) 
             sb.append("<li> "+s1+" </li>");
           sb.append("</ul>");
        }
        sb.append("</span>");
        return new String(sb);
    }
    
}

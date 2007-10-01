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
public class CampaignMonitorStatus extends MonitorStatus
{
    public  CampaignMonitorStatus(String name) 
    {
        super(name);
    }

    public String toHTML()
    {
        StringBuffer sb = new StringBuffer();
        sb.append("<table>");
        for (String s: data.keySet()) {
           for (String s1: data.get(s)) {
                sb.append("<tr> <td>");
                sb.append("<a href=\"monitor?getaddata="+s1+"\">"+s1+"</a>");
                sb.append("</tr> </td>");
           }            
        }
        sb.append("</table>");
        return new String(sb);
    }
    
}

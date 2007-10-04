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
 * Status description Class for JoZ
 *
 * @author vijay
 */
public class ProductQueryMonitorStatus extends MonitorStatus
{
    private List<Map<String, String>> products = null;

    public  ProductQueryMonitorStatus(String name)
    {
        super(name);
    }

    public void setProducts(List<Map<String, String>> products)
    {
       this.products = products;
    }

    public List<Map<String, String>>  getProducts()
    {
       return products;
    }

    public String toHTML()
    {
        StringBuffer sb = new StringBuffer();
        if (products != null)
        {
           sb.append("<table>");
           for (int i=0; i<products.size(); i++) {
                Map<String, String> obj = (Map<String, String>)products.get(i);
                Set keySet=obj.keySet();
                Iterator keys=keySet.iterator();
                while(keys.hasNext()) {
					String key=(String)keys.next();
					String value=(String)obj.get(key);
                	sb.append("<tr> <td> "+key+"</td><td> "+value+"</td></tr>");
				}
           }
           sb.append("</table>");
        }
        return new String(sb);
    }
}

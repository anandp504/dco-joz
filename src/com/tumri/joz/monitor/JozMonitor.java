package com.tumri.joz.monitor;

import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;


import org.apache.log4j.Logger;

/**
 * Monitor for JoZ 
 *
 * @author vijay
 */
public class JozMonitor 
{

    private static Logger log = null;
    private static List<ComponentMonitor> components = null;

    static {
        log = Logger.getLogger(JozMonitor.class);
        components = new ArrayList<ComponentMonitor>();
        install(new ProductDBMonitor());
        install(new ProductQueryMonitor());
        install(new CampaignMonitor());
    }


    public static String serviceQuery(String query)
    {
        StringBuffer s = null;

        if (query.equals(""))
           return(null);

        StringTokenizer st = new StringTokenizer(query, "=");
        String cmd = st.nextToken();
        String arg = null;
        while (st.hasMoreTokens())
             arg = st.nextToken();
        
        if (cmd.trim().equals("all")) {
            if (components.size() > 0) {
                s = new StringBuffer();
                for (ComponentMonitor comp: components)
                    s.append(comp.getStatus(arg).toHTML());
            }
            else
                s.append("<b>No components registered with JoZ Monitor!</b>");
        }
        else {
           for (ComponentMonitor comp: components) {
               if (comp.getName().equals(cmd.trim())) {
                    if (s == null)
                        s = new StringBuffer();
                    s.append(comp.getStatus(arg).toHTML());
               }
           }
           if (s == null) {
               s = new StringBuffer();
               s.append("<b>'"+cmd+"' component not found!</b>");
           }
        }
               
        return new String(s);
    }

    public static void install(ComponentMonitor component) 
    {
        components.add(component);
    }
    
}

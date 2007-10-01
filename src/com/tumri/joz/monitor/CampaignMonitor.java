package com.tumri.joz.monitor;

import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import com.tumri.joz.products.ProductDB;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.util.OSpecCondition;
import com.tumri.cma.CMAFactory;
import com.tumri.cma.service.OSpecProvider;
import com.tumri.joz.campaign.CampaignDB;

/**
 * Monitor for JoZ Campaign component
 *
 * @author vijay
 */
public class CampaignMonitor extends ComponentMonitor 
{

    private static Logger log = Logger.getLogger(CampaignMonitor.class);

    public CampaignMonitor()
    {
       super("campaign", new CampaignMonitorStatus("campaign"));
    } 

    private List<OSpec> getAllOSpecs()
    {
       List<OSpec> ospecs = null;
       try {
            CampaignDB campaigns = CampaignDB.getInstance();
            ospecs = campaigns.getAllOSpecs();
       }
       catch (Exception ex) {
            status.setValue("Error", ex.getMessage());
       } 

       return ospecs;
    }

    public MonitorStatus getStatus(String arg) 
    {
        List<OSpec> list = getAllOSpecs();
        if (list != null) {
            Iterator it = list.iterator();
            while (it.hasNext())
                status.setValue("opecs", ((OSpec)it.next()).getName(), false);
        }
        return status;
    }
    
}

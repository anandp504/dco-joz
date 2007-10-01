package com.tumri.joz.monitor;

import org.apache.log4j.Logger;
import com.tumri.joz.products.ProductDB;

/**
 * Monitor for JoZ ProductDB component
 *
 * @author vijay
 */
public class ProductDBMonitor extends ComponentMonitor 
{

    private static Logger log = Logger.getLogger(ProductDBMonitor.class);

    public ProductDBMonitor()
    {
       super("productdb", new ProductDBMonitorStatus("productdb"));
    } 

    public MonitorStatus getStatus(String arg) 
    {
        ProductDB pdb = ProductDB.getInstance();
        int count = 0;
        if (pdb != null)
            count = pdb.getAll().size();

        status.setValue("counts", Integer.toString(count));
        return status;
    }
    
}

package com.tumri.joz.monitor;

import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

public class JozMonitorServlet extends HttpServlet
{
    private static Logger log = Logger.getLogger (JozMonitorServlet.class);

    public void
    doGet (HttpServletRequest request, HttpServletResponse response)
    throws IOException, ServletException
    {
	doService (request, response);
    }

    public void
    doPost (HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException
    {
	doService (request, response);
    }

    protected void
    doService (HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException
    {
	    response.setContentType ("text/plain");
	    PrintWriter out = response.getWriter();

	    try
	    {
			String result;
			ProductQueryMonitor pqm = new ProductQueryMonitor();
            String defaultRealmQuery = AppProperties.getInstance().getDefaultRealmGetAdDataCommandStr();
            ProductQueryMonitorStatus pqms = (ProductQueryMonitorStatus)pqm.getStatusGetAdData(defaultRealmQuery);
			if (pqms.getProducts() !=null && pqms.getProducts().size() > 0)
				result = "success";
			else
				result = "failed";
            out.print(result);
        }
	    catch (Exception e)
	    {
            out.print("failed");
	    }
    }


}

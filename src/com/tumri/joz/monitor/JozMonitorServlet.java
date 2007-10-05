package com.tumri.joz.monitor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ResourceBundle;
import java.net.URLDecoder;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpIFASLWriter;

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
	    //String query = request.getQueryString ();

	    response.setContentType ("text/plain");
	    PrintWriter out = response.getWriter();

	    try
	    {
			String result = null;
			ProductQueryMonitor pqm = new ProductQueryMonitor();
			ProductQueryMonitorStatus pqms = (ProductQueryMonitorStatus)pqm.getStatus("http://default");
			if (pqms.getProducts().size() > 0)
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

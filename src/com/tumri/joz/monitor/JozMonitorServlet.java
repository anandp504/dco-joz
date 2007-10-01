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
	    String query = request.getQueryString ();

	    response.setContentType ("text/html");
	    PrintWriter out = response.getWriter();

	    try
	    {
	        query = URLDecoder.decode (query, "UTF-8");
	        log.info ("Monitor Query= " + query);
            String results = JozMonitor.serviceQuery(query);

            out.print("<html><head><title>Joz Monitor</title></head>");
            out.print("<body>");
            out.print(results);
            out.print("</body>");
            out.print("</html>");
        }
	    catch (Exception e)
	    {
            out.print("<html>");
            out.print("<title>Joz Console</title>");
            out.print("<body>");
            out.print("<strong>Joz Console Ver 0.1</strong>");
            out.print("</br>");
            out.print("<hr>");
            out.print("<strong>Links</strong>");
            out.print("</br>");
            out.print("<a href=\"monitor?campaign\">get-ad-data</a>");
            out.print("</body>");
            out.print("</html>");
	    }
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (JozMonitorServlet.class);
}

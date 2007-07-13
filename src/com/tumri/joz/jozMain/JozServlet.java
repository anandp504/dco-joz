// Joz servlet

package com.tumri.joz.jozMain;

import java.io.IOException;
//import java.io.PrintWriter;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.tumri.utils.strings.RFC1738Decoder;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpIFASLWriter;

public class JozServlet extends HttpServlet
{
    private static Logger log = null;

    static
    {
	String filename = System.getProperty ("LOG4J_PROPS");
	if (filename != null)
	{
	    System.out.println ("Loading log4j properties from " + filename);
	    PropertyConfigurator.configure (filename);
	}
	log = Logger.getLogger (JozServlet.class);
    }

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
	ServletOutputStream out = response.getOutputStream ();

	try
	{
	    log.info ("Query: " + query);
	    query = RFC1738Decoder.convertString (query);
	    log.info ("Query= " + query);

	    long start_time = System.nanoTime ();

	    Command cmd = Command.parse (query);
	    Sexp result = cmd.process ();

	    boolean uppercase_syms = cmd.need_uppercase_syms ();
	    SexpIFASLWriter.write (out, result, uppercase_syms);

	    long end_time = System.nanoTime ();

	    // FIXME: Need option to not print entire s-expression,
	    // they can be pretty large.
	    log.info ("Result: " + result);
	    log.info ("Response time: " + ((end_time - start_time) / 1000.0)
		      + " usecs");
	}
	catch (Exception e)
	{
	    log.info ("Bad command: " + e.toString ());
	    // ??? Protocol apparently says to return nothing.  True?
	}
    }
}

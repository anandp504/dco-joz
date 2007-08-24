test, break build on purpose
// Initialization servlet

package com.tumri.joz.jozMain;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ResourceBundle;

import javax.servlet.ServletException;
import javax.servlet.http.*;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class InitServlet extends HttpServlet
{
    private static Logger log = null;

    static
    {
	String filename = "../conf/jozLog4j.xml";
	try
	{
	    // hack to check for file-not-found
            FileInputStream fis = new FileInputStream (filename);
	    try { fis.close (); } catch (Exception e) { }
	    System.out.println ("Loading log4j properties from " + filename);
	    DOMConfigurator.configure (filename);
	    log = Logger.getLogger (InitServlet.class);
	}
	catch (FileNotFoundException e)
	{
	    log = Logger.getLogger (InitServlet.class);
	    log.error ("File not found: " + filename);
	}
    }

    public void
    init ()
	throws ServletException
    {
	log.info ("Initializing joz ...");

	try
	{
	    // Initialize this first in case something needs a property.
	    String config_dir = getInitParameter ("tumri.joz.config.dir");
	    String config_file = getInitParameter ("tumri.joz.config.file");
	    String app_config_file = getInitParameter ("tumri.joz.app.config.file");
	    Props.init (config_dir, config_file, app_config_file);

	    JozData.init ();
	}
	catch (Exception e)
	{
	    log.error (e.toString ());
	    throw new ServletException (e.toString ());
	}
    }

    public void
    doGet (HttpServletRequest request, HttpServletResponse response)
	throws IOException, ServletException
    {
	// FIXME
    }
}

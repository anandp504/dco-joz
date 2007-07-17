// User-configurable parameters (implemented as properties).

package com.tumri.joz.jozMain;

import java.io.File;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Properties;

import org.apache.log4j.Logger;

public class Props
{
    public static int
    get_server_port ()
    {
	if (server_port == -1)
	    server_port = get_int_property (SERVER_PORT, DEFAULT_SERVER_PORT);
	return server_port;
    }

    public static int
    get_monitor_port ()
    {
	if (monitor_port == -1)
	    monitor_port = get_int_property (MONITOR_PORT, DEFAULT_MONITOR_PORT);
	return monitor_port;
    }

    public static String
    get_joz_version ()
    {
	if (joz_version == null)
	    joz_version = props.getProperty (JOZ_VERSION);
	if (joz_version == null)
	{
	    log.error ("Property " + JOZ_VERSION + " not specified.");
	    joz_version = "unknown";
	}
	return joz_version;
    }

    // The host name really isn't a property, but its kept here with the
    // other configuration parameters.
    // WARNING: Must be called after init().

    public static String
    get_host_name ()
    {
	assert (host_name != null);
	return host_name;
    }

    public static void
    init ()
    {
	read_properties (props, PROP_DIR + "/" + CONFIG_PROPERTIES_FILE);
	read_properties (props, PROP_DIR + "/" + APP_PROPERTIES_FILE);
	find_host_name ();
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (Props.class);

    // Property files live here.
    private static final String PROP_DIR = "../conf";

    // This file contains user-configurable properties.
    private static final String CONFIG_PROPERTIES_FILE = "joz.properties";

    // This file contains properties specific to this version of joz
    // and should in general not be editied.
    private static final String APP_PROPERTIES_FILE = "joz-app.properties";

    // holds all properties from both property files
    // ??? one per file?
    private static Properties props;

    private static final String SERVER_PORT = "server.port";
    private static final String MONITOR_PORT = "monitor.port";
    private static final String JOZ_VERSION = "joz.version";

    private static final int DEFAULT_SERVER_PORT = 2501 /*42000*/;
    private static final int DEFAULT_MONITOR_PORT = 42080;

    // -1 = "unset"
    private static int server_port = -1;
    private static int monitor_port = -1;

    private static String joz_version = null;
    private static String host_name = null;

    private static void
    read_properties (Properties props, String path)
    {
	InputStream stream = null;

	try
	{
	    File file = new File (path);
	    stream = new FileInputStream (file);
	    props.load (stream);
	}
	catch (IOException e)
	{
	    System.out.println (e.getMessage ());
	}
	finally
	{
	    try
	    {
		if (stream != null)
		    stream.close ();
	    }
	    catch (Exception e) {}
	}
    }

    private static void
    find_host_name ()
    {
	try
	{
	    host_name = java.net.InetAddress.getLocalHost ().getHostName ();
	}
	catch (UnknownHostException e)
	{
	    System.out.println (e.getMessage ());
	    host_name = "unknown";
	}
    }

    private static int
    get_int_property (String name, int default_value)
    {
	try
	{
	    String p = props.getProperty (name);
	    if (p != null)
	    {
		Integer i = Integer.valueOf (p);
		return i.intValue ();
	    }
	    else
	    {
		return default_value;
	    }
	}
	catch (Exception e)
	{
	    log.error ("Unable to obtain property " + name + ": "
		       + e.getMessage ());
	    log.error ("Using default value: " + default_value);
	    return default_value;
	}
    }
}

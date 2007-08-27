// Testsuite properties interface.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;
import java.io.File;
import java.io.FileInputStream;

import java.util.Properties;

public class TestsuiteProps
{
    private static final String JOZ_URL = "joz.url";
    private static final String DEFAULT_JOZ_URL = "http://localhost:18080/Joz/joz";

    private static Properties props = new Properties ();

    public static void
    load_props (String path)
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

    public static String
    get_joz_url ()
    {
	if (props == null)
	    return DEFAULT_JOZ_URL;
	String url = props.getProperty (JOZ_URL);
	if (url == null)
	    return DEFAULT_JOZ_URL;
	return url;
    }
}

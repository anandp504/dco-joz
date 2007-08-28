// Http client driver for joz testsuite

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayInputStream;

import org.apache.commons.httpclient.*;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.*;

import com.tumri.zini.transport.FASLType;
import com.tumri.zini.transport.FASLReader;

public class JozClient
{
    public JozClient (String server_url, int timeout_seconds)
    {
	_server_url = server_url;
	_http_client = new HttpClient ();
	_http_client.getHttpConnectionManager ().getParams ()
	    .setConnectionTimeout (timeout_seconds * 1000);
    }

    public JozClient (String server_url)
    {
	this (server_url, DEFAULT_TIMEOUT);
    }

    public static final String SERVER_MSG_PROPERTY_KEY = "Message";

    public static final int DEFAULT_TIMEOUT = 10; // seconds

    // Result is null if there is a problem.

    public InputStream
    execute (String cmd)
    {
	_get_method = new GetMethod (_server_url);

	log.info ("Sent " + cmd);

	try
	{
	    // Execute the method.
	    NameValuePair[] pair = new NameValuePair[1];
	    pair[0] = new NameValuePair (SERVER_MSG_PROPERTY_KEY, cmd);
	    _get_method.setQueryString (pair);
	    String query = _get_method.getQueryString (); // FIXME: needed?

            int status_code = _http_client.executeMethod (_get_method);
	    byte[] response = _get_method.getResponseBody ();

	    // Try to pretty-print the response.  It's in IFASL format.

	    try
	    {
		InputStream in = new ByteArrayInputStream (response);
		FASLReader fr = new FASLReader (in);
		int version = fr.readVersion ();
		FASLType ft = fr.read ();
		log.info ("Got " + ft.toString ());
	    }
	    catch (Exception e)
	    {
		// That failed, just do something basic.
		log.info ("Got " + new String (response));
	    }

	    if (status_code != HttpStatus.SC_OK)
	    {
		log.error ("http request failed: " + _get_method.getStatusLine ());
		return null;
	    }
	    InputStream in = new ByteArrayInputStream (response);
	    return in;
        }
	catch (HttpException e)
	{
	    log.error ("Fatal protocol violation", e);
	    return null;
	}
	catch (IOException e)
	{
	    log.error ("Fatal IO exception", e);
	    return null;
	}
	catch (Exception e)
	{
	    log.error ("Unexpected exception reading response", e);
	    return null;
        }
	finally
	{
	    _get_method.releaseConnection ();
	}
    }

    // implementation details -------------------------------------------------

    private static JozTestsuite.Logger log = new JozTestsuite.Logger ();

    private String _server_url;
    private HttpClient _http_client;
    private HttpMethod _get_method;
}

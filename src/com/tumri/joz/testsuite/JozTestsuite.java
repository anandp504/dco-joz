// Joz Testsuite harness + utilities.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

public class JozTestsuite
{
    private static final String JOZ_PROP_FILE = "src/conf/testsuite.properties";

    // #seconds to wait before retrying to connect
    // NOTE: This doesn't include the JozClient timeout.
    private static final int READY_TIMEOUT = 30;

    // #reconnect tries
    private static final int NR_CONNECT_TRIES = 4;

    // ??? temp hack
    private static JozTestsuite.Logger log =
	new JozTestsuite.Logger ();

    // Simple testsuite driver.

    public static void
    main (String[] args)
    {
	if (args.length == 0)
	{
	    System.out.println ("Please select a test.");
	    return;
	}

	String prop_file = JOZ_PROP_FILE;
	TestsuiteProps.load_props (prop_file);

	if (! wait_until_ready ())
	{
	    log.error ("joz server not ready");
	    System.exit (1);
	}

	if (args.length == 1
	    && args[0].equals ("all"))
	{
	    TestGetCompleteTaxonomy.run ();
	    TestGetCounts.run ();
	    TestGetAttrsAndMetadata.run ();
	    TestGetProviders.run ();
	    TestGetRootCategories.run ();
	    TestIncorpMappingDeltas.run ();
	    TestTSpecAdd.run ();
	    TestTSpecDelete.run ();
	    TestTabulateSearchResults.run ();
	    return;
	}

	for (int i = 0; i < args.length; ++i)
	{
	    if (args[i].equals ("get-complete-taxonomy"))
		TestGetCompleteTaxonomy.run ();
	    else if (args[i].equals ("get-counts"))
		TestGetCounts.run ();
	    else if (args[i].equals ("get-attributes-and-metadata"))
		TestGetAttrsAndMetadata.run ();
	    else if (args[i].equals ("get-providers"))
		TestGetProviders.run ();
	    else if (args[i].equals ("get-root-categories"))
		TestGetRootCategories.run ();
	    else if (args[i].equals ("incorp-mapping-deltas"))
		TestIncorpMappingDeltas.run ();
	    else if (args[i].equals ("tspec-add"))
		TestTSpecAdd.run ();
	    else if (args[i].equals ("tspec-delete"))
		TestTSpecDelete.run ();
	    else if (args[i].equals ("tabulate-search-results"))
		TestTabulateSearchResults.run ();
	}

	// FIXME: wip
    }

    // Result is true if connection is established, otherwise false.

    private static boolean
    wait_until_ready ()
    {
	// Joz can take a minute to start, so use an extra long timeout.
	JozClient jc = new JozClient (TestsuiteProps.get_joz_url (), 30);

	for (int tries = 0; tries < NR_CONNECT_TRIES; ++tries)
	{
	    System.out.println ("Try " + tries);

	    // NOTE: soz3 isn't expected or required to understand (ready?).
	    // This is just a command to provoke a response.
	    InputStream is = jc.execute ("(ready?)");
	    if (is != null)
		return true;

	    if (tries + 1 == NR_CONNECT_TRIES)
		return false; // no point in performing the final sleep

	    try
	    {
		Thread.sleep (READY_TIMEOUT * 1000);
	    }
	    catch (InterruptedException e)
	    {
	    }
	}

	return false;
    }

    // Utility to look for (:error ...) responses from soz3.

    public static boolean
    error_p (Sexp s)
    {
	if (! s.isSexpList ())
	    return false;
	SexpList l = s.toSexpList ();
	if (l.size () >= 1
	    && l.get (0).isSexpKeyword ())
	{
	    SexpKeyword k = l.get (0).toSexpKeyword ();
	    return k.equalsStringIgnoreCase (":error");
	}
	return false;
    }

    // Interface to log4j.
    // FIXME: temp hack for now, loses source location for example

    public static class Logger
    {
	public Logger ()
	{
	}

	public void
	error (String msg)
	{
	    log.error (msg);
	}

	public void
	error (String msg, Exception e)
	{
	    log.error (msg + ": " + e);
	}

	public void
	info (String msg)
	{
	    log.info (msg);
	}

	public void
	fail (String test, String msg)
	{
	    log.info ("FAIL: " + test + ": " + msg);
	}

	public void
	pass (String test)
	{
	    log.info ("PASS: " + test);
	}

	private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger (Logger.class);
    }
}

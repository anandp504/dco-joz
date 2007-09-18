// Test the get-counts command.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.*;

public class TestGetCounts
{
    public static final String me = "get-counts";

    public static void
    run ()
    {
	JozClient jc = new JozClient (TestsuiteProps.get_joz_url ());
	String tspec_name = "|T-SPEC-http://www.shop4flowers.com/|"; // FIXME: Should be a parameter.
	//tspec_name = "nil"; // for entire mup
	InputStream is = jc.execute ("(" + me + " " + tspec_name + ")");

	if (is == null)
	{
	    log.fail (me, "protocol error");
	    return;
	}

	try
	{
	    SexpIFASLReader r = new SexpIFASLReader (is);

	    Sexp s = r.read ();
	    if (JozTestsuite.error_p (s))
	    {
		log.fail (me, s.toString ());
		return;
	    }
	    if (! validate_counts (s))
	    {
		log.fail (me, "not a valid get-counts result");
		return;
	    }

	    if (r.read () != null)
	    {
		log.fail (me, "should be only one sexp in result");
		return;
	    }
	}
	catch (IOException e)
	{
	    log.fail (me, "i/o exception");
	    return;
	}
	catch (IFASLFormatException e)
	{
	    log.fail (me, "malformed sexp");
	    return;
	}

	log.pass (me);
    }

    // implementation details -------------------------------------------------

    private static JozTestsuite.Logger log = new JozTestsuite.Logger ();

    private static boolean
    validate_counts (Sexp s)
    {
	if (! s.isSexpList ())
	{
	    log.info ("counts is not a list");
	    return false;
	}

	SexpList counts = s.toSexpList ();
	if (counts.size () != 3)
	{
	    log.info ("counts not list of three elements");
	    return false;
	}

	if (! validate_count_set (counts.get (0), "categories"))
	    return false;
	if (! validate_count_set (counts.get (1), "brands"))
	    return false;
	if (! validate_count_set (counts.get (2), "merchants"))
	    return false;

	return true;
    }

    // Subroutine of {validate_counts} to simplify it.
    // Validate a list of (symbol count) elements.

    private static boolean
    validate_count_set (Sexp s, String kind)
    {
	if (! s.isSexpList ())
	{
	    log.info (kind + " counts is not a list");
	    return false;
	}

	SexpList l = s.toSexpList ();

	for (Sexp c : l)
	{
	    if (! c.isSexpList ())
	    {
		log.info (kind + " element is not a list: " + c.toString ());
		return false;
	    }
	    SexpList count = c.toSexpList ();
	    if (count.size () != 2)
	    {
		log.info (kind + " element is not a list of two elements: " + c.toString ());
		return false;
	    }
	    if (! count.get (0).isSexpString ()
		|| ! count.get (1).isSexpNumber ())
	    {
		log.info (kind + " element not (string count): " + c.toString ());
		return false;
	    }
	}

	return true;
    }
}

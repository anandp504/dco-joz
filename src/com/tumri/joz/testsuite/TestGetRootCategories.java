// Test the get-root-categories command.
// NOTE: This command is not implemented as it appears no one uses it anymore.
// All we test for is that we get some kind of response back from soz3.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.*;

public class TestGetRootCategories
{
    public static final String me = "get-root-categories";

    public static void
    run ()
    {
	JozClient jc = new JozClient (TestsuiteProps.get_joz_url ());
	InputStream is = jc.execute ("(" + me + ")");

	if (is == null)
	{
	    log.fail (me, "protocol error");
	    return;
	}

	try
	{
	    SexpIFASLReader r = new SexpIFASLReader (is);

	    Sexp s = r.read ();
	    if (false // ??? Not implemented so error is expected.
		&& JozTestsuite.error_p (s))
	    {
		log.fail (me, s.toString ());
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
}

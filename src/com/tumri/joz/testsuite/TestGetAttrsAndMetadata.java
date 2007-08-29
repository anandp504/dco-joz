// Test the get-attributes-and-metadata command.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.*;

public class TestGetAttrsAndMetadata
{
    public static final String me = "get-attributes-and-metadata";

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
	    if (JozTestsuite.error_p (s))
	    {
		log.fail (me, s.toString ());
		return;
	    }
	    if (! validate_attributes_and_metadata (s))
	    {
		log.fail (me, "not a valid " + me + " result");
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

    // Validate response.
    // Result is true if {s} is valid.
    //
    // NOTE: This could do more validation.  Maybe over time it will,
    // depending on what bugs occur.

    private static boolean
    validate_attributes_and_metadata (Sexp s)
    {
	if (! s.isSexpList ())
	{
	    log.info (me + " result is not a list");
	    return false;
	}

	return true;
    }

    // implementation details -------------------------------------------------

    private static JozTestsuite.Logger log = new JozTestsuite.Logger ();
}

// Test the incorp-mapping-deltas command.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.*;

public class TestIncorpMappingDeltas
{
    public static final String me = "incorp-mapping-deltas";

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

	    // We should get a null-result (not an empty list, rather no
	    // response at all.  If there is an error we'll get (:error ...).
	    if (s != null)
	    {
		log.fail (me,
			  "expected no response for successful command, got: "
			  + s.toString ());
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
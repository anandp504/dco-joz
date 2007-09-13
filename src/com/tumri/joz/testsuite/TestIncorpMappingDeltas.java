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
	InputStream is = jc.execute ("(" + me + " ((:add :realm \"http://www.foo.com\" |T-SPEC-http://www.shop4flowers.com/| 1.0f 345345433545)))");

	if (is == null)
	{
	    log.fail (me, "protocol error");
	    return;
	}

	try
	{
	    SexpIFASLReader r = new SexpIFASLReader (is);

	    Sexp s = r.read ();

	    // FIXME: Not sure what the correct response is.
	    // If there is an error we'll get (:error ...).
	    // For success we recognize (), the empty list.
	    if (s == null
		|| ! s.isSexpList ()
		|| s.toSexpList ().size () != 0)
	    {
		log.fail (me,
			  "expected () response for successful command, got: "
			  + (s == null ? "null" : s.toString ()));
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

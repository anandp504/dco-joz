// Test the tspec-delete command.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.*;

public class TestTSpecDelete
{
    public static final String me = "t-spec-delete";

    public static void
    run ()
    {
	JozClient jc = new JozClient (TestsuiteProps.get_joz_url ());
	String tspec_name = "T-SPEC-test1del";

	// First add the t-spec so we can later delete it.

	InputStream is = jc.execute ("(" + "t-spec-add"
				     + " :name |" + tspec_name + "|"
				     + " :version 1"
				     + ")");

	if (is == null)
	{
	    log.fail (me, "protocol error");
	    return;
	}

	// Now delete it.

	is = jc.execute ("(" + me + " :name |" + tspec_name + "|)");

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

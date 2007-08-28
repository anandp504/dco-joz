// Test the get-complete-taxonomy command.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;

import com.tumri.utils.sexp.*;
import com.tumri.utils.ifasl.*;

public class TestGetCompleteTaxonomy
{
    public static final String me = "get-complete-taxonomy";

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
	    if (! validate_taxonomy (s))
	    {
		log.fail (me, "not a valid taxonomy");
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

    // Recursive function to validate taxonomy {s}.
    // Result is true if {s} is a valid taxonomy entry.
    //
    // NOTE: This could do more validation.  Maybe over time it will,
    // depending on what bugs occur.

    private static boolean
    validate_taxonomy (Sexp s)
    {
	if (! s.isSexpList ())
	{
	    log.info ("taxonomy not a list");
	    return false;
	}

	SexpList tax = s.toSexpList ();

	if (tax.size () != 2)
	{
	    log.info ("taxonomy list not two elements");
	    return false;
	}

	Sexp elm0 = tax.get (0);
	Sexp elm1 = tax.get (1);

	if (! elm0.isSexpList ())
	{
	    log.info ("elm0 not a list");
	    return false;
	}

	if (! elm1.isSexpList ())
	{
	    log.info ("elm1 not a list");
	    return false;
	}

	// recurse for each element in the child list (elm1)

	for (Sexp child : elm1.toSexpList ())
	{
	    if (! validate_taxonomy (child))
		return false;
	}

	return true;
    }

    // implementation details -------------------------------------------------

    private static JozTestsuite.Logger log = new JozTestsuite.Logger ();
}

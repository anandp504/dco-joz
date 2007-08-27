// Test the get-complete-taxonomy command.

package com.tumri.joz.testsuite;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import com.tumri.utils.sexp.*;

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

	InputStreamReader ir = new InputStreamReader (is);
	SexpReader sr = new SexpReader (ir);

	try
	{
	    Sexp s = sr.read ();

	    if (sr.read () != null)
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
	catch (BadSexpException e)
	{
	    log.fail (me, "malformed sexp");
	    return;
	}

	log.pass (me);
    }

    private static JozTestsuite.Logger log = new JozTestsuite.Logger ();
}

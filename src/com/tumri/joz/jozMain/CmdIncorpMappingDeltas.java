// incorp-mapping-deltas command

/* The incorp-mapping-deltas command has the following format.
   See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC#incorp_mapping_deltas.

   This is how joz's mappings get updated on the fly.  This function
   takes a single argument that is a list of 6-tuples; each 6-tuple is
   a mapping operation: it tells joz to add or to delete one arc in
   the many-to-many mapping that joz uses to pick t-specs.  The 6-tuple
   is interpreted as follows:

   (<op-type> <mapping-type> <data> <t-spec> <weight> <mod-time>)

   where

   op-type
	is either :add or :delete and tells joz how to update it's mapping 
   mapping-type
	is either :realm, :theme or :store-ID 
   data
	is the URL or theme or store-ID (strings) 
   t-spec
	the t-spec to use 
   weight
	is how often to choose this t-spec (for now it's always 1.0f0) 
   mod-time
	is when this mapping was last updated (format is same as for
	t-spec-add).  If the value is "nil" then the current time will be
	automatically filled in. 

   For example:

   (incorp-mapping-deltas 
     '((:add :realm "http://www.foo.com" |T-SPEC-foo| 1.0f0 345345433545)
       (:delete :store-ID "abcd" |T-SPEC-baz| 1.0f0 345345433545)))

   tells joz to perform two mapping operations: to add a mapping from the
   realm "http://www.foo.com" to t-spec foo and to delete the mapping from
   store-ID "abcd" to t-spec baz.  For delete ops joz ignores the weight and
   will not complain if it doesn't find a matching mapping.
*/

package com.tumri.joz.jozMain;

//import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Date;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

public class CmdIncorpMappingDeltas extends CommandDeferWriting
{
    public CmdIncorpMappingDeltas (Sexp e)
    {
	super (e);
    }

    public Sexp
    process ()
    {
	Sexp e;

	try
	{
	    if (! expr.isSexpList ())
		throw new BadCommandException ("expecting (incorp-mapping-deltas (delta1 delta2 ...))");
	    SexpList l = expr.toSexpList ();
	    if (l.size () != 2)
		throw new BadCommandException ("expecting (incorp-mapping-deltas (delta1 delta2 ...))");
	    Sexp arg = l.get (1);
	    if (! arg.isSexpList ())
		throw new BadCommandException ("expecting (incorp-mapping-deltas (delta1 delta2 ...))");
	    SexpList deltas = arg.toSexpList ();
	    e = incorp_mapping_deltas (deltas);
	}
	catch (Exception ex)
	{
	    e = SexpReader.readFromStringNoex ("(:error \""
					       // FIXME: need to escape "s
					       + ex.toString ()
					       + "\")");
	}

	return e;
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (CmdIncorpMappingDeltas.class);

    private static Sexp
    incorp_mapping_deltas (SexpList deltas)
	throws BadCommandException
    {
	for (Sexp s : deltas)
	{
	    Sexp t;

	    // Extract the parameters.

	    if (! s.isSexpList ())
		throw new BadCommandException ("mapping delta is not a list");
	    SexpList delta = s.toSexpList ();
	    if (delta.size () != 6)
		throw new BadCommandException ("expecting mapping delta of (cmd type data t-spec weight mod-time)");

	    t = delta.get (0);
	    if (! t.isSexpKeyword ())
		throw new BadCommandException ("mapping delta command is not a keyword");
	    SexpKeyword cmd = t.toSexpKeyword ();

	    t = delta.get (1);
	    if (! t.isSexpKeyword ())
		throw new BadCommandException ("mapping delta type is not a keyword");
	    SexpKeyword type = t.toSexpKeyword ();

	    t = delta.get (2);
	    if (! t.isSexpString ())
		throw new BadCommandException ("mapping delta data is not a string");
	    SexpString data = t.toSexpString ();

	    t = delta.get (3);
	    if (! t.isSexpSymbol ())
		throw new BadCommandException ("mapping delta t-spec is not a symbol");
	    SexpSymbol tspec_name = t.toSexpSymbol ();

	    t = delta.get (4);
	    if (! t.isSexpNumber ())
		throw new BadCommandException ("mapping delta weight is not a number");
	    Float weight = parse_weight (t);

	    t = delta.get (5);
	    if (! t.isSexpString () && ! t.isSexpNumber ())
		throw new BadCommandException ("mapping delta mod-time is not a string/number");
	    Long mod_time = parse_mod_time (t);

	    // Finished parsing the delta, now process it.

	    if (cmd.equalsStringIgnoreCase (":add"))
	    {
		JozData.mapping_db.add (type.toString (),
					data.toStringValue (), // ???
					tspec_name.toString (),
					weight, mod_time);
	    }
	    else if (cmd.equalsStringIgnoreCase (":delete"))
	    {
		JozData.mapping_db.delete (type.toString (),
					   data.toStringValue (), // ???
					   tspec_name.toString (),
					   mod_time);
	    }
	    else
	    {
		throw new BadCommandException ("mapping delta command is not :add or :delete");
	    }
	}

	// FIXME: It's not clear what the result should be.
	return new SexpList ();
    }

    // Convert weight to a Float.
    // FIXME: Eventually move to generic place with more generic name,
    // blah blah blah.

    private static Float
    parse_weight (Sexp weight)
    {
	Float result;

	if (weight.isSexpInteger ())
	{
	    SexpInteger i = weight.toSexpInteger ();
	    result = i.toNativeReal32 ();
	}
	else if (weight.isSexpReal ())
	{
	    SexpReal r = weight.toSexpReal ();
	    result = r.toNativeReal32 ();
	}
	else
	{
	    throw new RuntimeException ("parameter not a number");
	}

	return result;
    }

    // Number of seconds between Jan 1 1900 and 1970.
    static long epoch_1900_1970_diff =
	(70L * 365 * 24 * 60 * 60) // seconds per year
	+ (70L/4 * 24 * 60 * 60);  // adjust for leap days

    // Convert mod-time to a Date object.
    // FIXME: Eventually move to generic place with more generic name,
    // blah blah blah.
    //
    // If the value is a number it is interpreted as a CL (Common Lisp)
    // universal time, number of seconds since midnight, January 1, 1900 GMT.
    // If the value is a string it is interpreted an ISO 8601 date+time.

    private static Long
    parse_mod_time (Sexp t)
    {
	if (t.isSexpString ())
	{
	    SexpString s = t.toSexpString ();
	    String ts = s.toStringValue ();
	    if (s.equalsStringIgnoreCase ("nil"))
		return new Long (System.currentTimeMillis ()); // current time
	    return new Long (System.currentTimeMillis ()); // FIXME: for now
	}

	long time;

	if (t.isSexpInteger ())
	{
	    SexpInteger i = t.toSexpInteger ();
	    time = i.toNativeInteger64 ();
	}
	else if (t.isSexpReal ())
	{
	    SexpReal r = t.toSexpReal ();
	    Double tr = r.toNativeReal64 ();
	    time = tr.longValue ();
	}
	else
	{
	    throw new RuntimeException ("time not a string/number");
	}

	if (time < epoch_1900_1970_diff)
	    time = 0;
	else
	    time -= epoch_1900_1970_diff;

	time *= 1000; // seconds -> milliseconds
	return new Long (time);
    }
}

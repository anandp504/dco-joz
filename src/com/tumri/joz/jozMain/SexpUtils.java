// Joz sexp utils.

package com.tumri.joz.jozMain;

import java.util.Iterator;

import com.tumri.utils.sexp.*;

public class SexpUtils
{
    public static class BadGetNextException extends Exception
    {
	public BadGetNextException (String msg)
	{
	    super (msg);
	}
    }

    public static Boolean
    get_next_boolean (String param_name, Iterator<Sexp> iter)
	throws BadGetNextException
    {
	if (! iter.hasNext ())
	    throw new BadGetNextException ("missing value for " + param_name);
	String s = get_next_symbol (param_name, iter);
	if (s.equals ("t"))
	    return new Boolean (true);
	if (s.equals ("nil"))
	    return new Boolean (false);
	throw new BadGetNextException ("bad value for " + param_name);
    }

    public static Enums.MaybeBoolean
    get_next_maybe_boolean (String param_name, Iterator<Sexp> iter)
	throws BadGetNextException
    {
	if (! iter.hasNext ())
	    throw new BadGetNextException ("missing value for " + param_name);
	Sexp e = iter.next ();
	if (e.isSexpSymbol ())
	{
	    SexpSymbol s = e.toSexpSymbol ();
	    if (s.equalsString ("nil"))
		return Enums.MaybeBoolean.FALSE;
	    if (s.equalsString ("t"))
		return Enums.MaybeBoolean.TRUE;
	    // fall through
	}
	else if (e.isSexpKeyword ())
	{
	    SexpKeyword k = e.toSexpKeyword ();
	    if (k.equalsString (":maybe")) // FIXME: ignore case ...
		return Enums.MaybeBoolean.MAYBE;
	    // fall through
	}

	throw new BadGetNextException ("bad value for " + param_name);
    }

    public static String
    get_next_symbol (String param_name, Iterator<Sexp> iter)
	throws BadGetNextException
    {
	if (! iter.hasNext ())
	    throw new BadGetNextException ("missing value for " + param_name);
	Sexp e = iter.next ();
	if (! e.isSexpSymbol ())
	    throw new BadGetNextException ("bad value for " + param_name);
	SexpSymbol s = e.toSexpSymbol ();
	return s.toStringValue ();
    }

    public static String
    get_next_string (String param_name, Iterator<Sexp> iter)
	throws BadGetNextException
    {
	if (! iter.hasNext ())
	    throw new BadGetNextException ("missing value for " + param_name);
	Sexp e = iter.next ();
	if (! e.isSexpString ())
	    throw new BadGetNextException ("bad value for " + param_name);
	SexpString s = e.toSexpString ();
	return s.toStringValue ();
    }

    public static Integer
    get_next_integer (String param_name, Iterator<Sexp> iter)
	throws BadGetNextException
    {
	if (! iter.hasNext ())
	    throw new BadGetNextException ("missing value for " + param_name);
	Sexp e = iter.next ();
	if (! e.isSexpInteger ())
	    throw new BadGetNextException ("bad value for " + param_name);
	SexpInteger n = e.toSexpInteger ();
	try
	{
	    return n.toNativeInteger32 ();
	}
	catch (NumberFormatException ex)
	{
	    throw new BadGetNextException ("non-32-bit-integer value for "
					   + param_name);
	}
    }

    public static Double
    get_next_double (String param_name, Iterator<Sexp> iter)
	throws BadGetNextException
    {
	if (! iter.hasNext ())
	    throw new BadGetNextException ("missing value for " + param_name);
	Sexp e = iter.next ();
	if (! e.isSexpReal ())
	    throw new BadGetNextException ("bad value for " + param_name);
	SexpReal r = e.toSexpReal ();
	try
	{
	    return r.toNativeReal64 ();
	}
	catch (NumberFormatException ex)
	{
	    throw new BadGetNextException ("non-64-bit-double value for "
					   + param_name);
	}
    }
}

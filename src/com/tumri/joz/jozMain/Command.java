// Command parser.

package com.tumri.joz.jozMain;

import java.io.Reader;
import java.io.StringReader;
import java.io.OutputStream;

import com.tumri.utils.sexp.*;

public abstract class Command
{
    protected Sexp expr;

    private static final String CMD_PREFIX = "Message=";

    protected Command (Sexp e)
    {
	expr = e;
    }

    public static Command
    parse (String cmd)
	throws BadCommandException
    {
	// Strip leading "Message=".
	if (cmd.startsWith (CMD_PREFIX))
	    cmd = cmd.substring (CMD_PREFIX.length ());

	Reader r = new StringReader (cmd);
	SexpReader lr = new SexpReader (r);

	try
	{
	    Sexp e = lr.read ();

	    // Some initial error checking.

	    if (! e.isSexpList ())
		throw new BadCommandException ("not an expression: " + e.toString ());

	    SexpList l = e.toSexpList ();

	    if (l.size () == 0)
		throw new BadCommandException ("empty list");

	    // Strip outer "eval" if present.

	    Sexp first = l.getFirst ();

	    if (first.isSexpSymbol ()
		// FIXME: ignore case, blah blah blah
		&& first.toSexpSymbol ().equalsString ("eval"))
	    {
		if (l.size () != 2)
		    throw new BadCommandException ("extra values in eval");
		e = l.get (1);
		if (! e.isSexpList ())
		    throw new BadCommandException ("not an expression: " + e.toString ());
		l = e.toSexpList ();
		if (l.size () == 0)
		    throw new BadCommandException ("empty list");
	    }

	    Sexp cmd_expr = l.getFirst ();
	    if (! cmd_expr.isSexpSymbol ())
		throw new BadCommandException ("command name not a symbol: " + cmd_expr.toString ());

	    SexpSymbol sym = cmd_expr.toSexpSymbol ();
	    String cmd_name = sym.toString ();

	    // Return the right Cmd* class to handle this request.

	    if (cmd_name.equals ("get-attributes-and-metadata"))
		return new CmdGetAttrsAndMetadata (e);
	    if (cmd_name.equals ("tabulate-search-results"))
		return new CmdTabulateSearchResults (e);
	    if (cmd_name.equals ("get-ad-data"))
		return new CmdGetAdData (e);
	    if (cmd_name.equals ("get-counts"))
		return new CmdGetCounts (e);

	    throw new BadCommandException ("unknown command");
	}
	catch (Exception e)
	{
	    throw new BadCommandException (e.toString ());
	}
    }

    // Process the command, returning the result as an s-expression.
    public abstract Sexp process ();

    // Process the command, writing the results directly to {out}.
    public abstract void process_and_write (OutputStream out);

    // Return true if this command writes its own results.
    public abstract boolean write_own_results_p ();

    // Return true if result needs to be passed back with uppercase syms.
    // This is a hack for the tabulate-search-results command.
    public boolean need_uppercase_syms () { return false; }
}

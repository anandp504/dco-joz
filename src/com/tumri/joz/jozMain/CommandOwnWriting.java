// Like Command.java, but write results directly rather than building an
// intermediate format.

package com.tumri.joz.jozMain;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.Sexp;

public abstract class CommandOwnWriting extends Command
{
    protected CommandOwnWriting (Sexp e)
    {
	super (e);
    }

    public boolean write_own_results_p () { return true; }

    public Sexp
    process ()
    {
	log.fatal ("cannot call CommandOwnWriting.process");
	return null;
    }

    private static Logger log = Logger.getLogger (CommandOwnWriting.class);
}

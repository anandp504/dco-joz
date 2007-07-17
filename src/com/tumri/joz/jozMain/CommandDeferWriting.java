// For commands that build a temp result.

package com.tumri.joz.jozMain;

import java.io.OutputStream;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.Sexp;

public abstract class CommandDeferWriting extends Command
{
    protected CommandDeferWriting (Sexp e)
    {
	super (e);
    }

    public boolean write_own_results_p () { return false; }

    public void
    process_and_write (OutputStream out)
    {
	log.fatal ("cannot call CommandDeferWriting.process_and_write");
    }

    private static Logger log = Logger.getLogger (CommandDeferWriting.class);
}

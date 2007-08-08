// The MUP database.
// FIXME: wip
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

package com.tumri.joz.jozMain;

import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.strings.EString;

public interface MUPDB
{
    public int get_count ();

    // Entry numbers may change as objects are added/deleted.
    public MUPProductObj get_entry (int entry_nr);

    // Ids don't change as objects are added/deleted.
    public MUPProductObj get_id (int id);

    // FIXME: temp hack
    public Sexp get_default_realm_response ();

    // Support for the get-counts external API call.
    public Sexp get_counts (EString tspec);
}

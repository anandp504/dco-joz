// The TSpec database.
// FIXME: wip
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

package com.tumri.joz.jozMain;

public interface TSpecDB {
    
    public String get_default_realm_url();
    
    // Return tspec for {name} or null if not found.
    public TSpec get(String name);
    
    public void materialize();
}

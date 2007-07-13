// The TSpec database.
// FIXME: wip

package com.tumri.joz.jozMain;

public interface TSpecDB
{
    public String get_default_realm_url ();

    public TSpec get (String name);
}

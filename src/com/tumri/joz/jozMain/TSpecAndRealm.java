// Utility class to pass back a realm and t-spec from certain functions.

package com.tumri.joz.jozMain;

class TSpecAndRealm
{
    public TSpec _t_spec = null;
    public Realm _realm = null;

    public TSpecAndRealm (TSpec t, Realm r)
    {
	_t_spec = t;
	_realm = r;
    }
}

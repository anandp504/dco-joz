// A mapping DB entry.
// The mapping DB maps realms/themes/store-ids to t-specs.
// FIXME: wip
// TODO: OMG, OMG, this is all wrong.  Chill dude.  As things evolve this
// will get rewritten to be The Right Way.

package com.tumri.joz.jozMain;

public class MappingObj
{
    public MappingObj (MappingType type, float weight, long modified,
		       String t_spec)
    {
	_type = type;
	_weight = weight;
	_modified = modified;
	_t_spec = t_spec;
    }

    public enum MappingType
    {
	URL, THEME, STORE_ID
    }

    public MappingType get_type () { return _type; }
    public float get_weight () { return _weight; }
    public long get_modified () { return _modified; }
    public String get_t_spec () { return _t_spec; }

    // implementation details -------------------------------------------------

    private MappingType _type;
    private float _weight;
    private long _modified; // in seconds since jan 1, 1970
    private String _t_spec;
}

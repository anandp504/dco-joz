// An entry in the MUP taxonomy database.
// E.g. TUMRI_14385 Action Figures TUMRI_14384 Toys & Games
// FIXME: wip

package com.tumri.joz.jozMain;

public class MUPTaxonomyObj
{
    public MUPTaxonomyObj (String s, char delim)
	throws BadMUPDataException
    {
	// Methinks I'd rather use StringTokenizer here but the docs say
	// its use is discouraged.  Blech.
	String[] tokens = s.split (String.valueOf (delim));
	if (tokens.length != 4)
	    throw new BadMUPDataException ("taxonomy entry needs 4 elements: "
					   + s);
	_id = tokens[0];
	_name = tokens[1];
	_parent_id = tokens[2];
	_parent_name = tokens[3];
    }

    public String get_id () { return _id; }

    public String get_name () { return _name; }

    public String get_parent_id () { return _parent_id; }

    public String get_parent_name () { return _parent_name; }

    // implementation details -------------------------------------------------

    // E.g. TUMRI_14385
    String _id;

    // E.g. Action Figures
    String _name;

    // E.g. TUMRI_14384
    String _parent_id;

    // E.g. Toys & Games
    String _parent_name;
}

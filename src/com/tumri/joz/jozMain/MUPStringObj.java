// An entry in the MUP string database.
// E.g. ???
// FIXME: wip

package com.tumri.joz.jozMain;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.tumri.zini.transport.FASLType;

import com.tumri.utils.strings.EString;
import com.tumri.utils.sexp.SexpIFASLReader;

public class MUPStringObj
{
    public MUPStringObj (FASLType t, Iterator<FASLType> iter)
	throws BadMUPDataException
    {
	// usually not more than 3 strings
	_strings = new ArrayList<EString> (3);

	while (iter.hasNext ())
	{
	    FASLType elm = iter.next ();
	    FASLType first;
	    if (elm.type () != FASLType.list
		|| elm.length () < 1
		|| ((first = elm.first ()).type () != FASLType.string
		    && first.type () != FASLType.unsigned_16))
	    {
		throw new BadMUPDataException ("bad string entry: " + t.toString ());
	    }
	    _strings.add (SexpIFASLReader.ifasl_to_estring (first));
	}
    }

    public List<EString> get_strings () { return _strings; }

    // implementation details -------------------------------------------------

    ArrayList<EString> _strings;
}

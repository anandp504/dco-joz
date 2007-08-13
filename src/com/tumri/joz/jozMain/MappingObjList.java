// Class to abstract away a list of mapping objects.
// E.g. one realm can map to several t-specs.

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.List;

public class MappingObjList
{
    public MappingObjList ()
    {
    }

    public List<MappingObj> get_list () { return _list; }

    public void
    add (MappingObj obj)
    {
	_list.add (obj);
    }

    // implementation details -------------------------------------------------

    ArrayList<MappingObj> _list = new ArrayList<MappingObj> ();
}

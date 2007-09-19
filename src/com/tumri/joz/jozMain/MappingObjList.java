// Class to abstract away a list of mapping objects.
// E.g. one realm can map to several t-specs.

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class MappingObjList {
    
    public MappingObjList() {
    }
    
    public List<MappingObj> get_list() {
        return _list;
    }
    
    public void add(MappingObj obj) {
        _list.add(obj);
    }
    
    public void delete(String t_spec_name, Long modified) {
        long mod = modified.longValue();
        Iterator<MappingObj> iter = _list.iterator();
        while (iter.hasNext()) {
            MappingObj mo = iter.next();
            if (mo.get_t_spec().equals(t_spec_name) && mo.get_modified() == mod) {
                iter.remove();
            }
        }
    }
    
    public int size() {
        return _list.size();
    }
    
    // implementation details -------------------------------------------------
    
    ArrayList<MappingObj> _list = new ArrayList<MappingObj>();
}

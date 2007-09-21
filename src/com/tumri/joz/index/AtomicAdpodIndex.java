package com.tumri.joz.index;

import com.tumri.utils.data.MultiSortedSet;
import com.tumri.cma.domain.AdPod;

import java.util.concurrent.atomic.AtomicReference;
import java.util.SortedSet;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

/**
 * Holds atomic reference to the adpod index.
 * This class is expected to deprecate over time, once the CampaignDBCompleteRefreshImpl is deprecated.
 * The class extends AdpodIndex to maintain signature compatiblity with the CampaignDB methods retrun types.
 * However it doesnt truly take advantage of inheritance here, instead it uses composition to hold on to the
 * member variable of AdpodIndex. What this means is if a new method is added to AbstractAdPodIndex that needs to be used
 * by the client using AtomicAdpodIndex, an equivalent method will have to be added to this class and delegate the
 * responsibility to member adpodindex. There is a room for improvement in this design, but leaving it as it is for now
 * as the expectations are to deprecate this class over time.
 *
 * @author bpatel
 */
public class AtomicAdpodIndex<Key, Value> {

    private final AtomicReference<AdpodIndex<Key, Value>> atomicIndex = new AtomicReference<AdpodIndex<Key, Value>>();

    public AtomicAdpodIndex(AdpodIndex<Key, Value> index) {
        set(index);    
    }
    public void set(AdpodIndex<Key, Value> newIndex) {
        atomicIndex.compareAndSet(atomicIndex.get(), newIndex);
    }

    public AdpodIndex.Attribute getType() {
        if(atomicIndex.get() == null) {
            return null;
        }
        return atomicIndex.get().getType();
    }

    //This breaks the rule and exposes internal index to outside world. Since the class will be replaced over time, the flaw is ignored. 

    public AdpodIndex<Key, Value> get() {
        return atomicIndex.get();
    }

    public List<Map.Entry<Key, Value>> getEntries(AdPod adPod) {
        if(atomicIndex.get() == null) {
            return null;
        }
        return atomicIndex.get().getEntries(adPod);
    }

    public SortedSet<Value> get(final Key key) {
        if(atomicIndex.get() == null) {
            return null;
        }
        return atomicIndex.get().get(key);
    }

    public SortedSet<Value> get(List<Key> keys) {
        if(atomicIndex.get() == null) {
            return null;
        }
        return atomicIndex.get().get(keys);
    }

    public int getCount(final ArrayList<Key> keys) {
      if(atomicIndex.get() == null) {
          return 0;
      }
      return atomicIndex.get().getCount(keys);
    }

    public int getCount(final Key low, final Key high) {
        if(atomicIndex.get() == null) {
            return 0;
        }
        return atomicIndex.get().getCount(low, high);
    }

    public void put(final Key key, final Value val) {
        if(atomicIndex.get() == null) {
            return;
        }
        atomicIndex.get().put(key, val);
    }

    public void put(Map<Key, List<Value>> map) {
        if(atomicIndex.get() == null) {
            return;
        }
        atomicIndex.get().put(map);
    }

    public void put(final Key key, final MultiSortedSet<Value> set) {
        if(atomicIndex.get() == null) {
            return;
        }
        atomicIndex.get().put(key, set);
    }

    public void clear() {
      if(atomicIndex.get() == null) {
          return;
      }
      atomicIndex.get().clear();
    }
}

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

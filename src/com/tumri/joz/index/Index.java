package com.tumri.joz.index;

import com.tumri.joz.products.IProduct;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public abstract class Index<Key, Value> implements IIndex<Key, Value> {
  protected RWLockedTreeMap<Key, RWLockedSortedSet<Value>> m_map;

  public abstract IProduct.Attribute getType();

  public abstract Key getKey(IProduct p);

  public abstract Value getValue(IProduct p);

  /**
   * Adds a product to the index, uses getKey method to get key
   * @param p, the product to be added
   */
  public void addProduct(IProduct p) {
    Key k = getKey(p);
    RWLockedSortedSet<Value> set = null;
    try {
      m_map.writerLock();
      set = m_map.get(k);
      if (set == null) {
        set = createSet();
        m_map.put(k, set);
      }
    } finally {
      m_map.writerUnlock();
    }
    try {
      set.writerLock();
      set.add(getValue(p));
    } finally {
      set.writerUnlock();
    }
  }

  public void addProduct(ArrayList<IProduct> products) {
    TreeMap<Key,ArrayList<Value>> map = buildMap(products);
    Iterator<Key> iter = map.keySet().iterator();
    while (iter.hasNext()) {
      Key k = iter.next();
      ArrayList<Value> list = map.get(k);
      RWLockedSortedSet<Value> set = null;
      try {
        m_map.writerLock();
        set = m_map.get(k);
        if (set == null) {
          set = createSet();
          m_map.put(k, set);
        }
      } finally {
        m_map.writerUnlock();
      }
      try {
        set.writerLock();
        set.addAll(list);
      } finally {
        set.writerUnlock();
      }
    }
  }


  /**
   * Removes a product from the index. uses the getKey method to get the key
   * The handle to the product is adde
   * @param p, the product to be added
   */
  public void deleteProduct(IProduct p) {
    Key k = getKey(p);
    Value v = getValue(p);
    RWLockedSortedSet<Value> set = null;
    try {
      m_map.writerLock();
      set = m_map.get(k);
      if (set == null) {
        return;
      }
    } finally {
      m_map.writerUnlock();
    }
    try {
      set.writerLock();
      set.remove(v);
    } finally {
      set.writerUnlock();
    }
  }

  public void deleteProduct(ArrayList<IProduct> products) {
    TreeMap<Key,ArrayList<Value>> map = buildMap(products);
    Iterator<Key> iter = map.keySet().iterator();
    while (iter.hasNext()) {
      Key k = iter.next();
      ArrayList<Value> list = map.get(k);
      RWLockedSortedSet<Value> set = null;
      try {
        m_map.writerLock();
        set = m_map.get(k);
        if (set == null) {
          continue;
        }
      } finally {
        m_map.writerUnlock();
      }
      try {
        set.writerLock();
        set.removeAll(list);
      } finally {
        set.writerUnlock();
      }
    }
  }

  private TreeMap<Key,ArrayList<Value>> buildMap(ArrayList<IProduct> products) {
    TreeMap<Key,ArrayList<Value>> map = new TreeMap<Key, ArrayList<Value>>();
    for (int i = 0; i < products.size(); i++) {
      IProduct p = products.get(i);
      Key k = getKey(p);
      ArrayList<Value> list = map.get(k);
      if (list == null) {
        list = new ArrayList<Value>();
        map.put(k,list);
      }
      list.add(getValue(p));
    }
    return map;
  }

  /**
   * Constructs an empty index
   */
  protected Index() {
    m_map = new RWLockedTreeMap<Key, RWLockedSortedSet<Value>>();
  }

  /**
   * Add a Value val to the index for the given Key key.
   * Natural order of the keys is used internally. Key and Value both should implement Comparable
   * @param key the key object
   * @param val the value object
   */
  public void put(final Key key, final Value val) {
    RWLockedSortedSet<Value> set = null;
    try {
      m_map.writerLock();
      set = m_map.get(key);
      if (set == null) {
        set = createSet();
        m_map.put(key, set);
      }
    } finally {
      m_map.writerUnlock();
    }
    try {
      set.writerLock();
      set.add(val);
    } finally {
      set.writerUnlock();
    }
  }

  /**
   * Add a SortedSet of values to the Index key.
   * @param key
   * @param set
   */
  public void put(final Key key, final MultiSortedSet<Value> set) {
    try {
      m_map.writerLock();
      m_map.put(key, set);
    } finally {
      m_map.writerUnlock();
    }
  }

  /**
   * Given a Key key returns a SortedSet of values. The sort order is decided by the
   * natural order of the Value. Key and Value both should implement Comparable
   * @param key the key object
   */
  public SortedSet<Value> get(final Key key) {
    SortedSet<Value> set = null;
    try {
      m_map.readerLock();
      set = m_map.get(key);
    } finally {
      m_map.readerUnlock();
    }
    return set;
  }

  /**
   * Given a List of Key objects keys, returns the SortedSet of values. The sort order is decided by the
   * natural order of the Value. Key and Value both should implement Comparable
   * @param keys a List of key objects
   */
  public SortedSet<Value> get(List<Key> keys) {
    if (keys.size() == 1) {
      return get(keys.get(0));
    } else {
      MultiSortedSet<Value> set = new MultiSortedSet<Value>();
      for (int i = 0; i < keys.size(); i++) {
        set.add(get(keys.get(i)));
      }
      return set;
    }
  }

  /**
   * Gets the count of values associated with a given key
   * Use this method sparingly, this can be very exoensive
   * @param key
   * @return count of values associated with the key
   */
  public int getCount(final Key key) {
    SortedSet<Value> set = get(key);
    return (set == null ? 0 : set.size());
  }

  /**
   * Given a List of Key objects keys, returns the count of associated values.
   * Use this method sparingly, this can be very exoensive
   * @param keys a List of key objects
   */
  public int getCount(final ArrayList<Key> keys) {
    int count = 0;
    for (int i = 0; i < keys.size(); i++) {
      count += getCount(keys.get(i));
    }
    return count;
  }

  /**
   * Given a Range of keys, returns a SortedSet of associated values.
   * @param low key
   * @param high key
   * @return a SortedSet of values
   */
  public SortedSet<Value> get(final Key low, final Key high) {
    TreeSet<Value> set = new TreeSet<Value>();
    try {
      m_map.readerLock();
      if (low == null) {
        addElements(set, m_map.headMap(high));
      } else if (high == null) {
        addElements(set, m_map.tailMap(low));
      } else {
        addElements(set, m_map.tailMap(low).headMap(high));
      }
    } finally {
      m_map.readerUnlock();
    }
    return set;
  }

  /**
   * Given a Range of keys, returns the count of associated values.
   * This method can be very expensive
   * @param low
   * @param high
   * @return count
   */
  public int getCount(final Key low, final Key high) {
    int count = 0;
    try {
      m_map.readerLock();
      if (low == null) {
        count = getCount(m_map.headMap(high));
      } else if (high == null) {
        count = getCount(m_map.tailMap(low));
      } else {
        count = getCount(m_map.tailMap(low).headMap(high));
      }
    } finally {
      m_map.readerUnlock();
    }
    return count;
  }

  private void addElements(SortedSet<Value> set, SortedMap<Key, RWLockedSortedSet<Value>> map) {
    Iterator<Map.Entry<Key, RWLockedSortedSet<Value>>> iter = map.entrySet().iterator();
    while (iter.hasNext()) {
      Map.Entry<Key, RWLockedSortedSet<Value>> lEntry = iter.next();
      Iterator<Value> viter = lEntry.getValue().iterator();
      while (viter.hasNext()) {
        Value lValue = viter.next();
        set.add(lValue);
      }
    }
  }

  private int getCount(SortedMap<Key, RWLockedSortedSet<Value>> map) {
    Iterator<Map.Entry<Key, RWLockedSortedSet<Value>>> iter = map.entrySet().iterator();
    int count = 0;
    while (iter.hasNext()) {
      Map.Entry<Key, RWLockedSortedSet<Value>> lEntry = iter.next();
      count += lEntry.getValue().size();
    }
    return count;
  }

  public void clear() {
    try {
      m_map.writerLock();
      m_map.clear();
    } finally {
      m_map.writerUnlock();
    }
  }
  protected RWLockedSortedSet<Value> createSet() {
    return new RWLockedSortedArraySet<Value>();
  }
}
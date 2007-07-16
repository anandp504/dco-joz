package com.tumri.joz.Query;

import com.tumri.joz.Query.Query;

import java.util.LinkedHashMap;
import java.util.SortedSet;
import java.util.Collections;
import java.util.Map;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class QueryCache<Value> {
  private static QueryCache g_Cache;
  // Need LRU Cache
  Map<Query, SortedSet<Value>> m_map = new LinkedHashMap<Query, SortedSet<Value>>(128);
  // @todo understand the behavior of this as LRU cache or use softreference cache
  public QueryCache getInstance() {
    if (g_Cache == null) {
      synchronized(QueryCache.class) {
        if (g_Cache == null) {
          g_Cache = new QueryCache();
        }
      }
    }
    return g_Cache;
  }

  private QueryCache() {
    m_map = Collections.synchronizedMap(new LinkedHashMap<Query, SortedSet<Value>>(128));
  }

  public boolean contains(Query q) {
    return m_map.containsKey(q);
  }

  public SortedSet<Value> get(Query q) {
    return m_map.get(q);
  }

  public void add(Query q, SortedSet<Value> set) {
    m_map.put(q,set);
  }

}
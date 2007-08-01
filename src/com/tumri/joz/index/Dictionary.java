package com.tumri.joz.index;

import java.util.ArrayList;
import java.util.TreeMap;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Dictionary<Type> implements IDictionary<Type> {
  private int m_Id = 0;
  private TreeMap<Type,Integer> m_map = new TreeMap<Type, Integer>();
  private ArrayList<Type> m_List = new ArrayList<Type>();
  private ArrayList<Integer> m_freeSlots = new ArrayList<Integer>();

  public Integer getId(Type t) {
    isValid();
    synchronized (this) {
      if (!m_map.containsKey(t)) {
        Integer id = getNextId();
        m_map.put(t,id);
        if (id < m_List.size())
          m_List.set(id,t);
        else
          m_List.add(t);
      }
      return m_map.get(t);
    }
  }

  public Type getValue(int index) {
    isValid();
    return (index < m_List.size() ? m_List.get(index) : null);
  }

  public void remove(Type t) {
    isValid();
    synchronized (this) {
      if (m_map.containsKey(t))
        remove(m_map.get(t));
    }
  }

  public void remove(int index) {
    isValid();
    if (index < m_List.size()) {
      synchronized (this) {
        Type t = m_List.get(index);
        m_List.set(index,null);
        m_map.remove(t);
        m_freeSlots.add(index);
      }
    }
  }

  private Integer getNextId() {
    if (m_freeSlots.size() > 0) {
      return m_freeSlots.remove(m_freeSlots.size()-1);
    }
    return m_Id++;
  }
  private void isValid() {
    if (m_List.size() != m_Id) System.err.println("Dictionary next id doesn't match list size");
  }


  public int maxId() {
    return m_Id;
  }
}
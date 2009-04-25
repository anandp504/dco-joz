package com.tumri.joz.index;

import com.tumri.utils.dictionary.IDictionary;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductDictionary implements IDictionary<String> {
  private int m_lowId = 0x7fffffff;
  private int m_highId = 0;


  public ProductDictionary() {
  }

  public Integer getId(String t) {
    return getNextId(t);
  }

  public String getValue(int index) {
    return Integer.toString(index);
  }

  public void remove(String obj) {
  }

  public void remove(int index) {
  }

  private Integer getNextId(String s) {
    Integer i = new Integer(s);
    if (i < m_lowId) {
      m_lowId = i;
    } else if (i > m_highId) {
      m_highId = i;
    }
    return i;
  }

  public int maxId() {
    return m_highId;
  }

  public int minId() {
    return m_lowId;
  }


  public Integer suggestId(String t) {
      return getId(t);
  }
}

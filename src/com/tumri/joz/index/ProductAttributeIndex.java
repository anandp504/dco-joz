/* 
 * ProductAttributeIndex.java
 * 
 * COPYRIGHT (C) 2007 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE 
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY, 
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL 
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART 
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM 
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR 
 * WRITTEN PERMISSION OF TUMRI INC.
 * 
 * @author Bhavin Doshi (bdoshi@tumri.com)
 * @version 1.0     Aug 29, 2007
 * 
 */
package com.tumri.joz.index;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.TreeMap;

import com.tumri.joz.products.IProduct;
import com.tumri.utils.data.RWLockedSortedSet;
import com.tumri.utils.index.Index;

/**
 *
 * Created by Bhavin Doshi (bdoshi@tumri.com) on Aug 29, 2007
 * Company: Tumri Inc.
 */
public abstract class ProductAttributeIndex<Key, Value> extends Index<Key, Value> {
    
    public abstract IProduct.Attribute getType();

    public abstract Key getKey(IProduct p);

    public abstract Value getValue(IProduct p);

    public ProductAttributeIndex() {
        super();
    }
    

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

}

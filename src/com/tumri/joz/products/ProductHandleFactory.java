package com.tumri.joz.products;

import com.tumri.utils.data.SortedArraySet;

import java.util.SortedSet;

/**
 * Implementation that will create product handles
 * @author: nipun
 * Date: Apr 3, 2008
 * Time: 4:08:42 PM
 */
public class ProductHandleFactory {

    private SortedArraySet<Handle> productHandles = new SortedArraySet<Handle>();

    private static ProductHandleFactory _factory;

    /**
     * Gets the singleton instance of this impl
     * @return
     */
    public static ProductHandleFactory getInstance() {
      if (_factory == null) {
        synchronized (ProductHandleFactory.class) {
          if (_factory == null) {
            _factory = new ProductHandleFactory();
          }
        }
      }
      return _factory;
    }

    /**
     * Get the handle if already there, else create one 
     * @param id
     * @return
     */
    public Handle getHandle(long id) {
        ProductHandle p = new ProductHandle(1.0, id);
        Handle ph = productHandles.find(p);
        if (ph !=null) {
             p = (ProductHandle) ph;
        } else {
            //add it to the list
            productHandles.add(p);
        }
        return p;

    }

    /**
     * Clears out the handles already created.
     */
    public void clearProducts() {
        productHandles.clear();
    }

    /**
     * Returns the current set of products that have been added.
     * @return
     */
    public SortedSet<Handle> getProducts() {
        return productHandles;
    }
}

package com.tumri.joz.products;

import com.tumri.content.data.Product;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */

/**
 * Defines the interface object called IProduct
 */
public interface IProduct extends Product, Comparable<IProduct> {
    
    public Handle getHandle();

}
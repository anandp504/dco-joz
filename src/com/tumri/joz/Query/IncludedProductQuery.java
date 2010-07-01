package com.tumri.joz.Query;

import com.tumri.content.data.Product;
import com.tumri.joz.filter.Filter;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.data.SortedArraySet;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class IncludedProductQuery extends MUPQuery {
    protected SortedSet<Handle> m_pids = new SortedArraySet<Handle>();

    public Type getType() {
        return Type.kAttribute;
    }

    public IncludedProductQuery(long pid) {
        super(IProduct.Attribute.kId);
        addValue(pid);
    }

    public IncludedProductQuery(ArrayList<Long> values) {
        super(IProduct.Attribute.kId);
        for (Long id: values) {
            addValue(id);
        }
    }

    public void addValue(long aValue) {
        //Chck if valid
        Handle p = ProductDB.getInstance().getHandle(aValue);
        if (p!=null) {
            m_pids.add(p);
        }
    }

    public int getCount() {
        return m_pids.size();
    }


    @SuppressWarnings("unchecked")
    public SortedSet<Handle> exec() {
        return m_pids;
    }

    public double getCost() {
        return 0.0;
    }

    public Filter<Handle> getFilter() {
        return m_filter;
    }

    public boolean hasIndex() {
        return true;
    }

}
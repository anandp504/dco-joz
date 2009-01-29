package com.tumri.joz.filter;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.Pair;

/**
 * Filter implementaion for Long. This is going to be expensive since we need to lookup
 * the Longitude each time
 * User: nipun
 */
public class LongitudeRangeFilter extends Filter<Handle> {
    public LongitudeRangeFilter() {
        super();
    }

    public LongitudeRangeFilter(Filter<Handle> f) {
        super(f);
    }

    public boolean accept(Handle h) {
        if (!ProductDB.hasProductInfo()) {
            return super.accept(h);
        } else {
            Product p = ProductDB.getInstance().get(h);
            Integer zip;
            try {
                zip = Integer.parseInt(p.getZipStr());
            } catch (NumberFormatException e) {
                zip = null;
            }
            Pair<Double, Double> latLong = ZipCodeDB.getInstance().getLatLong(zip);
            Double lng = null;
            if (latLong!=null) {
                lng = latLong.getSecond();
            }
            return ((p != null) && (inRange(lng) ^ isNegation()));
        }
    }

    public Filter<Handle> clone() {
        return new LongitudeRangeFilter(this);
    }
}
package com.tumri.joz.filter;

import com.tumri.content.data.Product;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.Pair;

/**
 * Filter implementaion for Lat. This is going to be expensive since we need to lookup
 * the Latitude each time
 * User: nipun
 */
public class LatitudeRangeFilter extends Filter<Handle> {
    public LatitudeRangeFilter() {
        super();
    }

    public LatitudeRangeFilter(Filter<Handle> f) {
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
            Double lat = null;
            if (latLong!=null) {
                lat = latLong.getFirst();
            }
            return ((p != null) && (inRange(lat) ^ isNegation()));
        }
    }

    public Filter<Handle> clone() {
        return new LatitudeRangeFilter(this);
    }
}
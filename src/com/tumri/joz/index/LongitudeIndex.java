package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.Pair;

/**
 * Longitude Index
 * User: nipun
 */
public class LongitudeIndex extends ProductAttributeIndex<Integer, Handle>  {
    public LongitudeIndex() {
    }

    public IProduct.Attribute getType() {
        return IProduct.Attribute.kLongitude;
    }

    public Integer getKey(IProduct p) {
        Integer zip;
        try {
            zip = Integer.parseInt(p.getZipStr());
        } catch (NumberFormatException e) {
            zip = null;
        }
        Pair<Double, Double> latLong = ZipCodeDB.getInstance().getLatLong(zip);
        return latLong!=null?latLong.getSecond().intValue():null;
    }

    public Handle getValue(IProduct p) {
        return p.getHandle();
    }
}
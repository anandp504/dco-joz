package com.tumri.joz.index;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.Pair;

/**
 * Latitude Index
 * User: nipun
 */
public class LatitudeIndex extends ProductAttributeIndex<Integer, Handle>  {
    public LatitudeIndex() {
    }

    public IProduct.Attribute getType() {
        return IProduct.Attribute.kLatitude;
    }

    public Integer getKey(IProduct p) {
        Integer zip;
        try {
            zip = Integer.parseInt(p.getZipStr());
        } catch (NumberFormatException e) {
            zip = null;
        }
        Pair<Double, Double> latLong = ZipCodeDB.getInstance().getLatLong(zip);
        return latLong!=null?latLong.getFirst().intValue():null;
    }

    public Handle getValue(IProduct p) {
        return p.getHandle();
    }
}
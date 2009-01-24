package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.utils.ZipCodeHandle;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.content.data.Product;
import com.tumri.content.data.dictionary.DictionaryManager;

import java.util.ArrayList;
import java.util.SortedSet;

import org.apache.log4j.Logger;

/**
 * Radius query. Given a zip this class gets the neighboring zips and does a Zip query
 * for the adjoining zip codes. 
 * @author nnair
 */
public class RadiusQuery extends AttributeQuery {

    private static Logger log = Logger.getLogger (RadiusQuery.class);

    public RadiusQuery(IProduct.Attribute aAttribute, int aValue) {
        super(aAttribute, aValue);
    }

    public RadiusQuery(String zipCode, int radius) {
        super(IProduct.Attribute.kZip, getRadiusZips(zipCode, radius));
    }

    public RadiusQuery(IProduct.Attribute aAttribute,  ArrayList<Integer> values) {
        super(aAttribute, values);
    }

    public IWeight<Handle> getWeight() {
        return AttributeWeights.getWeight(IProduct.Attribute.kRadius);
    }

    private static ArrayList<Integer> getRadiusZips(String zipCode, int radius) throws NumberFormatException {
        ArrayList<Integer> radZipIds = new ArrayList<Integer>();
        try {
            SortedSet<ZipCodeHandle> nearByZips = ZipCodeDB.getInstance().getNearbyZips(Integer.parseInt(zipCode), radius);
            if (nearByZips!=null) {
                for (ZipCodeHandle zip : nearByZips) {
                    Integer zipId = (int)zip.getOid();
                    radZipIds.add(DictionaryManager.getId(IProduct.Attribute.kZip, Integer.toString(zipId)));
                }
            }
        } catch (NumberFormatException e) {
            log.error("Radius query failed : Zipcode is not a valid integer : " + zipCode);
            throw (e);
        }
        return radZipIds;
    }

}
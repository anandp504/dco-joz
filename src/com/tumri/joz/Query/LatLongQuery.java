/*
 * LatLongQuery.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.Query;

import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.utils.Pair;

import java.util.SortedSet;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;

import org.apache.log4j.Logger;

/**
 * Query for Latitude and Longitude index
 * @author: nipun
 * Date: Jan 28, 2009
 * Time: 9:53:22 AM
 */
public class LatLongQuery extends AttributeQuery {

    private static Logger log = Logger.getLogger (LatLongQuery.class);

    public LatLongQuery(IProduct.Attribute attr, String zipCode, int radius) {
      super(attr, getKeys(attr, zipCode, radius));
    }

    @SuppressWarnings("unchecked")
    public SortedSet<Handle> exec() {
      if (m_results == null) {
        ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
        List<Integer> values = m_values;
        m_results = (index != null) ? index.get(values) : tableScan();
      }
      return m_results;
    }


    /**
     * Gets the keys for the given lat or long query
     * We will assume that all distances are stored from 0.0 Lat and 0.0 Long
     * DistLat = 69.1 * (Lat1-0)
     * DistLong = 69.1 * (Lg1-0) * cos(Lat1 / 57.3)
     * @param zipCode
     * @param rad
     * @return - the query, or null if could not be constructed
     */
    private static ArrayList<Integer> getKeys(IProduct.Attribute attr, String zipCode, int rad) {
        ArrayList<Integer> keys = new ArrayList<Integer>();
        try {
            HashSet<Integer> keySet = new HashSet<Integer>();
            Pair<Double, Double> latLong = ZipCodeDB.getInstance().getLatLong(Integer.parseInt(zipCode));
            if (latLong==null) {
                log.warn("Invalid zip code passed in for lookup : " + zipCode);
                return null;
            }
            Double latObj = latLong.getFirst();
            Double longObj = latLong.getSecond();
            //compute max and min
            if (attr == IProduct.Attribute.kLatitude) {
                double deltaLat = rad/ 69.1;
                for (int i=(int)(latObj-deltaLat);i<=(int)(latObj+deltaLat);i++){
                    keySet.add(i);
                }
            } else {
                double deltaLong = rad / (69.1 * Math.cos(latObj/57.3));
                for (int i=(int)(longObj-deltaLong);i<=(int)(longObj+deltaLong);i++){
                    keySet.add(i);
                }
            }
            if (!keySet.isEmpty()) {
                keys.addAll(keySet);
            }
        } catch (Exception e) {
            log.error("LatLongQuery creation failed", e);
        }
        return keys;
    }

    public IWeight<Handle> getWeight() {
        return AttributeWeights.getWeight(IProduct.Attribute.kRadius);
    }
}

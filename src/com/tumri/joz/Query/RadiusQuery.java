package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.ranks.IWeight;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.joz.filter.IFilter;
import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.content.data.Product;

import java.util.SortedSet;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * Radius query. Given a zip this class gets the neighboring zips and does 
 *
 * @author nnair
 */
public class RadiusQuery extends AttributeQuery {

    private Integer currZipId = null;
    private int radius = 40;
    private static Logger log = Logger.getLogger(RadiusQuery.class);

    public int getRadius() {
        return radius;
    }

    public void setRadius(int rad) {
        if (rad == 10 || rad == 25 || rad == 40 || rad == 50 || rad == 100) {
            this.radius = rad;
        } else {
            log.warn("Radius value not supported : " + rad);
        }
    }

    public RadiusQuery(IProduct.Attribute aAttribute, int aValue) {
        super(aAttribute, aValue);
        currZipId = new Integer(aValue);
    }

    @SuppressWarnings("unchecked")
    public SortedSet<Handle> exec() {
      if (m_results == null) {
         List<Integer> zipIdAL = getNeighbouringZips();
         MultiSortedSet<Handle> radiusResult = new MultiSortedSet<Handle>();
         for (Integer nearbyZip: zipIdAL) {
             SortedSet<Handle> zipResult = selectProductsForZip(nearbyZip);
             if (zipResult!= null) {
                 radiusResult.add(zipResult);
             }
         }
         m_results = radiusResult;
      }
      return m_results;
    }

    public Filter<Handle> getFilter() {
        return ProductDB.getInstance().getFilter(IProduct.Attribute.kZip); 
    }

    private SortedSet<Handle> selectProductsForZip(Integer zipId) {
        ProductAttributeIndex<Integer,Handle> zipIndex = ProductDB.getInstance().getIndex(IProduct.Attribute.kZip);
        return zipIndex.get(zipId);
    }

    private List<Integer> getNeighbouringZips() {
        return ZipCodeDB.getInstance().getNearbyZips(currZipId, radius);
    }

}
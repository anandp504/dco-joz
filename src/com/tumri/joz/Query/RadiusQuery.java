package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.ZipCodeDB;
import com.tumri.joz.utils.ZipCodeHandle;
import com.tumri.utils.data.MultiSortedSet;
import org.apache.log4j.Logger;

import java.util.SortedSet;

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
        this.radius = rad;
    }

    public RadiusQuery(IProduct.Attribute aAttribute, int aValue) {
        super(aAttribute, aValue);
        currZipId = aValue;
    }

    public double getCost() {
      return ((double) getCount());
    }

    @SuppressWarnings("unchecked")
    public SortedSet<Handle> exec() {
      if (m_results == null) {
         SortedSet<ZipCodeHandle> zipIdAL = getNeighbouringZips();
         MultiSortedSet<Handle> radiusResult = new MultiSortedSet<Handle>();
         for (ZipCodeHandle nearbyZip: zipIdAL) {
             SortedSet<Handle> zipResult = selectProductsForZip((int)nearbyZip.getOid());
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

    @SuppressWarnings("unchecked")
    private SortedSet<Handle> selectProductsForZip(Integer zipId) {
        ProductAttributeIndex<Integer,Handle> zipIndex = ProductDB.getInstance().getIndex(IProduct.Attribute.kZip);
        return zipIndex.get(zipId);
    }

    private SortedSet<ZipCodeHandle> getNeighbouringZips() {
        return ZipCodeDB.getInstance().getNearbyZips(currZipId, radius);
    }

}
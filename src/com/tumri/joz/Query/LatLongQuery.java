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

    public LatLongQuery(IProduct.Attribute attr, String zipCode, int radius) {
      super(attr, ZipCodeDB.getInstance().getKeys(zipCode, (attr==IProduct.Attribute.kLatitude),radius));
    }

    @SuppressWarnings("unchecked")
    public SortedSet<Handle> exec() {
      if (m_results == null) {
        ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getIndex(getAttribute());
        m_results = (index != null) ? index.get(m_values) : tableScan();
      }
      return m_results;
    }

    public IWeight<Handle> getWeight() {
        return AttributeWeights.getWeight(IProduct.Attribute.kRadius);
    }


    public void setWeight(IWeight<Handle> wt) {
        throw new UnsupportedOperationException("This is not supported");
    }
}

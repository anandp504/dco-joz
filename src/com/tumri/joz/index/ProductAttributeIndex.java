/* 
 * ProductAttributeIndex.java
 * 
 * COPYRIGHT (C) 2007 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE 
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY, 
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL 
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART 
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM 
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR 
 * WRITTEN PERMISSION OF TUMRI INC.
 * 
 * @author Bhavin Doshi (bdoshi@tumri.com)
 * @version 1.0     Aug 29, 2007
 * 
 */
package com.tumri.joz.index;

import com.tumri.joz.products.IProduct;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * Created by Bhavin Doshi (bdoshi@tumri.com) on Aug 29, 2007
 * Company: Tumri Inc.
 */
public abstract class ProductAttributeIndex<Key, Value> extends com.tumri.utils.index.AbstractIndex<IProduct, IProduct.Attribute,  Key, Value> {

  public abstract Key getKey(IProduct p);

  public List<Key> getKeys(IProduct p) {
    List<Key> keys = new ArrayList<Key>();
    keys.add(getKey(p));
    return keys;
   }
}

/*
 * WMHandleFactory.java
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
package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;
import com.tumri.utils.data.SortedArraySet;

import java.util.*;

/**
 * @author: nipun
 * Date: Aug 10, 2009
 * Time: 12:37:45 PM
 */
public class WMHandleFactory {

    private SortedArraySet<WMHandle> wmHandles = new SortedArraySet<WMHandle>();

    private static WMHandleFactory _factory;

    /**
     * Gets the singleton instance of this impl
     * @return
     */
    public static WMHandleFactory getInstance() {
      if (_factory == null) {
        synchronized (WMHandleFactory.class) {
          if (_factory == null) {
            _factory = new WMHandleFactory();
          }
        }
      }
      return _factory;
    }

    /**
     * Get the handle if already there, else create one
     * @param id
     * @return
     */
    public WMHandle getHandle(long id,Map<WMIndex.Attribute, Integer> contextMap, List<RecipeWeight> recipeWeights) {
        WMHandle p = new WMHandle(id, contextMap, recipeWeights);
        Handle ph = wmHandles.find(p);
        if (ph !=null) {
             p = (WMHandle) ph;
        } else {
            //add it to the list
            wmHandles.add(p);
        }
        return p;

    }

    /**
     * Clears out the handles already created.
     */
    public void clear() {
        wmHandles.clear();
    }

    /**
     * Returns the current set of products that have been added.
     * @return
     */
    public SortedSet<WMHandle> getHandles() {
        return wmHandles;
    }
}

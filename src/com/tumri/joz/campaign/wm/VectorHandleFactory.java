/*
 * VectorHandleFactory.java
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

import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author: nipun
 * Date: Aug 10, 2009
 * Time: 12:37:45 PM
 */
public class VectorHandleFactory {

	private SortedArraySet<VectorHandle> newHandles = new SortedArraySet<VectorHandle>();
	private SortedArraySet<VectorHandle> currHandles = new SortedArraySet<VectorHandle>();

	private static VectorHandleFactory _factory;

	/**
	 * Gets the singleton instance of this impl
	 *
	 * @return
	 */
	public static VectorHandleFactory getInstance() {
		if (_factory == null) {
			synchronized (VectorHandleFactory.class) {
				if (_factory == null) {
					_factory = new VectorHandleFactory();
				}
			}
		}
		return _factory;
	}

	/**
	 * Get the handle if already there, else create one
	 *
	 * @param expId
	 * @param vectorId
	 * @param contextMap
     * @param multiple
	 * @return
	 */
	public VectorHandle getHandle(int expId, int vectorId, int type, Map<VectorAttribute, List<Integer>> contextMap, boolean multiple) {
        VectorHandle h = VectorDB.getInstance().getVectorHandle(expId, vectorId, VectorHandle.DEFAULT);
        if (h == null) {
            h = new VectorHandleImpl(expId, vectorId,type, contextMap, multiple);
            Handle ph = newHandles.find(h);
            if (ph != null) {
                h = (VectorHandle) ph;
            } else {
                //add it to the list
                newHandles.add(h);
            }
        }
        currHandles.add(h);

		return h;

	}

	/**
	 * Clears out the handles already created.
	 */
	public void clear() {
		newHandles.clear();
        currHandles.clear();
	}

	/**
	 * Returns the new handles that have been created - not yet added to vectordb.
	 *
	 * @return
	 */
	public SortedSet<VectorHandle> getNewHandles() {
		return newHandles;
	}

	/**
	 * Returns the current set of products that processed (includes new and created ones).
	 *
	 * @return
	 */
	public SortedSet<VectorHandle> getCurrHandles() {
		return newHandles;
	}
}
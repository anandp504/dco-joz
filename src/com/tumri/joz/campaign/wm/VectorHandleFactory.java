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

import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * @author: nipun
 * Date: Aug 10, 2009
 * Time: 12:37:45 PM
 */
public class VectorHandleFactory {

	private SortedSet<VectorHandle> newHandles = new TreeSet<VectorHandle>();
	private SortedSet<VectorHandle> currHandles = new TreeSet<VectorHandle>();


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
		VectorHandle h = VectorDB.getInstance().getVectorHandle(expId, vectorId, type);
		if (h == null) {
			h = new VectorHandleImpl(expId, vectorId, type, contextMap, multiple);
			Handle ph = find(newHandles, h);
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

	private static Handle find(SortedSet<VectorHandle> set, VectorHandle h) {
		SortedSet<VectorHandle> tailSet = set.tailSet(h);
		if (!tailSet.isEmpty()) {
			VectorHandle ph = tailSet.first();
			if (ph.compareTo(h) == 0)
				return ph;
		}
		return null;
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
		return currHandles;
	}
}
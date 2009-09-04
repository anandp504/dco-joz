package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;
import com.tumri.joz.Query.SetIntersector;
import com.tumri.joz.Query.AdPodSetIntersector;

/**
 * Set intersector for adpod results.
 *
 * @author bpatel
 */
public class WMSetIntersector extends SetIntersector<Handle> {

    public Handle getResult(Handle h, Double score) {
         return h.createHandle(score);
    }

    public WMSetIntersector(boolean strict) {
        super(strict);
    }

    public WMSetIntersector(WMSetIntersector set) {
        super(set);
    }

    public SetIntersector<Handle> clone() throws CloneNotSupportedException {
        return new WMSetIntersector(this);
    }
}
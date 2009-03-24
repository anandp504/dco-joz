package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;

/**
 * Set intersector for adpod results.
 *
 * @author bpatel
 */
public class AdPodSetIntersector extends SetIntersector<Handle> {

    public Handle getResult(Handle h, Double score) {
         return h.createHandle(score);
    }

    public AdPodSetIntersector(boolean strict) {
        super(strict);
    }

    public AdPodSetIntersector(AdPodSetIntersector set) {
        super(set);
    }

    public SetIntersector<Handle> clone() throws CloneNotSupportedException {
        return new AdPodSetIntersector(this);
    }
}

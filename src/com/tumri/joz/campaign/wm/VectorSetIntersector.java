package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;
import com.tumri.joz.Query.SetIntersector;

/**
 * Set intersector for adpod results.
 *
 * @author nipun
 */
public class VectorSetIntersector extends SetIntersector<Handle> {

    public Handle getResult(Handle h, Double score) {
         return h.createHandle(score);
    }

    public VectorSetIntersector(boolean strict) {
        super(strict);
    }

    public VectorSetIntersector(VectorSetIntersector set) {
        super(set);
    }

    public SetIntersector<Handle> clone() throws CloneNotSupportedException {
        return new VectorSetIntersector(this);
    }
}
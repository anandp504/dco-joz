package com.tumri.joz.campaign.wm;

import com.tumri.cma.rules.CreativeInstance;
import com.tumri.cma.rules.ListingClause;

/**
 * Bean class to hold the result of the vector targeting.
 */
public class VectorTargetingResult {
    private CreativeInstance ci = null;
    private ListingClause lc = null;

    public CreativeInstance getCi() {
        return ci;
    }

    public void setCi(CreativeInstance ci) {
        this.ci = ci;
    }

    public ListingClause getLc() {
        return lc;
    }

    public void setLc(ListingClause lc) {
        this.lc = lc;
    }
}

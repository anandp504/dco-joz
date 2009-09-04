package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;
import com.tumri.joz.ranks.IWeight;

/**
 * AttributeWeights implementation
 */
public class WMAttributeWeights  implements IWeight<Handle> {

    private static double kLineId = 1.0;
    private static double kState = 1.0;

    private WMHandle requestHandle = null;
    private WMIndex.Attribute attr = null;

    public WMAttributeWeights(WMHandle requestHandle, WMIndex.Attribute kAttr) {
        this.requestHandle = requestHandle;
        attr =kAttr;
    }
    
    /**
     * The weight returned here is a calculation based on the WMHandle's normalization factor
     * and the request handles norm factor
     * N factor of handle * N factor of request * ( SUM of square of Attribute Weights )
     * @param v
     * @return
     */
    public double getWeight(Handle v){
        WMHandle h = (WMHandle)v;
        double wt = getDefaultAttributeWeight(attr);
        return (wt*wt)*h.getNormFactor()*requestHandle.getNormFactor();
    }

    public int match(Handle v) {
        return 1;
    }

    public boolean mustMatch() {
        return true;
    }

    public static double getDefaultAttributeWeight(WMIndex.Attribute attr) {
        switch(attr) {
            case kLineId: return kLineId;
            case kState: return kState;
            default:
        }
        return 1.0;
    }
}

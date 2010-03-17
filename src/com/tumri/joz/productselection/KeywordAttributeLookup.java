package com.tumri.joz.productselection;

import java.util.HashMap;

/**
 * Class to specify the allowed terms in the keyword source field of the tspec
 */
public class KeywordAttributeLookup {

    public static enum KWAttribute {S1, F1, F2, F3, F4, F5, IGNORE}
    public static HashMap<String, KWAttribute> kwSrcMap = new HashMap<String, KWAttribute>();
    static {
        kwSrcMap.put("S1", KWAttribute.S1);
        kwSrcMap.put("F1", KWAttribute.F1);
        kwSrcMap.put("F2", KWAttribute.F2);
        kwSrcMap.put("F3", KWAttribute.F3);
        kwSrcMap.put("F4", KWAttribute.F4);
        kwSrcMap.put("F5", KWAttribute.F5);
        kwSrcMap.put("IGNORE", KWAttribute.IGNORE);
    }

    /**
     * Helper method to get the key for keyword source
     * @param val
     * @return
     */
    public static KWAttribute lookup(String val) {
        KWAttribute src = null;
        if (val!=null) {
            src = kwSrcMap.get(val);
        }
        if (src == null) {
           src =  KWAttribute.S1;
        }
        return src;
    }

}

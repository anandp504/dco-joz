package com.tumri.joz.productselection;

import java.util.HashMap;

/**
 * Class to specify the allowed terms in the keyword source field of the tspec
 */
public class KeywordAttributeLookup {

    public static enum KWAttribute {S1, F1, F2, F3, F4, F5, IGNORE}
    public static HashMap<String, KWAttribute> kwSrcMap = new HashMap<String, KWAttribute>();
    static {
        kwSrcMap.put("x2_s1", KWAttribute.S1);
        kwSrcMap.put("x2_f1", KWAttribute.F1);
        kwSrcMap.put("x2_f2", KWAttribute.F2);
        kwSrcMap.put("x2_f3", KWAttribute.F3);
        kwSrcMap.put("x2_f4", KWAttribute.F4);
        kwSrcMap.put("x2_f5", KWAttribute.F5);
        kwSrcMap.put("ignore", KWAttribute.IGNORE);
    }

    /**
     * Helper method to get the key for keyword source
     * @param val
     * @return
     */
    public static KWAttribute lookup(String val) {
        KWAttribute src = null;
        if (val!=null) {
            src = kwSrcMap.get(val.toLowerCase());
        }
        if (src == null) {
           src =  KWAttribute.S1;
        }
        return src;
    }

}

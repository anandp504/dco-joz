package com.tumri.joz.campaign.wm;

import com.tumri.joz.products.Handle;

/**
 * Abstract implementation of the VectorHandle
 */
public interface VectorHandle extends Handle {

    public static int DEFAULT = 0;
    public static int PERSONALIZATON = 2;
    public static int OPTIMIZATION = 1;

    
    public abstract boolean isMatch(VectorHandle that);

    public abstract boolean isDimensionSubset(VectorHandle that);

    public abstract int getType();

	public abstract int[] getContextDetails();

	public abstract int getSize();
    
}

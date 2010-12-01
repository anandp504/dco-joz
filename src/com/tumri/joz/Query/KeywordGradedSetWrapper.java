package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.utils.data.GradedSetWrapper;

import java.util.SortedSet;

/**
 * User: scbraun
 * Date: Nov 18, 2010
 */
public class KeywordGradedSetWrapper<Value> extends GradedSetWrapper<Value> {
	double modifier;

	public KeywordGradedSetWrapper(SortedSet<Value> handles) {
		super(handles);
		if (!handles.isEmpty()) {
			Handle h0 = (Handle) handles.first();
			modifier = 2.0 / h0.getScore();
		}
	}

	public double getGrade(Value p) {
		return modifier * ((Handle) p).getScore();
	}

	public double getMaxGrade() {
		return 2.0;
	}

	public boolean isMustMatch() {
		return true;
	}
}

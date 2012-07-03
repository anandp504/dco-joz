package com.tumri.joz.filter;

import com.tumri.content.TaxonomyProvider;
import com.tumri.content.data.Category;
import com.tumri.content.data.Product;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CategoryFilter extends Filter<Handle> {
	static Logger log = Logger.getLogger(CategoryFilter.class);
	SortedSet<Integer> m_descendants;

	public CategoryFilter() {
		super();
	}

	protected CategoryFilter(CategoryFilter f) {
		super(f); // m_descendents should not be copied
	}

	public boolean accept(Handle h) {
		if (!ProductDB.hasProductInfo()) {
			return super.accept(h);
		} else {
			Product p = ProductDB.getInstance().get(h);
			return (p != null && (getDescendants().contains(p.getCategory()) ^ isNegation()));
		}
	}

	private void computeDescendants() {
		ArrayList<Integer> descendants = new ArrayList<Integer>();
		List<Integer> values = getValues();
		for (Integer parent : values) {
			descendants.add(parent);
			computeChildren(descendants, parent);
		}
		m_descendants = new SortedArraySet<Integer>(descendants);
	}

	private void computeChildren(List<Integer> children, Integer parent) {
		//todo: improve
		Collection<TaxonomyProvider> tpc = AdvertiserTaxonomyMapperImpl.getInstance().getAllTaxonomyProviders();
		boolean found = false;
		for(TaxonomyProvider tp : tpc){
			Category c = tp.getTaxonomy().getCategory(parent);
			if (c != null) {
				found = true;
				Category[] categories = c.getChildren();
				if (categories != null) {
					for (Category cat : categories) {
						children.add(cat.getGlassId());
						computeChildren(children, cat.getGlassId());
					}
				}
			}
		}
		if(found) {
			log.warn("Error in t-spec category not found in taxonomy " + parent);
		}


	}

	public Filter<Handle> clone() {
		return new CategoryFilter(this);
	}

	public SortedSet<Integer> getDescendants() {
		if (m_descendants == null) {
			computeDescendants();
		}
		return m_descendants;
	}
}
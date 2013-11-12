package com.tumri.joz.Query;

import com.tumri.joz.filter.LongFilter;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

/**
 * User: scbraun
 * Date: 10/2/13
 */
public class OptTextQuery extends MUPQuery {
	private ArrayList<Integer> m_values = new ArrayList<Integer>();
	private int m_count = kMax;
	protected LongFilter<Handle> m_filter;
	private int experienceId;


	public Type getType() {
		return Type.kAttribute;
	}

	public OptTextQuery(IProduct.Attribute aAttribute, int experienceId, int aValue) {
		super(aAttribute);
		this.experienceId = experienceId;
		m_values.add(aValue);
	}

	public OptTextQuery(IProduct.Attribute aAttribute, int experienceId, ArrayList<Integer> values) {
		super(aAttribute);
		this.experienceId = experienceId;
		m_values.addAll(values);
	}

	public final ArrayList<Integer> getValues() {
		return m_values;
	}

	public void addValue(int aValue) {
		m_values.add(aValue);
	}

	public int getCount() {
		if (m_count == kMax) {
			@SuppressWarnings("unchecked")
			ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getOptIndex(getAttribute(), experienceId);
			if (index != null) {
				m_count = index.getCount(m_values);
			}
		}
		return m_count;
	}
	@SuppressWarnings("unchecked")
	public SortedSet<Handle> exec() {
		if (m_results == null) {
			// ??? This gets an "unchecked conversion" warning.
			ProductAttributeIndex<Integer,Handle> index = ProductDB.getInstance().getOptIndex(getAttribute(), experienceId);
			List<Integer> values = m_values;
			m_results = (index != null) ? index.get(values) : null;
		}
		return m_results;
	}

	public double getCost() {
		return getCount();
	}

	public LongFilter<Handle> getFilter() {
		return null;
	}

	public boolean hasIndex() {
     return true;
 }
}

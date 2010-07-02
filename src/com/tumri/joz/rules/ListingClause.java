package com.tumri.joz.rules;

import java.util.*;

/**
 * Portion of a listing query that can be injected into a tspec
 */
public class ListingClause implements Comparable {

	public static final String TYPE_CATEGORY = "kCategory";
	public static final String TYPE_MERCHANT = "kSupplier";
	public static final String TYPE_BRAND = "kBrand";
	public static final String TYPE_PRODUCT = "kId";
	public static final String TYPE_KEYWORD = "kKeywords";
	public static final String TYPE_GLOBALID = "kGlobalId";
	public static final String TYPE_HHMI = "kHHMI";
	public static final String TYPE_MS = "kMS";
	public static final String TYPE_BT = "kBT";
	public static final String TYPE_GENDER = "kGender";

	private HashMap<String, Set<String>> clauseTypeMap = new HashMap<String, Set<String>>();

	private static HashMap<String, Integer> clauseScoreMap = new HashMap<String, Integer>();

	static {
		clauseScoreMap.put(TYPE_CATEGORY, 1);
		clauseScoreMap.put(TYPE_MERCHANT, 1<<1);
		clauseScoreMap.put(TYPE_BRAND, 1<<2);
		clauseScoreMap.put(TYPE_PRODUCT, 1<<3);
		clauseScoreMap.put(TYPE_KEYWORD, 1<<4);
		clauseScoreMap.put(TYPE_GLOBALID, 1<<5);
		clauseScoreMap.put(TYPE_HHMI, 1<<6);
		clauseScoreMap.put(TYPE_MS, 1<<7);
		clauseScoreMap.put(TYPE_BT, 1<<8);
		clauseScoreMap.put(TYPE_GENDER, 1<<9);
	}

	public ListingClause(ListingClause lc) {
		merge(lc);
	}

    private int m_score = 0;

	/**
	 * Constructs a listing clause of the given type and value
	 *
	 * @param type
	 * @param value
	 */
	public ListingClause(String type, String value) {
		if (type != null) {
			Integer score = clauseScoreMap.get(type);
			if (score == null) {
				throw new UnsupportedOperationException("This type is not supported for listing optimization : " + type);
			}
			Set<String> res = clauseTypeMap.get(type);
			if (res == null) {
				res = new HashSet<String>();
				clauseTypeMap.put(type, res);
			}
			res.add(value);
            m_score|=score;
		}
	}

	/**
	 * Get the clause for a given type
	 *
	 * @param type
	 * @return
	 */
	public Set<String> getListingClause(String type) {
		Set<String> res = clauseTypeMap.get(type);
		if (res == null) {
			res = new HashSet<String>();
		}
		return res;
	}

	public int compareTo(Object o) {
        ListingClause that = (ListingClause)o;
        int diffScore = that.m_score-this.m_score;
        if (diffScore!=0){
            return diffScore;
        }
		int size = this.size();
		int oSize = that.size();
        return (oSize-size);
	}

	public int size() {
		int size = 0;
		for (String type : clauseTypeMap.keySet()) {
			size = size + clauseTypeMap.get(type).size();
		}
		return clauseTypeMap.size();
	}

	public void merge(ListingClause lc) {
		if (lc.clauseTypeMap != null && !lc.clauseTypeMap.isEmpty()) {
			for (String type : lc.clauseTypeMap.keySet()) {
				Set<String> res = clauseTypeMap.get(type);
				if (res == null) {
					res = new HashSet<String>();
					clauseTypeMap.put(type, res);
				}
				res.addAll(lc.clauseTypeMap.get(type));
			}
            this.m_score|=lc.m_score;
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (String type : clauseScoreMap.keySet()) {
			Set<String> vals = clauseTypeMap.get(type);
			if (vals.size() > 0) {
				sb.append(type);
				sb.append(": ");
				sb.append(vals);
			}
		}
		return sb.toString();
	}


}

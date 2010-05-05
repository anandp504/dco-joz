package com.tumri.joz.Query;

import com.tumri.joz.filter.Filter;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.utils.data.SortedArraySet;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class KeywordQuery extends MUPQuery {
    private String m_keywords;
    private String m_advertiser;
    private SortedSet<Handle> m_rawResults; // Maintains the raw results from search engine, sorted by score
    private boolean m_internal;

    public Type getType() {
        return Type.kKeyword;
    }

    /**
     * Construct a keyword query with a keyword string.
     * @param aKeywords the keyword string to be parsed by lucene
     * @param internal the boolean value should be true if the keywords are embeded in the T-Spec query
     */
    public KeywordQuery(String advertiser, String aKeywords, boolean internal) {
        super(IProduct.Attribute.kKeywords);
        m_keywords = aKeywords;
        m_internal = internal;
        m_advertiser = advertiser;
    }

    public String getKeywords() {
        return m_keywords;
    }

    public boolean isInternal() {
        return m_internal;
    }

    public int getCount() {
        return rawResults().size();
    }

    public double getCost() {
        return getCount();
    }

    public boolean hasIndex() {
        return true;
    }

    public SortedSet<Handle> exec() {
        if (m_results == null) {
            SortedSet<Handle> raw = rawResults();
            ArrayList<Handle> list = new ArrayList<Handle>(raw.size());
            for (Handle h : raw) {
                list.add(h);
            }
            m_results = new SortedArraySet<Handle>(list, false);
        }
        return m_results;
    }

    public SortedSet<Handle> rawResults() {
        if (m_rawResults == null) {
            ArrayList<Handle> res = ProductIndex.getInstance().search(m_advertiser, m_keywords,0.0,2000);
            m_rawResults = new SortedArraySet<Handle>(res, true);
        }
        return m_rawResults;
    }

    public Filter<Handle> getFilter() {
        return null;
    }

}


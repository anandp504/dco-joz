package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.data.SetUnion;

import java.util.ArrayList;
import java.util.SortedSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CNFQuery implements Query, Cloneable {
    private ArrayList<ConjunctQuery> m_queries = new ArrayList<ConjunctQuery>();
    private Handle m_reference;
    private Handle m_cache_reference = null;
    private int m_pagesize = 12;
    private int m_currentPage = 0;
    private boolean bPaginate = false;
    private boolean m_tableScan = false;

    public CNFQuery() {
    }

    public ArrayList<ConjunctQuery> getQueries() {
        return m_queries;
    }

    public void addQuery(ConjunctQuery q) {
        m_queries.add(q);
    }

    public Handle getReference() {
        return m_reference;
    }

    public void setCacheReference(Handle aReference) {
        m_cache_reference = aReference;
    }

    public Handle getCacheReference() {
        return m_cache_reference;
    }

    public void setReference(Handle aReference) {
        if (aReference!= null && m_cache_reference != null) {
            //Generate a new reference point
            Handle iProdHandle = ProductDB.getInstance().getHandle(m_cache_reference.getOid());
            if (iProdHandle!=null) {
                m_reference = iProdHandle;
            } else {
                m_reference = aReference;
            }
        } else {
            m_reference = aReference;
        }
    }

    // Clear the internal results of last computation
    public void clear() {
        for (ConjunctQuery query : m_queries) {
            query.clear();
        }
    }

    public SortedSet<Handle> exec() {
        Set<Handle> results;
        if (m_queries.size() == 1) {
            m_queries.get(0).setReference(m_reference);
            results = m_queries.get(0).exec();
        } else {
            SetUnion<Handle> unionizer = new SetUnion<Handle>();
            for (ConjunctQuery cjquery : m_queries) {
                unionizer.add(cjquery.exec());
            }
            if(unionizer.isEmpty()){
                return new SortedArraySet<Handle>();
            }
            results = unionizer;
        }
        //Paginate
        TreeSet<Handle> pageResults = new TreeSet<Handle>();
        if (bPaginate) {
            int start = (m_currentPage * m_pagesize) + 1;
            int end = start + m_pagesize;
            int i = 0;
            for (Handle handle : results) {
                i++;
                if (i < start) {
                    continue;
                } else if ((i >= start) && (i < end)) {
                    pageResults.add(handle);
                } else {
                    break;
                }
            }
        } else {
            int count = 0;
            for (Handle handle : results) {
                if (pageResults.add(handle) && (count++ >=m_pagesize)) {
                    break;
                }
            }
        }
        return new SortedArraySet<Handle>(pageResults);
    }

    public void setBounds(int pagesize, int currentPage) {
        bPaginate = true;
        if (pagesize ==0) {
            bPaginate = false;
        }
        m_pagesize = pagesize;
        m_currentPage = currentPage;
        for (ConjunctQuery cjq : m_queries) {
            cjq.setBounds(pagesize,currentPage);
        }
    }

    public Object clone() {
        CNFQuery copyCNF = null;
        try {
            copyCNF = (CNFQuery) super.clone();
        }
        catch (CloneNotSupportedException e) {
            // this should never happen
            throw new InternalError(e.toString());
        }
        if (m_queries != null) {
            ArrayList<ConjunctQuery> copyQueries = new ArrayList<ConjunctQuery>(m_queries.size());
            for (ConjunctQuery query : m_queries) {
                copyQueries.add((ConjunctQuery) query.clone());
            }
            copyCNF.m_queries = copyQueries;
        }
        copyCNF.setCacheReference(m_cache_reference);
        return copyCNF;
    }

    public void setStrict(boolean aStrict) {
        for (ConjunctQuery conjunctQuery : m_queries) {
            conjunctQuery.setStrict(aStrict);
        }
    }

    public void addSimpleQuery(SimpleQuery sQuery) {
        for (ConjunctQuery conjunctQuery : m_queries) {
            conjunctQuery.addQuery(sQuery);
        }
    }

    public void setScan(boolean tableScan) {
        m_tableScan = tableScan;
        for (ConjunctQuery cjq : m_queries) {
            cjq.setScan(m_tableScan);
        }
    }
}

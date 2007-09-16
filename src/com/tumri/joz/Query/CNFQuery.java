package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.SortedArraySet;

import java.util.ArrayList;
import java.util.SortedSet;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class CNFQuery implements Query, Cloneable {
  private ArrayList<ConjunctQuery> m_queries = new ArrayList<ConjunctQuery>();
  private Handle m_reference;
  private int m_pagesize = 12;
  private int m_currentPage = 0;
  private boolean bPaginate = false;

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

  public void setReference(Handle aReference) {
    m_reference = aReference;
  }

  // Clear the internal results of last computation
  public void clear() {
    for (ConjunctQuery query : m_queries) {
      query.clear();
    }
  }

  public SortedSet<Handle> exec() {
    SortedSet<Handle> results;
    if (m_queries.size() == 1) {
    	m_queries.get(0).setReference(m_reference);
      results = m_queries.get(0).exec();
    } else {
      MultiSortedSet<Handle> unionizer = new MultiSortedSet<Handle>();
      for (ConjunctQuery cjquery : m_queries) {
        cjquery.setReference(m_reference);
        unionizer.add(cjquery.exec(),true);
      }
      results = unionizer;
    }
    //Paginate
    ArrayList<Handle> pageResults = null;
    if (bPaginate) {
      pageResults = new ArrayList<Handle>(m_pagesize);
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
        pageResults = new ArrayList<Handle>(m_pagesize);
        for (Handle handle : results) {
            pageResults.add(handle);
        }
    }
    return new SortedArraySet<Handle>(pageResults, true);
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
}
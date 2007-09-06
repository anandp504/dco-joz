package com.tumri.joz.Query;

import com.tumri.joz.products.Handle;
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
  private int m_maxPages = 4;
  private int m_currentPage = 0;
  private boolean bPaginate = false;

  private SortedSet<Handle> m_results;

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
    for (int i = 0; i < m_queries.size(); i++) {
      m_queries.get(i).clear();
    }
    m_results = null;
  }

  public SortedSet<Handle> exec() {
    if (m_results == null) {
      SetUnionizer<Handle> unionizer = new SetUnionizer<Handle>();
      for (int i = 0; i < m_queries.size(); i++) {
        ConjunctQuery lConjunctQuery = m_queries.get(i);
        lConjunctQuery.setReference(m_reference);
        unionizer.include(lConjunctQuery.exec());
      }
      m_results = unionizer.union();
    } 
    //Paginate
    if (bPaginate) {
    	ArrayList<Handle> pageResults = new ArrayList<Handle>(m_pagesize);
    	int start = (m_currentPage*m_pagesize)+1;
    	int end = start+m_pagesize;
    	int i=0;
    	for (Handle handle : m_results) {
    		i++;
    		if (i < start) {
    			continue;
    		} else if ((i>=start) && (i<end)){
    			pageResults.add(handle);
    		} else {
    			break;
    		}
    	}
    	m_results = new SortedArraySet<Handle>(pageResults);
    }
    
    return m_results;
  }

  public void setBounds(int pagesize, int currentPage) {
	bPaginate = true;
    m_pagesize = pagesize;
    m_currentPage = currentPage;
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
      if (m_queries !=null) {
    	  ArrayList<ConjunctQuery> copyQueries = new ArrayList<ConjunctQuery>(m_queries.size());
    	  for (int i=0;i<m_queries.size();i++) {
    		  ConjunctQuery copyConjunct = (ConjunctQuery)m_queries.get(i).clone();
    		  copyQueries.add(copyConjunct);
    	  }
    	  copyCNF.m_queries = copyQueries;
      }
      return copyCNF;
  }

}
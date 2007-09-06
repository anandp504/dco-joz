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
  private boolean m_luceneSort = false;   
  private int m_currentPage = 0;
  private int m_pagesize = 0;
  private boolean bPaginate = false;
  
  public Type getType() {
    return Type.kKeyword;
  }

  public KeywordQuery(String aKeywords) {
    super(IProduct.Attribute.kKeywords);
    m_keywords = aKeywords;
  }

  public void setKeywords(String aKeywords) {
    m_keywords = aKeywords;
  }

  public String getKeywords() {
    return m_keywords;
  }

  public int getCount() {
    return exec().size();
  }

  public double getCost() {
    return getCount();
  }

  public boolean hasIndex() {
    return true;
  }

  @SuppressWarnings("unchecked")
  public SortedSet<Handle> exec() {
    if (m_results == null) {
      ArrayList<Handle> res = ProductIndex.getInstance().search(m_keywords,0.0,2000);
      m_results = new SortedArraySet(res, m_luceneSort);
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

  public Filter<Handle> getFilter() {
    return null;
  }
  
  public void setLuceneSortOrder(boolean luceneSortOrder) {
	  if (luceneSortOrder) {
		  m_luceneSort = false;
	  } else {
		  m_luceneSort = true;
	  }
  }

 public void setBounds(int currentPage, int pageSize){
	 bPaginate = true;
	 m_currentPage = currentPage;
	 m_pagesize = pageSize;
 }
  
}

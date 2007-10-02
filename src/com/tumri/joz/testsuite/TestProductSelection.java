package com.tumri.joz.testsuite;

import com.tumri.cma.RepositoryException;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.persistence.lisp.CampaignLispDataProviderImpl;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.Query.QueryProcessor;
import com.tumri.joz.campaign.OSpecQueryCacheHelper;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Sep 12, 2007
 * Time: 3:31:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestProductSelection {
  static Logger log = Logger.getLogger(TestProductSelection.class);

  private List<CNFQuery> m_queries = new ArrayList<CNFQuery>();

  public TestProductSelection() {
  }

  @Test
  public void test() {
    try {
      Properties props = AppProperties.getInstance().getProperties();
      ContentHelper.init(props);
      CampaignLispDataProviderImpl lispDeltaProvider = CampaignLispDataProviderImpl.getInstance(props);
      Iterator<OSpec> iter = lispDeltaProvider.getOspecs("US");
      QueryProcessor qp = new ProductQueryProcessor();
      ProductDB.getInstance();
      buildCNFQueries(iter);
      //setup();
      test0();
      test1();
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
  }

  private void test0() {
    long start = System.currentTimeMillis();
    for (CNFQuery cnf : m_queries) {
      SortedSet<Handle> set = cnf.exec();
    }
    System.out.println("Time is " + (System.currentTimeMillis() - start));
    for (int i = 0; i < 10; i++) {
      for (CNFQuery cnf : m_queries) {
        SortedSet<Handle> set = cnf.exec();
      }
    }
    System.out.println("Time is " + (System.currentTimeMillis() - start));
  }

  private List<CNFQuery> buildCNFQueries(Iterator<OSpec> iter) {
    List<CNFQuery> list = m_queries;
    int count =0;
    while (iter.hasNext()) {
      CNFQuery cnf = OSpecQueryCacheHelper.getQuery(iter.next());
      cnf.setStrict(true);
      cnf.setBounds(12, 0);
      list.add(cnf);
      count++;
    }
    System.out.println("Count is "+count);
    return list;
  }

  private void test1() {
    int k=0;
    for (CNFQuery cnf : m_queries) {
      cnf.setScan(true);
      cnf.setBounds(0,0);
      //long start = System.currentTimeMillis();
      SortedSet<Handle> set = cnf.exec();
      cnf.setScan(false);
      SortedSet<Handle> set1 = cnf.exec();
      //System.out.println(k + ": set: "+set.size() + " set1: " + set1.size() + " Time is " + (System.currentTimeMillis() - start)) ;
      Iterator<Handle> iter = set.iterator();
      Iterator<Handle> iter1 = set1.iterator();
      boolean hasNext = iter.hasNext();
      boolean hasNext1 = iter1.hasNext();
      Assert.assertEquals(hasNext,hasNext1);
      int count = 0;
      while(hasNext && hasNext1) {
        Handle h = iter.next();
        Handle h1 = iter1.next();
        Assert.assertEquals(h.getOid(),h1.getOid());
        hasNext = iter.hasNext();
        hasNext1 = iter1.hasNext();
        Assert.assertEquals(hasNext,hasNext1);
        count++;
      }
      k++;
    }
  }

  public static void main(String argv[]) {
    new TestProductSelection().test();
  }
}
package com.tumri.joz.testsuite;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.Test;

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

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Sep 12, 2007
 * Time: 3:31:53 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestProductSelection {
  static Logger log = Logger.getLogger(TestProductSelection.class);

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
      List<CNFQuery> cnfQueries = buildCNFQueries(iter);
      long start = System.currentTimeMillis();
      for (CNFQuery cnf : cnfQueries) {
        cnf.setStrict(true);
        cnf.setBounds(12,0);
        SortedSet<Handle> set = cnf.exec();
        System.out.println("Results: "+set.size());
      }
      System.out.println("Time is " + (System.currentTimeMillis() - start));
    } catch (RepositoryException e) {
      e.printStackTrace();
    }
  }

  private List<CNFQuery> buildCNFQueries(Iterator<OSpec> iter) {
    List<CNFQuery> list = new ArrayList<CNFQuery>();
    while (iter.hasNext()) {
      list.add(OSpecQueryCacheHelper.getQuery(iter.next()));
    }
    return list;
  }
}

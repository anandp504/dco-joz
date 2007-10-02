package com.tumri.joz.testsuite;

import com.tumri.cma.RepositoryException;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.persistence.lisp.CampaignLispDataProviderImpl;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.campaign.OSpecQueryCacheHelper;
import com.tumri.joz.campaign.CampaignDBDataLoader;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.products.Handle;
import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Sep 22, 2007
 * Time: 7:30:31 PM
 * To change this template use File | Settings | File Templates.
 */
public class TestMUPReload {
  static Logger log = Logger.getLogger(TestProductSelection.class);
  MUPSetup m_setup;

  public static void main(String argv[]) {
    new TestMUPReload().test();
  }
  
  public TestMUPReload() {
  }

  @Test public void test() {
    try {
      Properties p = AppProperties.getInstance().getProperties();
      m_setup = new MUPSetup(p);
      m_setup.setCount(100);

      m_setup.setOdd(true);
      m_setup.copyTo("test1");
      m_setup.initialLoad();
      m_setup.setOdd(false);
      m_setup.copyTo("test2");
      Thread.sleep(70000);

      CampaignDBDataLoader.getInstance().loadData();
      Thread ttest1 = new Thread(new TestProgram(p));
      ttest1.start();

      Thread.sleep(1000);
      m_setup.refresh();
      Thread ttest2 = new Thread(new TestProgram(p));
      ttest2.start();

      ttest1.join();
      ttest2.join();

    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      m_setup.cleanup();      
    }
    CampaignDB db = CampaignDB.getInstance();
  }
}

class TestProgram implements Runnable {
  List<CNFQuery> m_queries;

  public TestProgram(Properties aProperties) {
    try {
      Properties lProperties = (Properties) aProperties.clone();
      CampaignLispDataProviderImpl lispDeltaProvider = CampaignLispDataProviderImpl.newInstance(lProperties);
      Iterator<OSpec> iter = lispDeltaProvider.getOspecs("US");
      m_queries = buildCNFQueries(iter);
    } catch (RepositoryException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
  
  public void run() {
    try {
      long start = System.currentTimeMillis();
      for (CNFQuery cnf : m_queries) {
        SortedSet<Handle> set = cnf.exec();
      }
      System.out.println("Time is " + (System.currentTimeMillis() - start));
      for (int i = 0; i < 0; i++) {
        for (CNFQuery cnf : m_queries) {
          SortedSet<Handle> set = cnf.exec();
        }
      }
      System.out.println("Time is " + (System.currentTimeMillis() - start));
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
    m_queries = null;
  }

  private List<CNFQuery> buildCNFQueries(Iterator<OSpec> iter) {
    List<CNFQuery> list = new ArrayList<CNFQuery>();
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

}
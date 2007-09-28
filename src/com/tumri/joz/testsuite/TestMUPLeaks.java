package com.tumri.joz.testsuite;

import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.products.ProductDB;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.Assert;

import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Sep 27, 2007
 * Time: 10:44:16 AM
 * To change this template use File | Settings | File Templates.
 */
public class TestMUPLeaks {
  static Logger log = Logger.getLogger(TestMUPLeaks.class);
  MUPSetup m_setup;

  public static void main(String argv[]) {
    new TestMUPLeaks().test();
  }

  public TestMUPLeaks() {
  }

  @Test
  public void test() {
    try {
      Properties p = AppProperties.getInstance().getProperties();
      m_setup = new MUPSetup(p);
      m_setup.setCount(5);
      m_setup.setOdd(true);
      m_setup.copyTo("test5");
      m_setup.initialLoad();

      m_setup.setCount(4);
      m_setup.setOdd(false);
      m_setup.copyTo("test4");

      for(int i=0;i<5;i++) {
        Thread.sleep(69000);
        m_setup.refresh();
        Assert.assertEquals(ProductDB.getInstance().getAll().size(),4);
        m_setup.link("test5");
        Thread.sleep(61000);
        m_setup.refresh();
        Assert.assertEquals(ProductDB.getInstance().getAll().size(),5);
        m_setup.link("test4");
      }
    } catch(Exception e) {
    } finally {
      m_setup.cleanup();
    }
  }
}

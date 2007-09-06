package com.tumri.joz.utils;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AppProperties {
  private static Logger log = Logger.getLogger(AppProperties.class);
  public static String g_AppPropertyFile = "joz.properties";
  private static AppProperties g_properties;

  private Properties m_properties;

  public static AppProperties getInstance() {
    if (g_properties == null) {
      synchronized (AppProperties.class) {
        if (g_properties == null) {
          try {
            g_properties = new AppProperties();
            g_properties.init();
          } catch (IOException e) {
          }
        }
      }
    }
    return g_properties;
  }

  public String getProperty(String attr) {
    return m_properties.getProperty(attr);
  }

  public String getProperty(String attr, String def) {
    return m_properties.getProperty(attr, def);
  }
  
  public Properties getProperties() {
      return m_properties;
  }

  /**
   * This method can be called to read the property file again
   * @throws IOException
   */
  public void init() throws IOException {
    InputStream is = getInputStream();
    if (is != null) {
      try {
        Properties p = new Properties();
        BufferedInputStream bis = new BufferedInputStream(is);
        p.load(bis);
        m_properties = p; // @todo This should be atomic
      } finally {
        is.close();
      }
    } else if (m_properties == null) {
      m_properties = new Properties();
    }
  }

  private static InputStream getInputStream() {
    InputStream is =  ClassLoader.getSystemResourceAsStream(g_AppPropertyFile);
    if (is == null)
      log.error("Could not locate the resource file "+g_AppPropertyFile);
    return is;
  }

}
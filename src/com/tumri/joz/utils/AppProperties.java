package com.tumri.joz.utils;

import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
  public static String g_JozVersionPropertiesFile = "joz_version.properties";

  private static final String CONFIG_PROPERTY_JOZ_BUILD_VERSION = "build_version";
  private static final String CONFIG_PROPERTY_JOZ_CODE_LABEL = "code_label";
  private static final String CONFIG_PROPERTY_JOZ_RELEASE_VERSION = "release_version";
  private static final String CONFIG_PROPERTY_JOZ_ENV_HTML5 = "com.tumri.joz.targeting.htmlenv";
  private static final String CONFIG_PROPERTY_JOZ_ENV_FLASH = "com.tumri.joz.targeting.flashenv";
  private static final String CONFIG_PROPERTY_JOZ_ENV_MRAID = "com.tumri.joz.targeting.mraid";

  private static final String CONFIG_JOZ_VERSION_PROPERTIES_FILE_NAME = "com.tumri.joz.version.file.name";
  private static final String CONFIG_JOZ_BUILD_VERSION_PROPERTY_NAME = "com.tumri.joz.build.version.property";
  private static final String CONFIG_JOZ_RELEASE_VERSION_PROPERTY_NAME = "com.tumri.joz.release.version.property";
  private static final String CONFIG_JOZ_CODE_LABEL_PROPERTY_NAME= "com.tumri.joz.code.label.property";
  private static final String CONFIG_JOZ_MULTI_VALUE_DELIMITER = "com.tumri.joz.multivalue.delimiter";
  private static final String CONFIG_JOZ_SERVER_NIO_MODE = "com.tumri.joz.server.nio.mode";
  private static final String CONFIG_JOZ_MAX_CONCURRENT_CONTENT_LOAD = "com.tumri.joz.maxconcurrentcontentloading";
  private static final String JOZ_INDEX_VALIDATION_MODE = "com.tumri.joz.indexValidation.mode";

  private static AppProperties g_properties;
  private Properties m_jozVersionProperties;
    
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

  public String getVersionProperty(String attr) {
    return m_jozVersionProperties.getProperty(attr);
  }

  public String getVersionProperty(String attr, String def) {
    return m_jozVersionProperties.getProperty(attr, def);
  }

  public Properties getVersionProperties() {
    return m_jozVersionProperties;
  }

  public String getJozBuildVersion() {
     String jozBuildVersionPropName = getProperty(CONFIG_JOZ_BUILD_VERSION_PROPERTY_NAME);
     if (jozBuildVersionPropName==null){
        jozBuildVersionPropName = CONFIG_PROPERTY_JOZ_BUILD_VERSION;
     }
     return getVersionProperty(jozBuildVersionPropName);
  }

  public String getJozReleaseVersion() {
     String jozReleaseVersionPropName = getProperty(CONFIG_JOZ_RELEASE_VERSION_PROPERTY_NAME);
     if (jozReleaseVersionPropName==null){
        jozReleaseVersionPropName = CONFIG_PROPERTY_JOZ_RELEASE_VERSION;
     }
     return getVersionProperty(jozReleaseVersionPropName);
  }

  public String getJozCodeLabel() {
     String jozCodeLabelPropName = getProperty(CONFIG_JOZ_CODE_LABEL_PROPERTY_NAME);
     if (jozCodeLabelPropName==null){
        jozCodeLabelPropName = CONFIG_PROPERTY_JOZ_CODE_LABEL;
     }
     return getVersionProperty(jozCodeLabelPropName);
  }

  public char getMultiValueDelimiter() {
      char retChar = ',';
      String multiValueDelimiter = getProperty(CONFIG_JOZ_MULTI_VALUE_DELIMITER);
      if (multiValueDelimiter!= null) {
          char[] arr = multiValueDelimiter.toCharArray();
          retChar = arr[0];
      }
      return retChar;
  }

  public int getMaxConcurrentContentLoading() {
      int retVal = -1;
      String intString = getProperty(CONFIG_JOZ_MAX_CONCURRENT_CONTENT_LOAD);
      if (intString!= null) {
          retVal = Integer.parseInt(intString);
      }
      return retVal;
  }

  public String getTargetingHTMLEnv() {
      String htmlenv = getProperty(CONFIG_PROPERTY_JOZ_ENV_HTML5);
      if (htmlenv!= null) {
          htmlenv = htmlenv.trim();
      }
      return htmlenv;
  }
  public String getTargetingFlashEnv() {
      String flashenv = getProperty(CONFIG_PROPERTY_JOZ_ENV_FLASH);
      if (flashenv!= null) {
          flashenv = flashenv.trim();
      }
      return flashenv;
  }
  public String getTargetingMRaidEnv() {
      String mRaidEnv = getProperty(CONFIG_PROPERTY_JOZ_ENV_MRAID);
      if (mRaidEnv!= null) {
	      mRaidEnv = mRaidEnv.trim();
      }
      return mRaidEnv;
  }
  public boolean isNioEnabled(){
      return "true".equals(getProperty(CONFIG_JOZ_SERVER_NIO_MODE));
  }
  public boolean isIndexValidEnabled(){
      return "true".equals(getProperty(JOZ_INDEX_VALIDATION_MODE));
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

    InputStream versionIs = getJozVersionInputStream();
      if (versionIs != null) {
        try {
          Properties p = new Properties();
          BufferedInputStream bis = new BufferedInputStream(versionIs);
          p.load(bis);
          m_jozVersionProperties = p; // @todo This should be atomic
        } catch(Exception e) {
          e.printStackTrace();  
        } finally {
          is.close();
        }
      } else if (m_jozVersionProperties == null) {
        m_jozVersionProperties = new Properties();
      }

  }

  private static InputStream getInputStream() {
    InputStream is =  AppProperties.class.getClassLoader().getResourceAsStream(g_AppPropertyFile);
    if (is == null) {
      log.debug("Could not locate the resource file "+g_AppPropertyFile + ". Will try using catalina.base property if its tomcat");
      String catalinaBase = System.getProperty("catalina.base");
      if (catalinaBase != null) {
          String confFile = catalinaBase + File.separator + "conf" + File.separator + g_AppPropertyFile;
          try {
              is = new FileInputStream(confFile);
          } catch (FileNotFoundException ex) {
              log.debug("Could not locate the resource file "+g_AppPropertyFile + "in tomcat conf directory. Will try ../conf");
          }
      } else {
          log.debug("Could not locate the resource file "+g_AppPropertyFile + " in tomcat as catalina.base is not defined. Will try ../conf");
      }
      if (is == null) {
          try {
              is =  new FileInputStream("../conf/" + g_AppPropertyFile);
          } catch (FileNotFoundException ex) {
              log.debug("Couldn't find file " + g_AppPropertyFile + " in ../conf directory. Failing.");
          }
      }
      if (is == null) {
          String message = "Couldn't locate resource file " + g_AppPropertyFile + " in classpath, catalina.base/conf or ../conf directory.";
          log.error(message);
          LogUtils.getFatalLog().fatal(message);
      }
    } 
    
    return is;
  }

  private static InputStream getJozVersionInputStream() {
    String jozVersionFile = getInstance().getProperty(CONFIG_JOZ_VERSION_PROPERTIES_FILE_NAME);
    if (jozVersionFile == null || "".equals(jozVersionFile)) {
        jozVersionFile = g_JozVersionPropertiesFile;
    }
    InputStream is =  AppProperties.class.getClassLoader().getResourceAsStream(jozVersionFile);
    return is;
  }
}
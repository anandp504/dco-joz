package com.tumri.joz.monitor;

import java.util.ResourceBundle;
import java.io.InputStream;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.tumri.joz.JoZException;

public class JozMonitorException extends JoZException
{

  public JozMonitorException(String key)
  {
    super(getExceptionString(key));
  }

  public JozMonitorException(String key, String param)
  {
    super(getExceptionString(key)+" "+param);
  }
  // implementation details

  private static ResourceBundle rb;
  private static String propertiesFile = "com.tumri.joz.monitor.jozmonitor";
  private static Logger log = Logger.getLogger(JozMonitorException.class);

  protected static String getExceptionString(String key)
  {
    try {
        if (rb == null)
            rb = ResourceBundle.getBundle(propertiesFile);
        if (rb != null)
            return rb.getString(key);
    }
    catch (Exception ex)
    {
      log.error("Could not find string: "+ex+"  in the jozmonitor properties file");
    }
    return key;
  }
}

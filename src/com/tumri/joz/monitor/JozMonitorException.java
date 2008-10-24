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

  protected static String getExceptionString(String key)
  {
    return key;
  }
}

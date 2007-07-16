package com.tumri.joz.utils;

import org.w3c.dom.Document;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import java.io.File;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class DOMUtils {
  public static Document parse(File f) {
    FileInputStream fi = null;
    try {
      fi = new FileInputStream(f);
      BufferedInputStream bi = new BufferedInputStream(fi);

      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();

      return db.parse(bi);
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (fi != null)
          fi.close();
      } catch (IOException e) {
      }
    }
    return null;
  }
}

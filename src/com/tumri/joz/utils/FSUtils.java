package com.tumri.joz.utils;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.*;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Nov 7, 2007
 * Time: 3:58:37 PM
 * To change this template use File | Settings | File Templates.
 */
public class FSUtils {
  static private Logger log = Logger.getLogger(FSUtils.class);
  static private int PRIME = 179424673;
  /**
   * Recursively delete all the files from a directory, if the directory doesn't exist then nothing is done
   * @param dstDir, the target directory
   */
  static public void removeDir(File dstDir) {
    if (dstDir.exists() && dstDir.isDirectory()) {
      removeFiles(dstDir,true);
      if (dstDir.delete())
        log.info("Deleted dir "+dstDir.getPath());
    }
  }

  /**
   * Given a directory, removes all the files and directories inside the directory. All the directories are untouched if recursive flag is off
   * If the directory doesn't exist then nothing is done.
   * @param dstDir, the target directory
   */
  static public void removeFiles(File dstDir, boolean recursive) {
    if (dstDir.exists() && dstDir.isDirectory()) {
      File files[] = dstDir.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isFile() && f.delete()) {
            log.info("Deleted file "+f.getPath());
          } else if (f.isDirectory() && recursive) {
            removeDir(f);
            log.info("Deleted file "+f.getPath());
          }
        }
      }
    }
  }

  /**
   * Recursively delete all the files from a directory, if the directory doesn't exist then nothing is done
   * @param src, the source file
   * @param dst, the destination file
   * @throws IOException
   */
  public static void copyFile(File src, File dst) throws IOException {
    log.info("Copying file "+src.getPath() + " to " + dst.getPath() + " ...");
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(src);
      out = new FileOutputStream(dst);

      byte[] buf = new byte[8192];
      int len;
      while ((len = in.read(buf)) > 0) {
        out.write(buf, 0, len);
      }
    } finally {
      if (in != null) in.close();
      if (out != null) out.close();
    }
  }

  /**
   * Recursively copies all the files from a directory, if the src directory doesn't exist then nothing is done
   * @param srcDir, the source directory
   * @param dstDir, the destination directory
   * @throws IOException
   */
  public static void copyDir(File srcDir, File dstDir) throws IOException {
    if (srcDir.exists() && srcDir.isDirectory()) {
      if (!dstDir.isDirectory() && !dstDir.mkdirs()) {
        log.error("Failed to create directory "+dstDir);
        throw new IOException("Could not create directory"+dstDir);
      }
      File files[] = srcDir.listFiles();
      if (files != null) {
        for (File f : files) {
          if (f.isFile()) {
            log.info("Copying file " + f.getPath() + " to " + dstDir);
            copyFile(f,new File(dstDir,f.getName()));
          } else if (f.isDirectory()) {
            log.info("Copying directory " + f.getPath() + " to " + dstDir);
            copyDir(f,new File(dstDir,f.getName()));
          }
        }
      }
    }
  }

  public static File createTMPDir(String root, String prefix) {
    while (true) {
      long l = System.currentTimeMillis();
      int x = (int)(l % PRIME);
      File f = new File(root,prefix+Integer.toString(x));
      if (!f.exists())
        return f;
    }
  }


  @Test public void test() {
    try {
      File f   = new File("f"); f.mkdirs();
      File f1  = new File(f,"1"); f1.mkdirs();
      File f2  = new File(f,"2"); f2.mkdirs();
      File f11 = new File(f1,"1"); f11.mkdirs();
      File f12 = new File(f1,"2"); f12.mkdirs();
      File f21 = new File(f2,"1"); f21.mkdirs();
      File f22 = new File(f2,"2"); f22.mkdirs();
      File test = new File(f,"test");

      OutputStream out = null;
      try {
        byte buf[] = new byte[8193];
        out = new FileOutputStream(test);
        out.write(buf, 0, buf.length);
      } finally {
        if (out != null) out.close();
      }

      copyFile(test,new File(f1,"test"));
      copyFile(test,new File(f2,"test"));
      copyFile(test,new File(f11,"test"));
      copyFile(test,new File(f12,"test"));
      copyFile(test,new File(f21,"test"));
      copyFile(test,new File(f22,"test"));
      File d = new File("d");
      copyDir(f,d);
      removeDir(f);
      removeDir(d);
    } catch (IOException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

}
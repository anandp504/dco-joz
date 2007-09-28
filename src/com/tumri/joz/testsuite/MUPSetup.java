package com.tumri.joz.testsuite;

import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.products.ProductDB;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * Date: Sep 27, 2007
 * Time: 10:25:10 AM
 * To change this template use File | Settings | File Templates.
 */
public class MUPSetup {
  static Logger log = Logger.getLogger(MUPSetup.class);
  private File m_linkx;
  private boolean m_odd;
  private int m_count;
  private ArrayList<File> m_dirs = new ArrayList<File>();
  private Properties m_properties;
  private File m_src;

  public MUPSetup(Properties p) {
    p.setProperty("com.tumri.content.file.refreshInterval","-1");
    m_src = new File(p.getProperty("com.tumri.content.file.sourceDir"));
    m_linkx = new File(m_src.getParent(),"linkx");

    p.setProperty("com.tumri.content.file.sourceDir", m_linkx.getPath());
    p.setProperty("com.tumri.campaign.file.sourceDir",m_linkx.getPath());
    p.setProperty("com.tumri.content.file.batchReadSize","-1");

    m_properties = p;
    link(m_src);
  }

  public void initialLoad() {
    ContentHelper.init(m_properties);
    setCount(ProductDB.getInstance().getAll().size());
  }

  public void setOdd(boolean odd) {
    m_odd = odd;
  }

  public void setCount(int count) {
    m_count = count;
  }

  public void cleanup() {
    for(File dir : m_dirs) {
      removedir(dir);
    }
    m_linkx.delete();
  }

  public void refresh() {
    try {
      ContentProviderFactory.getInstance().getContentProvider().refresh();
    } catch (InvalidConfigException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }

  private void removedir(File dst) {
    removeFiles(dst);
    File[] files = dst.listFiles(new DirFilter());
    if (files != null) {
      for (File f : files) {
        removedir(f);
      }
    }
    if (dst.delete())
      log.info("Deleted dir "+dst.getPath());
  }

  private void removeFiles(File dst) {
    File files[] = dst.listFiles();
    if (files != null) {
      for (File f : files) {
        if (f.isFile() && f.delete())
          log.info("Deleted file "+f.getPath());
      }
    }
  }

  public void copyTo(String destdir) throws IOException {
    File dst = new File(m_src.getParentFile(),destdir);
    List<File> dirs = new ArrayList<File>();
    m_dirs.add(dst);
    listFiles(m_src,dirs, new DirFilter());
    for (File f : dirs) {
      File dir = (f == m_src ? dst : new File(dst,f.getPath().substring(m_src.getPath().length()+1)));
      if (dir.mkdirs())
        log.info("Adding directory "+dir.getPath());
      List<File> files = new ArrayList<File>();
      listFiles(f,files,new FilesFilter());
      for (File file : files) {
        File fileDst = new File(dst,file.getPath().substring(m_src.getPath().length()+1));
        if (file.getName().startsWith("USpub") && file.getName().endsWith(".utf8"))
          copyMUPFile(file,fileDst,m_odd);
        else
          copyFile(file,fileDst);
      }
    }
    link(dst);
    m_dirs.add(dst);
  }

  public void link(String dir) {
    for (File f : m_dirs) {
      if (f.getName().equals(dir)) {
        link(f);
        return;
      }
    }
  }
  private void link(File content) {
    {
      StringBuilder sb = new StringBuilder();
      String cmd1 = "rm -f " + m_linkx.getPath();
      sb.append("ln -s ").append(content.getPath()).append(" ").append(m_linkx.getPath());
      String cmd2 = sb.toString();
      System.out.println("Executing command "+cmd1 +" ; "+ sb.toString());
      try {
        Runtime.getRuntime().exec(cmd1);
        Runtime.getRuntime().exec(cmd2);
        Runtime.getRuntime().exec("sync");
      } catch (IOException e) {
        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
      }
    }
  }

  private void copyFile(File src, File dst) throws IOException {
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

  private void copyMUPFile(File src, File dst, boolean odd) throws IOException {
    log.info("Copying file "+src.getPath() + " to " + dst.getPath() + " ...");
    InputStream in = null;
    OutputStream out = null;
    try {
      in = new FileInputStream(src);
      InputStreamReader isr = new InputStreamReader(in, "utf8");
      BufferedReader br = new BufferedReader(isr);
      out = new FileOutputStream(dst);
      OutputStreamWriter osr = new OutputStreamWriter(out, "utf8");
      BufferedWriter bw = new BufferedWriter(osr);
      String line;
      int count = 0;
      while((line = br.readLine()) != null) {
        if (!odd ^ (count++ % 2) == 1) {
          bw.write(line);
          bw.write("\n");
        }
        if (count >= (m_count*2)) break;
      }
      bw.flush();
    } finally {
      if (in != null) in.close();
      if (out != null) out.close();
    }
  }


  private void listFiles(File src, List<File> files, FileFilter filter) {
    File[] flist = src.listFiles(filter);
    if (flist != null) {
      for (File f : flist) {
        files.add(f);
        listFiles(f,files,filter);
      }
    }
  }
}

class DirFilter implements FileFilter {
  DirFilter() {
  }
  public boolean accept(File aFile) {
    return aFile.isDirectory();
  }
}
class FilesFilter implements FileFilter {
  FilesFilter() {
  }
  public boolean accept(File aFile) {
    return !aFile.isDirectory();
  }
}

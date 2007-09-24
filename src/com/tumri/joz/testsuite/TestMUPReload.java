package com.tumri.joz.testsuite;

import com.tumri.cma.RepositoryException;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.persistence.lisp.CampaignLispDataProviderImpl;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.campaign.OSpecQueryCacheHelper;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ContentHelper;
import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.*;
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


  public TestMUPReload() {
  }

  @Test public void test() {
    try {
      Properties properties = AppProperties.getInstance().getProperties();
      Thread main = new Thread(new TestProgram(properties));
      main.start();

      String sourcedir = properties.getProperty("com.tumri.content.file.sourceDir");
      File src = new File(sourcedir);

      if (true) {
        File test1 = new File(src.getParentFile(),"test1");
        copydir(src,test1,true);
        properties.setProperty("com.tumri.content.file.sourceDir",test1.getPath());
        properties.setProperty("com.tumri.campaign.file.sourceDir",test1.getPath());
        Thread ttest1 = new Thread(new TestProgram(properties));
        ttest1.start();

        File test2 = new File(src.getParentFile(),"test2");
        copydir(src,test2,false);
        properties.setProperty("com.tumri.content.file.sourceDir",test2.getPath());
        properties.setProperty("com.tumri.campaign.file.sourceDir",test2.getPath());
        Thread ttest2 = new Thread(new TestProgram(properties));
        ttest2.start();

        main.join();
        ttest1.join();
        ttest2.join();

        removedir(test1);
        removedir(test2);
      }
    } catch (Exception e) {
      e.printStackTrace();
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

  private void copydir(File src, File dst, boolean odd) throws IOException {
    List<File> dirs = new ArrayList<File>();
    dirs.add(src);
    listFiles(src,dirs, new DirFilter());
    for (File f : dirs) {
      File dir = (f == src ? dst : new File(dst,f.getPath().substring(src.getPath().length()+1)));
      if (dir.mkdirs())
        log.info("Adding directory "+dir.getPath());
      List<File> files = new ArrayList<File>();
      listFiles(f,files,new FilesFilter());
      for (File file : files) {
        File fileDst = new File(dst,file.getPath().substring(src.getPath().length()+1));
        if (file.getName().startsWith("USpub") && file.getName().endsWith(".utf8"))
          copyMUPFile(file,fileDst,odd);
        else
          copyFile(file,fileDst);
      }
    }
  }

  void copyFile(File src, File dst) throws IOException {
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

  void copyMUPFile(File src, File dst, boolean odd) throws IOException {
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
        }
      }
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

class TestProgram implements Runnable {
  List<CNFQuery> m_queries;

  public TestProgram(Properties aProperties) {
    try {
      Properties lProperties = (Properties) aProperties.clone();
      ContentHelper.init(lProperties);
      CampaignLispDataProviderImpl lispDeltaProvider = CampaignLispDataProviderImpl.getInstance(lProperties);
      Iterator<OSpec> iter = lispDeltaProvider.getOspecs("US");
      m_queries = buildCNFQueries(iter);
    } catch (RepositoryException e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
  }
  
  public void run() {
    try {
      for (CNFQuery cnf : m_queries) {
        SortedSet<Handle> set = cnf.exec();
      }
      long start = System.currentTimeMillis();
      for (int i = 0; i < 10; i++) {
        for (CNFQuery cnf : m_queries) {
          SortedSet<Handle> set = cnf.exec();
        }
      }
      System.out.println("Time is " + (System.currentTimeMillis() - start));
    } catch (Exception e) {
      e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
    }
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
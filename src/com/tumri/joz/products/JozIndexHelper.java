package com.tumri.joz.products;

import com.tumri.joz.index.creator.PersistantProviderIndex;
import com.tumri.joz.index.creator.JozIndexUpdater;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.content.ContentProviderFactory;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

/**
 * Class that will load the Joz index files from a given directory
 * @author: nipun
 * Date: Feb 21, 2008
 */

public class JozIndexHelper {

    private String JOZ_INDEX_FILE_PATTERN = ".*_jozindex_.*.bin";
    private String indexDirName = "/data/jozindex";
    private static Logger log = Logger.getLogger(JozIndexHelper.class);
    private JozIndexUpdater updateHandler = null;
    private static JozIndexHelper inst = null;

    private boolean coldStart = false;
    private boolean debugMode = false;

    public static JozIndexHelper getInstance() {
        if (inst == null) {
            synchronized (JozIndexHelper.class) {
                if (inst == null) {
                    inst = new JozIndexHelper();
                }
            }
        }
        return inst;
    }

    private JozIndexHelper() {
        coldStart = false;
        debugMode = false;
        updateHandler = new JozIndexUpdater();
        init();
    }

    public void setColdStart(boolean cs) {
        coldStart = cs;
    }

    public void setDebugMode(boolean debug) {
        debugMode = debug;
    }

    public JozIndexUpdater getUpdater() {
        return updateHandler;
    }

    public boolean isColdStart() {
        return coldStart;
    }

    public boolean isDebugMode() {
        return debugMode;
    }

    /**
     * Load the Joz index from the default dir that is set in the joz.properties
     */
    public synchronized void loadJozIndex() {
        try {
            log.info("Starting to load the Joz indexes. Hot Deploy = " + !coldStart);
            Date start = new Date();
            //Look for any Joz index files
            List<File> indexFiles = getSortedJozIndexFileList(indexDirName);
            ArrayList<String> indexFileNames = new ArrayList<String>();
            for (File f: indexFiles) {
                readFromSerializedFile(f);
                indexFileNames.add(f.getName());
            }

            ContentProviderStatus status = null;
            try {
                status = ContentProviderFactory.getInstance().getContentProvider().getStatus();
                status.jozIndexFileNames = indexFileNames;
            } catch (Exception ex) {
                status = null;
            }
            log.info("Finished loading the Joz indexes");
            log.info( ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes" );
        } catch (Exception e) {
            log.error("Joz index load failed.", e);
        }
    }

    /**
     * Load the index for a given set of Bin files. This is used by the console utlity
     * @param idxDir
     * @param myBinFiles
     */
    public synchronized void loadIndexForDebug(String idxDir, ArrayList<String> myBinFiles) {
        if(myBinFiles!=null && myBinFiles.size() > 0){
            loadJozIndexFiles(idxDir, myBinFiles);
        } else {
            loadJozIndexFiles(idxDir, null);
        }
    }

    private void init() {
        JOZ_INDEX_FILE_PATTERN = AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.indexFileNamePattern");
        indexDirName = AppProperties.getInstance().getProperty("com.tumri.content.file.sourceDir");
        indexDirName = indexDirName + "/" + AppProperties.getInstance().getProperty("com.tumri.content.jozindexDir");
    }

    /**
     * Gets the current list of Joz index file in the Dir specified in indexDirName
     * @return
     */
    private List<File> getSortedJozIndexFileList(String dirName) {

        List<File> indexFiles = new ArrayList<File>();
        File indexDir = new File(dirName);
        if (!indexDir.exists()) {
            log.error("Directory does not exist : " + dirName);
        }

        File[] files = indexDir.listFiles();
        if (files != null) {
            for (File f: files) {
                if (f.getName().matches(JOZ_INDEX_FILE_PATTERN)) {
                    indexFiles.add(f);
                }
            }
        }
        if (indexFiles.size() == 0) {
            log.error("No joz index files found in directory: " + indexDir.getAbsolutePath());
        }

        //Sort the files by Name
        Collections.sort(indexFiles,
                new Comparator<File>(){
                    public int compare( File f1, File f2 )
                    {
                        String s1 = f1.getName();
                        String s2 = f2.getName();
                        //Strip off the extension
                        s1 = s1.substring(0,s1.indexOf(".bin"));
                        s2 = s2.substring(0,s2.indexOf(".bin"));
                        StringTokenizer st1 = new StringTokenizer( s1, "_" );
                        StringTokenizer st2 = new StringTokenizer( s2, "_" );
                        while ( st1.hasMoreTokens() && st2.hasMoreTokens() )
                        {
                            String t1 = st1.nextToken();
                            String t2 = st2.nextToken();

                            int c;
                            try
                            {
                                Integer i1 = new Integer( t1 );
                                Integer i2 = new Integer( t2 );
                                c = i1.compareTo( i2 );
                            }
                            catch ( NumberFormatException e )
                            {
                                c = t1.compareTo( t2 );
                            }
                            if ( c != 0 )
                            {
                                return c;
                            }
                        }

                        return 0;
                    }
                });
        return indexFiles;
    }

    /**
     *  Load the specific set of Joz Index Files
     */
    private void loadJozIndexFiles(String dirName, List<String> fileNames) {
        try {
            log.info("Starting to load the specified Joz indexes.");
            if (fileNames!=null && fileNames.size()>0) {
                for (String indexFileName: fileNames) {
                    File indexFile = new File (dirName + "/" + indexFileName);
                    if (indexFile.exists()) {
                        readFromSerializedFile(indexFile);
                    } else {
                        log.error("Specified file does not exist - cannot load : " + indexFile);
                    }
                }
            } else {
                List<File> idxFiles = getSortedJozIndexFileList(dirName);
                for (File indexFile: idxFiles) {
                    readFromSerializedFile(indexFile);
                }
            }
            log.info("Finished loading the Joz indexes");
        } catch (Exception e) {
            log.error("Joz index load failed.", e);
        }
    }

    /**
     * Helper method to read the index from a file.
     * @param inFile
     */
    private void readFromSerializedFile(File inFile) throws IOException, ClassNotFoundException {
        log.info("Going to load the index from file : " + inFile.getAbsolutePath());
        FileInputStream fis = null;
        ObjectInputStream in = null;

        try {
            fis = new FileInputStream(inFile);
            in = new ObjectInputStream(new BufferedInputStream(fis));
            PersistantProviderIndex pProvIndex = (PersistantProviderIndex)in.readObject();
            in.close();
            //Add any new products into the db
            if (!debugMode) {
                ProductDB.getInstance().addNewProducts(ProductHandleFactory.getInstance().getProducts());
            }
            ProductHandleFactory.getInstance().clearProducts();

        } catch (IOException ex) {
            log.error("Could not load index file.");
            throw ex;
        } catch (ClassNotFoundException ex){
            log.error("Deserialization failed from file. " + inFile.getAbsolutePath());
            throw ex;
        } catch (Throwable t){
            LogUtils.getFatalLog().info("Index load failed for : " + inFile.getAbsolutePath(),t);
        } finally {
            try {
                if (in!=null) {
                    in.close();
                }
            } catch(Throwable t) {
                log.error("Error in closing the file input stream", t);
            }
        }
    }


    /**
     * Test method
     * @param args
     */
    public static void main(String[] args){
        JozIndexHelper jh = JozIndexHelper.getInstance();
        jh.setColdStart(true);
        jh.setDebugMode(false);
        jh.loadJozIndex();
    }

}

package com.tumri.joz.products;

import com.tumri.joz.index.creator.PersistantProviderIndex;
import com.tumri.joz.utils.AppProperties;
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

    protected static boolean coldStart = false;

    public static JozIndexHelper getInstance(boolean coldStart) {
        return new JozIndexHelper(coldStart);
    }

    private JozIndexHelper() {

    }
    
    private JozIndexHelper(boolean cs) {
        coldStart = cs;
        init();
    }

    private void init() {
        JOZ_INDEX_FILE_PATTERN = AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.indexFileNamePattern");
        indexDirName = AppProperties.getInstance().getProperty("com.tumri.content.file.sourceDir");
        indexDirName = indexDirName + "/" + AppProperties.getInstance().getProperty("com.tumri.content.jozindexDir");
    }

    /**
     * Load the Joz index from the given dir
     */
    public void loadJozIndex() {
        try {
            log.info("Starting to load the Joz indexes. Hot Deploy = " + !coldStart);

            Date start = new Date();
            File indexDir = new File(indexDirName);
            if (!indexDir.exists()) {
                log.error("Directory does not exist : " + indexDirName);
            }

            //Look for any Joz index files
            File[] files = indexDir.listFiles();
            List<File> indexFiles = new ArrayList<File>();
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
                            java.util.StringTokenizer st1 = new java.util.StringTokenizer( s1, "_" );
                            java.util.StringTokenizer st2 = new java.util.StringTokenizer( s2, "_" );
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
            
            for (File f: indexFiles) {
                readFromSerializedFile(f);
            }

            log.info("Finished loading the Joz indexes");
            log.info( ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes" );
        } catch (Exception e) {
            log.error("Joz index load failed.", e);
        }
    }

    /**
     * Helper method to read from a file.
     * @param inFile
     */
    private static void readFromSerializedFile(File inFile) throws IOException, ClassNotFoundException {
        log.info("Going to load the index from file : " + inFile.getAbsolutePath());
        FileInputStream fis = null;
        ObjectInputStream in = null;

        try {
            fis = new FileInputStream(inFile);
            in = new ObjectInputStream(new BufferedInputStream(fis));
            PersistantProviderIndex pProvIndex = (PersistantProviderIndex)in.readObject();
            in.close();
            //Add any new products into the db
            ProductDB.getInstance().addNewProducts(ProductHandleFactory.getInstance().getProducts());
            ProductHandleFactory.getInstance().clearProducts();

        } catch (IOException ex) {
            log.error("Could not load index file.");
            throw ex;
        } catch (ClassNotFoundException ex){
            log.error("Deserialization failed from file. " + inFile.getAbsolutePath());
            throw ex;
        } catch (Throwable t){
            log.error("Index load failed for : " + inFile.getAbsolutePath(),t );
        } finally {
            try {
                in.close();
            } catch(Throwable t) {
                log.error("Error in closing the file input stream");
                t.printStackTrace();
            }
        }
    }

    public static boolean isColdStart() {
        return coldStart;
    }

    /**
     * Used by the test framework
     * @param idxDir
     */
    public static void loadIndex(String idxDir) {
        JozIndexHelper jh = new JozIndexHelper();
        jh.indexDirName = idxDir;
        coldStart = true;
        jh.loadJozIndex();
    }

    /**
     * Test method
     * @param args
     */
    public static void main(String[] args){
        JozIndexHelper.getInstance(true).loadJozIndex();
    }

}

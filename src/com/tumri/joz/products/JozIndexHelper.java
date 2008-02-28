package com.tumri.joz.products;

import com.tumri.joz.index.creator.PersistantProviderIndex;
import com.tumri.joz.utils.AppProperties;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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


    private JozIndexHelper(boolean cs) {
        coldStart = cs;
        init();
    }

    private void init() {
        JOZ_INDEX_FILE_PATTERN = AppProperties.getInstance().getProperty("com.tumri.joz.index.reader.indexFileNamePattern");
        indexDirName = AppProperties.getInstance().getProperty("com.tumri.content.file.sourceDir");
        indexDirName = indexDirName + "/" + AppProperties.getInstance().getProperty("com.tumri.content.jozindexDir");
    }

    //TODO: Also do the full reload - for the QAC revert content scenario.
    
    /**
     * Load the Joz index from the given dir
     */
    public void loadJozIndex() {
        try {
            log.info("Starting to load the Joz indexes. Hot Deploy = " + !coldStart);

            Date start = new Date();
            File indexDir = new File(indexDirName);
            if (!indexDir.exists()) {
                throw new RuntimeException("Directory does not exist : " + indexDirName);
            }

            //Look for any Joz index files
            File[] files = indexDir.listFiles();
            List<File> indexFiles = new ArrayList<File>();
            for (File f: files) {
                if (f.getName().matches(JOZ_INDEX_FILE_PATTERN)) {
                    indexFiles.add(f);
                }
            }
            if (indexFiles.size() == 0) {
                throw new RuntimeException("No joz index files found in directory: " + indexDir.getAbsolutePath());
            }

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
            in = new ObjectInputStream(fis);
            PersistantProviderIndex pProvIndex = (PersistantProviderIndex)in.readObject();
            in.close();
            //Add any new products into the db
            ProductDB.getInstance().addNewProducts();

        } catch (IOException ex) {
            log.error("Could not load index file.");
            throw ex;
        } catch (ClassNotFoundException ex){
            log.error("Deserialization failed from file. " + inFile.getAbsolutePath());
            throw ex;
        }
    }

    public static boolean isColdStart() {
        return coldStart;
    }

    /**
     * Test method
     * @param args
     */
    public static void main(String[] args){
        JozIndexHelper.getInstance(true).loadJozIndex();
    }

}

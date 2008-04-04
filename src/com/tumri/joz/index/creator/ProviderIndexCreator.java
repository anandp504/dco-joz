package com.tumri.joz.index.creator;

import com.tumri.utils.strings.StringTokenizer;
import com.tumri.joz.utils.FSUtils;

import java.io.*;
import java.util.*;

import org.apache.log4j.Logger;

/**
 * Given a set of old and new provider mup files, create a Joz incremental index file(s)
 * This will be invoked from the master joz index creator.
 * User: nipun
 */

public class ProviderIndexCreator {

    private Properties mupConfig = null;
    private int maxPidEntriesPerChunk = 10000;
    private String mupConfigFileName = "jozindex.properties";
    private File newFile = null;
    private File oldFile = null;
    private HashMap<String, ProviderIndexBuilder> indexMap = new HashMap<String, ProviderIndexBuilder>();
    private String currProvider = null;
    private String outDirPath = "/tmp";
    int chunkId = 0;
    private static Logger log = Logger.getLogger(ProviderIndexCreator.class);

    /**
     * Default constructor. Note that if f2 is null, then it is assumed that we are not building an incremental index.
     * ie., the resulting joz index file will not contain any DELETE or NO CHANGE specifications.
     * @param provider --> Prefix to the index file
     * @param outDirPath --> Path where the output files will be created
     * @param f1
     * @param f2
     */
    public ProviderIndexCreator(String provider, String outDirPath, File f1, File f2) {
        this.newFile = f1;
        this.oldFile = f2;
        this.currProvider = provider;
        this.outDirPath = outDirPath;
        init();
    }


    public ProviderIndexCreator(String provider, String outDirPath, File f1, File f2, int maxLinesPerChunk) {
        this.newFile = f1;
        this.oldFile = f2;
        this.currProvider = provider;
        this.outDirPath = outDirPath;
        this.maxPidEntriesPerChunk = maxLinesPerChunk;
        init();
    }
    /**
     * 1. Load the mup config files from classpath
     * 2. Initialize the Hashsets for the indices
     */
    public void init() {
        log.debug("Going to create the provider index for " + currProvider + ". Using new mup file " + newFile.getAbsolutePath());
        sortFile(newFile.getParentFile().getAbsolutePath(), newFile.getName());
        if (oldFile!=null) {
            log.debug("Old mup used for comparison is : " + oldFile.getAbsolutePath());
            sortFile(oldFile.getParentFile().getAbsolutePath(), oldFile.getName());
        }
        InputStream is = null;
        try {
            mupConfig = new Properties();
            is = getPropertyInputStream(mupConfigFileName);
            mupConfig.load(is);

            //Initialize the indexMaps
            Iterator indexNameIter = mupConfig.keySet().iterator();
            while (indexNameIter.hasNext()) {
                String indexName = (String)(mupConfig.get(indexNameIter.next()));
                indexMap.put(indexName, new ProviderIndexBuilder(indexName));
            }
            //Adding the geoEnabled column as a separate index
            indexMap.put("geoenabled", new ProviderIndexBuilder("geoenabled"));
        } catch (IOException e) {
            log.error("Exception caught during the init of the ProviderIndexCreator", e);
        } catch (Exception e) {
            log.error("Exception caught during the init of the ProviderIndexCreator", e);
        } finally {
            try {
                is.close();
            } catch(Exception e) {
                log.error("Exception caught", e);
            }
        }
    }

    /**
     * Reads the next line for the give is - returns null if EOF
     * @param br
     * @return
     */
    private String readLine(BufferedReader br) throws IOException {
        String line= br.readLine();
        return line;
    }

    /**
     * For a given provider, read the 2 mups and build the joz index files.
     * The process expects that the files are presorted by pid ( which is the fist column
     */
    public void createIndicesForProvider() throws IOException {
        if (!newFile.exists()) {
            log.debug("Cannot create the Joz index, the new mup file is missing :" + newFile.getAbsolutePath());
        }
        log.debug("Going to load the product info from file : " + newFile.getName());

        //Read first file
        boolean bAddMode = false;
        if (oldFile== null || !oldFile.exists()) {
            bAddMode = true;
        }
        FileInputStream fir1=null,fir2=null;
        InputStreamReader isr1=null,isr2=null;
        BufferedReader br1=null,br2=null;
        int lineCount = 0;
        try {
            fir1 = new FileInputStream(newFile);
            isr1 = new InputStreamReader(fir1, "utf8");
            br1 = new BufferedReader(isr1);

            if (!bAddMode) {
                fir2 = new FileInputStream(oldFile);
                isr2 = new InputStreamReader(fir2, "utf8");
                br2 = new BufferedReader(isr2);
            }
            boolean eof1 = false, eof2 = false;
            String line1 = null, line2= null;
            String pid1 = null, pid2= null;
            HashMap<String, String> prodDetailsMap1 = null;
            HashMap<String, String> prodDetailsMap2 = null;
            boolean bReadSecondFile = true;
            Long pid2Dbl = null;

            while(!eof1) {
                line1 = readLine(br1);
                lineCount++;
                if (line1 == null) {
                    eof1 = true;
                    continue;
                } else {
                    pid1 = getProductID(line1);
                    prodDetailsMap1 = convertLine(line1);
                }
                Long pid1Dbl = new Long(pid1);
                if (!bAddMode) {

                    if (!bReadSecondFile) {
                        //The old file was not read, compare and act on the line from first file
                        if (pid1Dbl.equals(pid2Dbl)) {
                            //Product appears in both MUPs
                            compareAndUpdateIndices(pid1Dbl, prodDetailsMap1, prodDetailsMap2);
                        } else if (pid1Dbl>pid2Dbl){
                            bReadSecondFile = true;
                        } else {
                            //pid1 is a new product in MUP
                            addIndices(prodDetailsMap1, pid1Dbl);
                        }
                    }


                    while (!bAddMode && bReadSecondFile) {
                        line2 = readLine(br2);
                        if (line2 == null) {
                            //This means that the rest of the products in new file will be all ADDED
                            bAddMode = true;
                            eof2 = true;
                            break;
                        }
                        pid2 = getProductID(line2);
                        prodDetailsMap2 = convertLine(line2);
                        pid2Dbl = new Long(pid2);

                        if (pid1Dbl.equals(pid2Dbl)) {
                            //Product appears in both MUPs
                            compareAndUpdateIndices(pid1Dbl, prodDetailsMap1, prodDetailsMap2);
                            bReadSecondFile = false;
                        } else if (pid1Dbl>pid2Dbl){
                            //pid2 has been deleted from MUP
                            deleteIndices(prodDetailsMap2, pid2Dbl);
                        } else {
                            //pid1 is a new product in MUP
                            bReadSecondFile = false;
                            addIndices(prodDetailsMap1, pid1Dbl);
                        }
                    }

                }

                if (bAddMode) {
                    //pid1 is a new product in MUP
                    addIndices(prodDetailsMap1, pid1Dbl);
                }

                if (lineCount>= maxPidEntriesPerChunk) {
                    lineCount = 0;
                    writeChunkToFile();
                }
            }

            if (!eof2 && br2!=null) {
                // We have more entries in second file that needs to be deleted
                while (!eof2) {
                    lineCount++;
                    line2 = readLine(br2);
                    if (line2 == null) {
                        eof2 = true;
                        break;
                    }
                    pid2 = getProductID(line2);
                    pid2Dbl = new Long(pid2);
                    prodDetailsMap2 = convertLine(line2);
                    //pid2 has been deleted from MUP
                    deleteIndices(prodDetailsMap2, pid2Dbl);
                    if (lineCount>= maxPidEntriesPerChunk) {
                        lineCount = 0;
                        writeChunkToFile();
                    }
                }
            }

        } finally {
            if (br1 != null) {
                br1.close();
            }
            if (isr1!=null) {
                isr1.close();
            }
            if (fir1 != null) {
                fir1.close();
            }
            if (br2 != null) {
                br2.close();
            }
            if (isr2!=null) {
                isr2.close();
            }
            if (fir2 != null) {
                fir2.close();
            }
        }

        //Flush any remaining indices to file
        writeChunkToFile();
    }

    /**
     * Get the Product ID from the given line. The PID is assumed to be the first column, and we drop any chracters that
     * it starts with and convert the rest into a Long.
     * @param line
     * @return
     */
    private String getProductID(String line) {
        //Find the first column
        String pidStr =line.substring(0,line.indexOf('\t'));
        char[] pidCharArr = pidStr.toCharArray();
        //Drop any non digit characters
        StringBuffer spid = new StringBuffer();
        for (char ch: pidCharArr) {
            if (Character.isDigit(ch)) {
                spid.append(ch);
            }
        }
        return spid.toString();
    }

    /**
     * Read the line and build the list of values based on the pos that is being indexed.
     * @param line
     * @return
     */
    protected HashMap<String,String> convertLine(String line) {
        HashMap<String,String> retVal = null;
        if (mupConfig==null) {
            return null;
        }
        Set indexPosSet = mupConfig.keySet();
        if (line != null && !"".equals(line.trim())) {
            StringTokenizer str = new StringTokenizer(line,'\t');
            ArrayList<String> strings = str.getTokens();
            retVal = new HashMap<String,String>();
            String currCat = "";
            for (int i=0;i<strings.size();i++) {
                if (indexPosSet.contains(new Integer(i).toString())) {
                    String val = strings.get(i);
                    String key = mupConfig.getProperty(new Integer(i).toString());
                    if (key.equals("category")) {
                        currCat = val;
                    }
                    if (key.startsWith("categoryfield")) {
                        if (currCat != null && !currCat.equals(""))  {
                            retVal.put(key,currCat + "|" + val);
                        }
                    } else {
                        retVal.put(key,val);
                    }
                    //If the product is geo filtered - then update the flag accordingly.
                    if (key.equals("country") || key.equals("state") || key.equals("city") ||
                            key.equals("dma") || key.equals("area")) {
                        if (val != null && !val.equals("")) {
                            retVal.put("geoenabled","true");
                        } else {
                            retVal.put("geoenabled","false");
                        }
                    }
                }
            }
        }
        return (retVal);
    }


    /**
     * Helper method to get a resource from the classpath
     * @param propertyFile
     * @return
     */
    private static InputStream getPropertyInputStream(String propertyFile) {
        InputStream is =  ProviderIndexCreator.class.getClassLoader().getResourceAsStream(propertyFile);
        return is;
    }

    /**
     * Update all the indexes with the add
     * @param entry
     * @param pid
     */
    private void addIndices(HashMap<String, String> entry,Long pid) {
        Iterator indexIter = indexMap.keySet().iterator();
        while (indexIter.hasNext()) {
            String indexName = (String)indexIter.next();
            ProviderIndexBuilder currIndex = indexMap.get(indexName);
            String value = entry.get(indexName);
            if (value != null) {
                currIndex.handleAdd(value,pid);
            }
        }
    }

    /**
     * Update all indexes for delete event
     * @param entry
     * @param pid
     */
    private void deleteIndices(HashMap<String, String> entry,Long pid) {
        Iterator indexIter = indexMap.keySet().iterator();
        while (indexIter.hasNext()) {
            String indexName = (String)indexIter.next();
            ProviderIndexBuilder currIndex = indexMap.get(indexName);
            String value = entry.get(indexName);
            if (value != null) {
                currIndex.handleDelete(value,pid);
            }

        }
    }

    /**
     * Update the given indexes with the no change event
     * @param indexName
     * @param pid
     * @param addIndexVal
     */
    private void addNoChangeIndices(String indexName,Long pid,String addIndexVal) {
        ProviderIndexBuilder currIdx = indexMap.get(indexName);
        if (addIndexVal != null) {
            currIdx.handleNoChange(addIndexVal, pid);
        }
    }

    /**
     * Implement an add and a delete for the given index and the values
     * @param indexName
     * @param pid
     * @param addIndexVal
     * @param delIndexVal
     */
    private void handleChangeEvent(String indexName,Long pid,String addIndexVal, String delIndexVal) {
        ProviderIndexBuilder currIdx = indexMap.get(indexName);
        if (addIndexVal != null) {
            currIdx.handleAddModified(addIndexVal, pid);
        }
        if ( delIndexVal != null) {
            currIdx.handleDeleteModified(delIndexVal, pid);
        }
    }

    /**
     * Compare the entries - and see what changed and add to the index
     * @param pid
     * @param newEntry
     * @param oldEntry
     */
    private void compareAndUpdateIndices(Long pid , HashMap<String, String> newEntry, HashMap<String, String> oldEntry)  {
        Iterator<String> currIndexIterator = newEntry.keySet().iterator();
        while (currIndexIterator.hasNext()) {
            String currIndexKey = currIndexIterator.next();
            if (newEntry.get(currIndexKey).equals(oldEntry.get(currIndexKey))) {
                //add no change
                addNoChangeIndices(currIndexKey, pid, newEntry.get(currIndexKey));
            } else {
                //handle change
                handleChangeEvent(currIndexKey, pid, newEntry.get(currIndexKey), oldEntry.get(currIndexKey));
            }
        }

    }

    /**
     * Write the current set of indices to file, and reinit the indices.
     */
    private void writeChunkToFile() {
        chunkId++;
        writeIndexToFile();
    }

    private void writeIndexToFile() {
        Iterator indexIter = indexMap.keySet().iterator();
        String fileName = outDirPath + "/" + currProvider + "_jozindex_" + chunkId + ".bin";
        File outFile = new File(fileName);
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        log.debug("Writing binary file : " + outFile.getAbsolutePath());
        try {
            fos = new FileOutputStream (outFile);
            out = new ObjectOutputStream(fos);
            PersistantProviderIndex provIndex = new PersistantProviderIndex();
            provIndex.setProviderName(currProvider);
            ArrayList<PersistantIndex> pindices = new ArrayList<PersistantIndex>(indexMap.size());
            while (indexIter.hasNext()) {
                String indexName = (String)indexIter.next();
                ProviderIndexBuilder currIndex = indexMap.get(indexName);
                PersistantIndex pIndex = currIndex.serializeIndex();
                if (pIndex.getDetails() != null) {
                    pindices.add(pIndex);
                }
            }
            provIndex.setIndices(pindices.toArray(new PersistantIndex[0]));
            out.writeObject(provIndex);
        } catch(IOException e) {
            log.error("Could not write to outfile : " + outFile.getAbsolutePath(), e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                //
            }
        }

    }

    /**
     * Helper method to invoke the unix sort on the file
     */
    private static boolean sortFile(String filePath, String fileName) {
        File currentFile = new File(filePath + fileName);
        if (!currentFile.exists()) {
           return false;
        }

        File tmpFile = new File(filePath + "tmp" + fileName);

        String[] args = new String[]{"sh", "-c", "sort " + currentFile.getAbsolutePath() + " > " + tmpFile.getAbsolutePath()};
        boolean bSuccess = false;
        Runtime rt = Runtime.getRuntime();
        try {
            Process proc = rt.exec(args);
            int rc = proc.waitFor();
            if (rc == 0) {
                bSuccess = true;
                //Copy the tmp file back to the orig file
                FSUtils.copyFile(tmpFile, currentFile);
                tmpFile.delete();
            }
        } catch (IOException e) {
           bSuccess = false;
        } catch (InterruptedException e) {
           bSuccess = false;
        }
        if (!bSuccess) {
            log.warn("Sort failed for the file : " + fileName);
        }

        return bSuccess;
    }
}

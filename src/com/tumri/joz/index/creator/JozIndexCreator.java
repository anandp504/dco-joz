package com.tumri.joz.index.creator;

import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Main class that will create the Joz Indices.
 * @author: nipun
 * Date: Feb 14, 2008
 * Time: 1:24:23 PM
 */
public class JozIndexCreator {

    private static Logger log = Logger.getLogger(JozIndexCreator.class);
    private  String newDataDir = "/opt/joz/data/caa/current/USpub0085";
    private  String oldDataDir = "/opt/joz/data/caa/current/USpub0084";
    private  String indexDir = "../tmp/output";
    protected  final String MUP_FILE_FORMAT = ".*_provider-content_.*.utf8";
    private  int MAX_LINES_PER_CHUNK = 50000;

    public JozIndexCreator(String currDataDir, String prevDataDir, String jozIndexDir, int linesPerChunk) {
        indexDir = jozIndexDir;
        newDataDir = currDataDir;
        oldDataDir = prevDataDir;
        MAX_LINES_PER_CHUNK = linesPerChunk;
    }

    /**
     * Entry point to create the Indexes
     * @param args
     */
    public static void main(String[] args) {
        String usage = "java -jar JozIndexer.jar [-newDataDir XXX] [-oldDataDir XXX] [-indexDir XXX] [-maxPidsPerLine XX] [-maxLinesPerChunk XX]";
        String newDataDir="", oldDataDir="", indexDir="";
        int maxLinesPerChunk=0;
        
        for (int i = 0; i < args.length; i++)
        {
            String arg = args[i];
            if (arg.equals ("-newDataDir")) {
                newDataDir = args[++i];
            } else if (arg.equals ("-oldDataDir")) {
                oldDataDir = args[++i];
            } else if (arg.equals ("-indexDir")) {
                indexDir = args[++i];
            } else if (arg.equals ("-maxLinesPerChunk")) {
                maxLinesPerChunk = Integer.parseInt(args[++i]);
            } else
            {
                log.info("Usage: " + usage);
                System.exit(1);
            }
        }

        File indexDirF = new File(indexDir);
        if (!indexDirF.exists())
        {
            log.info("Creating dir '" +indexDir+ "'");
            indexDirF.mkdirs();
        } else {
            log.fatal("Document directory '" +indexDirF.getAbsolutePath()+ "' exists already - please delete it or provide a new dir");
            System.exit(1);
        }
        final File newDataDocDirF = new File(newDataDir);
        if (!newDataDocDirF.exists() || !newDataDocDirF.canRead())
        {
            log.fatal("Document directory '" +newDataDocDirF.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }
        final File oldDataDocDirF = new File(oldDataDir);
        if (!oldDataDocDirF.exists() || !oldDataDocDirF.canRead())
        {
            log.fatal("Document directory '" +oldDataDocDirF.getAbsolutePath()+ "' does not exist or is not readable, please check the path");
            System.exit(1);
        }
        JozIndexCreator jic = new JozIndexCreator(newDataDir, oldDataDir, indexDir,maxLinesPerChunk);
        jic.createJozIndexes();
    }

    public void createJozIndexes() {
        try
        {
            Date start = new Date();
            indexDocs();
            log.info( "Joz indexing completed : " + ((new Date()).getTime() - start.getTime()) * 1E-3 / 60.0 + " total minutes" );
        }
        catch (Exception e)
        {
            log.error("something screwed up: ", e );
            // If we fail we must exit with a non-zero error code.
            System.exit(1);
        }

    }

    /**
     * Create the joz indexes for the files in the given dir
     * @throws IOException
     */
    private void indexDocs() throws IOException {
        log.info( "Creating Joz Indexes into '" + indexDir + "' ..." );
        File dir = new File(newDataDir + "/data");
        if (!dir.exists() || !dir.isDirectory()) {
            throw new IOException("Directory " + newDataDir + "/data/ doesn't exist or is not a directory");
        }
        File[] files = dir.listFiles();
        List<File> newMupFiles = new ArrayList<File>();
        for (File f: files) {
            if (f.getName().matches(MUP_FILE_FORMAT)) {
                newMupFiles.add(f);
            }
        }
        if (newMupFiles.size() == 0) {
            throw new IOException("No new provider content data found in directory: " + dir.getAbsolutePath());
        }

        List<File> oldMupFiles = null;

        if (oldDataDir != null && !"".equals(oldDataDir)) {
            File oldDir = new File(oldDataDir + "/data");
            if (!oldDir.exists() || !oldDir.isDirectory()) {
                throw new IOException("Directory " + oldDataDir + "/data/ doesn't exist or is not a directory");
            }
            File[] oldFiles = oldDir.listFiles();
            oldMupFiles = new ArrayList<File>();
            for (File f: oldFiles) {
                if (f.getName().matches(MUP_FILE_FORMAT)) {
                    oldMupFiles.add(f);
                }
            }
            if (oldMupFiles.size() == 0) {
                throw new IOException("No old provider content data found in directory: " + oldDir.getAbsolutePath());
            }
        }

        File[] sortedFiles = newMupFiles.toArray(new File[0]);
        Arrays.sort(sortedFiles);
        for (File f: sortedFiles) {
            File oldFile = null;
            if (oldDataDir != null && !"".equals(oldDataDir)) {
                oldFile = findOldMupFile(f, oldMupFiles);
            }
            //Create the index for this provider
            String providerName = getProviderFromFileName(f.getName());
            log.info("Creating joz index for provider : " + providerName);
            ProviderIndexCreator ic = new ProviderIndexCreator(getJozIndexFileNamePrefix(f.getName()),indexDir,f,oldFile,MAX_LINES_PER_CHUNK);
            ic.createIndicesForProvider();
        }

    }

    /**
     * Helper method to find the old MUP from the given list of files.
     * @param currentFile
     * @param oldFiles
     * @return
     */
    private static File findOldMupFile(File currentFile,List<File> oldFiles) {
        String currProvName = getProviderFromFileName(currentFile.getName());
        File result = null;
        for(File f: oldFiles) {
            String oldProvName = getProviderFromFileName(f.getName());

            if (oldProvName.equals(currProvName)) {
                result = f;
                break;
            }
        }
        return result;
    }

    /**
     * Gets the provider name from a given mup file by tokenizing by _ char
     * It is assumed that the provider name will be between the first _ char and the 5th _ char from the end
     * Returns an empty string if the file name was not of correct syntax
     * @param fileName - mup file name
     * @return - provider name
     */
    private static String getProviderFromFileName(String fileName) {
        String providerName = "";
        if (fileName!=null) {
            String[] parts = fileName.split("_");
            if (parts.length<7) {
                return "";
            }
            for (int i=1; i<parts.length-5; i++) {
                String delim = "";
                if (i>1) {
                    delim = "_";
                }
                providerName = providerName + delim + parts[i];
            }
        }
        return providerName;
    }

    /**
     * Helper method to get joz index file name prefix
     * @return
     */
    private static String getJozIndexFileNamePrefix(String fileName) {
        String fileNamePrefix = "";
        int idx = fileName.indexOf("_provider-content_");
        if (fileName != null && idx > 0) {
            fileNamePrefix = fileName.substring(0,idx);
        }
        return fileNamePrefix;
    }
}

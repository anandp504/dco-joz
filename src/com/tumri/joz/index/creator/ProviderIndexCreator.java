package com.tumri.joz.index.creator;

import com.tumri.utils.FDUtils;
import com.tumri.utils.data.SetDifference;
import com.tumri.utils.data.SortedArraySet;
import com.tumri.utils.data.persistent.FileManager;
import com.tumri.utils.data.persistent.SetFile;
import com.tumri.utils.data.persistent.Writer;
import com.tumri.utils.data.persistent.fs.LocalFile;
import com.tumri.utils.strings.StringTokenizer;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.*;

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
	private String tmpDirPath = "/tmp";
	int chunkId = 0;
	private static Logger log = Logger.getLogger(ProviderIndexCreator.class);
	private static final String NEW_SET_FILE = "/newSetFile";
	private static final String OLD_SET_FILE = "/oldSetFile";

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
	private void init() {
		log.debug("Going to create the provider index for " + currProvider + ". Using new mup file " + newFile.getAbsolutePath());
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

	public void createIndicesForProvider() throws IOException {
		File f = new File(tmpDirPath + NEW_SET_FILE);
		File f2 = new File(tmpDirPath + OLD_SET_FILE);
		SetFile<MupRecord> newSetFile = new SetFile<MupRecord>(FileManager.getInstance().createFile(f.getAbsolutePath()).toURI());
		SetFile<MupRecord> oldSetFile = new SetFile<MupRecord>(FileManager.getInstance().createFile(f2.getAbsolutePath()).toURI());

		try{
			if (!newFile.exists()) {
				log.error("Cannot create the Joz index, the new mup file is missing :" + newFile.getAbsolutePath());
				throw new IOException("Cannot create the Joz index, the new mup file is missing :" + newFile.getAbsolutePath());
			}
			log.debug("Going to load the product info from file : " + newFile.getName());

			writeSetFile(f, newFile);

			if(oldFile !=null && oldFile.exists()){
				writeSetFile(f2, oldFile);
			}


			SortedSet<MupRecord> newSortedSet = newSetFile.open();
			SortedSet<MupRecord> oldSortedSet = oldSetFile.open();

			if(oldSortedSet == null){
				oldSortedSet = new SortedArraySet<MupRecord>();
			}

			SetDifference<MupRecord> sd1 = new SetDifference<MupRecord>(newSortedSet, oldSortedSet); // new - old == adds

			int lineCount = 0;

			for(MupRecord r : sd1){
				addIndices(r.getProdProperties(), r.getPId());
				lineCount++;
				if (lineCount>= maxPidEntriesPerChunk) {
					lineCount = 0;
					writeChunkToFile();
				}
			}

			SetDifference<MupRecord> sd2 = new SetDifference<MupRecord>(oldSortedSet, newSortedSet); // old - new == dels
			for(MupRecord r : sd2){
				deleteIndices(r.getProdProperties(), r.getPId());
				lineCount++;
				if (lineCount>= maxPidEntriesPerChunk) {
					lineCount = 0;
					writeChunkToFile();
				}
			}

			SetDifference<MupRecord> sd3 = new SetDifference<MupRecord>(oldSortedSet, sd2); // old - (del) == old - (old - new) == no change
			for(MupRecord r : sd3){
				compareAndUpdateIndices(r.getPId(), newSortedSet.tailSet(r).first().getProdProperties(), oldSortedSet.tailSet(r).first().getProdProperties());
				lineCount++;
				if (lineCount>= maxPidEntriesPerChunk) {
					lineCount = 0;
					writeChunkToFile();
				}
			}
			writeChunkToFile();
		} finally {
			try {
				newSetFile.close();
				oldSetFile.close();
			} catch(Exception e) {
				//Ignore
			}
			FileManager.getInstance().deleteDir(new LocalFile(f.getAbsolutePath()));
			FileManager.getInstance().deleteDir(new LocalFile(f2.getAbsolutePath()));
		}
	}

	private void writeSetFile(File setFile, File fromFile) throws IOException {
		Writer<MupRecord> w = null;
		FileInputStream fstream = null;
		DataInputStream in = null;
		BufferedReader br = null;
		try {
			SetFile<MupRecord> newSetFile = new SetFile<MupRecord>(FileManager.getInstance().createFile(setFile.getAbsolutePath()).toURI());
			w = newSetFile.getWriter();
			fstream = new FileInputStream(fromFile);
			in = new DataInputStream(fstream);
			br = new BufferedReader(new InputStreamReader(in));
			String strLine;
			while ((strLine = br.readLine()) != null)   {
				MupRecord r = getMupRecordFromLine(strLine);
				w.append(r);
			}
		} finally {
			if(w != null){
				FDUtils.close(w);
			}
			if(br != null){
				FDUtils.close(br);
			}
			if(in != null){
				FDUtils.close(in);
			}
			if(fstream != null){
				FDUtils.close(fstream);
			}
		}
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
			boolean bGeoEnabled = false;
			for (int i=0;i<strings.size();i++) {
				if (indexPosSet.contains(new Integer(i).toString())) {
					String val = strings.get(i);
					if (val == null && val.equals("")) {
						//Skip any fields that are empty.
						continue;
					}
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
					if (!bGeoEnabled && (key.equals("country") || key.equals("state") || key.equals("city") ||
							key.equals("dma") || key.equals("area") || key.equals("zip"))) {
						if (val != null && !val.equals("")) {
							retVal.put("geoenabled","true");
							bGeoEnabled = true;
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

	private MupRecord getMupRecordFromLine(String str){
		MupRecord retRec = new MupRecord();
		String pidS = getProductID(str);
		retRec.setPId(Long.valueOf(pidS));
		retRec.setProdProperties(convertLine(str));
		return retRec;
	}


}

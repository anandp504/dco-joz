package com.tumri.joz.index;

import com.tumri.joz.index.creator.JozIndexCreator;
import com.tumri.joz.utils.IndexDebugUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;

/**
 * Class to test creation on index from a given MUP
 * 1. Create a new and old test MUP.
 * 2. Create the corresponding Taxonomy and Merchant data files.
 * 3. Do in the indexing.
 * 4. Read the index and make sure that we have the ADD, DELETE and NO CHANGE information matching.
 * 5. Load the index into Joz, and perform the TSpec queries and ensure that the indexes are working.
 *
 * contents must be moved from test/data/testJozIndex/* to /tmp/testJozIndex/*
 * @author: nipun
 * Date: Feb 28, 2008
 * Time: 2:37:03 PM
 */
public class TestJozIndexCreation extends TestCase{
//	private static final String oldDir = "/tmp/testJozIndex/old";
//	private static final String newDir = "/tmp/testJozIndex/new";
//	private static final String binDir = "/tmp/testJozIndex/jozIndex";
	
    private static final String baseDir = "/tmp/joztest";
    private static final String taxonomyDir = "/tmp/taxonomy";
    private static File _baseDir = null;
//
//    //@BeforeClass
//    public static void init() {
//        _baseDir = new File(baseDir);
//        if (!_baseDir.exists()) {
//            _baseDir.delete();
//            _baseDir.mkdir();
//        }
//        setupDataDirs();
//    }
//
//    /**
//     * Create the mup and taxonomy files
//     */
//    private static void setupDataDirs() {
//        TestProductData.writeMupFiles(_baseDir);
//        TestProductData.writeTaxonomy(_baseDir);
//    }

   // @Test
    public void testIndexingSameContent() {
        //Do the indexing
	    String oldDir = "/tmp/testJozIndex/old/old1";
		String newDir = "/tmp/testJozIndex/new/new1";
		String binDir = "/tmp/testJozIndex/jozIndex";
        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
        ic.createJozIndexes();
	    StringBuffer debugBuff;
	    IndexDebugUtils debugUtil = new IndexDebugUtils();
	    String[] args = new String[2];
	    args[0] = "-binLoc";
	    args[1] = binDir;
	    debugBuff = debugUtil.execute(args);

	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
	    ic.createJozIndexes();
	    StringBuffer debugBuff2;
	    debugBuff2 = debugUtil.execute(args);
	    if(!debugBuff.toString().equals(debugBuff2.toString())){
		    fail("outputs not equal");
	    }
    }

	//@Test
    public void testIndexingSameFile() {
        //Do the indexing
	    String oldDir = "/tmp/testJozIndex/old/old1";
		String binDir = "/tmp/testJozIndex/jozIndex";
        JozIndexCreator ic = new JozIndexCreator(oldDir, oldDir, binDir, 10000);
        ic.createJozIndexes();
	    StringBuffer debugBuff;
	    IndexDebugUtils debugUtil = new IndexDebugUtils();
	    String[] args = new String[2];
	    args[0] = "-binLoc";
	    args[1] = binDir;
	    debugBuff = debugUtil.execute(args);

	    ic = new JozIndexCreator(oldDir, oldDir, binDir, 10000);
	    ic.createJozIndexes();
	    StringBuffer debugBuff2;
	    debugBuff2 = debugUtil.execute(args);
	    if(!debugBuff.toString().equals(debugBuff2.toString())){
		    fail("outputs not equal");
	    }
    }

	@Test
    public void testIndexingOneDifference() {
		String oldDir = "/tmp/testJozIndex/old/old2";
		String newDir = "/tmp/testJozIndex/new/new2";
		String binDir = "/tmp/testJozIndex/jozIndex";

        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
        ic.createJozIndexes();
	    StringBuffer debugBuff;
	    IndexDebugUtils debugUtil = new IndexDebugUtils();
	    String[] args = new String[2];
	    args[0] = "-binLoc";
	    args[1] = binDir;
	    debugBuff = debugUtil.execute(args);

	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
	    ic.createJozIndexes();
	    StringBuffer debugBuff2;
	    debugBuff2 = debugUtil.execute(args);
	    if(debugBuff.toString().equals(debugBuff2.toString())){
		    fail("outputs are equal");
	    }
		int numAdds1 = countWord(debugBuff.toString(), "ADD");
		int numDeletes1 = countWord(debugBuff.toString(), "DELETE");
		int numNoChanges1 = countWord(debugBuff.toString(), "NO-CHANGE");

		int numAdds2 = countWord(debugBuff2.toString(), "ADD");
		int numDeletes2 = countWord(debugBuff2.toString(), "DELETE");
		int numNoChanges2 = countWord(debugBuff2.toString(), "NO-CHANGE");

		if(numDeletes1 != 0){
			fail("num Deletes should be zero for (old, new)");
		}

		if(numAdds2 != 0){
			fail("num Adds should be zero for (new, old)");
		}

		if(numAdds1 != numDeletes2){
			fail("num adds for (old, new) should equal num deletes (new, old)");
		}
		if(numNoChanges1 != numNoChanges2){
			fail("num NO-CHANGES are not equal");
		}
    }

	@Test
    public void testIndexingOneReArranged() {
		String oldDir = "/tmp/testJozIndex/old/old3";
		String newDir = "/tmp/testJozIndex/new/new3";
		String binDir = "/tmp/testJozIndex/jozIndex";

        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
        ic.createJozIndexes();
	    StringBuffer debugBuff;
	    IndexDebugUtils debugUtil = new IndexDebugUtils();
	    String[] args = new String[2];
	    args[0] = "-binLoc";
	    args[1] = binDir;
	    debugBuff = debugUtil.execute(args);

	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
	    ic.createJozIndexes();
	    StringBuffer debugBuff2;
	    debugBuff2 = debugUtil.execute(args);
	    if(!debugBuff.toString().equals(debugBuff2.toString())){
		    fail("outputs not equal");
	    }
	}

	@Test
    public void testIndexingAlteredInfo() {
		String oldDir = "/tmp/testJozIndex/old/old4";
		String newDir = "/tmp/testJozIndex/new/new4";
		String binDir = "/tmp/testJozIndex/jozIndex";

        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
        ic.createJozIndexes();
	    StringBuffer debugBuff;
	    IndexDebugUtils debugUtil = new IndexDebugUtils();
	    String[] args = new String[2];
	    args[0] = "-binLoc";
	    args[1] = binDir;
	    debugBuff = debugUtil.execute(args);

	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
	    ic.createJozIndexes();
	    StringBuffer debugBuff2;
	    debugBuff2 = debugUtil.execute(args);
	    if(debugBuff.toString().equals(debugBuff2.toString())){
		    fail("outputs are equal");
	    }
		int numAdds1 = countWord(debugBuff.toString(), "ADD");
		int numDeletes1 = countWord(debugBuff.toString(), "DELETE");
		int numNoChanges1 = countWord(debugBuff.toString(), "NO-CHANGE");
		int numAddMods = countWord(debugBuff.toString(), "ADD-MOD");
		int numDeleteMods = countWord(debugBuff.toString(), "DELETE-MOD");

		int numAdds2 = countWord(debugBuff2.toString(), "ADD");
		int numDeletes2 = countWord(debugBuff2.toString(), "DELETE");
		int numNoChanges2 = countWord(debugBuff2.toString(), "NO-CHANGE");
		int numAddMods2 = countWord(debugBuff.toString(), "ADD-MOD");
		int numDeleteMods2 = countWord(debugBuff.toString(), "DELETE-MOD");

		if(numDeletes1 != 1){
			fail("num Deletes should be zero for (old, new)");
		}
		if(numDeleteMods != 1 || numDeleteMods != numDeleteMods2){
			fail("number of delete-mods incorrect");	
		}
		if(numAddMods != 1 || numAddMods != numAddMods2){
			fail("number of add-mods incorrect");
		}

		if(numAdds2 != 1){
			fail("num Adds should be zero for (new, old)");
		}
		if(numNoChanges1 != numNoChanges2){
			fail("num NO-CHANGES are not equal");
		}
    }

	@Test
    public void testIndexingAllProviders() {
		String oldDir = "/tmp/testJozIndex/old/old4";
		String newDir = "/tmp/testJozIndex/new/new4";
		String binDir = "/tmp/testJozIndex/jozIndex";

        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
        ic.createJozIndexes();

		File dir = new File(oldDir);
		File dir2 = new File(newDir);
		File[] files = dir.listFiles();
		ArrayList<StringBuffer> bufferList1 = new ArrayList<StringBuffer>();
		ArrayList<StringBuffer> bufferList2 = new ArrayList<StringBuffer>();
		IndexDebugUtils debugUtil = new IndexDebugUtils();
		// This filter only returns directories
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File file) {
				return file.isDirectory();
			}
		};
		files = dir.listFiles(fileFilter);
		for(File file: files){
			String binName = file.getName();
			StringBuffer debugBuff;
			String[] args = new String[4];
			args[0] = "-binLoc";
			args[1] = binDir;
			args[2] = "-binFile";
			args[3] = binName;
			debugBuff = debugUtil.execute(args);
			bufferList1.add(debugBuff);
		}
		ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
		ic.createJozIndexes();
		files = dir2.listFiles(fileFilter);
		for(File file: files){
			String binName = file.getName();
			StringBuffer debugBuff;
			String[] args = new String[4];
			args[0] = "-binLoc";
			args[1] = binDir;
			args[2] = "-binFile";
			args[3] = binName;
			debugBuff = debugUtil.execute(args);
			bufferList2.add(debugBuff);
		}

		if(bufferList1.size() != bufferList2.size()){
			fail("bufferList1.size() != bufferList2.size()");
		}
		for(int i = 0; i < bufferList1.size(); i++){
			StringBuffer buffer1 = bufferList1.get(i);
			StringBuffer buffer2 = bufferList2.get(i);

			int numAllAdds1 = countWord(buffer1.toString(), "ADD");
			int numAllDeletes1 = countWord(buffer1.toString(), "DELETE");
			int numNoChanges1 = countWord(buffer1.toString(), "NO-CHANGE");
			int numAddMods1 = countWord(buffer1.toString(), "ADD-MOD");
			int numDeleteMods1 = countWord(buffer1.toString(), "DELETE-MOD");
			int numAdds1 = numAllAdds1 - numAddMods1;
			int numDeletes1 = numAllDeletes1 - numDeleteMods1;

			int numAllAdds2 = countWord(buffer2.toString(), "ADD");
			int numAllDeletes2 = countWord(buffer2.toString(), "DELETE");
			int numNoChanges2 = countWord(buffer2.toString(), "NO-CHANGE");
			int numAddMods2 = countWord(buffer2.toString(), "ADD-MOD");
			int numDeleteMods2 = countWord(buffer2.toString(), "DELETE-MOD");
			int numAdds2 = numAllAdds2 - numAddMods2;
			int numDeletes2 = numAllDeletes2 - numDeleteMods2;

			if(numAdds1 != numDeletes2){
				fail("buffer"+i+"'s numAdds1 != numDeletes2");
			}
			if(numAdds2 != numDeletes1){
				fail("buffer"+i+"'s numAdds2 != numDeletes1");
			}
		}
    }

    @Test
    public void testCreateSingleIndex() {
        
    }

    @Test
    public void testIncrementalIndexNoChange() {

    }

    @Test
    public void testIncrementalIndexNewFileLarger() {

    }

    @Test
    public void testIncrementalIndexOldFileLarger() {

    }

    @Test
    public void testIncrementalIndexNoCommon() {

    }

	private int countWord(String s, String w){
		int count = 0;
		int index = 0;
		while((index = s.indexOf(w, index)) != -1){
			count++;
			index++;
		}
		return count;
	}

}

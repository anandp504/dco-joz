package com.tumri.joz.index;

import com.tumri.joz.index.creator.JozIndexCreator;
import com.tumri.joz.utils.IndexDebugUtils;
import junit.framework.TestCase;
import org.junit.Test;

import java.io.File;
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
//    public void testIndexingSameContent() {
//        //Do the indexing
//	    String oldDir = "/tmp/testJozIndex/old/old1";
//		String newDir = "/tmp/testJozIndex/new/new1";
//		String binDir = "/tmp/testJozIndex/jozIndex";
//	    deleteDir(new File(binDir));
//        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
//        ic.createJozIndexes();
//	    StringBuffer debugBuff;
//	    IndexDebugUtils debugUtil = new IndexDebugUtils();
//	    String[] args = new String[2];
//	    args[0] = "-binLoc";
//	    args[1] = binDir;
//	    debugBuff = debugUtil.execute(args);
//
//	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
//	    ic.createJozIndexes();
//	    StringBuffer debugBuff2;
//	    debugBuff2 = debugUtil.execute(args);
//	    if(!debugBuff.toString().equals(debugBuff2.toString())){
//		    fail("outputs not equal");
//	    }
//    }
//
//	//@Test
//    public void testIndexingSameFile() {
//        //Do the indexing
//	    String oldDir = "/tmp/testJozIndex/old/old1";
//		String binDir = "/tmp/testJozIndex/jozIndex";
//		deleteDir(new File(binDir));
//        JozIndexCreator ic = new JozIndexCreator(oldDir, oldDir, binDir, 10000);
//        ic.createJozIndexes();
//	    StringBuffer debugBuff;
//	    IndexDebugUtils debugUtil = new IndexDebugUtils();
//	    String[] args = new String[2];
//	    args[0] = "-binLoc";
//	    args[1] = binDir;
//	    debugBuff = debugUtil.execute(args);
//		deleteDir(new File(binDir));
//	    ic = new JozIndexCreator(oldDir, oldDir, binDir, 10000);
//	    ic.createJozIndexes();
//	    StringBuffer debugBuff2;
//	    debugBuff2 = debugUtil.execute(args);
//	    if(!debugBuff.toString().equals(debugBuff2.toString())){
//		    fail("outputs not equal");
//	    }
//    }
//
//	@Test
//    public void testIndexingOneDifference() {
//		String oldDir = "/tmp/testJozIndex/old/old2";
//		String newDir = "/tmp/testJozIndex/new/new2";
//		String binDir = "/tmp/testJozIndex/jozIndex";
//		deleteDir(new File(binDir));
//        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
//        ic.createJozIndexes();
//	    StringBuffer debugBuff;
//	    IndexDebugUtils debugUtil = new IndexDebugUtils();
//	    String[] args = new String[2];
//	    args[0] = "-binLoc";
//	    args[1] = binDir;
//	    debugBuff = debugUtil.execute(args);
//		deleteDir(new File(binDir));
//	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
//	    ic.createJozIndexes();
//	    StringBuffer debugBuff2;
//	    debugBuff2 = debugUtil.execute(args);
//	    if(debugBuff.toString().equals(debugBuff2.toString())){
//		    fail("outputs are equal");
//	    }
//		int numAllAdds1 = countWord(debugBuff.toString(), "\tADD\t");
//		int numAllDeletes1 = countWord(debugBuff.toString(), "\tDELETE\t");
//		int numNoChanges1 = countWord(debugBuff.toString(), "\tNO-CHANGE\t");
//		int numAddMods1 = countWord(debugBuff.toString(), "\tADD-MOD\t");
//		int numDeleteMods1 = countWord(debugBuff.toString(), "\tDELETE-MOD\t");
//		int numAdds1 = numAllAdds1 - numAddMods1;
//		int numDeletes1 = numAllDeletes1 - numDeleteMods1;
//
//		int numAllAdds2 = countWord(debugBuff2.toString(), "\tADD\t");
//		int numAllDeletes2 = countWord(debugBuff2.toString(), "\tDELETE\t");
//		int numNoChanges2 = countWord(debugBuff2.toString(), "\tNO-CHANGE\t");
//		int numAddMods2 = countWord(debugBuff2.toString(), "\tADD-MOD\t");
//		int numDeleteMods2 = countWord(debugBuff2.toString(), "\tDELETE-MOD\t");
//		int numAdds2 = numAllAdds2 - numAddMods2;
//		int numDeletes2 = numAllDeletes2 - numDeleteMods2;
//
//		if(numAdds1 != numDeletes2){
//			fail("buffer"+"'s numAdds1 != numDeletes2");
//		}
//		if(numAdds2 != numDeletes1){
//			fail("buffer"+"'s numAdds2 != numDeletes1");
//		}
//
//		if(numDeletes1 != 0){
//			fail("num Deletes should be zero for (old, new)");
//		}
//
//		if(numAdds2 != 0){
//			fail("num Adds should be zero for (new, old)");
//		}
//
//		if(numAdds1 != numDeletes2){
//			fail("num adds for (old, new) should equal num deletes (new, old)");
//		}
//		if(numNoChanges1 != numNoChanges2){
//			fail("num NO-CHANGES are not equal");
//		}
//    }
//
//	@Test
//    public void testIndexingOneReArranged() {
//		String oldDir = "/tmp/testJozIndex/old/old3";
//		String newDir = "/tmp/testJozIndex/new/new3";
//		String binDir = "/tmp/testJozIndex/jozIndex";
//		deleteDir(new File(binDir));
//        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
//        ic.createJozIndexes();
//	    StringBuffer debugBuff;
//	    IndexDebugUtils debugUtil = new IndexDebugUtils();
//	    String[] args = new String[2];
//	    args[0] = "-binLoc";
//	    args[1] = binDir;
//	    debugBuff = debugUtil.execute(args);
//		deleteDir(new File(binDir));
//	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
//	    ic.createJozIndexes();
//	    StringBuffer debugBuff2;
//	    debugBuff2 = debugUtil.execute(args);
//	    if(!debugBuff.toString().equals(debugBuff2.toString())){
//		    fail("outputs not equal");
//	    }
//	}
//
//	@Test
//    public void testIndexingAlteredInfo() {
//		String oldDir = "/tmp/testJozIndex/old/old4";
//		String newDir = "/tmp/testJozIndex/new/new4";
//		String binDir = "/tmp/testJozIndex/jozIndex";
//		deleteDir(new File(binDir));
//        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 10000);
//        ic.createJozIndexes();
//	    StringBuffer debugBuff;
//	    IndexDebugUtils debugUtil = new IndexDebugUtils();
//	    String[] args = new String[2];
//	    args[0] = "-binLoc";
//	    args[1] = binDir;
//	    debugBuff = debugUtil.execute(args);
//		deleteDir(new File(binDir));
//	    ic = new JozIndexCreator(oldDir, newDir, binDir, 10000);
//	    ic.createJozIndexes();
//	    StringBuffer debugBuff2;
//	    debugBuff2 = debugUtil.execute(args);
//	    if(debugBuff.toString().equals(debugBuff2.toString())){
//		    fail("outputs are equal");
//	    }
//		int numAllAdds1 = countWord(debugBuff.toString(), "\tADD\t");
//		int numAllDeletes1 = countWord(debugBuff.toString(), "\tDELETE\t");
//		int numNoChanges1 = countWord(debugBuff.toString(), "\tNO-CHANGE\t");
//		int numAddMods1 = countWord(debugBuff.toString(), "\tADD-MOD\t");
//		int numDeleteMods1 = countWord(debugBuff.toString(), "\tDELETE-MOD\t");
//		int numAdds1 = numAllAdds1 - numAddMods1;
//		int numDeletes1 = numAllDeletes1 - numDeleteMods1;
//
//		int numAllAdds2 = countWord(debugBuff2.toString(), "\tADD\t");
//		int numAllDeletes2 = countWord(debugBuff2.toString(), "\tDELETE\t");
//		int numNoChanges2 = countWord(debugBuff2.toString(), "\tNO-CHANGE\t");
//		int numAddMods2 = countWord(debugBuff2.toString(), "\tADD-MOD\t");
//		int numDeleteMods2 = countWord(debugBuff2.toString(), "\tDELETE-MOD\t");
//		int numAdds2 = numAllAdds2 - numAddMods2;
//		int numDeletes2 = numAllDeletes2 - numDeleteMods2;
//
//		if(numAdds1 != numDeletes2){
//			fail("buffer"+"'s numAdds1 != numDeletes2");
//		}
//		if(numAdds2 != numDeletes1){
//			fail("buffer"+"'s numAdds2 != numDeletes1");
//		}
//
//		if(numDeletes1 != 0){
//			fail("num Deletes should be zero for (old, new)");
//		}
//		if(numDeleteMods1 != 1 || numDeleteMods1 != numDeleteMods2){
//			fail("number of delete-mods incorrect");
//		}
//		if(numAddMods1 != 1 || numAddMods1 != numAddMods2){
//			fail("number of add-mods incorrect");
//		}
//
//		if(numAdds2 != 0){
//			fail("num Adds should be zero for (new, old)");
//		}
//		if(numNoChanges1 != numNoChanges2){
//			fail("num NO-CHANGES are not equal");
//		}
//    }

	@Test
	/**
	 * This method will take two sets of data and compare them as if one were old and one were new: recording information
	 * such as: number of nochanges, adds, deletes, addmods and deletemods...The method will then flip the data and do
	 * the collection again(old will be new and new will be old).  Finally it will compare the data collected both times.
	 * The results of which should be as follows:
	 * numNoChange1 == numNoChange2
	 * numAdds1 == numDeletes2
	 * numDeletes1 == numAdds2
	 * numAddMods1 == numDeleteMods2
	 * numDeleteMods1 == numAddMods2
	 *
	 * if any of thse are false you can either output to the screeon or fail the test directly.
	 * Two folders with two different versions of content need to be located in old5 and new5.
	 * Content should included *provider*.utf8 files; it is up to you to determine how many files to include and
	 * which versions to use.
	 *
	 * Finally, care must be taken to ensure that the resulting index can fit into one bin file.  If not, then the
	 * comparisons will not necessarily work.
	 */
    public void testIndexingAllProviders() {
		String oldDir = "/tmp/testJozIndex/old/old5";
		String newDir = "/tmp/testJozIndex/new/new5";
		String binDir = "/tmp/testJozIndex/jozIndex";
		deleteDir(new File(binDir));
        JozIndexCreator ic = new JozIndexCreator(newDir, oldDir, binDir, 1000000);
        ic.createJozIndexes();

		File dir = new File(binDir);
		File dir2 = new File(binDir);
		File[] files = dir.listFiles();
		ArrayList<StringBuffer> bufferList1 = new ArrayList<StringBuffer>();
		ArrayList<StringBuffer> bufferList2 = new ArrayList<StringBuffer>();

		ArrayList<String> binFileNames = new ArrayList<String>();
		ArrayList<String> binFileNames2 = new ArrayList<String>();

		for(File file: files){
			IndexDebugUtils debugUtil = new IndexDebugUtils();
			String binName = file.getName();
			binFileNames.add(binName);
			StringBuffer debugBuff;
			String[] args = new String[4];
			args[0] = "-binLoc";
			args[1] = binDir;
			args[2] = "-binFile";
			args[3] = binName;
			debugBuff = debugUtil.execute(args);
			bufferList1.add(debugBuff);
		}

		deleteDir(new File(binDir));
		ic = new JozIndexCreator(oldDir, newDir, binDir, 1000000);
		ic.createJozIndexes();
		files = dir2.listFiles();
		for(File file: files){
			IndexDebugUtils debugUtil = new IndexDebugUtils();
			String binName = file.getName();
			binFileNames2.add(binName);
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

			int numAdds1 = countWord(buffer1.toString(), "\tADD\t");
			int numDeletes1 = countWord(buffer1.toString(), "\tDELETE\t");
			int numNoChanges1 = countWord(buffer1.toString(), "\tNO-CHANGE\t");
			int numAddMods1 = countWord(buffer1.toString(), "\tADD-MOD\t");
			int numDeleteMods1 = countWord(buffer1.toString(), "\tDELETE-MOD\t");

			int numAdds2 = countWord(buffer2.toString(), "\tADD\t");
			int numDeletes2 = countWord(buffer2.toString(), "\tDELETE\t");
			int numNoChanges2 = countWord(buffer2.toString(), "\tNO-CHANGE\t");
			int numAddMods2 = countWord(buffer2.toString(), "\tADD-MOD\t");
			int numDeleteMods2 = countWord(buffer2.toString(), "\tDELETE-MOD\t");

			System.out.println("File1 = " + binFileNames.get(i));
			System.out.println("File2 = " + binFileNames2.get(i));
			System.out.println("numNoChanges1 = " + numNoChanges1);
			System.out.println("numAddMods1 = " + numAddMods1);
			System.out.println("numDeleteMods1 = " + numDeleteMods1);
			System.out.println("numAdds1 = " + numAdds1);
			System.out.println("numDeltes1 = " + numDeletes1);

			System.out.println("numNoChanges2 = " + numNoChanges2);
			System.out.println("numAddMods2 = " + numAddMods2);
			System.out.println("numDeleteMods2 = " + numDeleteMods2);
			System.out.println("numAdds2 = " + numAdds2);
			System.out.println("numDeltes2 = " + numDeletes2);

			if(numNoChanges1 != numNoChanges2){
				System.out.println("numNoChanges1 != numNoChanges2");
				//fail("file " + binFileNames.get(i) + " numNoChanges1 != numNoChanges2");
			}
			if(numAddMods1 != numDeleteMods2){
				System.out.println("numAddMods1 != numDeleteMods2");
				//fail("fle " + binFileNames.get(i) + " numAddMods1 != numDeleteMods2");
			}
			if(numDeleteMods1 != numAddMods2){
				System.out.println("numDeleteMods1 != numAddMods2");
				//fail("fle " + binFileNames.get(i) + " numDeleteMods1 != numAddMods2");
			}
			if(numAdds1 != numDeletes2){
				System.out.println("numAdds1 != numDeletes2");
				//fail("fle " + binFileNames.get(i) + " numAdds1 != numDeletes2");
			}
			if(numAdds2 != numDeletes1){
				System.out.println("numAdds2 != numDeletes1");
				//fail("fle " + binFileNames.get(i) + " numAdds1 != numDeletes2");
			}
			System.out.println();
		}
    }

	/**
	 * Used for cleanup of bin directory before each indexing.
	 * @param dir Directory to clean contents of
	 * @return returns true once all sub files are deleted.
	 */
	public boolean deleteDir(File dir) {
		if (dir.isDirectory()) {
			File[] files = dir.listFiles();
			for(File file: files){
				file.delete();
			}
		}

		// The directory is now empty so delete it
		return true;
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

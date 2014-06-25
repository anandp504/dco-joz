/**
 * 
 */
package com.tumri.joz.util;

import org.junit.Test;

import com.tumri.joz.utils.IndexDebugUtils;

/**
 * @author omprakash
 * @date Jun 5, 2014
 * @time 3:30:32 PM
 */
public class TestIndexDebugUtils {

	@Test
	public void test(){
		String inxDir = "./test/data/testJozIndex/jozIndex";
		//String optInxDir = "./test/data/testJozIndex/jozIndex";
		//String outDir = "/tmp/out"
		String indexFileName = "BESTBUY_01638_jozindex_1.bin";
		String optIndexFileName = "BESTBUY_9013_optjozindex.bin";
		String args[] = {"-saveDir",inxDir , "-saveFile", "bby.txt", "-binFile", indexFileName, "-binLoc", inxDir};
		String argsOpt[] = {"-saveDir", inxDir, "-saveFile", "opt_bby.txt", "-binFile", optIndexFileName, "-binLoc", inxDir, "-opt"};
		String argsPids[] = {"-saveDir", inxDir, "-saveFile", "pid_bby.txt", "-binFile", indexFileName, "-binLoc", inxDir, "-prodId", "41009583", "-prodId", "2589126"};
		IndexDebugUtils indexUtil = new IndexDebugUtils();
		indexUtil.execute(args);
		indexUtil.execute(argsOpt);
		indexUtil.execute(argsPids);
	}
}

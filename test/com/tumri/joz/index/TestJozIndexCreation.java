package com.tumri.joz.index;

import com.tumri.joz.utils.FSUtils;
import com.tumri.joz.products.JozIndexHelper;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.index.creator.JozIndexUpdater;
import com.tumri.joz.index.creator.JozIndexCreator;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.assertNotNull;

import java.io.File;
import java.io.IOException;

/**
 * Class to test creation on index from a given MUP
 * 1. Create a new and old test MUP.
 * 2. Create the corresponding Taxonomy and Merchant data files.
 * 3. Do in the indexing.
 * 4. Read the index and make sure that we have the ADD, DELETE and NO CHANGE information matching.
 * 5. Load the index into Joz, and perform the TSpec queries and ensure that the indexes are working.
 * @author: nipun
 * Date: Feb 28, 2008
 * Time: 2:37:03 PM
 */
public class TestJozIndexCreation {

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

    @Test
    public void testIndexing() {
        //Do the indexing
        JozIndexCreator ic = new JozIndexCreator("/tmp/new", "/tmp/old", "/tmp/jozindex", 10000);
        ic.createJozIndexes();
        
        //TODO: Validate the file.
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

}

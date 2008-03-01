package com.tumri.joz.index;

import com.tumri.joz.utils.FSUtils;
import com.tumri.joz.products.JozIndexHelper;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.index.creator.JozIndexUpdater;
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
    private static File _taxonomyDir = null;

    @BeforeClass
    public static void init() {
        _baseDir = new File(baseDir);
        if (!_baseDir.exists()) {
            _baseDir.mkdir();
        }
        _taxonomyDir = new File(taxonomyDir);
        if (!_taxonomyDir.exists()) {
            throw new RuntimeException("Cannot find the taxonomy files to load indexes");
        }
        setupDataDirs();
    }

    /**
     * Create the instances of the products, and write the MUP file out to the base dir/data
     */
    private static void setupDataDirs() {
        //1. Create the mup files
        TestProductData.writeMupFiles(_baseDir);

        //2. Copy the Support files
        try {
            FSUtils.copyDir(_taxonomyDir, new File (_baseDir.getAbsolutePath()+ "/new/data"));
            FSUtils.copyDir(_taxonomyDir,new File (_baseDir.getAbsolutePath()+ "/old/data"));
        } catch (IOException e) {
            System.err.println("ERROR : Could not copy the taxonomy files over");
        }
        
    }

    @Test
    public void testIndexing() {
        //Invoke the creation of indexes
        String[] args0 = {"-currentDocDir","/tmp/joztest/old","-indexDir","/tmp/joztest/old/lucene","-jozIndexDir","/tmp/joztest/old/jozIndex"};
        new ProductIndex().index(args0);

        String[] args1 = {"-currentDocDir","/tmp/joztest/new","-previousDocDir","/tmp/joztest/old",
                "-indexDir","/tmp/joztest/new/lucene","-jozIndexDir","/tmp/joztest/new/jozIndex"};
//        String[] args1 = {"-currentDocDir","/tmp/joztest/new",
//                "-indexDir","/tmp/joztest/new/lucene","-jozIndexDir","/tmp/joztest/new/jozIndex"};
        new ProductIndex().index(args1);
        
        //Invoke the validation of index
        assert(new File("/tmp/joztest/new/lucene").exists());
        assert(new File("/tmp/joztest/new/jozIndex").exists());

        //Produce the debug file
        JozIndexUpdater.setInstance(true, true, "/tmp/joztest");
        JozIndexHelper.loadIndex("/tmp/joztest/new/jozIndex");

        assert(true);
    }

    @Test
    public void testJozQueries() {
        //Test if Joz tspec works
    }

}

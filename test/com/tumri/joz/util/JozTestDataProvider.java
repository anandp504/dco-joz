/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.util;

import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.utils.FSUtils;

import java.io.File;

/**
 * JozTestDataProvider sets up the data directory with the caa and cma data that is checked in as part of the
 * joz unit test suite.
 * This utility depends on the jozindex.properties and the luceneindex.properties to be in the classpath
 * @author: nipun
 * Date: Jul 2, 2008
 * Time: 2:14:10 PM
 */
public class JozTestDataProvider {

    private static final String LUCENE_INDEXDIR = "/lucene";
    private static final String JOZ_INDEXDIR = "/jozindex";


    /**
     * Do all the setup opertaions
     */
    public static void setup(String dataDir) {
        teardown(dataDir);
        setupIndex(dataDir);
    }

    /**
     * Release any resources here
     */
    public static void teardown(String datadir) {
        try {
            FSUtils.removeDir(new File(datadir + LUCENE_INDEXDIR));
            FSUtils.removeDir(new File(datadir + JOZ_INDEXDIR));
        } catch (Exception e) {
            System.err.println("Exception caught during delete of index files ");
            e.printStackTrace();
        }
    }
    
    /**
     * Creates the Joz and lucene indexes in the base dir
     */
    private static void setupIndex(String baseDataDir) {
        System.out.println("Going to setup the indexes using base dir = " + baseDataDir);
        String[] indexArgs = {"-docDir",baseDataDir,"-indexDir", baseDataDir + LUCENE_INDEXDIR,"-jozindexDir", baseDataDir + JOZ_INDEXDIR};
        new ProductIndex().index(indexArgs);
        System.out.println("Index setup complete");
    }

    public static void main(String[] a) {
        String baseDir = "/Users/nipun/ws/depot/dev/branch/tcmjoz/tas/joz/test/data/caa";
        setup(baseDir);
    }
}

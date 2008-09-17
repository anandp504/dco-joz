/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C) ${year} TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (${user}@tumri.com)
 * @version 1.0     ${date}
 *
 */
package com.tumri.joz.client;

import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.utils.FSUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import java.io.File;
import junit.framework.TestCase;


/**
 * @author: nipun
 * Date: Mar 24, 2008
 * Time: 10:09:13 AM
 */
public class TestJozIndexSetup extends TestCase {

    private static final String LUCENE_INDEXDIR = "/lucene";
    private static final String JOZ_INDEXDIR = "/jozindex";
    private static String baseDataDir = "./test/data/caa/current";

    
    @BeforeClass
    public static void init() {}
    @Test
    public void testSetUp(){
    	try {
        	clearIndexes();
        	setupIndex(baseDataDir);        
        } catch (Exception e) {
            System.out.println("exception in initialisation of JozServer");
        }
    }
    private static void setupIndex(String baseDataDir) {
        System.out.println("Going to setup the indexes using base dir = " + baseDataDir);
        String[] indexArgs = {"-docDir",baseDataDir,"-indexDir", baseDataDir + LUCENE_INDEXDIR,"-jozindexDir", baseDataDir + JOZ_INDEXDIR};
        new ProductIndex().index(indexArgs);
        System.out.println("Index setup complete");
    }
    private static void clearIndexes(){
    	try {
            FSUtils.removeDir(new File(baseDataDir + LUCENE_INDEXDIR));
            FSUtils.removeDir(new File(baseDataDir + JOZ_INDEXDIR));
        } catch (Exception e) {
            System.err.println("Exception caught during delete of index files ");
            e.printStackTrace();
        }
    }
}
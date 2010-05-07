/* 
 * ContentHelper.java
 * 
 * COPYRIGHT (C) 2007 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE 
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY, 
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL 
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART 
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM 
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR 
 * WRITTEN PERMISSION OF TUMRI INC.
 * 
 * @author Bhavin Doshi (bdoshi@tumri.com)
 * @version 1.0     Aug 30, 2007
 * 
 */
package com.tumri.joz.products;

import com.tumri.content.*;
import com.tumri.content.data.Content;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.joz.campaign.TSpecQueryCache;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.utils.IndexUtils;
import com.tumri.joz.utils.LogUtils;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.FSUtils;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.io.File;
import java.util.*;

/**
 *
 * Created by Bhavin Doshi (bdoshi@tumri.com) on Aug 30, 2007
 * Company: Tumri Inc.
 */
public class ContentHelper implements ContentListener {
    
    protected static Logger log = Logger.getLogger(ContentHelper.class); 
    
    public static void init() {
        try { 
            load(getContentProvider());
        } catch (InvalidConfigException e) {
            LogUtils.getFatalLog().fatal("Error during initialization of content",e);
            log.error("Error during initialization of content",e);
        }
    }
    
    public static void init(String file) {
        try {
            load(getContentProvider(file));
        } catch (InvalidConfigException e) {
            LogUtils.getFatalLog().fatal("Error during initialization of content",e);
            log.error("Error during initialization of content",e);
        }
    }
    
    public static void init(Properties props) {
        try {
            load(getContentProvider(props));
        } catch (InvalidConfigException e) {
            LogUtils.getFatalLog().fatal("Error during initialization of content",e);
            log.error("Error during initialization of content",e);
        }
    }
    
    protected static void load(ContentProvider p) {
        try {
            ContentProviderStatus st = p.getStatus();
            Content data = p.getContent();

            if (!st.merchantDataDisabled) {
                initMerchantDataDatabase(data);
            } 

            if (!st.taxonomyDisabled) {
                initTaxonomyDatabase(data);
            }

            if (!st.mupDisabled) {
                initProductsDatabase(data);
            }

            boolean bJozIndexDisabled = false;

            try {
                bJozIndexDisabled = Boolean.parseBoolean(AppProperties.getInstance().getProperty("com.tumri.content.file.disableJozIndex"));
            } catch (Exception e) {
                bJozIndexDisabled = false;
            }

            if (!bJozIndexDisabled) {
                updateAdvertiserIndex(null, true);
            }

            ContentHelper h = new ContentHelper(p);
            p.addContentListener(h);

        } catch (InvalidConfigException e) {
            e.printStackTrace();
        }
        
    }
    
    protected static ContentProvider getContentProvider() throws InvalidConfigException {
        ContentProviderFactory f = ContentProviderFactory.getDefaultInitializedInstance();
        return f.getContentProvider();
    }
    
    
    protected static ContentProvider getContentProvider(String file) throws InvalidConfigException {
        ContentProviderFactory f = null;
        if (file != null) {
            f = ContentProviderFactory.getInstance();
            f.init(file);
            return f.getContentProvider();
        } else {
            return getContentProvider();
        }
        
    }
    
    protected static ContentProvider getContentProvider(Properties props) throws InvalidConfigException {
        ContentProviderFactory f = null;
        // Filename or Properties ???
        if (props != null) {
            f = ContentProviderFactory.getInstance();
            f.init(props);
            return f.getContentProvider();
        } else {
            return getContentProvider();
        }
    }
    
    protected static void initLucene() {
        ProductIndex.init();
    }

    protected static void initProductsDatabase(Content p) {
        ProductDB pdb = ProductDB.getInstance();
            if (p != null &&  p.getProducts() != null) {
                ProductProvider pp = p.getProducts();
                List<com.tumri.content.data.Product> all = pp.getAll();
                Iterator<com.tumri.content.data.Product> it = all.iterator();
                ArrayList<IProduct> allProds = new ArrayList<IProduct>(all.size());
                while (it.hasNext()) {
                    ProductWrapper pw = new ProductWrapper(it.next());
                    allProds.add(pw);
                }
                // Create Deltas. Return value is an array of 3 ArrayList.
                // Index 0: New Products
                // Index 1: Update Products
                // Index 2: Delete Products
                ArrayList<IProduct>[] deltas = createDeltas(pdb, allProds);
                
                // Apply Deltas
                pdb.addProduct(deltas[0]);
                pdb.addProduct(deltas[1]); // Update is also done through Add.

                // Need to update the lucene index before deleting old products.
                boolean bJozIndexDisabled = false;

                try {
                    bJozIndexDisabled = Boolean.parseBoolean(AppProperties.getInstance().getProperty("com.tumri.content.file.disableJozIndex"));
                } catch (Exception e) {
                    bJozIndexDisabled = false;
                }

                if (bJozIndexDisabled) {
                    initLucene();
                }

                pdb.deleteProduct(deltas[2]);
                
                // Clear all Ospec Query Cache.
                TSpecQueryCache.getInstance().clear();
            }
    }


    private static void updateAdvertiserIndex(String advertiserName, boolean bColdStart) {
        if (bColdStart) {
            //Delete all the prev joz index files
            String prevJozindexDirName = AppProperties.getInstance().getProperty("com.tumri.content.prevjozindexDir");
            File prevIndexDir = new File(prevJozindexDirName);
            if (!prevIndexDir.exists()) {
                FSUtils.removeFiles(prevIndexDir, true);
            }

        }
        //Load Joz Indexes
        if (advertiserName!=null) {
            JozIndexHelper.getInstance().loadJozIndex(advertiserName,!bColdStart, false);
        } else {
            JozIndexHelper.getInstance().loadJozIndex(!bColdStart, false);
        }
        // Need to update the lucene index before deleting old products.
        ProductIndex.loadIndexForAdvertiser(advertiserName);
        // Clear all Ospec Query Cache.
        TSpecQueryCache.getInstance().clear();

    }
    
    /**
     * 
     * @param pdb Current Database
     * @param allProds
     * @return
     */
    @SuppressWarnings("unchecked")
    protected static ArrayList<IProduct>[] createDeltas(ProductDB pdb, ArrayList<IProduct> allProds) {
        // Creating the array causes warnings.
        ArrayList[] retValArray = new ArrayList[3];
        ArrayList<IProduct>[] retVal = retValArray;
        retVal[0] = new ArrayList<IProduct>();
        retVal[1] = new ArrayList<IProduct>();
        retVal[2] = new ArrayList<IProduct>();
        
        if (pdb == null) {
            retVal[0].addAll(allProds);
            return retVal;
        }
        
        SortedSet<Handle> currProdsSet = pdb.getAll();        
        SortedSet<IProduct> allProdsSet = new SortedArraySet<IProduct>(allProds);
        Iterator<Handle> currProdsIt = null;
        if (currProdsSet == null || currProdsSet.size() == 0) {
            retVal[0].addAll(allProdsSet);
            return retVal;
        } else {
            currProdsIt = currProdsSet.iterator();
        }

        Iterator<IProduct> allProdsIt = allProdsSet.iterator();
                
        boolean end = false;
        IProduct prod = null;
        IProduct currProduct = null;
        int compare = 0;
        while (!end) {
            if ((prod==null) && !allProdsIt.hasNext()) {
                if (currProduct != null) {
                    retVal[2].add(currProduct);
                    currProduct = null;
                }                
                while (currProdsIt.hasNext()) {
                    retVal[2].add(pdb.get(currProdsIt.next()));
                }
                end = true;
                continue;
            }
            if ((currProduct == null) && !currProdsIt.hasNext()) {
                if (prod != null) {
                    retVal[0].add(prod);
                    prod = null;
                }                
                while (allProdsIt.hasNext()) {
                    retVal[0].add(allProdsIt.next());
                }
                end = true;
                continue;
            }
            if (prod == null) {
                prod = allProdsIt.next();
            }
            if (currProduct == null) {
                currProduct = pdb.get(currProdsIt.next());
            }
            compare = prod.compareTo(currProduct); 
            if ( compare == 0) {
                retVal[1].add(prod);
                prod = null;
                currProduct = null;
            } else if (compare < 0) {
                retVal[0].add(prod);
                prod = null;
            } else {
                retVal[2].add(currProduct);
                currProduct = null;
            }
        }
        
        return retVal;
    }
    
    protected static void initTaxonomyDatabase(Content p) {
        JOZTaxonomy tax = JOZTaxonomy.getInstance();
        tax.setTaxonomy(p.getTaxonomy().getTaxonomy());
    }
    
    protected static void initMerchantDataDatabase(Content p) {
        MerchantDB db = MerchantDB.getInstance();
        db.setMerchantData(p.getMerchantData());
    }
    
    protected ContentProvider provider = null;
    
    public ContentHelper(ContentProvider p) {
        super();
        provider = p; 
    }
    
    public void contentUpdated(String advertiser) {
        
        if (provider == null) {
            return;
        }
        try {
            Content data = provider.getContent();
            ContentProviderStatus st = provider.getStatus();
            boolean bColdStart = false;
            //Need to check for revert mode.
            if (ProductDB.getInstance().isEmpty()) {
                bColdStart = true;
                log.info("Doing full load for all advertisers");
            }
            if (advertiser!=null) {
                IProduct.Attribute kAttr = IndexUtils.getAttribute("provider");
                ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(kAttr);
                Integer keyId = DictionaryManager.getInstance().getId (kAttr, advertiser);
                if (theIndex!= null && keyId!=null && keyId>=0) {
                    SortedSet<Handle> results = theIndex.get(keyId);
                    if (results==null || results.isEmpty()) {
                        bColdStart = true;
                        log.info("Doing full load for specific advertiser : " + advertiser);
                    }
                }

            }

            if (!st.merchantDataDisabled) {
                initMerchantDataDatabase(data);
            }

            if (!st.taxonomyDisabled) {
                initTaxonomyDatabase(data);
            }

            if (!st.mupDisabled) {
                initProductsDatabase(data);
            }
            boolean bJozIndexDisabled = false;

            try {
                bJozIndexDisabled = Boolean.parseBoolean(AppProperties.getInstance().getProperty("com.tumri.content.file.disableJozIndex"));
            } catch (Exception e) {
                bJozIndexDisabled = false;
            }

            if (!bJozIndexDisabled) {
               updateAdvertiserIndex(advertiser, bColdStart);
            }

        } catch (InvalidConfigException e) {
            log.error("Error while updating content",e);
            LogUtils.getFatalLog().error("Error while updating content",e);
        }
    }
}

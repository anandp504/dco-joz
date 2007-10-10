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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.tumri.content.ContentListener;
import com.tumri.content.ContentProvider;
import com.tumri.content.ContentProviderFactory;
import com.tumri.content.InvalidConfigException;
import com.tumri.content.ProductProvider;
import com.tumri.content.data.Content;
import com.tumri.content.data.ContentProviderStatus;
import com.tumri.joz.campaign.OSpecQueryCache;
import com.tumri.joz.jozMain.MerchantDB;
import com.tumri.joz.keywordServer.ProductIndex;
import com.tumri.joz.utils.LogUtils;
import com.tumri.utils.data.SortedArraySet;

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
                initLucene();

                pdb.deleteProduct(deltas[2]);
                
                // Clear all Ospec Query Cache.
                OSpecQueryCache.getInstance().clear();
            }
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
        Iterator<Handle> currProdsIt = null;
        if (currProdsSet == null) {
            retVal[0].addAll(allProds);
            return retVal;
        } else {
            currProdsIt = currProdsSet.iterator();
        }

        SortedSet<IProduct> allProdsSet = new SortedArraySet<IProduct>(allProds);
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
    
    private ContentHelper(ContentProvider p) {
        super();
        provider = p; 
    }
    
    public void contentUpdated() {
        
        if (provider == null) {
            return;
        }
        try {
            Content data = provider.getContent();
            initMerchantDataDatabase(data);
            initTaxonomyDatabase(data);
            initProductsDatabase(data);
        } catch (InvalidConfigException e) {
            log.error("Error while updating content",e);
            LogUtils.getFatalLog().error("Error while updating content",e);
        }
    }
    
    
}

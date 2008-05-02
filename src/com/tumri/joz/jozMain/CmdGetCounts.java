// get-counts command

/* The get-counts command has the following format:
 See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC.

 (get-counts :t-spec-name 'symbol)

 If `symbol' is `nil' then process the entire MUP, otherwise process
 all products selected by the t-spec.  We count up the
 number of products in each category, the number of products in each
 brand and the number of products in each merchant.  Returns a 3-tuple:
 (category-counts brand-counts merchant-counts) where category counts
 is a list where cars are category strings and cadrs are counts.   Only
 non-zero counts are included.  Similarly, for the brand-counts, the car
 is the brand and for the merchant-counts the car is the merchant name.
 */

package com.tumri.joz.jozMain;

import com.tumri.cma.domain.OSpec;
import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.Query.*;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.OSpecQueryCache;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.index.ProductAttributeIndex;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.utils.sexp.*;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.util.*;

public class CmdGetCounts extends CommandDeferWriting {

    private static Logger log = Logger.getLogger(CmdGetCounts.class);
    private static Category rootCat = null;

    public CmdGetCounts(Sexp e) {
        super(e);
        Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
        if (t != null) {
            rootCat = t.getRootCategory();
        }
    }

    @Override
    public Sexp process() {
        Sexp retVal;

        try {
            if (!expr.isSexpList()) {
                throw new BadCommandException("expecting (get-counts :t-spec-name t-spec-name)");
            }
            SexpList l = expr.toSexpList();
            if (l.size() != 3) {
                throw new BadCommandException("expecting (get-counts :t-spec-name t-spec-name)");
            }
            Sexp arg = l.get(2);
            if (arg.isSexpSymbol()) {
                SexpSymbol sym = arg.toSexpSymbol();
                retVal = getCounts(sym.toStringValue());
            } else if (arg.isSexpList() && arg.toSexpList().size() == 0) {
                retVal = getCounts(null);
            } else {
                throw new BadCommandException("expected t-spec name as a symbol");
            }
        } catch (BadCommandException ex) {
            log.warn("Invalid command.",ex);
            retVal = returnError(ex);
        } catch (Exception ex) {
            log.error("Unknown Exception.",ex);
            retVal = returnError(ex);
        }

        return retVal;
    }

    // See docs for the get-counts external API call for a description
    // of the format of the result.
    // This method is invoded from CmdTSpecAdd also.
    public static Sexp getCounts(String tspec_name) throws BadCommandException {

        String rootCatId = null;
        if (rootCat==null) {
            Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
            if (t != null) {
                rootCat = t.getRootCategory();
            }
        }

        if (rootCat != null) {
            rootCatId = rootCat.getGlassIdStr();
        }
        HashMap<String, Counter>[] counters = getCounters(tspec_name);

        HashMap<String, Counter> category_counts = counters[0];
        HashMap<String, Counter> brand_counts = counters[1];
        HashMap<String, Counter> provider_counts = counters[2];


        // Now create the return result.  

        // In Category we are supposed to use value GLASSVIEW.Product for root category.
        // This seems to be an undocumented quirk in the API.
        // This is the only way that the client currently knows that this is the count for 
        // root category (essentially for the entire t-spec).
        SexpList category_list = new SexpList();
        Counter c = category_counts.get(rootCatId.toString());
        if (c != null) {
            category_list.addLast(new SexpList(new SexpString("GLASSVIEW.Product"), new SexpInteger(c.get())));
            // Remove it once we have added it.
            category_counts.remove(rootCatId);
        }

        //Also remove the Tumri Category root - which is not handled correctly by the Portals
        category_counts.remove("GLASSVIEW.TUMRI_14111");

        Set<Map.Entry<String, Counter>> cat_counts = category_counts.entrySet();
        for (Map.Entry<String, Counter> count : cat_counts) {
            SexpList l = new SexpList(new SexpString(count.getKey()), new SexpInteger(count.getValue().get()));
            category_list.addLast(l);
        }

        SexpList brand_list = new SexpList();
        Set<Map.Entry<String, Counter>> br_counts = brand_counts.entrySet();
        for (Map.Entry<String, Counter> count : br_counts) {
            SexpList l = new SexpList(new SexpString(count.getKey()), new SexpInteger(count.getValue().get()));
            brand_list.addLast(l);
        }

        SexpList merchant_list = new SexpList();
        Set<Map.Entry<String, Counter>> mer_counts = provider_counts.entrySet();
        for (Map.Entry<String, Counter> count : mer_counts) {
            SexpList l = new SexpList(new SexpString(count.getKey()), new SexpInteger(count.getValue().get()));
            merchant_list.addLast(l);
        }

        SexpList result = new SexpList();
        result.addLast(category_list);
        result.addLast(brand_list);
        result.addLast(merchant_list);
        return result;
    }

    @SuppressWarnings("unchecked")
    public static HashMap<String, Counter>[] getCounters(String tspec_name) throws BadCommandException {

        HashMap<String, Counter>[] retVal = new HashMap[3];

        HashMap<String, Counter> category_counts = new HashMap<String, Counter>();
        HashMap<String, Counter> brand_counts = new HashMap<String, Counter>();
        HashMap<String, Counter> provider_counts = new HashMap<String, Counter>();

        retVal[0] = category_counts;
        retVal[1] = brand_counts;
        retVal[2] = provider_counts;

        if (tspec_name != null) {
            getOSpecAttributeCount(provider_counts, tspec_name,IProduct.Attribute.kProvider);
            getOSpecAttributeCount(brand_counts, tspec_name,IProduct.Attribute.kBrand);
            getOSpecAttributeCount(category_counts, tspec_name,IProduct.Attribute.kCategory);

            OSpec ospec = CampaignDB.getInstance().getOspec(tspec_name);
            ArrayList<Handle> includedProducts = null;
            if (ospec !=null) {
                includedProducts = OSpecHelper.getIncludedProducts(ospec);
            }
            if (includedProducts!=null && includedProducts.size() > 0) {
                SortedSet<Handle> sortedInclProds = new SortedArraySet<Handle>();
                sortedInclProds.addAll(includedProducts);

                getIncludedProductAttributeCount(provider_counts, sortedInclProds, IProduct.Attribute.kProvider);
                getIncludedProductAttributeCount(brand_counts, sortedInclProds,IProduct.Attribute.kBrand);
                getIncludedProductAttributeCount(category_counts, sortedInclProds,IProduct.Attribute.kCategory);

            }
        } else {
            //Provider Counts
            getGlobalAttributeCount(provider_counts, IProduct.Attribute.kProvider);
            //Brand Counts
            getGlobalAttributeCount(brand_counts, IProduct.Attribute.kBrand);
            //Category Counts
            getGlobalAttributeCount(category_counts, IProduct.Attribute.kCategory);

        }

        return retVal;

    }

    /**
     * Compute the global attribute counts for the given attribute
     * @param kAttr
     * @return
     */
    private static HashMap<String, Counter> getGlobalAttributeCount(HashMap<String, Counter> attrCounts,
                                                                    IProduct.Attribute kAttr) {
        ProductAttributeIndex<Integer,Handle> ai = ProductDB.getInstance().getIndex(kAttr);
        if (ai != null) {
            Set<Integer> keySet = ai.getKeys();
            for (Integer theKey: keySet) {
                String keyStrVal = DictionaryManager.getInstance().getValue(kAttr, theKey);
                incrementCounter(ai.getCount(theKey),attrCounts, keyStrVal, kAttr);
            }
        }
        return attrCounts;
    }

    /**
     * Get the OSpec attribute count for the given OSpec
     * @param tspecName
     * @param kAttr
     * @return
     */
    private static HashMap<String, Counter> getOSpecAttributeCount(HashMap<String, Counter> attrCounts,
                                                                   String tspecName, IProduct.Attribute kAttr) throws BadCommandException{
        ProductAttributeIndex<Integer,Handle> ai = ProductDB.getInstance().getIndex(kAttr);
        //TODO: Need to get the dictionary and walk thru instead of walking thru the index    
//        com.tumri.utils.dictionary.Dictionary<String> attrDict = DictionaryManager.getInstance().getDictionary(kAttr);
//        if (attrDict != null) {
//            attrDict.
//        }
        CNFQuery query = OSpecQueryCache.getInstance().getCNFQuery(tspecName);
        if (query == null) {
            log.error("t-spec " + tspecName + " not found.");
            throw new BadCommandException("t-spec " + tspecName + " not found.");
        }

        ArrayList<ConjunctQuery> conjQueries = query.getQueries();
        if (ai != null) {
            for(ConjunctQuery cq: conjQueries) {
                //Check if there are any queries in this
                if (cq.getQueries().size()==0) {
                    continue;
                }
                Set<Integer> keySet = ai.getKeys();

                for (Integer theKey: keySet) {
                    String keyStrVal = DictionaryManager.getInstance().getValue(kAttr, theKey);
                    SimpleQuery sq = new AttributeQuery(kAttr, theKey);
                    CNFQuery tmpQuery = new CNFQuery();
                    ConjunctQuery tmpCq = (ConjunctQuery)cq.clone();
                    tmpCq.addQuery(sq);
                    tmpQuery.addQuery(tmpCq);
                    tmpQuery.setStrict(true);
                    tmpQuery.setBounds(0, 0);
                    SortedSet<Handle> results = tmpQuery.exec();
                    if (results.size() > 0) {
                       incrementCounter(results.size(), attrCounts, keyStrVal, kAttr);
                    }
                }

            }
        }
        return attrCounts;
    }

    /**
     * For the given set of included product, return the count of the attribute
     * @return
     */
    private static HashMap<String, Counter> getIncludedProductAttributeCount(HashMap<String, Counter> attrCounts,
                                                                             SortedSet<Handle> sortedInclProds,
                                                                             IProduct.Attribute kAttr) {
        ProductAttributeIndex<Integer,Handle> ai = ProductDB.getInstance().getIndex(kAttr);
        if (ai != null) {
            Set<Integer> keySet = ai.getKeys();
            for (Integer theKey: keySet) {
                String keyStrVal = DictionaryManager.getInstance().getValue(kAttr, theKey);
                //Intersect between the 2 sets
                ProductSetIntersector aIntersector = new ProductSetIntersector();
                aIntersector.include(ai.get(theKey), AttributeWeights.getWeight(kAttr));
                aIntersector.include(sortedInclProds, AttributeWeights.getWeight(kAttr));
                aIntersector.setStrict(true);
                aIntersector.setMax(0);
                SortedSet<Handle> results = aIntersector.intersect();
                //Walk thru the loop - to avoid the warning of size() on SetIntersector
                for (Handle h: results) {
                    incrementCounter(attrCounts, keyStrVal, kAttr);
                }
            }
        }
        return attrCounts;
    }

    /**
     * Convinence method to increment the count by 1
     * @param attrCounts
     * @param keyStrVal
     * @param kAttr
     */
    private static void incrementCounter(HashMap<String, Counter> attrCounts, String keyStrVal, IProduct.Attribute kAttr) {
        incrementCounter(1, attrCounts, keyStrVal, kAttr);
    }
    /**
     * Increment the counter taking into consideration the special case for Category, where parent counts are also
     * to be included.
     * @param attrCounts
     * @param keyStrVal
     * @param kAttr
     */
    private static void incrementCounter(int size, HashMap<String, Counter> attrCounts, String keyStrVal, IProduct.Attribute kAttr) {
        if (keyStrVal != null) {
            //If this is category, then need to get the cats of parents also
            if (kAttr == IProduct.Attribute.kCategory) {
                if (rootCat==null) {
                    Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
                    if (t != null) {
                        rootCat = t.getRootCategory();
                    }
                }

                //Check if this is a lead node
                Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
                if (t!=null) {
                    Category cat = t.getCategory(keyStrVal);
                    if (cat != null && t.getChildren(cat) == null) {
                        incrementCategoryCount(size, keyStrVal, attrCounts);
                    }
                }

            } else {
                Counter ctr = getCounter(attrCounts,keyStrVal);
                ctr.inc(size);
            }
        }
    }

    /**
     * Increment the count for the category, as well as all its parents
     * @param size
     * @param catIdStr
     * @param attrCounts
     */
    private static void incrementCategoryCount(int size, String catIdStr, HashMap<String, Counter> attrCounts) {
        List<String> categories = new ArrayList<String>();
        categories.add(catIdStr);
        categories = getAllCategories(categories);
        if (categories!=null) {
            for (String cat : categories) {
                Counter ctr = getCounter(attrCounts,cat);
                ctr.inc(size);
            }
        }

    }
    /**
     * Return list of all categories in cats and their parents.
     */
    private static List<String> getAllCategories(List<String> cats) {
        JOZTaxonomy tax = JOZTaxonomy.getInstance();
        Taxonomy t = tax.getTaxonomy();
        if (t != null) {
            List<String> result = new ArrayList<String>();
            HashSet<Integer> idSet = new HashSet<Integer>();
            for (String c : cats) {
                Category p = t.getCategory(c);
                while (p != null && !idSet.contains(p.getGlassId())) {
                    idSet.add(p.getGlassId());
                    result.add(p.getGlassIdStr());
                    p = p.getParent();
                };
            }
            return result;
        }
        return null;
    }


    
    private static Counter getCounter(HashMap<String, Counter> counts, String key) {
        if (counts == null) {
            return null;
        }
        Counter ctr = counts.get(key);
        if (ctr == null) {
            ctr = new Counter(0);
            counts.put(key,ctr);
        }
        return ctr;
    }

    public static class Counter {

        int count;

        public Counter(int i) {
            count = i;
        }

        public void inc() {
            ++count;
        }

        public void inc(int ctr) {
            count = count + ctr;
        }

        public int get() {
            return count;
        }
    }


}

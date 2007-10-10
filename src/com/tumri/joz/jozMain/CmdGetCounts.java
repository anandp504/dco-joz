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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.OSpec;
import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.OSpecQueryCache;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpInteger;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.sexp.SexpSymbol;

public class CmdGetCounts extends CommandDeferWriting {

    private static Logger log = Logger.getLogger(CmdGetCounts.class);

    /**
     * Return list of all categories in cats and their parents.
     */
    private static List<String> get_all_categories(List<String> cats) {
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
    

    public CmdGetCounts(Sexp e) {
        super(e);
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
        
        HashMap<String, Counter>[] counters = getCounters(tspec_name);
        
        HashMap<String, Counter> category_counts = counters[0];
        HashMap<String, Counter> brand_counts = counters[1];
        HashMap<String, Counter> provider_counts = counters[2];

        String rootCatId = null;
        Taxonomy t = JOZTaxonomy.getInstance().getTaxonomy();
        if (t != null) {
            Category rootCat = t.getRootCategory();
            if (rootCat != null) {
                rootCatId = rootCat.getGlassIdStr();
            }
        }

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
        
        JOZTaxonomy tax = JOZTaxonomy.getInstance();
        ProductDB pdb = ProductDB.getInstance();
        
        SortedSet<Handle> product_handles_set = null;
        if (tspec_name != null) {
            // Get products from all the t-specs.
            OSpecQueryCache qcache = OSpecQueryCache.getInstance();
            CNFQuery query = qcache.getCNFQuery(tspec_name);
            if (query == null) {
                log.error("t-spec " + tspec_name + " not found.");
                throw new BadCommandException("t-spec " + tspec_name + " not found.");
            }
            query.setStrict(true);
            query.setBounds(0, 0);
            product_handles_set = query.exec();
            OSpec ospec = CampaignDB.getInstance().getOspec(tspec_name);
            ArrayList<Handle> includedProducts = null;
            if (ospec !=null) {
            	includedProducts = OSpecHelper.getIncludedProducts(ospec);
            }
            if (includedProducts!=null && includedProducts.size() > 0) {
            	product_handles_set.addAll(includedProducts);
            }
        } else { 
            // search entire mup. Is this the right way to do it ?
            product_handles_set = pdb.getAll();
        }

        Iterator<Handle> product_handles = product_handles_set.iterator();

        while (product_handles.hasNext()) {
            Handle h = product_handles.next();
            IProduct p = pdb.get(h);
            Counter ctr = null;
            
            // Create information about the counts.
            
            // Category Information
            String category = p.getCategoryStr();
            List<String> categories = new ArrayList<String>();
            categories.add(category);
            
            categories = get_all_categories(categories);
            for (String cat : categories) {
                ctr = getCounter(category_counts,cat);
                ctr.inc();
            }
            
            // Brand Information
            String brand = p.getBrandStr();
            ctr = getCounter(brand_counts,brand);
            ctr.inc();
            
            // Provider Information
            String provider = p.getProviderStr();
            ctr = getCounter(provider_counts,provider);
            ctr.inc();
        }
        
        return retVal;

    }
    
    
    protected static Counter getCounter(HashMap<String, Counter> counts, String key) {
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
        
        public int count;
        
        public Counter(int i) {
            count = i;
        }
        
        public void inc() {
            ++count;
        }
        
        public int get() {
            return count;
        }
    }
    

}

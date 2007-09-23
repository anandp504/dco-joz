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

import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.Query.CNFQuery;
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
                throw new BadCommandException("expecting (get-counts t-spec-name)");
            }
            SexpList l = expr.toSexpList();
            if (l.size() != 2) {
                throw new BadCommandException("expecting (get-counts t-spec-name)");
            }
            Sexp arg = l.get(1);
            if (arg.isSexpSymbol()) {
                SexpSymbol sym = arg.toSexpSymbol();
                retVal = get_counts(sym.toString());
            } else if (arg.isSexpList() && arg.toSexpList().size() == 0) {
                retVal = get_counts(null);
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
    protected Sexp get_counts(String tspec_name) throws BadCommandException {
        
        HashMap<String, Counter> category_counts = new HashMap<String, Counter>();
        HashMap<String, Counter> brand_counts = new HashMap<String, Counter>();
        HashMap<String, Counter> provider_counts = new HashMap<String, Counter>();
        JOZTaxonomy tax = JOZTaxonomy.getInstance();
        ProductDB pdb = ProductDB.getInstance();
        
        SortedSet<Handle> product_handles_set = null;
        if (tspec_name != null) {
            // Get products from all the t-specs.
            OSpecQueryCache qcache = OSpecQueryCache.getInstance();
            CNFQuery query = qcache.getCNFQuery(tspec_name);
            if (query == null) {
                throw new BadCommandException("t-spec " + tspec_name + " not found.");
            }
            product_handles_set = query.exec();
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
                ctr = getCounters(category_counts,cat);
                ctr.inc();
            }
            
            // Brand Information
            String brand = p.getBrandStr();
            ctr = getCounters(brand_counts,brand);
            ctr.inc();
            
            // Provider Information
            String provider = p.getProviderStr();
            ctr = getCounters(provider_counts,provider);
            ctr.inc();
        }

        // Now create the return result.        
        SexpList category_list = new SexpList();
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
    
    
    protected Counter getCounters(HashMap<String, Counter> counts, String key) {
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
    
    private class Counter {
        
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

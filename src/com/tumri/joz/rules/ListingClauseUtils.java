package com.tumri.joz.rules;

import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.Pair;
import com.tumri.utils.data.SortedBag;
import com.tumri.utils.data.SortedListBag;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Utility to validate the listing clause items
 * User: nipun
 */
public class ListingClauseUtils {

    private static final Logger log = Logger.getLogger(ListingClauseUtils.class);

    /**
     * Validate the bag of Listing Clauses and get the weights normalized accordingly
     * @param bag - input LC bag.
     * @return - Validated Set of Listing Clause items
     */
    public static SortedBag<Pair<ListingClause, Double>> validateListingClauses(SortedBag<Pair<ListingClause, Double>> bag) {
        SortedBag<Pair<ListingClause, Double>> finalClauses = new SortedListBag<Pair<ListingClause, Double>>();

        if (bag != null) {
            Iterator<SortedBag.Group<Pair<ListingClause, Double>>> groups = bag.groupBy(null);
            //1. Walk thru the incoming clauses - grouped by the like types
            while (groups.hasNext()) {
                SortedBag.Group<Pair<ListingClause, Double>> group = groups.next();
                Pair<ListingClause, Double> key = group.key();
                //2. Prepare list of valid items
                SortedBag<Pair<ListingClause, Double>> validClauses = null;
                double badWeights = 0;
                group = bag.getGroup(key);
                while (group.hasNext()) {
                    Pair<ListingClause, Double> pair = group.next();
                    double wt = pair.getSecond();
                    ListingClause lc = pair.getFirst();
                    //3. Throw out invalid items, get their weights and add it up
                    if (isValid(lc)) {
                        if (validClauses==null){
                            validClauses = new SortedListBag<Pair<ListingClause, Double>>();
                        }
                        validClauses.add(new Pair<ListingClause, Double>(lc, wt));
                    } else {
                        badWeights += wt;
                    }
                }

                if (validClauses!=null && validClauses.size()>0 && badWeights>0){
                    //4. Distribute the weights of the invalid items to the valid items
                    double normWt = badWeights/validClauses.size();
                    Iterator<SortedBag.Group<Pair<ListingClause, Double>>> valGroups = validClauses.groupBy(null);
                    while(valGroups.hasNext()) {
                        SortedBag.Group<Pair<ListingClause, Double>> valGroup = valGroups.next();
                        while (valGroup.hasNext()) {
                            Pair<ListingClause, Double> pair = valGroup.next();
                            double wt = pair.getSecond();
                            pair.setSecond(wt+normWt);
                        }
                    }

                }

                finalClauses.addAll(validClauses);

            }
        }



        return finalClauses;
    }

    /**
     * Checks if the lc is valid or not. If even one of the included values are bad - the LC is deemed invalid
     * @param lc - the input clause to be analyzed
     * @return - TRUE if valid, FALSE otherwise
     */
    private static boolean isValid(ListingClause lc) {
        Set<IProduct.Attribute> lcAttrs = new HashSet<IProduct.Attribute>();
        lcAttrs.add(IProduct.Attribute.kId);
        lcAttrs.add(IProduct.Attribute.kBrand);
        lcAttrs.add(IProduct.Attribute.kSupplier);
        lcAttrs.add(IProduct.Attribute.kGlobalId);
        lcAttrs.add(IProduct.Attribute.kKeywords);

        for (IProduct.Attribute attr: lcAttrs) {
            Set<String> values = lc.getListingClause(attr.name());
            if (values!=null && !values.isEmpty()) {
                if (attr == IProduct.Attribute.kId) {
                    for (String productId: values) {
                        if (productId=="") {
                            //Default
                            continue;
                        }
                        if (productId.indexOf(".") > -1) {
                            productId = productId.substring(productId.indexOf("."), productId.length());
                        }
                        char[] pidCharArr = productId.toCharArray();
                        //Drop any non digit characters
                        StringBuffer spid = new StringBuffer();
                        for (char ch: pidCharArr) {
                            if (Character.isDigit(ch)) {
                                spid.append(ch);
                            }
                        }

                        productId = spid.toString();
                        Handle p = ProductDB.getInstance().getHandle(new Long(productId));
                        if (p==null) {
                            log.warn("Skipping Invalid product in listing clause : " + productId);
                            return false;
                        }
                    }
                } else {
                    ArrayList<Integer> idList = new ArrayList<Integer>();
                    StringBuilder buff = new StringBuilder();
                    for (String v: values) {
                        if (v=="") {
                            //Default
                            continue;
                        }
                        Integer dictId = DictionaryManager.getId (attr, v);
                        if(dictId != null){
                            idList.add(dictId);
                        }
                        buff.append(v+",");
                    }
                    SimpleQuery theQuery = new AttributeQuery(attr, idList);
                    SortedSet<Handle> res = theQuery.exec();
                    if (res==null || res.isEmpty()) {
                        log.warn("Skipping Invalid " + attr.name() + " in listing clause : " + buff.toString());
                        return false;
                    }
                }
            }


        }
        return true;
    }
}

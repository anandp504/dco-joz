// A t-spec.
// NOTE: There are no setter methods on purpose!
// NOTE: null means "unspecified".
// FIXME: wip

package com.tumri.joz.jozMain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.Query.ProductQueryProcessor;
import com.tumri.joz.Query.RangeQuery;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.IProduct;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpKeyword;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpUtils;

public class TSpec {
    
    public TSpec(Iterator<Sexp> iter) throws BadTSpecException {
        process_iter(iter);
        
        // Generate a name if not provided.
        if (_name == null) {
            _name = "t-spec-" + String.valueOf(next_anonymous);
            ++next_anonymous;
        }
        
        _query.addQuery(_cjquery);
    }
    
    public TSpec(String name, Integer version, Integer max_prods, Long modified) {
        _name = name;
        _version = version;
        _max_prods = max_prods;
        _modified_time = modified;
        
        _query.addQuery(_cjquery);
    }
    
    public String get_name() {
        return _name;
    }
    
    public Integer get_version() {
        return _version;
    }
    
    public Integer get_max_prods() {
        return _max_prods;
    }
    
    public Long get_modified_time() {
        return _modified_time;
    }
    
    // FIXME: for now
    public boolean private_label_p() {
        return false;
    }
    
    public CNFQuery get_query() {
        return _query;
    }
    
    // implementation details -------------------------------------------------
    
    private String _name = null;
    
    private Integer _version = -1;
    
    private Integer _max_prods = 42; // FIXME, max-prods-per-realm
    
    private Long _modified_time = System.currentTimeMillis();
    
    private CNFQuery _query = new CNFQuery();
    
    private ConjunctQuery _cjquery = new ConjunctQuery(
            new ProductQueryProcessor());
    
    // Used to give unique names to anonymous t-specs.
    private static long next_anonymous = 0;
    
    private static Logger log = Logger.getLogger(TSpec.class);
    
    // FIXME: wip
    private enum TSpecParam {
        NAME, VERSION, MAX_PRODS, MODIFIED_TIME, LOAD_TIME_KEYWORD_EXPR, ZINI_KEYWORDS, INCLUDE_CATEGORIES, EXCLUDE_CATEGORIES, ATTR_INCLUSIONS, ATTR_EXCLUSIONS, INCOME_PERCENTILE, REF_PRICE_CONSTRAINTS, RANK_CONSTRAINTS, CPC_RANGE, CPO_RANGE,
    }
    
    private static HashMap<String, TSpecParam> tspec_params = new HashMap<String, TSpecParam>();
    
    static {
        // FIXME: collisions?
        tspec_params.put(":name", TSpecParam.NAME);
        tspec_params.put(":version", TSpecParam.VERSION);
        tspec_params.put(":max-prods", TSpecParam.MAX_PRODS);
        tspec_params.put(":modified", TSpecParam.MODIFIED_TIME);
        tspec_params.put(":load-time-keyword-expr",
                TSpecParam.LOAD_TIME_KEYWORD_EXPR);
        tspec_params.put(":zini-keywords", TSpecParam.ZINI_KEYWORDS);
        tspec_params.put(":include-categories", TSpecParam.INCLUDE_CATEGORIES);
        tspec_params.put(":exclude-categories", TSpecParam.EXCLUDE_CATEGORIES);
        tspec_params.put(":attr-inclusions", TSpecParam.ATTR_INCLUSIONS);
        tspec_params.put(":attr-exclusions", TSpecParam.ATTR_EXCLUSIONS);
        tspec_params.put(":income-percentile", TSpecParam.INCOME_PERCENTILE);
        tspec_params.put(":ref-price-constraints",
                TSpecParam.REF_PRICE_CONSTRAINTS);
        tspec_params.put(":rank-constraints", TSpecParam.RANK_CONSTRAINTS);
        tspec_params.put(":cpc-range", TSpecParam.CPC_RANGE);
        tspec_params.put(":cpo-range", TSpecParam.CPO_RANGE);
    }
    
    private void process_iter(Iterator<Sexp> iter) throws BadTSpecException {
        DictionaryManager dm = DictionaryManager.getInstance();
        
        while (iter.hasNext()) {
            Sexp elm = iter.next();
            Sexp t;
            
            if (!elm.isSexpKeyword()) {
                // FIXME: TODO
                assert (false);
            }
            
            SexpKeyword k = elm.toSexpKeyword();
            String name = k.toStringValue();
            
            TSpecParam p = tspec_params.get(name.toLowerCase());
            if (p == null) {
                // bad/unsupported parameter
                // FIXME: TODO, we still don't support all the current ones
                log.error("unsupported t-spec parameter: " + name);
                t = iter.next(); // gobble up the parameter value
                continue;
            }
            
            String value = null;
            SimpleQuery sq = null;
            
            try {
                switch (p) {
                    case NAME:
                        this._name = SexpUtils.get_next_symbol(name, iter);
                        break;
                    
                    case VERSION:
                        this._version = SexpUtils.get_next_integer(name, iter);
                        break;
                    
                    case MAX_PRODS:
                        this._max_prods = SexpUtils
                                .get_next_integer(name, iter);
                        break;
                    
                    case MODIFIED_TIME: {
                        // FIXME: wip
                        // this._modified_time = ???
                        t = iter.next();
                        break;
                    }
                        
                    case LOAD_TIME_KEYWORD_EXPR:
                        // FIXME: wip
                        t = iter.next();
                        break;
                    
                    case ZINI_KEYWORDS:
                        // FIXME: wip
                        t = iter.next();
                        break;
                    
                    case INCLUDE_CATEGORIES: {
                        // FIXME: wip
                        SexpList l = SexpUtils.get_next_list(name, iter);
                        Iterator<Sexp> iter2 = l.iterator();
                        ArrayList<Integer> values = get_attr_values(name,
                                IProduct.Attribute.kCategory, iter2);
                        sq = new AttributeQuery(IProduct.Attribute.kCategory,
                                values);
                        break;
                    }
                        
                    case EXCLUDE_CATEGORIES: {
                        // FIXME: wip
                        SexpList l = SexpUtils.get_next_list(name, iter);
                        Iterator<Sexp> iter2 = l.iterator();
                        ArrayList<Integer> values = get_attr_values(name,
                                IProduct.Attribute.kCategory, iter2);
                        sq = new AttributeQuery(IProduct.Attribute.kCategory,
                                values);
                        sq.setNegation(true);
                        break;
                    }
                        
                    case ATTR_INCLUSIONS: {
                        // FIXME: wip
                        SexpList l = SexpUtils.get_next_list(name, iter);
                        for (Sexp e : l) {
                            if (!e.isSexpList())
                                throw new BadTSpecException(
                                        "unexpected value for " + name + ": "
                                                + e.toString());
                            SexpList attr_expr = e.toSexpList();
                            sq = get_query(name, attr_expr);
                            _cjquery.addQuery(sq);
                        }
                        continue; // we've already added the queries
                    }
                        
                    case ATTR_EXCLUSIONS: {
                        // FIXME: wip
                        SexpList l = SexpUtils.get_next_list(name, iter);
                        for (Sexp e : l) {
                            if (!e.isSexpList())
                                throw new BadTSpecException(
                                        "unexpected value for " + name + ": "
                                                + e.toString());
                            SexpList attr_expr = e.toSexpList();
                            sq = get_query(name, attr_expr);
                            sq.setNegation(true);
                            _cjquery.addQuery(sq);
                        }
                        continue; // we've already added the queries
                    }
                        
                    case INCOME_PERCENTILE: {
                        // FIXME: wip
                        SexpList range = SexpUtils.get_next_list(name, iter);
                        ArrayList<Double> values = get_range_values(name, range);
                        /*
                         * sq = new RangeQuery (IProduct.Attribute.???,
                         * values.get (0), values.get (1));
                         */
                        break;
                    }
                        
                    case REF_PRICE_CONSTRAINTS: {
                        // FIXME: wip
                        SexpList range = SexpUtils.get_next_list(name, iter);
                        ArrayList<Double> values = get_range_values(name, range);
                        sq = new RangeQuery(IProduct.Attribute.kPrice, values
                                .get(0), values.get(1));
                        break;
                    }
                        
                    case RANK_CONSTRAINTS: {
                        // FIXME: wip
                        value = SexpUtils.get_next_string(name, iter);
                        break;
                    }
                        
                    case CPC_RANGE: {
                        // FIXME: wip
                        SexpList range = SexpUtils.get_next_list(name, iter);
                        ArrayList<Double> values = get_range_values(name, range);
                        sq = new RangeQuery(IProduct.Attribute.kCPC, values
                                .get(0), values.get(1));
                        break;
                    }
                        
                    case CPO_RANGE: {
                        // FIXME: wip
                        value = SexpUtils.get_next_string(name, iter);
                        break;
                    }
                        
                    default:
                        assert (false);
                }
            } catch (SexpUtils.BadGetNextException ex) {
                throw new BadTSpecException(ex.getMessage());
            }
            
            if (sq != null) {
                _cjquery.addQuery(sq);
            }
        }
    }
    
    private ArrayList<Integer> getValues(IProduct.Attribute attr, String value) {
        // FIXME: use of StringTokenizer is discouraged
        StringTokenizer tokens = new StringTokenizer(value, " ", false);
        DictionaryManager dm = DictionaryManager.getInstance();
        ArrayList<Integer> vals = new ArrayList<Integer>();
        String last = null;
        while (tokens.hasMoreTokens()) {
            String token = tokens.nextToken().trim();
            if (last == null) {
                if (token.startsWith("|")) {
                    last = token;
                    if (token.endsWith("|")) {
                        last = last.substring(1, last.length() - 1);
                        vals.add(dm.getId(attr, last));
                        last = null;
                    }
                } else {
                    vals.add(dm.getId(attr, token));
                }
            } else {
                last = last + " " + token;
                if (token.endsWith("|")) {
                    last = last.substring(1, last.length() - 1);
                    vals.add(dm.getId(attr, last));
                    last = null;
                }
            }
        }
        return vals;
    }
    
    private ArrayList<Double> get_range_values(String name, SexpList range)
            throws SexpUtils.BadGetNextException {
        ArrayList<Double> vals = new ArrayList<Double>();
        Iterator<Sexp> iter = range.iterator();
        while (iter.hasNext()) {
            Double d = SexpUtils.get_next_maybe_double(name, iter);
            // watch for NIL
            if (d == null)
                d = new Double(vals.size() == 0 ? 0 : 100000);
            vals.add(d);
        }
        while (vals.size() < 2) {
            vals.add(new Double(vals.size() == 0 ? 0 : 100000));
        }
        return vals;
    }
    
    private ArrayList<Integer> get_attr_values(String name,
            IProduct.Attribute attr_kind, Iterator<Sexp> iter)
            throws SexpUtils.BadGetNextException {
        DictionaryManager dm = DictionaryManager.getInstance();
        ArrayList<Integer> values = new ArrayList<Integer>();
        while (iter.hasNext()) {
            String cat = SexpUtils.get_next_symbol(name, iter);
            Integer cat_id = dm.getId(attr_kind, cat);
            values.add(cat_id);
        }
        return values;
    }
    
    private SimpleQuery get_query(String name, SexpList attr_expr)
            throws BadTSpecException, SexpUtils.BadGetNextException {
        DictionaryManager dm = DictionaryManager.getInstance();
        if (attr_expr.size() < 2)
            throw new BadTSpecException("unexpected value for " + name + ": "
                    + attr_expr.toString());
        Iterator<Sexp> iter = attr_expr.iterator();
        String attr_kind_name = SexpUtils.get_next_symbol(name, iter);
        IProduct.Attribute attr_kind = IProduct.Attribute.kNone;
        if (attr_kind_name.equalsIgnoreCase("provider")) {
            attr_kind = IProduct.Attribute.kProvider;
        } else if (attr_kind_name.equalsIgnoreCase("supplier")) {
            attr_kind = IProduct.Attribute.kSupplier;
        } else if (attr_kind_name.equalsIgnoreCase("brand")) {
            attr_kind = IProduct.Attribute.kBrand;
        } else {
            throw new BadTSpecException("unexpected value for " + name + ": "
                    + attr_expr.toString());
        }
        ArrayList<Integer> values = new ArrayList<Integer>();
        while (iter.hasNext()) {
            String attr_value = SexpUtils.get_next_symbol(name, iter);
            Integer value_id = dm.getId(attr_kind, attr_value);
            values.add(value_id);
        }
        SimpleQuery sq = new AttributeQuery(attr_kind, values);
        return sq;
    }
}

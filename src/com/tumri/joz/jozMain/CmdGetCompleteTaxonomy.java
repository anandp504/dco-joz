// get-complete-taxonomy command

/* The get-complete-taxonomy command has the following format.
 It takes no parameters and returns the taxonomy formatted as:

 ((root "root_name")
 (((child1 "child1_name")
 (((grandchild11 "grandchild11_name") NIL)
 ((grandchild12 "grandchild12_name") NIL)))
 ((child2 "child2_name")
 (((grandchild21 "grandchild21_name") NIL)
 ((grandchild22 "grandchild22_name") NIL)))))

 i.e. a recursive data structure where each element is

 ((name "pretty_name") (child1-spec child2-spec ...))
 */

package com.tumri.joz.jozMain;

// import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.content.data.Category;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.products.JOZTaxonomy;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpInteger;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpString;
import com.tumri.utils.sexp.SexpSymbol;

public class CmdGetCompleteTaxonomy extends CommandDeferWriting {

    protected static Logger log = Logger.getLogger(CmdGetCompleteTaxonomy.class);
    
    protected int maxDepth = -1;
    protected boolean fetchCounts = false;
    // What do we do with agentName ?
    protected String agentName = null;

    public CmdGetCompleteTaxonomy(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp retVal;
        
        try {
            parseExpression();
            
            DictionaryManager dm = DictionaryManager.getInstance();
            JOZTaxonomy tax = JOZTaxonomy.getInstance();
            Taxonomy t = tax.getTaxonomy();
            HashMap<String, CmdGetCounts.Counter> categoryCounts = null;
            if (fetchCounts) {
                HashMap<String, CmdGetCounts.Counter>[] counts = null;
                counts = CmdGetCounts.getCounters(null);
                if (counts != null) {
                    categoryCounts = counts[0];
                }
            }
            retVal = get_taxonomy(dm, t, t.getRootCategory(), fetchCounts, categoryCounts, 0, maxDepth);
        } catch (Exception ex) {
            return returnError(ex);
        }
        
        return retVal;
    }
    
    protected void parseExpression() throws BadCommandException {
        if (!expr.isSexpList())
            throw new BadCommandException("get-complete-taxonomy not a list");
        SexpList l = expr.toSexpList();
        int n = l.size();
        
        Iterator<Sexp> iter = l.iterator();
        
        Sexp cmd = iter.next();
        
        while (iter.hasNext()) {
            Sexp reqParam = iter.next();
            Sexp paramValue = null;
            String reqParamString = reqParam.toStringValue(); 
            if (reqParamString.equals(":max-depth")) {
                if (iter.hasNext()) {
                    paramValue = iter.next();
                    if (paramValue == null) {
                        handleNullValue(reqParamString);
                    }
                    if (paramValue.isSexpInteger()) {
                        maxDepth = paramValue.toSexpInteger().toNativeInteger32();
                    } else {
                        handleInvalidTypeValue(reqParamString, paramValue, "integer");
                    }
                } else {
                    handleMissingValue(reqParamString);
                }
                continue;
            }
            
            if (reqParamString.equals(":return-counts")) {
                if (iter.hasNext()) {
                    paramValue = iter.next();
                    if (paramValue == null) {
                        handleNullValue(reqParamString);
                    }
                    if (paramValue.isSexpSymbol()) {
                        fetchCounts = paramValue.toSexpSymbol().toStringValue().trim().equalsIgnoreCase("t");
                    } else if (paramValue.isSexpList() && paramValue.toSexpList().size() == 0) {
                        fetchCounts = false;
                    } else {
                        handleInvalidTypeValue(reqParamString, paramValue, "t or nil");
                    }
                } else {
                    handleMissingValue(reqParamString);
                }
                continue;                
            }
            
            if (reqParamString.equals(":*agent-name*")) {
                if (iter.hasNext()) {
                    paramValue = iter.next();
                    if (paramValue == null) {
                        handleNullValue(reqParamString);
                    }
                    if (paramValue.isSexpSymbol()) {
                        agentName = paramValue.toSexpSymbol().toStringValue();
                    } else {
                        handleInvalidTypeValue(reqParamString, paramValue, "symbol");
                    }
                } else {
                    handleMissingValue(reqParamString);
                }
                continue;                
            }
            
        }

    }
    
    protected void handleInvalidTypeValue(String paramName, Sexp paramValue, String typeExpected) throws BadCommandException {
        throw new BadCommandException("Invalid command. " + paramName + " value of invalid type. Got " + paramValue.getKind().name() + ", expected " + typeExpected);
    }
    
    protected void handleNullValue(String paramName) throws BadCommandException {
        throw new BadCommandException("Invalid command. " + paramName + " specified without null value");
    }

    protected void handleMissingValue(String paramName) throws BadCommandException {
        throw new BadCommandException("Invalid command. " + paramName + " specified without a value");
    }
    
    /**
     * Returns the taxonomy tree.
     * 
     * Format of the response is 
     * 
     * (( |CATEGORY| "CATEGORY" COUNT SUBCHILDREN ) ( (( |CHILD1| "CHILD1" COUNT SUBCHILDREN ) (...)) ))
     * OR
     * (( |CATEGORY| "CATEGORY" ) ( (( |CHILD1| "CHILD1" ) (...)) ))
     * 
     * COUNT parameter shows number of products in that category.
     * SUBCHILDREN parameter is t or nil
     * SUBCHILDREN parameter is present only if COUNT is present.
     * 
     */ 
    
    
    private static SexpList get_taxonomy(DictionaryManager dm, Taxonomy tax, Category category, 
            boolean countsReq, HashMap<String, CmdGetCounts.Counter> counts, 
            int depth, int maxDepth) {
        
        // This id is the GlassView.... pretty name.
        String glassIdStr = category.getGlassIdStr();
        String name = category.getName();
        int count = 0;
        CmdGetCounts.Counter counter = ((counts==null)?null:counts.get(glassIdStr));        
        if (counter != null) {
            count = counter.get();
        }
        
        SexpList currCategory = new SexpList();

        currCategory.addLast(new SexpSymbol(glassIdStr));
        currCategory.addLast(new SexpString(name));
        
        if (countsReq) {
            currCategory.addLast(new SexpInteger(count));
        }

        SexpList result = new SexpList();
        result.addLast(currCategory);
                
        if ((maxDepth <= 0) || ((depth+1) < maxDepth)) {
            if (countsReq) {
            currCategory.addLast(new SexpSymbol("t"));
            }
            SexpList children = new SexpList();
            Category[] childrens = category.getChildren();
            
            if (childrens != null) {
                for (Category child_id : childrens) {
                    children.addLast(get_taxonomy(dm, tax, child_id, countsReq, counts, depth+1, maxDepth));
                }
            }
            
            result.addLast(children);
        } else {
            if (countsReq) {
                currCategory.addLast(new SexpList());
            }
        }

        return result;
    }
}

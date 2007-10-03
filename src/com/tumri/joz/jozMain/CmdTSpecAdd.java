// t-spec-add command

/* The t-spec-add command has the following format.
 See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC#t_spec_add

 (t-spec-add
 :name '<name-of-t-spec>
 :version -1
 :modified <number|string>
 :max-prods <integer> ;; maximum number of prods in this t-spec (defaults to 5,000)
 :load-time-keyword-expr [nil|string]
 :zini-keywords ;; deprecated
 :include-categories '()   
 :exclude-categories '()
 :attr-inclusions '()
 :attr-exclusions '()
 :include-products '()
 :exclude-products '()
 ;; car is lower %ile, cdr is upper %ile, nil = none:
 :income-percentile '()
 ;; ditto:
 :ref-price-constraints
 ;; ditto:
 :rank-constraints
 ;; ditto:
 :CPC-range
 ;; ditto:
 :CPO-range
 :sex nil ;; not currently used
 :age nil ;; not currently used
 :weight-map-name nil ;; not currently used
 :weight-map-combo-scheme (or :both :just-global :just-local))
 :psychographics-p [t|nil|:maybe]  ;; use psychographic curves when building roulette wheel
 :mine-ref-URL-p [t|nil|:maybe]   ;; not currently used
 :mine-pub-URL-p [t|nil|:maybe]   ;; pull keywords out of publisher URL and get products based on them
 :irrelevad-p [t|nil|:maybe]  ;; not currently used
 :script-keywords-within-t-spec-only [nil|t] ;; see note below
 :pub-URL-keywords-within-t-spec-only [nil|t] ;; see note below
 :pub-URL-keywords-stop-words [nil|string] ;; see note below
 :pub-URL-keywords-scavenger ;; see note below
 :pub-URL-keywords-q-wrapper ;; see note below
 :pub-URL-keywords-query-names ;; see note below
 :allow-too-small-leaf-cats-p [t|nil]
 )

 Only :name and :version are mandatory.

 */

package com.tumri.joz.jozMain;

import org.apache.log4j.Logger;

import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.joz.campaign.TransientDataException;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;

public class CmdTSpecAdd extends CommandDeferWriting {
    
    public CmdTSpecAdd(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp retVal;
        
        try {
            if (!expr.isSexpList()) {
                log.error("Invalid Request: " + expr);
                throw new BadCommandException("expected (t-spec-add ...)");
            }
            retVal = add_tspec(expr.toSexpList());
        } catch (BadCommandException ex) {
            return returnError(ex);
            
        } catch (Exception ex) {
            return returnError(ex);
        }
        
        return retVal;
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger.getLogger(CmdTSpecAdd.class);
    
    private Sexp add_tspec(SexpList rqst) throws BadCommandException {
        String tspecName;
        Sexp result;
        try {
            tspecName = OSpecHelper.doTSpecAdd(rqst);
        }
        catch(TransientDataException e) {
            log.error("Error occured while adding t-spec", e);
            throw new BadCommandException("Error while processing request: " + rqst.toString());
        }

        try {
            result = CmdGetCounts.getCounts(tspecName);
        }
        catch (BadCommandException ex) {
            log.error("Error while forming response after adding t-spec.",ex);
            throw ex;
        }
        return result;
    }
}

// incorp-mapping-deltas command

/* The incorp-mapping-deltas command has the following format.
 See https://secure.tumri.com/twiki/bin/view/Engineering/SoZandTC#incorp_mapping_deltas.

 This is how joz's mappings get updated on the fly.  This function
 takes a single argument that is a list of 6-tuples; each 6-tuple is
 a mapping operation: it tells joz to add or to delete one arc in
 the many-to-many mapping that joz uses to pick t-specs.  The 6-tuple
 is interpreted as follows:

 (<op-type> <mapping-type> <data> <t-spec> <weight> <mod-time>)

 where

 op-type
 is either :add or :delete and tells joz how to update it's mapping 
 mapping-type
 is either :realm, :theme or :store-ID 
 data
 is the URL or theme or store-ID (strings) 
 t-spec
 the t-spec to use 
 weight
 is how often to choose this t-spec (for now it's always 1.0f0) 
 mod-time
 is when this mapping was last updated (format is same as for
 t-spec-add).  If the value is "nil" then the current time will be
 automatically filled in. 

 For example:

 (incorp-mapping-deltas 
 '((:add :realm "http://www.foo.com" |T-SPEC-foo| 1.0f0 345345433545)
 (:delete :store-ID "abcd" |T-SPEC-baz| 1.0f0 345345433545)))

 tells joz to perform two mapping operations: to add a mapping from the
 realm "http://www.foo.com" to t-spec foo and to delete the mapping from
 store-ID "abcd" to t-spec baz.  For delete ops joz ignores the weight and
 will not complain if it doesn't find a matching mapping.
 */

package com.tumri.joz.jozMain;

// import java.io.PrintWriter;
import org.apache.log4j.Logger;

import com.tumri.joz.campaign.OSpecHelper;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpString;

public class CmdIncorpMappingDeltas extends CommandDeferWriting {
    
    public CmdIncorpMappingDeltas(Sexp e) {
        super(e);
    }
    
    public Sexp process() {
        Sexp e;
        
        try {
            if (!expr.isSexpList())
                throw new BadCommandException(
                        "expecting (incorp-mapping-deltas (delta1 delta2 ...))");
            SexpList l = expr.toSexpList();
            if (l.size() != 2)
                throw new BadCommandException(
                        "expecting (incorp-mapping-deltas (delta1 delta2 ...))");
            Sexp arg = l.get(1);
            if (!arg.isSexpList())
                throw new BadCommandException(
                        "expecting (incorp-mapping-deltas (delta1 delta2 ...))");
            SexpList deltas = arg.toSexpList();
            e = incorp_mapping_deltas(deltas);
        } catch (Exception ex) {
            // Convert {ex} to SexpString first so we can use its toString()
            // method to escape "s.
            SexpString ex_string = new SexpString(ex.toString());
            e = SexpReader.readFromStringNoex("(:error " + ex_string.toString()
                    + ")");
        }
        
        return e;
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger.getLogger(CmdIncorpMappingDeltas.class);
    
    private static Sexp incorp_mapping_deltas(SexpList rqst)
            throws BadCommandException {
        OSpecHelper.doUpdateTSpecMapping(rqst);
        // FIXME: not sure what the "success" result is
        return new SexpList();
    }
}

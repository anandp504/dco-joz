// The TSpec database.
// FIXME: This is just scaffolding until the design is worked out.

package com.tumri.joz.jozMain;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.SortedSet;

import org.apache.log4j.Logger;

import com.tumri.joz.Query.ConjunctQuery;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

public class TmpTSpecDB implements TSpecDB {
    
    TmpTSpecDB(String tspecs_path) throws FileNotFoundException, IOException,
            BadTSpecException {
        init(tspecs_path);
    }
    
    public String get_default_realm_url() {
        return "http://default-realm"; // FIXME: wip
    }
    
    // Return tspec for {name} or null if not found.
    
    public TSpec get(String name) {
        TSpec ts = _tspec_db.get(name);
        return ts;
    }
    
    // Called after the t-specs have been read in to verify them.
    
    public void materialize() {
        long start = System.currentTimeMillis();
        ProductDB pdb = ProductDB.getInstance();
        Collection<TSpec> tspecs = _tspec_db.values();
        for (TSpec tspec : tspecs) {
            Handle ref = pdb.genReference();
            ConjunctQuery cjq = tspec.get_query().getQueries().get(0);
            cjq.setStrict(true);
            cjq.setReference(ref);
            SortedSet<Handle> results = cjq.exec();
            /*
             * boolean validate = true; if (validate) { cjq.clear ();
             * cjq.setScan (true); cjq.setReference (ref); SortedSet<Result>
             * results1 = cjq.exec (); Iterator<Result> iter = results.iterator
             * (); Iterator<Result> iter1 = results1.iterator (); boolean
             * hasNext = iter.hasNext (); boolean hasNext1 = iter1.hasNext ();
             * Assert.assertEquals (hasNext, hasNext1); while (hasNext &&
             * hasNext1) { Assert.assertEquals (iter.next ().getOid (),
             * iter1.next ().getOid ()); hasNext = iter.hasNext (); hasNext1 =
             * iter1.hasNext (); Assert.assertEquals (hasNext,hasNext1); } }
             */
        }
        log.info("TSpec materialization time is "
                + (System.currentTimeMillis() - start));
    }
    
    // implementation details -------------------------------------------------
    
    HashMap<String, TSpec> _tspec_db;
    
    private static final TSpec default_t_spec = new TSpec(
            "T-SPEC-http://default-realm/", new Integer(42), new Integer(42),
            new Long(42));
    
    private static Logger log = Logger.getLogger(TmpTSpecDB.class);
    
    private void init(String tspecs_path) throws FileNotFoundException,
            IOException, BadTSpecException {
        log.info("Loading T-Specs from " + tspecs_path);
        load_tspecs_from_lisp_file(tspecs_path);
    }
    
    private void load_tspecs_from_lisp_file(String path)
            throws FileNotFoundException, IOException, BadTSpecException {
        _tspec_db = new HashMap<String, TSpec>();
        
        FileReader fr = new FileReader(path);
        
        try {
            SexpReader sr = new SexpReader(fr);
            Sexp s;
            int count = 0;
            
            while ((s = sr.read()) != null) {
                if (!s.isSexpList()) {
                    log.error("Bad sexp in tspecs file: " + s.toString());
                    continue;
                }
                SexpList l = s.toSexpList();
                Iterator<Sexp> iter = l.iterator();
                if (!iter.hasNext()) {
                    log.error("Bad t-spec entry: " + s.toString());
                    continue;
                }
                Sexp t = iter.next();
                if (!t.isSexpSymbol()) {
                    log.error("Bad t-spec entry: " + s.toString());
                    continue;
                }
                SexpSymbol cmd = t.toSexpSymbol();
                if (cmd.equalsStringIgnoreCase("in-package"))
                    continue; // ignore
                if (cmd.equalsStringIgnoreCase("setf")) {
                    continue; // FIXME: wip, ignore for now
                }
                if (cmd.equalsStringIgnoreCase("t-spec-add")) {
                    TSpec tspec = new TSpec(iter);
                    String name = tspec.get_name();
                    _tspec_db.put(name, tspec); // FIXME: collisions
                    ++count;
                    if (count % 10000 == 0)
                        log.info("Loaded " + count + " entries ...");
                    continue;
                }
                if (cmd.equalsStringIgnoreCase("t-spec-delete")) {
                    continue; // FIXME: wip, ignore for now
                }
                log.error("Bad t-spec entry, unknown request: " + s.toString());
                // FIXME: throw exception?
            }
            
            if (count > 0) {
                log.info("Loaded " + count + " t-specs.");
            }
        } catch (IOException e) {
            throw (e);
        } catch (Exception e) {
            throw new BadTSpecException(e.getMessage());
        } finally {
            fr.close();
        }
    }
}

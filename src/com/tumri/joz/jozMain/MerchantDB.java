// The merchant database.
// FIXME: wip

package com.tumri.joz.jozMain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Reader;
import java.io.FileReader;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;

public class MerchantDB
{
    MerchantDB (String attributes_and_metadata_path,
		String tabulated_search_results_path)
    {
	init (attributes_and_metadata_path,
	      tabulated_search_results_path);
    }

    public Sexp
    get_attributes_and_metadata ()
    {
	return _attributes_and_metadata;
    }

    public Sexp
    get_tabulated_search_results ()
    {
	return _tabulated_search_results;
    }

    // implementation details -------------------------------------------------

    // Content of soz's inputs/MD/attributes-and-metadata.lisp.
    private Sexp _attributes_and_metadata = null;

    // Content of soz's inputs/MD/attributes-and-metadata.lisp.
    private Sexp _tabulated_search_results = null;

    private static Logger log = Logger.getLogger (MerchantDB.class);

    private void
    init (String attributes_and_metadata_path,
	  String tabulated_search_results_path)
    {
	log.info ("Loading merchant data from "
		  + attributes_and_metadata_path);

	Sexp e = load_sexp_from_file (attributes_and_metadata_path);
	_attributes_and_metadata = e;

	log.info ("Loading tabulated search results from "
		  + tabulated_search_results_path);

	e = load_sexp_from_file (tabulated_search_results_path);
	_tabulated_search_results = e;
    }

    private static Sexp
    load_sexp_from_file (String path)
    {
	Sexp expr = null;

	try
	{
	    expr = SexpReader.readFromFile (path);

	    if (expr == null)
		log.info ("empty attributes and metadata file"); // FIXME
	}
	catch (FileNotFoundException e)
	{
	    log.info (e.toString ()); // FIXME
	}
	catch (IOException e)
	{
	    log.info (e.toString ()); // FIXME
	}
	catch (BadSexpException e)
	{
	    log.info (e.toString ()); // FIXME
	}

	return expr;
    }
}

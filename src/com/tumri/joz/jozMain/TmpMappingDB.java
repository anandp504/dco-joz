// The mapping database.
// FIXME: This is just scaffolding until the design is worked out.

package com.tumri.joz.jozMain;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.LineNumberReader;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.utils.sexp.*;
import com.tumri.utils.strings.EString;

public class TmpMappingDB implements MappingDB
{
    TmpMappingDB (String mapping_path)
	throws FileNotFoundException, IOException, BadMappingDataException
    {
	init (mapping_path);
    }

    public MappingObjList
    get_url_t_specs (String url)
    {
	return _url_db.get (url);
    }

    public MappingObjList
    get_url_t_specs (JozURI uri)
    {
	return _url_db.get (uri.get_orig_string ()); // FIXME
    }

    public MappingObjList
    get_theme_t_specs (String theme)
    {
	return _theme_db.get (theme);
    }

    public MappingObjList
    get_store_id_t_specs (String store_id)
    {
	return _store_id_db.get (store_id);
    }

    public void
    add (String kind, String domain, String t_spec_name,
	 Float weight, Long mod_time)
    {
	float fweight = weight.floatValue ();
	long modified = mod_time.longValue ();

	// NOTE: :realm is actually a URL mapping
	if (kind.equalsIgnoreCase (":realm"))
	{
	    MappingObjList mol = _url_db.get (domain);
	    if (mol == null)
	    {
		mol = new MappingObjList ();
		_url_db.put (domain, mol);
	    }
	    mol.add (new MappingObj (MappingObj.MappingType.URL,
				     fweight, modified, t_spec_name));
	}
	else if (kind.equalsIgnoreCase (":theme"))
	{
	    MappingObjList mol = _theme_db.get (domain);
	    if (mol == null)
	    {
		mol = new MappingObjList ();
		_theme_db.put (domain, mol);
	    }
	    mol.add (new MappingObj (MappingObj.MappingType.THEME,
				     fweight, modified, t_spec_name));
	}
	else if (kind.equalsIgnoreCase (":store-id"))
	{
	    MappingObjList mol = _store_id_db.get (domain);
	    if (mol == null)
	    {
		mol = new MappingObjList ();
		_store_id_db.put (domain, mol);
	    }
	    mol.add (new MappingObj (MappingObj.MappingType.STORE_ID,
				     fweight, modified, t_spec_name));
	}
    }

    public void
    delete (String kind, String domain, String t_spec_name, Long mod_time)
    {
	MappingObjList mol;

	// NOTE: :realm is actually a URL mapping
	if (kind.equalsIgnoreCase (":realm"))
	{
	    mol = _url_db.get (domain);
	}
	else if (kind.equalsIgnoreCase (":theme"))
	{
	    mol = _theme_db.get (domain);
	}
	else if (kind.equalsIgnoreCase (":store-id"))
	{
	    mol = _store_id_db.get (domain);
	}
	else
	{
	    return; // ??? ignore
	}

	if (mol == null)
	    return;

	mol.delete (t_spec_name, mod_time);
    }

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (TmpMappingDB.class);

    HashMap<String, MappingObjList> _url_db;
    HashMap<String, MappingObjList> _theme_db;
    HashMap<String, MappingObjList> _store_id_db;

    private void
    init (String mapping_path)
	throws FileNotFoundException, IOException, BadMappingDataException
    {
	log.info ("Loading mapping products from " + mapping_path);
	load_mappings_from_lisp_file (mapping_path);
    }

    private void
    load_mappings_from_lisp_file (String mapping_path)
	throws FileNotFoundException, IOException, BadMappingDataException
    {
	_url_db = new HashMap<String, MappingObjList> ();
	_theme_db = new HashMap<String, MappingObjList> ();
	_store_id_db = new HashMap<String, MappingObjList> ();

	// ??? At present the entire file is one sexp.

	Sexp file_sexp = null;
	try
	{
	    file_sexp = SexpReader.readFromFile (mapping_path);
	}
	catch (BadSexpException e)
	{
	    throw new BadMappingDataException ("error reading sexp");
	}
	if (! file_sexp.isSexpList ())
	    throw new BadMappingDataException ("expected list");
	SexpList file_list = file_sexp.toSexpList ();
	Iterator<Sexp> iter = file_list.iterator ();
	while (iter.hasNext ())
	{
	    Sexp e = iter.next ();
	    if (! e.isSexpList ())
	    {
		log.error ("Bad mapping entry: " + e.toString ());
		continue;
	    }
	    SexpList l = e.toSexpList ();
	    if (l.size () != 5)
	    {
		log.error ("Bad mapping entry: " + e.toString ());
		continue;
	    }

	    Sexp tmp_kind = l.get (0);
	    Sexp tmp_domain = l.get (1);
	    Sexp tmp_t_spec = l.get (2);
	    Sexp tmp_weight = l.get (3);
	    Sexp tmp_modified = l.get (4);
	    if (! tmp_kind.isSexpKeyword ()
		|| ! tmp_domain.isSexpString ()
		|| ! tmp_t_spec.isSexpSymbol ()
		|| (! tmp_weight.isSexpInteger ()
		    && ! tmp_weight.isSexpReal ())
		|| (! tmp_modified.isSexpString ()
		    && ! tmp_modified.isSexpInteger ()))
	    {
		log.error ("Bad mapping entry: " + e.toString ());
		continue;
	    }
	    String kind = tmp_kind.toSexpKeyword ().toString ();
	    String domain = tmp_domain.toStringValue (); // ???
	    String t_spec = tmp_t_spec.toStringValue (); // ???
	    float weight;
	    if (tmp_weight.isSexpReal ())
		weight = new Float (tmp_weight.toSexpReal ().toNativeReal64 ()).floatValue ();
	    else
		weight = (float) tmp_weight.toSexpInteger ().toNativeInteger32 ();
	    long modified;
	    if (tmp_modified.isSexpString ())
		modified = date_string_to_int (tmp_modified.toStringValue ());
	    else
		modified = tmp_modified.toSexpInteger ().toNativeInteger64 ();

	    add (kind, domain, t_spec, weight, modified);
	}
    }

    private static long
    date_string_to_int (String date)
    {
	return 42; // FIXME
    }
}

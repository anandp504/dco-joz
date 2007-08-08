// Container for the feature list passed back in get-ad-data requests.
// NOTE: There are no setter methods on purpose.

package com.tumri.joz.jozMain;

import com.tumri.utils.sexp.*;

public class Features
{
    public Features (Integer seed /*FIXME: wip*/ /*, FIXME: others?*/)
    {
	_seed = seed;
	_joz_version = Props.get_joz_version ();
	_host_name = Props.get_host_name ();
	_mup_version = "666"; // FIXME: wip
    }

    public String get_joz_version () { return _joz_version; }
    public String get_mup_version () { return _mup_version; }
    public String get_host_name () { return _host_name; }
    public Integer get_seed () { return _seed; } // FIXME: wip

    // Convert the feature spec to a list that can be passed back to a client.
    // {elapsed_time} is in nanoseconds.

    public SexpList
    toSexpList (long elapsed_time)
    {
	SexpList flist = new SexpList ();
	SexpList l;
	SexpString s;
	SexpKeyword k;
	SexpInteger i;

	flist.addLast (build_list (":time", format_elapsed_time (elapsed_time)));

	flist.addLast (build_list (":soz-ver", get_joz_version ()));

	flist.addLast (build_list (":mup-ver", get_mup_version ()));

	flist.addLast (build_list (":host", get_host_name ()));

	flist.addLast (build_list (":seed", get_seed ()));

	// FIXME: wip
	l = new SexpList ();
	k = new SexpKeyword (":features");
	l.addLast (k);
	s = new SexpString ("nil");
	l.addLast (s);
	flist.addLast (l);

	return flist;
    }

    // implementation details -------------------------------------------------

    private String _joz_version;
    private String _mup_version;
    private String _host_name;
    private Integer _seed;

    private static SexpList
    build_list (String k, String s)
    {
	SexpList l = new SexpList ();
	SexpKeyword kk = new SexpKeyword (k);
	l.addLast (kk);
	SexpString ss = new SexpString (s);
	l.addLast (ss);
	return l;
    }

    private static SexpList
    build_list (String k, int i)
    {
	SexpList l = new SexpList ();
	SexpKeyword kk = new SexpKeyword (k);
	l.addLast (kk);
	SexpInteger ii = new SexpInteger (i);
	l.addLast (ii);
	return l;
    }

    // Convert time delta {t} (which is in nanoseconds) to a string with
    // units seconds.

    private static String
    format_elapsed_time (long t)
    {
	// FIXME: wip
	double d = (double) t;
	d /= 1e9;
	return String.format ("%,.3f", d);
    }
}

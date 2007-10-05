package com.tumri.joz.jozMain;

import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Iterator;

import com.tumri.utils.sexp.SexpInteger;
import com.tumri.utils.sexp.SexpKeyword;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpString;

/**
 * Container for the feature list passed back in get-ad-data requests.
 * NOTE: There are no setter methods on purpose.
 * @author nipun
 *
 */
public class Features {
    public static final String JOZ_VERSION = "3.0";
    public static final String FEATURE_WIDGET_SEARCH = "SEARCH-IN-WIDGET";
    public static final String FEATURE_MINE_URL_SEARCH = "SEARCH-MINE-URL";
    public static final String FEATURE_SCRIPT_SEARCH = "SEARCH-SCRIPT_KEYWORD";
    
    private String _joz_version;
    private Integer _mup_version;
    private String _host_name;
    private Integer _seed;
    private HashMap<String, String> jozFeaturesMap = null;
    
    public Features(HashMap<String, String> _jozFeaturesMap) {
    	//TODO: Get the Joz version from a properties file, or system parm. Major version + Build/Patch number
    	_joz_version = JOZ_VERSION;
        try {
        	_host_name = java.net.InetAddress.getLocalHost().getHostName();
        } catch (UnknownHostException e) {
            _host_name = "unknown";
        }
        //TODO: Get the MUP version from CAA
        _mup_version = 666;
        //TODO: Check if the seed, ie, the random handle needs to passed back.
        if (_jozFeaturesMap!=null){
        	setJozFeaturesMap(_jozFeaturesMap);
        }
    }
    
    private String get_joz_version() {
        return _joz_version;
    }
    
    private Integer get_mup_version() {
        return _mup_version;
    }
    
    private String get_host_name() {
        return _host_name;
    }
    
    private Integer get_seed() {
        return _seed;
    } 
    
    private void setJozFeaturesMap(HashMap<String, String> jozFeatures) {
    	this.jozFeaturesMap = jozFeatures;
    }
    
    /**
     * Generate Sexp string that needs to be appended to the results
     * @param elapsed_time
     * @return
     */
    public SexpList toSexpList(long elapsed_time) {
        SexpList flist = new SexpList();
        SexpList l;
        SexpString s;
        SexpKeyword k;
        flist.addLast(build_list(":TIME", format_elapsed_time(elapsed_time)));
        flist.addLast(build_list(":SOZ-VER", get_joz_version()));
        flist.addLast(build_list(":MUP-VER", get_mup_version()));
        flist.addLast(build_list(":HOST", get_host_name()));
        l = new SexpList();
        k = new SexpKeyword(":FEATURES");
        l.addLast(k);
        if (jozFeaturesMap==null || jozFeaturesMap.isEmpty()) {
        	s = new SexpString("\"NIL\"");
        } else {
        	Iterator featureKeys = jozFeaturesMap.keySet().iterator();
        	String featureBuiltUpStr = "";
        	while (featureKeys.hasNext()) {
        		String featureKeyStr = (String)featureKeys.next();
        		String featureValStr = (String)jozFeaturesMap.get(featureKeyStr);
        		if (featureKeyStr!=null && !"".equals(featureKeyStr) && featureValStr!=null && !"".equals(featureValStr)) {
        			featureBuiltUpStr = "(" + featureKeyStr + " \"" + featureValStr + "\")";
        		}
        	}
        	s = new SexpString(featureBuiltUpStr);
        }
        l.addLast(s);
        flist.addLast(l);
        return flist;
    }
    
    /**
     * Helper method to build a Sexp list from a String key and string val
     * @param k
     * @param s
     * @return
     */
    private static SexpList build_list(String k, String s) {
        SexpList l = new SexpList();
        SexpKeyword kk = new SexpKeyword(k);
        l.addLast(kk);
        SexpString ss = new SexpString("\"" + s + "\"");
        l.addLast(ss);
        return l;
    }
    
    /**
     * Helper method to build a Sexp list from a String key and int val
     * @param k
     * @param i
     * @return
     */
    private static SexpList build_list(String k, int i) {
        SexpList l = new SexpList();
        SexpKeyword kk = new SexpKeyword(k);
        l.addLast(kk);
        SexpInteger ii = new SexpInteger(i);
        l.addLast(ii);
        return l;
    }
    
    /**
     * Convert time delta {t} (which is in nanoseconds) to a string with units seconds
     */
    private static String format_elapsed_time(long t) {
        // FIXME: wip
        double d = (double) t;
        d /= 1e9;
        return String.format("%,.3f", d);
    }
}

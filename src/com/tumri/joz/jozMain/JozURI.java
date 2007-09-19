// FIXME: wip
// NOTE: We can't subclass URI because it's "final".

package com.tumri.joz.jozMain;

import java.net.URI;
import java.net.URISyntaxException;

import org.apache.log4j.Logger;

public class JozURI {
    
    public JozURI(String uri) {
        try {
            _orig_string = uri;
            _uri = new URI(uri);
        } catch (URISyntaxException e) {
            log.error("Bad URI: " + uri);
            _uri = null;
        }
    }
    
    static public JozURI build_lax_uri(String uri) {
        // Convert http%3A -> http:
        if (uri.startsWith("http%3A"))
            uri = "http:" + uri.substring(7);
        
        return new JozURI(uri);
    }
    
    public URI get_uri() {
        return _uri;
    }
    
    public String get_orig_string() {
        return _orig_string;
    }
    
    // implementation details -------------------------------------------------
    
    protected String _orig_string;
    
    protected URI _uri;
    
    private static Logger log = Logger.getLogger(JozURI.class);
}

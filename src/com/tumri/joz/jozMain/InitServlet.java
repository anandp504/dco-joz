// Initialization servlet

package com.tumri.joz.jozMain;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

public class InitServlet extends HttpServlet {
    
    private static Logger log = null;
    
    static {
        String filename = "../conf/jozLog4j.xml";
        File f = new File(filename);
        if (f.exists()) {
            DOMConfigurator.configure(filename);
        }
        log = Logger.getLogger(InitServlet.class);
        if (!f.exists()) {
            log.error("Log4j configuration file " + filename + " missing.");
        }
    }
    
    public void init() throws ServletException {
        log.info("Initializing joz ...");
        
        try {
            // Initialize this first in case something needs a property.
            String config_dir = getInitParameter("tumri.joz.config.dir");
            String config_file = getInitParameter("tumri.joz.config.file");
            String app_config_file = getInitParameter("tumri.joz.app.config.file");
            Props.init(config_dir, config_file, app_config_file);
            
            JozData.init();
        } catch (Exception e) {
            log.error(e.toString());
            throw new ServletException(e.toString());
        }
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // FIXME
    }
}

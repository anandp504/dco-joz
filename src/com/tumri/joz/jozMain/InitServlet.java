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
    private static final String g_Log4JPropertiesFile = "jozLog4j.xml";
    
    static {
        String fileName = getLog4JConfigFilePath();
    	File f = new File(fileName);
        if (f.exists()) {
            DOMConfigurator.configure(fileName);
        }
        log = Logger.getLogger(InitServlet.class);
        if (!f.exists()) {
            log.error("Log4j configuration file " + g_Log4JPropertiesFile + " missing.");
        }
    }
    
    public void init() throws ServletException {
        log.info("Initializing joz ...");
        
        try {
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
    
    private static String getLog4JConfigFilePath() {
    	String log4JFilePath = "";
    	String catalinaBase = System.getProperty("catalina.base");
    	if (catalinaBase != null) {
    		String confFile = catalinaBase + File.separator + "conf" + File.separator + g_Log4JPropertiesFile;
    		if (confFile != null){
    			log4JFilePath = confFile;
    		} else {
    			System.out.println("Could not locate the resource file "+g_Log4JPropertiesFile + " in tomcat as catalina.base is not defined. Will try ../conf");
    			log4JFilePath = "../conf/" +g_Log4JPropertiesFile ;
    		}
    	}
    	return log4JFilePath;
    }
 
}
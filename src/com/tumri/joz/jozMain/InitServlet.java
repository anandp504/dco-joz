// Initialization servlet

package com.tumri.joz.jozMain;

import com.tumri.joz.campaign.CMAContentPoller;
import com.tumri.joz.campaign.wm.loader.WMContentPoller;
import com.tumri.joz.server.JozBaseServer;
import com.tumri.joz.server.JozNioServer;
import com.tumri.joz.server.JozServer;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.Polling;
import com.tumri.utils.stats.PerformanceStats;
import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;

public class InitServlet extends HttpServlet {

    private static Logger log = null;
    private static final String g_Log4JPropertiesFile = "jozLog4j.xml";
    private Thread jozServerThread = null;
    private JozBaseServer jozServer = null;


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

            int poolSize = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.poolSize"));
            int port = Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.port"));
            int timeout= Integer.parseInt(AppProperties.getInstance().getProperty("tcpServer.timeout"));
            String queryHandlers = AppProperties.getInstance().getProperty("tcpServer.queryHandlers");
            log.info("Starting joz server");
            log.info("poolSize = "+poolSize);
            log.info("port = "+port);
            log.info("queryHandlers = "+queryHandlers);
            log.info("timeout = "+timeout);

            if (AppProperties.getInstance().isNioEnabled()) {
                log.info("Nio mode enabled");
                jozServer = new JozNioServer(port,poolSize,queryHandlers);
            } else {
                jozServer = new JozServer(poolSize,port,timeout,queryHandlers);
            }
            jozServerThread = new Thread("JoZServerThread") {
                public void run() {
                    jozServer.start();
                }
            };

            jozServerThread.start();
            log.info("Started joz server .....");
        } catch (Exception e) {
            log.fatal("Fatal exception caught on startup of server", e);
            throw new ServletException(e.toString());
        }
    }
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        // FIXME
    }
    
    @Override
    public void destroy() {
        Polling.getInstance().shutdown();  
        CMAContentPoller.getInstance().shutdown();
        WMContentPoller.getInstance().shutdown();
        ListingProviderFactory.shutdown();
        if  (jozServerThread!=null) {
        	jozServerThread.interrupt();
        }
        jozServer.stop();
        PerformanceStats.getInstance().destroy();
    }
    
    private static String getLog4JConfigFilePath() {
    	String log4JFilePath;
    	String catalinaBase = System.getProperty("catalina.base");
    	if (catalinaBase != null) {
    		log4JFilePath = catalinaBase + File.separator + "conf" + File.separator + g_Log4JPropertiesFile;
        } else {
            System.out.println("Could not locate the resource file "+g_Log4JPropertiesFile + " in tomcat as catalina.base is not defined. Will try ../conf");
            log4JFilePath = "../conf/" +g_Log4JPropertiesFile ;
        }
    	return log4JFilePath;
    }
 
}
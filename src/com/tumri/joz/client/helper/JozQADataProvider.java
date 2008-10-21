package com.tumri.joz.client.helper;

import com.tumri.joz.server.domain.JozQARequest;
import com.tumri.joz.server.domain.JozQAResponseWrapper;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.apache.log4j.Logger;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by IntelliJ IDEA.
 * User: scbraun
 * Date: Oct 10, 2008
 * Time: 1:56:25 PM
 */
public class JozQADataProvider {
	private static Logger log = Logger.getLogger(JozQADataProvider.class);

    public JozQADataProvider() {
    }

	public JozQAResponseWrapper processRequest(JozQARequest request) {
    	JozQAResponseWrapper responseWrapper =null;
    	boolean bDone = false;
        while (!bDone) {
            //Get connection from Pool
            TcpSocketConnectionPool.SocketStream s = TcpSocketConnectionPool.getInstance().getConnection();
            if (s==null) {
                log.error("Cannot connect to TCP server, aborting request");
                bDone = true;
                break;
            }
            log.debug("Socket being used is : " + s.s.getLocalPort());

            boolean bError = false;
            try {
                //Open the outputstream and flush out the request
                ObjectOutputStream oos = s.getOos();
                oos.writeUnshared(request);
                oos.flush();
                oos.reset();

                //Next read the response
                ObjectInputStream ois = s.getOis();
                Object o = null;
                try {
                    o = ois.readUnshared();
                } catch(ClassNotFoundException cfe) {
                    log.error("Class not found exception caught");
                }
                if (o!=null && o instanceof JozQAResponseWrapper) {
                    responseWrapper = (JozQAResponseWrapper)o;
                } else {
                    log.error("Error in response.");
                }
                bDone = true;

            } catch (Throwable t) {
                log.error("Process request failed", t);
                bError = true;
            } finally {
                s.setToBeClosed(bError);
                //Release connection to pool
                TcpSocketConnectionPool.getInstance().freeConnection(s);
            }

        }
        return responseWrapper;
    }
}

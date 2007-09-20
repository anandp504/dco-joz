// Joz servlet

package com.tumri.joz.jozMain;

import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpIFASLWriter;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;

public class JozServlet extends HttpServlet {
    
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doService(request, response);
    }
    
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException, ServletException {
        doService(request, response);
    }
    
    protected void doService(HttpServletRequest request,
            HttpServletResponse response) throws IOException, ServletException {
        String query = request.getQueryString();
        
        response.setContentType("text/html");
        ServletOutputStream out = response.getOutputStream();
        
        try {
            query = URLDecoder.decode(query,"UTF-8");
            log.info("Query= " + query);
            
            long start_time = System.nanoTime();
            
            Command cmd = Command.parse(query);
            
            // Allow commands to write the result out themselves.
            // For large results it doesn't make much sense to build up a
            // temp result in one format format only to convert it to a
            // different format and only then write out the result.
            
            if (cmd.write_own_results_p()) {
                cmd.process_and_write(out);
            } else {
                Sexp result = cmd.process();
                boolean uppercase_syms = cmd.need_uppercase_syms();
                SexpIFASLWriter.writeOne(out, result, uppercase_syms);
                // FIXME: Need option to not print entire s-expression,
                // they can be pretty large.
                /*
                 * log.info ("Result: " + result);
                 */
            }
            
            long end_time = System.nanoTime();
            
            log.info("Response time: " + ((end_time - start_time) / 1000.0)
                    + " usecs");
        } catch (Exception e) {
            log.info("Bad command: ", e);
            // ??? Protocol apparently says to return nothing. True?
        }
    }
    
    // implementation details -------------------------------------------------
    
    private static Logger log = Logger.getLogger(JozServlet.class);
}

package com.tumri.joz.monitor;

import java.io.StringReader;
import java.io.StringReader;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import com.tumri.joz.products.ProductDB;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpIFASLReader;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.joz.jozMain.CmdGetAdData;

/**
 * Monitor for JoZ ProductQuery component
 *
 * @author vijay
 */
public class ProductQueryMonitor extends ComponentMonitor
{

    private static Logger log = Logger.getLogger(ProductQueryMonitor.class);

    public ProductQueryMonitor()
    {
       super("getaddata", new ProductQueryMonitorStatus("getaddata"));
    }

    public MonitorStatus getStatus(String arg)
    {
        byte[] bytes;
        List<Map<String, String>>  results;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Sexp sexp = createSexp(arg);
            ((ProductQueryMonitorStatus)status.getStatus()).setProductQuery(sexp.toString());
            CmdGetAdData cmd = new CmdGetAdData(sexp);
            cmd.process_and_write(out);
            bytes = out.toByteArray();
            ByteArrayInputStream in = new ByteArrayInputStream(bytes);
            SexpIFASLReader ifaslreader = new SexpIFASLReader(in);
            Sexp sexpr = ifaslreader.read();
            results = getProductData(sexpr);
        }
        catch(Exception ex) {
          log.error("Error reading sexpression:  "+ex.getMessage());
          results = null;
        }

        ((ProductQueryMonitorStatus)status.getStatus()).setProducts(results);
        return status;
    }

    /**
     * Returns the status for a get-ad-data string
     * @param arg
     * @return
     */
    public MonitorStatus getStatusGetAdData(String arg)
    {
        byte[] bytes;
        List<Map<String, String>>  results = null;

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Sexp sexp = null;
            SexpReader r = new SexpReader(new StringReader(arg));
            try {
               sexp = r.read();
            }
            catch(Exception ex) {
               log.error("Error creating sexpression:  "+ex.getMessage());
               return null;
            }
            if (sexp != null) {
                ((ProductQueryMonitorStatus)status.getStatus()).setProductQuery(sexp.toString());
                CmdGetAdData cmd = new CmdGetAdData(sexp);
                cmd.process_and_write(out);
                bytes = out.toByteArray();
                ByteArrayInputStream in = new ByteArrayInputStream(bytes);
                SexpIFASLReader ifaslreader = new SexpIFASLReader(in);
                Sexp sexpr = ifaslreader.read();
                results = getProductData(sexpr);
            }
        }
        catch(Exception ex) {
          log.error("Error reading sexpression:  "+ex.getMessage());
          results = null;
        }

        ((ProductQueryMonitorStatus)status.getStatus()).setProducts(results);
        return status;
    }

    private List<Map<String, String>> getProductData(Sexp sexpr) throws JozMonitorException
    {
        List<Map<String, String>> products=new ArrayList<Map<String,String>>();
        if (!sexpr.isSexpList())
           throw new JozMonitorException("Expected sexp is not a list.");

        ((ProductQueryMonitorStatus)status.getStatus()).setProductRawData(sexpr.toString());
        //get Product Data
        Sexp tmp=((SexpList)sexpr).get(1);
        if (tmp == null)
            throw new JozMonitorException("Products not found. Null encountered.");
        if (!tmp.isSexpList())
           throw new JozMonitorException("Expected sexp is not a list.");
        tmp=((SexpList)tmp).get(1);
        //tmp should be a SexpString.
        if (!tmp.isSexpString())
           throw new JozMonitorException("Expected sexp is not a string.");

        //strip off the quotes around the entire JSON array string
        String jsonStr = tmp.toString();
        if (jsonStr.startsWith("\""))
            jsonStr = jsonStr.substring(1, jsonStr.length()-1);

		//Replace "\"" by escaping it. Each character preceded by two(2) forward slashes.
      	jsonStr = jsonStr.replaceAll("\\\\\"","\"");

        try {
            JSONArray jsonArray = new JSONArray(jsonStr);
            for (int i=0; i<jsonArray.length(); i++) {
                Map<String, String> attributes = new HashMap<String, String>();
                JSONObject jsonObj  = (JSONObject)jsonArray.get(i);
                Iterator it = jsonObj.keys();
                String key = null;
                String value = null;
                while (it.hasNext()) {
                    key = (String)it.next();
                    value = (String)jsonObj.get(key);
                    if (key != null)
                        attributes.put(key, value);
                }
                products.add(attributes);
            }
        }
        catch (Exception ex) {
            throw new JozMonitorException("Unexpected Json library error.");
        }

        return products;
    }


    private Sexp createSexp(String tspec)
    {
       String defaultTspec = "TSPEC-http://www.howstuffworks.com";
       if (tspec == null)
          tspec = defaultTspec;
       Sexp e = null;
       String s = "(:get-ad-data :t-spec '|"+tspec+"| :num-products 100)";
       SexpReader r = new SexpReader(new StringReader(s));
       try {
          e = r.read();
       }
       catch(Exception ex) {
          log.error("Error creating sexpression:  "+ex.getMessage());
          e = null;
       }
       return e;
    }
}

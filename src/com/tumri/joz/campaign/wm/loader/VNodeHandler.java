/*
 * ANodeHandler.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.campaign.wm.loader;

// JDK Classes
import java.util.*;
import java.io.*;

// Xerces Classes
import org.xml.sax.*;
import org.apache.xerces.parsers.*;
import org.apache.log4j.Logger;
import org.xml.sax.helpers.DefaultHandler;
import com.tumri.joz.campaign.wm.*;

/**
 * @author: nipun
 * Date: Aug 13, 2009
 * Time: 12:11:20 PM
 */
public class VNodeHandler extends DefaultHandler
{
    private CharArrayWriter text = new CharArrayWriter ();
    private Stack path;
    private Map params;
    private DefaultHandler parent;
    private SAXParser parser;
    int adPodId = 0;
    int vectorId = 0;
    Map<WMIndex.Attribute, Integer> requestMap = new HashMap<WMIndex.Attribute, Integer>();
    List<RecipeWeight> rwList = new ArrayList<RecipeWeight>();
    private static final Logger log = Logger.getLogger(VNodeHandler.class);

    public VNodeHandler(int adPodId, int vectorId, Stack path, Map params,
                           Attributes attributes, SAXParser parser, DefaultHandler parent)  throws SAXException
    {
        this.adPodId = adPodId;
        this.vectorId = vectorId;
        this.path = path;
        this.params = params;
        this.parent = parent;
        this.parser = parser;
        start(attributes);
    }


    public void start (Attributes attributes)  throws SAXException
    {
    }

    public void end () throws SAXException
    {
    }


    public String getText()
    {
        return text.toString().trim();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        if (qName.equals("c")) {
            String type = attributes.getValue("type");
            String val = attributes.getValue("val");
            WMIndex.Attribute kAttr = WMUtils.getAttribute(type);
            if (kAttr!=null && val!=null) {
               Integer id = WMUtils.getDictId(kAttr, val);
               if (id!=null) {
                   requestMap.put(kAttr, id);
               }
            } else {
                log.error("Skipping the request context - invalid/unsupported values " +
                        "for type/value. Type = " + type + ". Value = " + val);
            }
        }
        if (qName.equals("rw")) {
            try {
                Integer recipeId = Integer.parseInt(attributes.getValue("id"));
                Float wt = Float.parseFloat(attributes.getValue("wt"));
                if (recipeId!=null && wt!=null){
                    rwList.add(new RecipeWeight(recipeId, wt));
                }
            } catch (NumberFormatException e) {
                log.error("Skipping recipe weight - RecipeID/Weight are badly formatted");
            }

        }
        text.reset();

    }

    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        if (qName.equals("v"))
        {
            if (!requestMap.isEmpty() && !rwList.isEmpty()) {
                WMHandle h = WMHandleFactory.getInstance().getHandle(vectorId, requestMap, rwList);
                WMDBLoader.updateDb(adPodId, requestMap, h);
            } else {
                log.warn("Skipping vector info for. AdPod = " + adPodId + ". Vector = " + vectorId);
            }
            end();
            path.pop();
            parser.setContentHandler (parent);
        }


    }

    public void characters(char[] ch, int start, int length)
    {
        text.write (ch,start,length);
    }

}
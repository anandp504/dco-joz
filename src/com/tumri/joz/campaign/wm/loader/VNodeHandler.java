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

import java.io.*;
import java.util.*;

// Xerces Classes
import com.tumri.joz.utils.AppProperties;
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
public class VNodeHandler extends DefaultHandler {
    private CharArrayWriter text = new CharArrayWriter();
    private Stack path;
    private Map params;
    private DefaultHandler parent;
    private SAXParser parser;
    int adPodId = 0;
    int vectorId = 0;
    Map<WMAttribute, String> requestMap = new HashMap<WMAttribute, String>();
    List<RecipeWeight> rwList = new ArrayList<RecipeWeight>();
    private static final char MULTI_VALUE_DELIM = AppProperties.getInstance().getMultiValueDelimiter();
    private static final Logger log = Logger.getLogger(VNodeHandler.class);

    public VNodeHandler(int adPodId, int vectorId, Stack path, Map params,
                        Attributes attributes, SAXParser parser, DefaultHandler parent) throws SAXException {
        this.adPodId = adPodId;
        this.vectorId = vectorId;
        this.path = path;
        this.params = params;
        this.parent = parent;
        this.parser = parser;
        start(attributes);
    }


    public void start(Attributes attributes) throws SAXException {
    }

    public void end() throws SAXException {
    }


    public String getText() {
        return text.toString().trim();
    }

    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equals("c")) {
            String type = attributes.getValue("type");
            WMAttribute kAttr = WMUtils.getAttribute(type);
            if (kAttr != null) {
                if (WMUtils.getRangeAttributes().contains(kAttr)) { //this is a range query
                    String min = attributes.getValue("min");
                    String max = attributes.getValue("max");
                    if (min != null && max != null) {
                        try {
                            int minI = Integer.parseInt(min);
                            int maxI = Integer.parseInt(max);
                            if (minI <= maxI) {
                                updateMap(kAttr, WMUtils.getUniqueIntRangeString(min, max));
                            } else {
                                log.error("Skipping the request context - invalid min/max " +
                                        "for type/value. Type = " + type + ". min = " + min + "max = " + max);
                            }
                        } catch (NumberFormatException e) {
                            log.error("Skipping the request context - invalid/unsupported values " +
                                    "for type/value. Type = " + type + ". min = " + min + "max = " + max);
                        }

                    } else {
                        log.error("Skipping the request context - invalid/unsupported values " +
                                "for type/value. Type = " + type + ". min = " + min + "max = " + max);
                    }
                } else {
                    String val = attributes.getValue("val");
                    if (val != null) {
                        updateMap(kAttr, val);
                    } else {
                        log.error("Skipping the request context - invalid/unsupported values " +
                                "for type/value. Type = " + type + ". Value = " + val);
                    }
                }
            }
        }
        if (qName.equals("rw")) {
            try {
                Integer recipeId = Integer.parseInt(attributes.getValue("id"));
                Float wt = Float.parseFloat(attributes.getValue("wt"));
                if (recipeId != null && wt != null) {
                    rwList.add(new RecipeWeight(recipeId, wt));
                }
            } catch (NumberFormatException e) {
                log.error("Skipping recipe weight - RecipeID/Weight are badly formatted");
            }

        }
        text.reset();

    }

    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equals("v")) {
            if (!requestMap.isEmpty() && !rwList.isEmpty()) {
                List<Map<WMAttribute,  Integer>> explodedMap = explodeRequestMap(requestMap);
                int count = 0;
                for (Map<WMAttribute,  Integer> rMap: explodedMap) {
                    WMDB.WMIndexCache cache = WMDB.getInstance().getWeightDB(adPodId);
                    WMHandle h = cache.getWMHandle((long) vectorId, (long) count);
                    if (h == null) {
                        h = WMHandleFactory.getInstance().getHandle(vectorId, count, rMap, rwList);
                    } else {
                        h.setRecipeList(rwList);
                    }
                    WMDBLoader.updateDb(adPodId, rMap, h);
                    count++;
                }
            } else {
                log.warn("Skipping vector info for. AdPod = " + adPodId + ". Vector = " + vectorId);
            }
            end();
            path.pop();
            parser.setContentHandler(parent);
        }


    }


    private List<Map<WMAttribute,  Integer>> explodeRequestMap(Map<WMAttribute, String> reqMap) {
        //Get the map of att to list of integers
        Map<WMAttribute, List<Integer>> idMap = new HashMap<WMAttribute,  List<Integer>>();
        int count = 0;
        for (WMAttribute attr: reqMap.keySet()) {
            List<String> parsedList = WMUtils.parseValues(reqMap.get(attr));
            for (String val: parsedList) {
                Integer id = WMUtils.getDictId(attr, val);
                List<Integer> idList = idMap.get(attr);
                if (idList==null) {
                    idList = new ArrayList<Integer>();
                }
                idList.add(id);
                idMap.put(attr, idList);
            }
            if (count ==0) {
                count = parsedList.size();
            } else {
                count = count*parsedList.size();
            }
        }
        //Initialize the maps
        List<Map<WMAttribute,  Integer>> listMaps = new ArrayList<Map<WMAttribute,  Integer>>(count);
        //Populate the maps
        //add base empty Map to return list...this will be built upon.
        listMaps.add(new HashMap<WMAttribute, Integer>());
        Set<WMAttribute> keys = reqMap.keySet();
        Map<WMAttribute, Integer> tmpMap = new HashMap<WMAttribute, Integer>();

        for (WMAttribute attr: keys) {
            List<Integer> tmpIds = idMap.get(attr);
            if(tmpIds.size()>1){
                List<Map<WMAttribute, Integer>> tmpList = new ArrayList<Map<WMAttribute, Integer>>();
                for(Map<WMAttribute, Integer> retMap: listMaps){
                    for(Integer id : tmpIds){
                        Map<WMAttribute, Integer> retMapBuilder = new HashMap<WMAttribute, Integer>();
                        retMapBuilder.putAll(retMap);
                        retMapBuilder.putAll(tmpMap);
                        retMapBuilder.put(attr, id);
                        tmpList.add(retMapBuilder);
                    }
                }
                listMaps=tmpList;
                tmpMap.clear();
            } else if(tmpIds.size() == 1) {
                tmpMap.put(attr, tmpIds.get(0));
            }

        }
        for(Map<WMAttribute, Integer> retMap: listMaps){
            retMap.putAll(tmpMap);
        }
        return listMaps;
    }

    public void characters(char[] ch, int start, int length) {
        text.write(ch, start, length);
    }


    private void updateMap(WMAttribute attr, String val) {
        if (val != null && !val.isEmpty()) {
            requestMap.put(attr, val);
        }
    }

}
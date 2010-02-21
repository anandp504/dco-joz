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
import com.tumri.joz.campaign.wm.WMDB;
import com.tumri.joz.campaign.wm.WMHandleFactory;
import com.tumri.joz.campaign.wm.WMHandle;
import com.tumri.joz.products.Handle;

/**
 * @author: nipun
 * Date: Aug 13, 2009
 * Time: 12:11:20 PM
 */
public class ANodeHandler extends DefaultHandler {
	private CharArrayWriter text = new CharArrayWriter();
	private Stack path;
	private Map params;
	private DefaultHandler parent;
	private SAXParser parser;
	int adPodId = 0;
	private static final Logger log = Logger.getLogger(ANodeHandler.class);
	private Set<Integer> vectorInclList = new HashSet<Integer>();

	public ANodeHandler(int adPodId, Stack path, Map params, Attributes attributes, SAXParser parser,
	                    DefaultHandler parent) throws SAXException {
		this.adPodId = adPodId;
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

	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName,
	                         Attributes attributes) throws SAXException {
		if (qName.equals("a")) {
			log.info("Processing Adpod = " + attributes.getValue("id"));
		}
		if (qName.equals("v")) {
			Integer vectorId = null;
			try {
				vectorId = Integer.parseInt(attributes.getValue("id"));
				log.info("Processing Vector id = " + vectorId);
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid Id for the vector - skipping vector node");
			}
			vectorInclList.add(vectorId);
			DefaultHandler handler = new VNodeHandler(adPodId, vectorId, path, params, attributes, parser, this);
			path.push("v");
			parser.setContentHandler(handler);

		}
		text.reset();

	}

	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName) throws SAXException {
		if (qName.equals("a")) {
			WMDB.WMIndexCache cache = WMDB.getInstance().getWeightDB(adPodId);
			if (cache != null) {
				cache.materializeRangeIndices();
				cache.purgeOldKeys(vectorInclList);
				SortedSet<WMHandle> allHandles = WMHandleFactory.getInstance().getHandles();
				cache.addNewHandles(allHandles);
			}
			WMHandleFactory.getInstance().clear();
			end();
			path.pop();
			parser.setContentHandler(parent);
		}
	}

	public void characters(char[] ch, int start, int length) {
		text.write(ch, start, length);
	}

}  

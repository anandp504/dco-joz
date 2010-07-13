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

import com.tumri.joz.campaign.wm.VectorHandle;
import com.tumri.joz.campaign.wm.VectorHandleFactory;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.util.*;

/**
 * Handler for the experience node
 * @author: nipun
 * Date: Aug 13, 2009
 * Time: 12:11:20 PM
 */
public class ENodeHandler extends DefaultHandler {
	private CharArrayWriter text = new CharArrayWriter();
	private Stack path;
	private Map params;
	private DefaultHandler parent;
	private SAXParser parser;
	int expId = 0;
	private static final Logger log = Logger.getLogger(ENodeHandler.class);
    private VectorHandleFactory vhFactory = null;

	public ENodeHandler(int expId,  Stack path, Map params, Attributes attributes, SAXParser parser,
	                    DefaultHandler parent, VectorHandleFactory vf ) throws SAXException {
		this.expId = expId;
		this.path = path;
		this.params = params;
		this.parent = parent;
		this.parser = parser;
        this.vhFactory = vf;
		start(attributes);
	}


	public void start(Attributes attributes) throws SAXException {
	}

	public void end() throws SAXException {
	}


	public String getText() {
		return text.toString().trim();
	}

    @SuppressWarnings("unchecked")
    public void startElement(String uri, String localName, String qName,
	                         Attributes attributes) throws SAXException {
		if (qName.equals("e")) {
			log.debug("Processing Experience = " + attributes.getValue("id"));
		}
		if (qName.equals("v")) {
			Integer vectorId = null;
			try {
				vectorId = Integer.parseInt(attributes.getValue("id"));
				log.debug("Processing Vector id = " + vectorId);
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid Id for the vector - skipping vector node");
			}
			DefaultHandler handler = new VNodeHandler(expId, vectorId, path, params, attributes, parser, this, vhFactory);
			path.push("v");
			parser.setContentHandler(handler);

		}
		text.reset();

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("e")) {
			end();
			path.pop();
			parser.setContentHandler(parent);
		}
	}

	public void characters(char[] ch, int start, int length) {
		text.write(ch, start, length);
	}

}
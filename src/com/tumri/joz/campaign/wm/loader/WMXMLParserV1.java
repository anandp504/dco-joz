/*
 * WMXMLParserV1.java
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

import com.tumri.joz.campaign.wm.*;
import com.tumri.joz.products.Handle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.data.SortedArraySet;
import org.apache.xerces.parsers.SAXParser;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


/**
 * Parser that will read the wm.xml file and populate the DB.
 *
 * @author: nipun
 * Date: Aug 11, 2009
 * Time: 1:58:27 PM
 */
public class WMXMLParserV1 extends DefaultHandler implements WMXMLParser {
	SAXParser parser = new SAXParser();
	Stack path = new Stack();
	Map params = new HashMap();
	File xmlFile = null;
	File schemaFile = null;
	private static final Logger log = Logger.getLogger(WMXMLParserV1.class);
	private static final String XSD_FILE_NAME = "wm_1.xsd";
	private VectorHandleFactory vhFactory = null;
	private ExperienceVectorHandleFactory evhFactory = null;

	public WMXMLParserV1() {
		String schemaFilePath = AppProperties.getInstance().getProperty("com.tumri.campaign.wm.xmlSchemaPath");
		if (schemaFilePath == null) {
			schemaFilePath = "/opt/Tumri/joz/current/tomcat5/conf";
		}
		schemaFile = new File(schemaFilePath + File.separator + XSD_FILE_NAME);
		vhFactory = new VectorHandleFactory();
		evhFactory = new ExperienceVectorHandleFactory();

	}

	private void validate() throws SAXException, IOException {
		// validate the wm file against the schema
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		Schema schema = sf.newSchema(schemaFile);
		Validator validator = schema.newValidator();
		validator.validate(new StreamSource(xmlFile));
	}

	public void process(String xmlFileStr) throws WMLoaderException {
		xmlFile = new File(xmlFileStr);
		process();
	}

	public void finalize() {
		try {
			super.finalize();
		} catch (Throwable throwable) {
			log.error("Exception caught on finalize");
		}
	}

	public void process() throws WMLoaderException {
		try {
			validate();
			FileInputStream fis = new FileInputStream(xmlFile);
			parse(fis);

		} catch (Throwable t) {
			throw new WMLoaderException(t);
		}
	}

	public void startDocument() throws SAXException {
		log.info("Starting processing the document");
	}

	public void endDocument() throws SAXException {
		//Do all the post processing here
		VectorDB.getInstance().materializeRangeIndices();
		SortedSet<VectorHandle> allHandles = vhFactory.getCurrHandles();
		VectorDB.getInstance().addOpsNewHandles(allHandles);
		ExperienceVectorDB.getInstance().materializeRangeIndices();
		SortedSet<VectorHandle> allHandles2 = evhFactory.getCurrHandles();
		ExperienceVectorDB.getInstance().addOpsNewHandles(allHandles2);
		log.info("Finished processing the document");
	}

	@SuppressWarnings("unchecked")
	public void startElement(java.lang.String uri, java.lang.String localName, java.lang.String qName,
	                         Attributes attributes) throws SAXException {
		if (qName.equals("a")) {
			Integer adPodId = null;
			try {
				adPodId = Integer.parseInt(attributes.getValue("id"));
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid Id for the adpod - skipping adpod node");
			}

			log.info("Now processing adpod id : " + adPodId);
			DefaultHandler handler = new ANodeHandler(adPodId, path, params, attributes, parser, this, vhFactory, evhFactory);
			path.push("a");
			parser.setContentHandler(handler);
		}
		if (qName.equals("e")) {
			Integer expId = null;
			try {
				expId = Integer.parseInt(attributes.getValue("id"));
			} catch (NumberFormatException e) {
				throw new SAXException("Invalid Id for the experience - skipping experience node");
			}

			log.info("Now processing exp id : " + expId);
			DefaultHandler handler = new ENodeHandler(expId, path, params, attributes, parser, this, vhFactory);
			path.push("e");
			parser.setContentHandler(handler);
		}

	}

	public void endElement(java.lang.String uri, java.lang.String localName, java.lang.String qName)
			throws SAXException {
	}

	public Map parse(InputStream is) throws SAXException, IOException {
		parser.setContentHandler(this);
		parser.parse(new InputSource(is));

		return params;
	}

	@Test
	public void testParsing() {
		try {
			WMXMLParser parser = new WMXMLParserV1();
			parser.process("/Users/nipun/ws/depot/Tumri/tas/joz/test/data/cof/wm-test.xml");
			//Query the DB and make sure stuff is there
		} catch (WMLoaderException e) {
			e.printStackTrace();
			Assert.fail("Something screwed up");
		}
	}

}


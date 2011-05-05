package com.tumri.joz.campaign.wm.loader;

import com.tumri.cma.domain.*;
import com.tumri.cma.rules.CreativeSet;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.wm.*;
import com.tumri.joz.rules.ListingClause;
import com.tumri.joz.rules.ListingClauseUtils;
import com.tumri.utils.Pair;
import com.tumri.utils.data.SortedBag;
import com.tumri.utils.data.SortedListBag;
import org.apache.log4j.Logger;
import org.apache.xerces.parsers.SAXParser;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.CharArrayWriter;
import java.util.*;

/**
 * User: scbraun
 * Date: Apr 27, 2011
 * Time: 1:21:40 PM
 */
public class EVNodeHandler extends DefaultHandler {

	private CharArrayWriter text = new CharArrayWriter();
	private Stack path;
	private Map params;
	private DefaultHandler parent;
	private SAXParser parser;
	int adPodId = 0;
	int vectorId = 0;
	Map<VectorAttribute, String> requestMap = new HashMap<VectorAttribute, String>();
	SortedBag<Pair<Integer, Double>> optRules = new SortedListBag<Pair<Integer, Double>>();
	private ExperienceVectorHandleFactory vhFactory = null;
	private static final Logger log = Logger.getLogger(VNodeHandler.class);

	public EVNodeHandler(int adPodId, int vectorId, Stack path, Map params,
	                     Attributes attributes, SAXParser parser, DefaultHandler parent,
	                     ExperienceVectorHandleFactory vF) throws SAXException {
		this.adPodId = adPodId;
		this.vectorId = vectorId;
		this.path = path;
		this.params = params;
		this.parent = parent;
		this.parser = parser;
		this.vhFactory = vF;
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
			VectorAttribute kAttr = VectorUtils.getAttribute(type);
			if (kAttr != null) {
				if (VectorUtils.getRangeAttributes().contains(kAttr)) { //this is a range query
					String min = attributes.getValue("min");
					String max = attributes.getValue("max");
					if (min != null && max != null) {
						try {
							int minI = Integer.parseInt(min);
							int maxI = Integer.parseInt(max);
							if (minI <= maxI) {
								updateMap(kAttr, VectorUtils.getUniqueIntRangeString(min, max));
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
		if (qName.equals("cs")) {
			try {
				Integer expId = Integer.parseInt(attributes.getValue("id"));
				Double wt = Double.parseDouble(attributes.getValue("wt"));
				if (expId != null && wt != null) {
					Pair<Integer, Double> rulePair = new Pair<Integer, Double>();
					rulePair.setFirst(expId);
					rulePair.setSecond(wt);
					optRules.add(rulePair);
				} else {
					log.warn("Skipping creative set : invalid cam/cs/weight");
				}
			} catch (NumberFormatException e) {
				log.warn("Skipping the creative set since weight is invalid : ");
			}
		}
		text.reset();

	}

	public void endElement(String uri, String localName, String qName) throws SAXException {
		if (qName.equals("v")) {
			if (!requestMap.isEmpty() && !optRules.isEmpty()) {
				Map<VectorAttribute, List<Integer>> idMap = explodeRequestMap(requestMap);
				int type = VectorHandle.OPTIMIZATION;
				VectorHandle h = vhFactory.getHandle(adPodId, vectorId, type, idMap, true);
				if (h != null) {
					WMDBLoader.updateDb(adPodId, optRules, idMap, h);
				}
			} else {
				log.warn("Skipping vector info for. AdPod = " + adPodId + ". Vector = " + vectorId);
			}
			end();
			path.pop();
			parser.setContentHandler(parent);
		}

	}


	private Map<VectorAttribute, List<Integer>> explodeRequestMap(Map<VectorAttribute, String> reqMap) {
		//Get the map of att to list of integers
		Map<VectorAttribute, List<Integer>> idMap = new HashMap<VectorAttribute, List<Integer>>();
		int count = 0;
		for (VectorAttribute attr : reqMap.keySet()) {
			List<String> parsedList = VectorUtils.parseValues(reqMap.get(attr));
			for (String val : parsedList) {
				Integer id = VectorUtils.getDictId(attr, val);
				List<Integer> idList = idMap.get(attr);
				if (idList == null) {
					idList = new ArrayList<Integer>();
				}
				idList.add(id);
				idMap.put(attr, idList);
			}
			if (count == 0) {
				count = parsedList.size();
			} else {
				count = count * parsedList.size();
			}
		}
		return idMap;
	}

	public void characters(char[] ch, int start, int length) {
		text.write(ch, start, length);
	}


	private void updateMap(VectorAttribute attr, String val) {
		if (val != null && !val.isEmpty()) {
			requestMap.put(attr, val);
		}
	}

	/**
	 * Construct a creative set from the given recipe id
	 *
	 * @param recipeId
	 * @param wt
	 * @return
	 */
	private CreativeSet getCreativeSet(Integer recipeId, Float wt) {
		return null;
	}


}

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
import com.tumri.cma.domain.*;
import com.tumri.cma.rules.CreativeSet;
import com.tumri.joz.rules.ListingClause;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.rules.ListingClauseUtils;
import com.tumri.utils.Pair;
import com.tumri.utils.data.SortedBag;
import com.tumri.utils.data.SortedListBag;
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
	int expId = 0;
	int vectorId = 0;
	Map<VectorAttribute, String> requestMap = new HashMap<VectorAttribute, String>();
	SortedBag<Pair<CreativeSet, Double>> optRules = new SortedListBag<Pair<CreativeSet, Double>>();
	SortedBag<Pair<ListingClause, Double>> lcRules = new SortedListBag<Pair<ListingClause, Double>>();
	Map<String, Pair<CreativeSet, Double>> recipeRuleMap = new HashMap<String, Pair<CreativeSet, Double>>();
	private VectorHandleFactory vhFactory = null;
	private CAM cam;
	private static final Logger log = Logger.getLogger(VNodeHandler.class);

	public VNodeHandler(int adPodId, int vectorId, Stack path, Map params,
	                    Attributes attributes, SAXParser parser, DefaultHandler parent,
	                    VectorHandleFactory vF) throws SAXException {
		this.adPodId = adPodId;
		this.vectorId = vectorId;
		this.path = path;
		this.params = params;
		this.parent = parent;
		this.parser = parser;
		this.cam = getCAM(adPodId);
		this.vhFactory = vF;
		start(attributes);
	}

	/**
	 * Get teh CAM object from teh CampaignsDB
	 *
	 * @param adpodId
	 * @return
	 */
	private CAM getCAM(int adpodId) {
		AdPod theAdPod = CampaignDB.getInstance().getAdPod(adpodId);
		CAM theCAM = null;

		if (theAdPod != null) {
			List<Recipe> recipes = theAdPod.getRecipes();
			expId = theAdPod.getExperienceId();
			if (recipes != null || expId <= 0) {
				theCAM = CampaignDB.getInstance().getDefaultCAM(adPodId);
				if (recipes != null) {
					for (Recipe r : recipes) {
						if (r.getWeight() > 0.0) {
							CreativeSet oRule = new CreativeSet(theCAM);
							String rid = Integer.toString(r.getId());
							oRule.add(CAMDimensionType.RECIPEID, rid);
							Pair<CreativeSet, Double> rulePair = new Pair<CreativeSet, Double>();
							rulePair.setFirst(oRule);
							rulePair.setSecond(r.getWeight());
							//optRules.add(rulePair);
							recipeRuleMap.put(rid, rulePair);
						}
					}
				}

			} else {
				Experience exp = CampaignDB.getInstance().getExperience(expId);
				if (exp != null) {
					theCAM = exp.getCam();
				}
			}
		} else {
			Experience exp = CampaignDB.getInstance().getExperience(adpodId);
			if (exp != null) {
				expId = exp.getId();
				theCAM = exp.getCam();
			}
		}
		return theCAM;
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
		if (qName.equals("rw")) {
			try {
				Integer recipeId = Integer.parseInt(attributes.getValue("id"));
				Double wt = Double.parseDouble(attributes.getValue("wt"));
				if (recipeId != null && wt != null && cam != null) {
					Pair<CreativeSet, Double> rulePair = recipeRuleMap.get(recipeId.toString());
					if (rulePair != null) {
						//Merge of the default weight is being done to the recipe weights here.
						//TODO: Need to calculate the actual weights and put that in here !! this will remove any ambiguity
						rulePair.setSecond(wt);
						//optRules.add(rulePair);
					} else {
						log.warn("Recipe id in the weight matrix file is invalid : " + recipeId + ". For adpod id = " + adPodId);
					}
				} else {
					log.warn("Skipping recipe weight : invalid cam/recipeid/weight");
				}
			} catch (NumberFormatException e) {
				log.error("Skipping recipe weight - RecipeID/Weight are badly formatted");
			}

		}
		if (qName.equals("cs")) {
			try {
				String rule = attributes.getValue("rule");
				Double wt = Double.parseDouble(attributes.getValue("wt"));
				if (rule != null && wt != null && cam != null) {
					CreativeSet csSet = new CreativeSet(cam, rule);
					Pair<CreativeSet, Double> rulePair = new Pair<CreativeSet, Double>();
					rulePair.setFirst(csSet);
					rulePair.setSecond(wt);
					optRules.add(rulePair);
				} else {
					log.warn("Skipping creative set : invalid cam/cs/weight");
				}
			} catch (NumberFormatException e) {
				log.warn("Skipping the creative set since weight is invalid : ");
			}
		}
		if (qName.equals("lc")) {
			try {
				String clause = attributes.getValue("clause");
				String value = attributes.getValue("val");
				Double wt = Double.parseDouble(attributes.getValue("wt"));
				if (clause != null && wt != null && cam != null) {
					ListingClause lc = new ListingClause(clause, value);
					Pair<ListingClause, Double> lcPair = new Pair<ListingClause, Double>();
					lcPair.setFirst(lc);
					lcPair.setSecond(wt);
					lcRules.add(lcPair);

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
			if (!recipeRuleMap.isEmpty()) {
				//Add to bag
				optRules = new SortedListBag<Pair<CreativeSet, Double>>();
				for (String rid : recipeRuleMap.keySet()) {
					optRules.add(recipeRuleMap.get(rid));
				}
			}
			SortedBag<Pair<ListingClause, Double>> validRules = ListingClauseUtils.validateListingClauses(lcRules);
			if (!requestMap.isEmpty() && (!optRules.isEmpty() || !validRules.isEmpty())) {
				Map<VectorAttribute, List<Integer>> idMap = explodeRequestMap(requestMap);
				int type = VectorHandle.OPTIMIZATION;
				if (vectorId == 1 && (idMap.size() == 1 && (idMap.containsKey(VectorAttribute.kExpId)) || idMap.containsKey(VectorAttribute.kAdpodId))) {
					//This is a default handle
					type = VectorHandle.DEFAULT;
				}
				VectorHandle h = vhFactory.getHandle(adPodId, vectorId, type, idMap, true);
				if (h != null) {
					if (expId <= 0) {
						WMDBLoader.updateDb(-1, adPodId, optRules, validRules, idMap, h);
					} else {
						WMDBLoader.updateDb(adPodId, -1, optRules, validRules, idMap, h);
					}
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
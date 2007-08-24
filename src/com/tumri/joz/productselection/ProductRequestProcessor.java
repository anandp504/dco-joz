package com.tumri.joz.productselection;

import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.Query.AttributeQuery;
import com.tumri.joz.Query.CNFQuery;
import com.tumri.joz.Query.KeywordQuery;
import com.tumri.joz.Query.ProductSetIntersector;
import com.tumri.joz.Query.SimpleQuery;
import com.tumri.joz.campaign.CampaignDataCache;
import com.tumri.joz.index.DictionaryManager;
import com.tumri.joz.index.SortedArraySet;
import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.MUPProductObj;
import com.tumri.joz.jozMain.SelectedProduct;
import com.tumri.joz.jozMain.Enums.MaybeBoolean;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.IProduct;
import com.tumri.joz.products.Product;
import com.tumri.joz.products.ProductDB;
import com.tumri.joz.ranks.AttributeWeights;
import com.tumri.joz.targeting.TSpecTargetingHelper;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * Processes the product selection request, and creates the result set
 * @author nipun
 *
 */
public class ProductRequestProcessor {

	private ProductSetIntersector m_prodSetIntersector = null;
	private boolean m_SearchTSpecOnly = true;
	private static Logger log = Logger.getLogger (ProductRequestProcessor.class);
	private Integer m_NumProducts = null;
	private Integer m_currentPage = null;
	private Integer m_pageSize = null;
	private boolean m_scavengeUrls = false;
	
	/**
	 * Default constructor
	 *
	 */
	public ProductRequestProcessor() {
	}
	
	
	@SuppressWarnings("unchecked")
	/**
	 * Select the Campaign --> AdPod --> OSpec based on the request parm, and process the query
	 * @param request
	 * @return
	 */
	public SortedSet<Handle> processRequest(AdDataRequest request) {
		SortedSet<Handle> rResult = null;
		CNFQuery _tSpecQuery = null;
		
		m_NumProducts = request.get_num_products ();
		if (m_NumProducts!=null) {
			int numProducts = m_NumProducts.intValue();

			if (numProducts < 12)
				numProducts = 12;
			if (numProducts > 100)
				numProducts = 100;
			m_NumProducts = new Integer(numProducts);
		}
		
		m_currentPage = request.get_which_row();
		m_pageSize = request.get_row_size();
		
		//if row-size and which-row are non-nil and integers then deterministically return a row/page of results:
		boolean revertToDefaultRealm = (request.get_revert_to_default_realm()!=null)?request.get_revert_to_default_realm().booleanValue():false;
		String tSpecName = request.get_t_spec();
		OSpec oSpec = null;
		if ((tSpecName!=null) && (!"".equals(tSpecName))) {
			//Locate TSpec query object
			_tSpecQuery = CampaignDataCache.getInstance().getCNFQuery(tSpecName);
			oSpec = CampaignDataCache.getInstance().getOSpec(tSpecName);
		} else {
			//Target campaign and select TSpec
			String oSpecName = TSpecTargetingHelper.doTargeting(request);
			if (oSpecName != null) {
				_tSpecQuery = CampaignDataCache.getInstance().getCNFQuery(oSpecName);
			}
			oSpec = CampaignDataCache.getInstance().getOSpec(oSpecName);
		}

		MaybeBoolean mMineUrls = request.get_mine_pub_url_p();

		//Randomize results only when there is no keyword search, and there is no pagination
		Handle ref = null;
		if (((mMineUrls == MaybeBoolean.FALSE) && (request.get_keywords() ==null) && (request.get_script_keywords() ==null) && !hasKeywords(oSpec)) || ((m_currentPage==null) && (m_pageSize==null))) {
			ref = ProductDB.getInstance().genReference ();
		}
		m_prodSetIntersector = new ProductSetIntersector(ref);

		if (mMineUrls == MaybeBoolean.TRUE) {
			m_scavengeUrls = true;
		}
		MaybeBoolean mAllowTooFewProducts = request.get_allow_too_few_products();

		if ((mAllowTooFewProducts == MaybeBoolean.FALSE)||(!revertToDefaultRealm)) {
			m_prodSetIntersector.setStrict(true);
		} else {
			m_prodSetIntersector.setStrict(false);
		}
		
		if (_tSpecQuery != null) {
			//See if there is a script Keyword
			String scriptKeywords = request.get_script_keywords();
			if ((scriptKeywords!=null)&&(!"".equals(scriptKeywords))) {
				//TODO get the tspec object and get the flag value - after Bhupen adds the flag to OSpec
				m_SearchTSpecOnly = true;
			}
			rResult = doProductSelection(request, _tSpecQuery);
			
			//Do Outer Disjunction
			List<Handle> disjunctedProds = getIncludedProducts(oSpec);
			
			if (disjunctedProds!=null){
				//Append to the top of the results
				disjunctedProds.addAll(rResult);
				SortedSet<Handle> sortedResult = new SortedArraySet<Handle>(ProductDB.getInstance().genReference());
				sortedResult.addAll(disjunctedProds);
				rResult = sortedResult;
			}
			
			//Cull the result to get the right page
			if ((rResult!=null) && (m_NumProducts!=null)){
				ArrayList<Handle> results = new ArrayList<Handle>();
				//Cull the results further by num products
				int i = 0;
				for (Handle handle : rResult) {
					if (i<m_NumProducts.intValue()){
						results.add(handle);
					} else {
						break;
					}
					i++;
				}
				//sort by the score
				rResult = new SortedArraySet(results);
			} 
			
		} else {
			//This shouldnt happen since we always will get back the TSpec out of targeting
			throw new RuntimeException("Could not locate the TSpec to use for the given request");
		}
		
		
		return rResult;
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Process the request and perform the product selection
	 * @param request
	 * @param tSpecQuery
	 * @return
	 */
	private SortedSet<Handle> doProductSelection(AdDataRequest request, CNFQuery _tSpecQuery) {
		SortedSet<Handle> qResult = null;

		//Keywords
		String requestKeyWords = request.get_keywords();
		
		KeywordQuery rKwQuery = null;
		KeywordQuery sKwQuery = null;
		
		if ((requestKeyWords!=null)&&(!"".equals(requestKeyWords))) {
			if ((m_pageSize!=null) && (m_currentPage !=null)) {
				rKwQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue() );
			}
			rKwQuery = new KeywordQuery(requestKeyWords);
			qResult = rKwQuery.exec();
			return qResult;
		}

		String scriptKeywords = request.get_script_keywords();
		if ((scriptKeywords!=null)&&(!"".equals(scriptKeywords))) {
			sKwQuery = new KeywordQuery(scriptKeywords);
		}

		if (!m_SearchTSpecOnly) {
			//Go against the whole MUP
			if ((m_pageSize!=null) && (m_currentPage !=null)) {
				sKwQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue());
			}
			sKwQuery.setLuceneSortOrder(true);
			qResult = sKwQuery.exec();
			return qResult;
		} else {
			if ((m_pageSize!=null) && (m_currentPage !=null)) {
				_tSpecQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue() );
			}
			m_prodSetIntersector.include(_tSpecQuery.exec(), AttributeWeights.getWeight(IProduct.Attribute.kNone));
			
			SortedSet<Handle> keywordQueryResult = null;
			if (sKwQuery != null) {
				if ((m_pageSize!=null) && (m_pageSize !=null)) {
					sKwQuery.setBounds(m_pageSize.intValue(),m_currentPage.intValue());
				}
				sKwQuery.setLuceneSortOrder(true);
				keywordQueryResult = sKwQuery.exec();
			}

			//Category
			String requestCategory = request.get_category();
			if ((requestCategory!=null)&&(!"".equals(requestCategory))) {
				addCategoryIntersect(requestCategory);
			}

			qResult = m_prodSetIntersector.intersect();
			
			//URL scavenging
			if (m_scavengeUrls) {
				addURLKeywordIntersect(request);
			}
			
			//If there was a keyword query result, then return the top "n" matches with the tSpec query 
			if (keywordQueryResult!=null) {
				int numResults = 100; //Setting the limit to 100
				if (m_NumProducts!=null) {
					numResults = m_NumProducts.intValue();
				}
				ArrayList<Handle> finalResult = new ArrayList<Handle>(numResults);
				int count = 0;
				for (Handle handle : keywordQueryResult) {
					if (count>numResults) {
						break;
					}
					//Check if this is present in the tspec query result
					if (qResult.contains(handle)) {
						finalResult.add(handle);
					}
					count++;
				}
				qResult = new SortedArraySet<Handle>(finalResult,false);
			} else {
				//Sort by the score
				SortedSet<Handle> sortedResult = new SortedArraySet<Handle>(ProductDB.getInstance().genReference());
				sortedResult.addAll(qResult);
				qResult = sortedResult;
			}

		}
		
		return qResult;
	}
	

	/**
	 * Category may be passed in from the request. If this is the case we always intersect with the TSpec results
	 * Assumption is that the Category is a "included" condition and not excluded condition
	 * @param ospec
	 * @param requestCategory
	 * @return
	 */
	private void addCategoryIntersect(String requestCategory) {
		ArrayList<Integer> catList = new ArrayList<Integer>(); 
		DictionaryManager dm = DictionaryManager.getInstance ();
		Integer catId = dm.getId (IProduct.Attribute.kCategory, requestCategory);
		catList.add(catId);
		SimpleQuery catQuery = new AttributeQuery (IProduct.Attribute.kCategory, catList);
		m_prodSetIntersector.include(catQuery.exec(), catQuery.getWeight());
	}
	
	/**
	 * 
	 *
	 */
	private void addURLKeywordIntersect(AdDataRequest request) {
		String urlKeywords = URLScavenger.mineKeywords(request);
		KeywordQuery pKwQuery = null;
		if ((urlKeywords!=null)&&(!"".equals(urlKeywords))) {
			pKwQuery = new KeywordQuery(urlKeywords);
			m_prodSetIntersector.include(pKwQuery.exec(), pKwQuery.getWeight());
		}
	}
	
	/**
	 * returns true if the oSpec has included keywords
	 * @param ospec
	 * @return
	 */
	private boolean hasKeywords(OSpec ospec) {
		List<TSpec> tspeclist = ospec.getTspecs();
		for (TSpec tspec : tspeclist) {
			if (tspec.getIncludedKeywords() != null) {
				return true;
			}
		}
		return false;
	}
	
	
	/**
	 * returns true if the oSpec has included products
	 * @param ospec
	 * @return
	 */
	private List<Handle> getIncludedProducts(OSpec ospec) {
		List<TSpec> tspeclist = ospec.getTspecs();
		List<Handle> prodList = null;
		for (TSpec tspec : tspeclist) {
			List<ProductInfo> prodInfoList = tspec.getIncludedProducts();
			if (prodInfoList != null) {
				prodList = new ArrayList<Handle>();
				for (ProductInfo info : prodInfoList) {
					String productId = info.getName();
					Product p = new Product();
					p.setProductName(productId);
					Handle pHandle = ProductDB.getInstance().get(p);
					prodList.add(pHandle);
				}				
			}

		}
		return prodList;
	}

	@Test
	public void testProductRequestProcessor() {
		//Temp init called due to dependancy on JozData
		JozData.init ();
		ProductRequestProcessor prodRequest = new ProductRequestProcessor();
//		Reader r = new StringReader ("(get-ad-data :theme \"http://www.photography.com/\" :which-row 2 :row-size 12)");
//		Reader r = new StringReader ("(get-ad-data :theme \"http://www.photography.com/\")");
		Reader r = new StringReader ("(get-ad-data :theme \"http://www.consumersearch.com/\")");

		SexpReader lr = new SexpReader (r);
		try {
			Sexp e = lr.read ();
			SexpList l = e.toSexpList ();
		 	Sexp cmd_expr = l.getFirst ();
			if (! cmd_expr.isSexpSymbol ())
				log.error("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();

			// Return the right Cmd* class to handle this request.

			if (cmd_name.equals ("get-ad-data")) {
				AdDataRequest rqst = new AdDataRequest (e);
				SortedSet<Handle> results = prodRequest.processRequest(rqst);
				//Inspect the results
				log.info("Number of results returned are : " + results.size());
				Assert.assertTrue(results!=null);
				
				List<SelectedProduct> selProducts = new ArrayList<SelectedProduct>(results.size());
				ProductDB pdb = ProductDB.getInstance ();

				for (Handle res : results)
				{
					int id = res.getOid ();
					IProduct ip = pdb.get (id);
					MUPProductObj p = new MUPProductObj (ip);
					selProducts.add (new SelectedProduct (p));
					log.info(p.get_name() + "     " + p.get_description());
				}
				
			} else {
				log.error("The request could not be parsed correctly");
				Assert.assertTrue(false);
			}
		} catch(Exception e) {
			log.error("Exception caught during test run");
			e.printStackTrace();
		}

	}
}

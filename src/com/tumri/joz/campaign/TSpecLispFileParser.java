package com.tumri.joz.campaign;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tumri.cma.domain.BrandInfo;
import com.tumri.cma.domain.CategoryInfo;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.jozMain.BadTSpecException;
import com.tumri.joz.jozMain.SexpUtils;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpKeyword;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * "t-specs.lisp" file parser which will create the OSpec Data
 * User: nipun
 * Date: Aug 16, 2007
 */
public class TSpecLispFileParser {

	private static Logger log = Logger.getLogger(TSpecLispFileParser.class);
	public static HashMap<String, OSpec> g_OSpecMap = null;

	//String constants
	private static final String STR_IN_PACKAGE = "in-package";
	private static final String STR_SETF = "setf";
	private static final String STR_TSPEC_ADD = "t-spec-add";
	private static final String STR_TSPEC_DELETE = "t-spec-delete";
	
	private enum TSpecParam {
		NAME,
		VERSION,
		MAX_PRODS,
		MODIFIED_TIME,
		LOAD_TIME_KEYWORD_EXPR,
		ZINI_KEYWORDS,
		INCLUDE_CATEGORIES,
		EXCLUDE_CATEGORIES,
		ATTR_INCLUSIONS,
		ATTR_EXCLUSIONS,
		INCOME_PERCENTILE,
		REF_PRICE_CONSTRAINTS,
		RANK_CONSTRAINTS,
		CPC_RANGE,
		CPO_RANGE,
		ADV,
	}
	
	private enum AttrTypes {
		PROVIDER,
		MERCHANT,
		BRAND,
	}

	private static HashMap<String, TSpecParam> tspecParams = new HashMap<String, TSpecParam>();

	private static HashMap<String, AttrTypes> attrTypesHash = new HashMap<String, AttrTypes>();

	static {
		tspecParams.put(":name", TSpecParam.NAME);
		tspecParams.put(":version", TSpecParam.VERSION);
		tspecParams.put(":max-prods", TSpecParam.MAX_PRODS);
		tspecParams.put(":modified", TSpecParam.MODIFIED_TIME);
		tspecParams.put(":load-time-keyword-expr", TSpecParam.LOAD_TIME_KEYWORD_EXPR);
		tspecParams.put(":zini-keywords", TSpecParam.ZINI_KEYWORDS);
		tspecParams.put(":adv", TSpecParam.ADV);
		tspecParams.put(":include-categories", TSpecParam.INCLUDE_CATEGORIES);
		tspecParams.put(":exclude-categories", TSpecParam.EXCLUDE_CATEGORIES);
		tspecParams.put(":attr-inclusions", TSpecParam.ATTR_INCLUSIONS);
		tspecParams.put(":attr-exclusions", TSpecParam.ATTR_EXCLUSIONS);
		tspecParams.put(":income-percentile", TSpecParam.INCOME_PERCENTILE);
		tspecParams.put(":ref-price-constraints", TSpecParam.REF_PRICE_CONSTRAINTS);
		tspecParams.put(":rank-constraints", TSpecParam.RANK_CONSTRAINTS);
		tspecParams.put(":cpc-range", TSpecParam.CPC_RANGE);
		tspecParams.put(":cpo-range", TSpecParam.CPO_RANGE);
		
		attrTypesHash.put("provider", AttrTypes.PROVIDER);
		attrTypesHash.put("merchant", AttrTypes.MERCHANT);
		attrTypesHash.put("supplier", AttrTypes.MERCHANT);
		attrTypesHash.put("brand", AttrTypes.BRAND);
	}

	/**
	 * Read the TSpec data from the lisp file. This is the entry point into the parsing logic
	 *
	 * @param path --> Full path to the t-specs.lisp file
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 * @throws com.tumri.joz.jozMain.BadTSpecException
	 *
	 */
	public static void loadTspecsFromFile(String path) throws FileNotFoundException, IOException, BadTSpecException {
		g_OSpecMap = new HashMap<String, OSpec>(1000);

		FileReader fr = new FileReader(path);
		try {
			SexpReader sr = new SexpReader(fr);
			Sexp s;
			int count = 0;

			while ((s = sr.read()) != null) {
				if (!s.isSexpList()) {
					log.error("Bad sexp in tspecs file: " + s.toString());
					continue;
				}
				SexpList l = s.toSexpList();
				Iterator<Sexp> iter = l.iterator();
				if (!iter.hasNext()) {
					log.error("Bad t-spec entry: " + s.toString());
					continue;
				}
				Sexp t = iter.next();
				if (!t.isSexpSymbol()) {
					log.error("Bad t-spec entry: " + s.toString());
					continue;
				}
				SexpSymbol cmd = t.toSexpSymbol();
				if (cmd.equalsStringIgnoreCase(STR_IN_PACKAGE))
					continue; // ignore
				if (cmd.equalsStringIgnoreCase(STR_SETF)) {
					continue; // FIXME: wip, ignore for now
				}
				if (cmd.equalsStringIgnoreCase(STR_TSPEC_ADD)) {
					OSpec oSpec = readTSpecDetailsFromSExp(iter);
					g_OSpecMap.put(oSpec.getName(), oSpec);
					++count;
					if (count % 10000 == 0)
						log.info("Loaded " + count + " entries ...");
					continue;
				}
				if (cmd.equalsStringIgnoreCase(STR_TSPEC_DELETE)) {
					continue; // FIXME: wip, ignore for now
				}
				log.error("Bad t-spec entry, unknown request: " + s.toString());
				// FIXME: throw exception?
			}

			if (count > 0) {
				log.info("Loaded " + count + " o-specs.");
			}
		}
		catch (IOException e) {
			throw (e);
		}
		catch (Exception e) {
			throw new BadTSpecException(e.getMessage());
		}
		finally {
			fr.close();
		}
	}


	/**
	 * Helper method to parse the TSPEC-ADD entry and create the OSpec object
	 * @param iter
	 * @return
	 * @throws BadTSpecException
	 */
	private static OSpec readTSpecDetailsFromSExp(Iterator<Sexp> iter) throws BadTSpecException {
		OSpec oSpec = new OSpec();
		TSpec oTspec = new TSpec();

		while (iter.hasNext ())
		{
			Sexp elm = iter.next ();
			Sexp t;

			if (! elm.isSexpKeyword ())
			{
				// FIXME: TODO
				assert (false);
			}

			SexpKeyword k = elm.toSexpKeyword ();
			String name = k.toStringValue ();

			TSpecParam p = tspecParams.get (name.toLowerCase ());
			if (p == null)
			{
				// bad/unsupported parameter
				// FIXME: TODO, we still don't support all the current ones
				log.error ("unsupported t-spec parameter: " + name);
				t = iter.next (); // gobble up the parameter value
				continue;
			}

			try
			{
				switch (p)
				{
				case NAME:
					oSpec.setName(SexpUtils.get_next_symbol (name, iter));
					break;

				case MODIFIED_TIME:
				{
					t = iter.next ();
					//TODO: Set the created date with the modified time ?
					//oTspec.setModifiedTime(t.toStringValue());
					break;
				}

				case LOAD_TIME_KEYWORD_EXPR:
					t = iter.next ();
					oTspec.setLoadTimeKeywordExpression(t.toStringValue());
					break;

				case ZINI_KEYWORDS:
					// FIXME: wip
					t = iter.next ();
					List<String> keywordsTSpecList = oTspec.getIncludedKeywords();
					if (keywordsTSpecList == null) {
						keywordsTSpecList = new ArrayList<String>();
					}

					if (t.isSexpString()) {
						String keywords = t.toStringValue();
						keywordsTSpecList.add(keywords);
					} else if (t.isSexpList()) {
						SexpList attr_expr = t.toSexpList ();
						ArrayList<String> keyWords = getKeywordDetails(name, attr_expr);
						keywordsTSpecList.addAll(keyWords);
					} else {
						throw new BadTSpecException ("unexpected value for "
								+ name + ": "
								+ t.toString ());
					}
					oTspec.setIncludedKeywords(keywordsTSpecList);
					break;

				case INCLUDE_CATEGORIES:
				{
					// FIXME: wip
					SexpList l = SexpUtils.get_next_list (name, iter);
					Iterator<Sexp> iter2 = l.iterator ();
					ArrayList<CategoryInfo> catNames = getCategoryDetails (name,iter2);
					oTspec.setIncludedCategories(catNames);
					break;
				}

				case EXCLUDE_CATEGORIES:
				{
					// FIXME: wip
					SexpList l = SexpUtils.get_next_list (name, iter);
					Iterator<Sexp> iter2 = l.iterator ();
					ArrayList<CategoryInfo> catNames = getCategoryDetails (name,iter2);
					oTspec.setExcludedCategories(catNames);
					break;
				}

				case ATTR_INCLUSIONS:
				{
					// FIXME: wip
					SexpList l = SexpUtils.get_next_list (name, iter);
					for (Sexp e : l)
					{
						if (! e.isSexpList ())
							throw new BadTSpecException ("unexpected value for "
									+ name + ": "
									+ e.toString ());
						SexpList attr_expr = e.toSexpList ();
						ArrayList<String> attrDetails = getAttributeDetails(name, attr_expr);
						
						if ((attrDetails!=null)&&(attrDetails.size()>1)) {
							String attributeType = attrDetails.get(0);
							AttrTypes attrType = attrTypesHash.get(attributeType.toLowerCase());
							switch (attrType) {
								case PROVIDER:
									attrDetails.remove(0);
									oTspec.setIncludedProviders(attrDetails);
									break;
								
								case MERCHANT:
									attrDetails.remove(0);
									oTspec.setIncludedMerchants(attrDetails);
									break;
								
								case BRAND:
									attrDetails.remove(0);
									ArrayList<BrandInfo> bInfoAL = new ArrayList<BrandInfo>();
									for (int i=0;i<attrDetails.size();i++) {
										BrandInfo bInfo = new BrandInfo();
										//TODO get the brand display name
										bInfo.setName(attrDetails.get(i));
										bInfoAL.add(bInfo);
									}
									oTspec.setIncludedBrands(bInfoAL);
									break;

								default:
									log.error("Unexpected attribute inclusion type : " + attributeType);
									break;
							}
						}
						
					}
					continue;
				}

				case ATTR_EXCLUSIONS:
				{
					// FIXME: wip
					SexpList l = SexpUtils.get_next_list (name, iter);
					for (Sexp e : l)
					{
						if (! e.isSexpList ())
							throw new BadTSpecException ("unexpected value for "
									+ name + ": "
									+ e.toString ());
						SexpList attr_expr = e.toSexpList ();
						ArrayList<String> attrDetails = getAttributeDetails(name, attr_expr);
						if ((attrDetails!=null)&&(attrDetails.size()>1)) {
							String attributeType = attrDetails.get(0);
							AttrTypes attrType = attrTypesHash.get(attributeType.toLowerCase());
							switch (attrType) {
								case PROVIDER:
									attrDetails.remove(0);
									oTspec.setExcludedProviders(attrDetails);
									break;
								
								case MERCHANT:
									attrDetails.remove(0);
									oTspec.setExcludedMerchants(attrDetails);
									break;
								
								case BRAND:
									attrDetails.remove(0);
									ArrayList<BrandInfo> bInfoAL = new ArrayList<BrandInfo>();
									for (int i=0;i<attrDetails.size();i++) {
										BrandInfo bInfo = new BrandInfo();
										//TODO get the brand display name
										bInfo.setName(attrDetails.get(i));
										bInfoAL.add(bInfo);
									}
									oTspec.setExcludedBrands(bInfoAL);
									break;

								default:
									log.error("Unexpected attribute exclusion type : " + attributeType);
									break;
							}
						}
					}
					continue; 
				}

				case INCOME_PERCENTILE:
				{
					// FIXME: wip
					SexpList range = SexpUtils.get_next_list (name, iter);
					ArrayList<Double> values = getRangeValues (name, range);
					//TODO: Where to set the income percentile ?
//					oTspec.setLowIncomePercentile(values.get(0));
//					oTspec.setHighIncomePercentile(values.get(1));
					break;
				}

				case REF_PRICE_CONSTRAINTS:
				{
					// FIXME: wip
					SexpList range = SexpUtils.get_next_list (name, iter);
					ArrayList<Double> values = getRangeValues (name, range);
					oTspec.setLowPrice(values.get(0).intValue());
					oTspec.setHighPrice(values.get(1).intValue());
					break;
				}

				case RANK_CONSTRAINTS:
				{
					// FIXME: wip
//					value = SexpUtils.get_next_string (name, iter);
					break;
				}

				case CPC_RANGE:
				{
					// FIXME: wip
					SexpList range = SexpUtils.get_next_list (name, iter);
					ArrayList<Double> values = getRangeValues (name, range);
					oTspec.setLowCPC(values.get(0).intValue());
					oTspec.setHighCPC(values.get(1).intValue());
					break;
				}

				case CPO_RANGE:
				{
					// FIXME: wip
//					value = SexpUtils.get_next_string (name, iter);
					break;
				}

				default:
					assert (false);
				}
			}
			catch (SexpUtils.BadGetNextException ex)
			{
				throw new BadTSpecException (ex.getMessage ());
			}
		}
		oSpec.addTSpec(oTspec);
		return oSpec;
	}

	private static ArrayList<Double> getRangeValues (String name, SexpList range) throws SexpUtils.BadGetNextException
	{
		ArrayList<Double> vals = new ArrayList<Double>();
		Iterator<Sexp> iter = range.iterator ();
		while (iter.hasNext ())
		{
			Double d = SexpUtils.get_next_maybe_double (name, iter);
			// watch for NIL
			if (d == null)
				d = new Double (vals.size () == 0 ? 0 : 100000);
			vals.add (d);
		}
		while (vals.size () < 2)
		{
			vals.add (new Double (vals.size () == 0 ? 0 : 100000));
		}
		return vals;
	}

	private static ArrayList<CategoryInfo> getCategoryDetails (String name, Iterator<Sexp> iter) throws SexpUtils.BadGetNextException
	{
		ArrayList<CategoryInfo> categories = new ArrayList<CategoryInfo> ();
		while (iter.hasNext ())
		{
			String catName = SexpUtils.get_next_symbol (name, iter);
			CategoryInfo catInfo = new CategoryInfo();
			catInfo.setName(catName);
			//TODO: Lookup Cat display names ?
			categories.add (catInfo);
		}
		return categories;
	}
	
	private static ArrayList<String> getAttributeDetails (String name, SexpList attr_expr) throws BadTSpecException, SexpUtils.BadGetNextException
	{
		if (attr_expr.size () < 2)
			throw new BadTSpecException ("unexpected value for "
					+ name + ": "
					+ attr_expr.toString ());
		Iterator<Sexp> iter = attr_expr.iterator ();
		ArrayList<String> result = new ArrayList<String> ();
		while (iter.hasNext ())
		{
			String attr_value = SexpUtils.get_next_symbol (name, iter);
			result.add (attr_value);
		}
		return result;
	}

	private static ArrayList<String> getKeywordDetails (String name, SexpList attr_expr) throws BadTSpecException, SexpUtils.BadGetNextException
	{
		if (attr_expr.size () < 2)
			throw new BadTSpecException ("unexpected value for "
					+ name + ": "
					+ attr_expr.toString ());
		Iterator<Sexp> iter = attr_expr.iterator ();
		ArrayList<String> result = new ArrayList<String> ();
		while (iter.hasNext ())
		{
			Sexp t = iter.next();
			if (t.isSexpKeyword()) {
				continue;
			} else if (t.isSexpString()) {
				String attr_value = t.toStringValue();
				result.add (attr_value);
			}
		}
		return result;
	}
	
	@Test
	public static void testLoadTSpecFileParser() {
		try {
			loadTspecsFromFile("/Users/nipun/Documents/Tumri/JoZ/t-specs.lisp");
			if (g_OSpecMap!=null) {
				log.info(g_OSpecMap.toString());
			}
		} catch (IOException e) {
			e.printStackTrace();  
		} catch (BadTSpecException e) {
			e.printStackTrace();  
		}
	}
	

}

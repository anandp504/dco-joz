package com.tumri.joz.jozMain;

import com.tumri.content.MerchantDataProvider;
import com.tumri.content.data.impl.AdvertiserTaxonomyMapperImpl;
import com.tumri.content.data.impl.AdvertiserMerchantDataMapperImpl;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.content.data.Product;
import com.tumri.content.data.Category;
import com.tumri.content.data.MerchantData;

import java.text.StringCharacterIterator;
import java.text.CharacterIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import org.apache.log4j.Logger;

/**
 * Helper class for the Joz Listing Provider, to build the JSON response 
 * @author: nipun
 * Date: Mar 29, 2008
 * Time: 7:09:20 AM
 */
public class JozJSONResponseBuilder {

	private static Logger log = Logger.getLogger(JozJSONResponseBuilder.class);

	/**
	 * Create the listing response for a given product handle
	 * @param h
	 * @param maxProdDescLength
	 * @return
	 */
	public static String getListingDetails(String advertiser, Handle h, int maxProdDescLength) {
		ProductDB pdb = ProductDB.getInstance();
		Product p = pdb.get(h);

		StringBuilder b = new StringBuilder();

		b.append("{");
		b.append("id:\"");
		b.append(encode(p.getIdSymbol()));
		b.append("\",category_id:\"");
		b.append(encode(p.getCategoryStr()));
		b.append("\",display_category_name:\"");
		// Use the first parent as the category.
		Category cat = null;
		try {
			//todo: verify correct tax
			cat = AdvertiserTaxonomyMapperImpl.getInstance().getTaxonomyProvider(advertiser).getTaxonomy().getCategory(p.getCategory());
		} catch (NullPointerException npe) {
			log.warn("The category specified for product is valid for the current taxonomy :" + p.getCategoryStr());
		}

		if (cat != null) {
			b.append(encode(cat.getName()));
		}
		b.append("\",name:\"");
		String name = p.getProductName();
		b.append(encode(name));
		b.append("\",price:\"");
		b.append(encodePrice(p.getPrice()));
		b.append("\",discount_price:\"");
		b.append(encodePrice(p.getDiscountPrice()));
		b.append("\",brand:\"");
		b.append(encode(p.getBrandStr()));
		b.append("\",merchant_id:\"");
		b.append(encode(p.getSupplierStr()));
		b.append("\",provider:\"");
		b.append(encode(p.getProviderStr()));

		MerchantDataProvider mdp = AdvertiserMerchantDataMapperImpl.getInstance().getMerchantDataProvider(advertiser);
		MerchantData md = mdp.getMerchant(p.getSupplierStr());

		b.append("\",merchantlogo:\"");
		if (md != null) {
			b.append(encode(md.getLogoUrl()));
		}
		b.append("\",ship_promo:\"");
		if (md != null) {
			b.append(encode(md.getShippingPromotionText()));
		}

		b.append("\",description:\"");
		String desc = p.getDescription();
		if (maxProdDescLength > 0) {
			if (desc.length() > maxProdDescLength) {
				desc = desc.substring(0, maxProdDescLength);
			}
		}
		b.append(encode(desc));
		b.append("\",thumbnailraw:\"");
		b.append(encode(p.getThumbnail()));
		b.append("\",product_url:\"");
		b.append(encode(p.getPurchaseUrl()));
		b.append("\",picture_url:\"");
		b.append(encode(p.getImageUrl()));
		b.append("\",c_code:\"");
		b.append(encode(p.getCurrencyStr()));
		b.append("\",offer_type:\"");
		b.append(encode(p.getProductTypeStr()).toUpperCase());

		b.append("\",cpc:\"");
		b.append(encodePrice(p.getCPC()));
		b.append("\",cpo:\"");
		b.append(encodePrice(p.getCPO()));
		b.append("\",rank:\"");
		b.append((p.getRank()==null)?"":p.getRank().toString());
		b.append("\"");

		b.append("}");

		return b.toString();
	}

	/**
	 * Escape characters for text appearing in Javascript.
	 *
	 */
	private static String encode(String aText) {
		if (aText == null)
			return "";
		final StringBuilder result = new StringBuilder();
		final StringCharacterIterator iterator = new StringCharacterIterator(aText);
		char character =  iterator.current();
		while (character != CharacterIterator.DONE ){
			//BUG 1690: Adding this escape for / causes the iCS JSON parsing to break.
//            if (character == '\\') {
//                result.append("\\\\"); } else
			if (character == '"') {
				result.append("\\\"");
			}
			else if (character == '\'') {
				result.append("\\\'");
			}
			else {
				//the char is not a special one
				//add it to the result as is
				result.append(character);
			}
			character = iterator.next();
		}
		return result.toString();
	}

	/**
	 * Create a comma seprated string from given list of long product ids
	 * @param oids
	 * @return
	 */
	public static String constructListingIdList(long[] oids) {
		ProductDB pdb = ProductDB.getInstance();
		StringBuilder b = new StringBuilder();
		boolean done_one = false;

		for (long id : oids) {
			if (done_one)
				b.append(",");
			Product p = pdb.get(id);
			b.append(p.getIdSymbol());
			done_one = true;
		}

		return b.toString();
	}

	/**
	 * Returns the list of categories in an arrayList in the same order of the products
	 * @param pids
	 * @return
	 */
	public static List<Category> constructCategoryList(String advertiser,
			long[] pids) {
		ProductDB pdb = ProductDB.getInstance();
		ArrayList<Category> categories = new ArrayList<Category>();
		HashMap<String,String> catNames = new  HashMap<String,String>();
		for (long id : pids) {
			Product p = pdb.get(id);
			//todo: verify correct use of adv vs tumri tax
			Category cat = AdvertiserTaxonomyMapperImpl.getInstance().getTaxonomyProvider(advertiser).getTaxonomy().getCategory(p.getCategory());
			if (cat != null) {
				if (catNames.get(cat.getGlassIdStr())==null) {
					catNames.put(cat.getGlassIdStr(),cat.getGlassIdStr());
					categories.add(cat);
				}
			}
		}

		return categories;
	}

	/**
	 * Get the details in the JSON form for the given list of categories
	 * @param cats
	 * @return
	 */
	public static String getCategoryDetails(List<Category> cats) {
		StringBuilder sb = new StringBuilder();

		sb.append("[");
		boolean done_one = false;

		for (Category c : cats) {
			if (done_one)
				sb.append(",");
			sb.append("{categoryName:\"");
			sb.append(c.getGlassIdStr());
			sb.append("\",categoryDisplayName:\"");
			sb.append(encode(c.getName()));
			sb.append("\"}");
			done_one = true;
		}

		sb.append("]");

		return sb.toString();
	}

	/**
	 * Construct delimited list of Category names
	 * @param cats
	 * @return
	 */
	public static String getCategoryNameList(List<Category> cats) {
		StringBuilder sb = new StringBuilder();

		boolean done_one = false;

		for (Category c : cats) {
			if (done_one)
				sb.append("||");
			sb.append(encode(c.getName()));
			done_one = true;
		}

		return sb.toString();
	}

	/**
	 * Helper method to encode the price
	 * @param d
	 * @return
	 */
	private static String encodePrice(Double d) {
		if (d == null)
			return "";
		return String.format("%.2f", d);
	}

}

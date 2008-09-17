package com.tumri.joz.campaign;

import java.util.ArrayList;
import java.util.List;
import org.apache.log4j.Logger;
import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;


/**
 * Class to handle the related utilities
 * @author nipun
 *
 */
public class TSpecHelper {

	private static Logger log = Logger.getLogger (TSpecHelper.class);
	
	/**
	 * Returns the sorted set of included products if the TSpec has included products
	 * @param tSpec
	 * @return   ArrayList<Handles>
	 */
	public static ArrayList<Handle> getIncludedProducts(TSpec tSpec) {
		ArrayList<Handle> prodsAL = new ArrayList<Handle>();
		List<ProductInfo> prodInfoList = tSpec.getIncludedProducts();
		if (prodInfoList!=null) {
			for (ProductInfo info : prodInfoList) {
				try {
					String productId = info.getName();
					if (productId != null) {
                        if (productId.indexOf(".") > -1) {
                            productId = productId.substring(productId.indexOf("."), productId.length());
                        }
                        char[] pidCharArr = productId.toCharArray();
                        //Drop any non digit characters
                        StringBuffer spid = new StringBuffer();
                        for (char ch: pidCharArr) {
                            if (Character.isDigit(ch)) {
                                spid.append(ch);
                            }
                        }
                        productId = spid.toString();
                        Handle prodHandle = ProductDB.getInstance().getHandle(new Long(productId));
                        if (prodHandle != null) {
                            prodsAL.add(prodHandle);
                        }

					}
				} catch(Exception e) {
					log.error("Could not get the product info from the Product DB");
					e.printStackTrace();
				}
			}
		}
		
		return prodsAL;
	}
}

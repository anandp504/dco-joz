package com.tumri.joz.campaign;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.Geocode;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.ProductInfo;
import com.tumri.cma.domain.TSpec;
import com.tumri.cma.misc.SexpOSpecHelper;
import com.tumri.cma.misc.SexpOSpecHelper.MappingsParams;
import com.tumri.joz.products.Handle;
import com.tumri.joz.products.ProductDB;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpSymbol;

/**
 * Class to handle the t-spec-add, t-spec-delete and incorp-mapping-delta API
 * @author nipun
 *
 */
public class OSpecHelper {

	private static Logger log = Logger.getLogger (OSpecHelper.class);
	
	/**
	 * Parses the tspec-add directive and adds an Ospec to the cache.
	 * This is used when creating a tSpec from the consoles. This tSpec does not become part of the Campaign Cache
	 * @param tSpecAddSpec - the string expression that contains the tspec add command
	 * @return name of the t-spec added.
	 */
	public static String doTSpecAdd(SexpList tSpecAddSpec) throws TransientDataException {
		SexpList l = tSpecAddSpec;
		Sexp cmd_expr = l.getFirst ();
		if (! cmd_expr.isSexpSymbol ()) {
			log.error("command name not a symbol: " + cmd_expr.toString ());
			return null;
		}

		SexpSymbol sym = cmd_expr.toSexpSymbol ();
		String cmd_name = sym.toString ();
		if (cmd_name.equalsIgnoreCase("t-spec-add")) {
			Iterator<Sexp> iter = l.iterator();
			iter.next(); //ignore the t-spec-add keyword
			OSpec theOSpec = SexpOSpecHelper.readTSpecDetailsFromSExp(iter);
			TransientDataManager.getInstance().addOSpec(theOSpec);
	        TSpecQueryCache.getInstance().removeQuery(theOSpec.getId());
			return theOSpec.getName();
			//Note that we are not touching the Query cache here since the next access to the Tspec using get-ad-data will add it to the cache
		} else {
			log.error("Unexpected command received : " + cmd_expr);
			return null;
		}
	}

	/**
	 * Deletes the reference of the TSpec from JoZ cache
	 * @param tSpecName - name of the tspec to delete
	 * @return name of tspec deleted.
	 */
	public static String doTSpecDelete(String tSpecName) {
		OSpec oSpec = CampaignDB.getInstance().getOspec(tSpecName);
        if (oSpec!=null) {
            List<TSpec> tspeclist = oSpec.getTspecs();
            for (TSpec tspec: tspeclist) {
                TSpecQueryCache.getInstance().removeQuery(tspec.getId());
            }
        }
        TransientDataManager.getInstance().deleteOSpec(tSpecName);
		return tSpecName;
	}

	/**
	 * Update the TSpec mapping on the fly
	 * @param updtMappingCommands list of mapping commands
	 */
	public static void doUpdateTSpecMapping(SexpList updtMappingCommands) throws TransientDataException {
		//TODO: Finalize what needs to be done for updating the mappings after looking at the usecases for TMC/QAC/Publisher/Advertiser
		for (Sexp cmd : updtMappingCommands) {
			if (! cmd.isSexpList ()) {
				log.error("the command is not a list: " + cmd.toString());
				continue;
			}
			SexpList updtMappingCommand = cmd.toSexpList();
			if (updtMappingCommand.size() != 7) {
				log.error("Invalid syntax for the incorp-mapping-deltas command: " + updtMappingCommand.toString());
				continue;
			}

			Sexp tmp_mapping_directive = updtMappingCommand.get(0);
			Sexp tmp_site_constraint_spec = updtMappingCommand.get(1);
			Sexp tmp_site_constraint = null;
			String tmp_site_spec = null;
			SexpList siteSpecList = tmp_site_constraint_spec.toSexpList();
			if (siteSpecList.size() == 2) {
				tmp_site_constraint = siteSpecList.get(0).toSexpKeyword();
				tmp_site_spec = siteSpecList.get(1).toSexpString().toStringValue();
			}
			Sexp tmp_geo_constraint_spec = updtMappingCommand.get(2);
			Sexp tmp_demo_constraint_spec = updtMappingCommand.get(3);
			Sexp tmp_t_spec = updtMappingCommand.get(4);
			Sexp tmp_weight = updtMappingCommand.get(5);
			Sexp tmp_modified = updtMappingCommand.get(6);

			String opType = tmp_mapping_directive.toStringValue();
			String opLookupDataType = tmp_site_constraint.toStringValue();
			Geocode geoObj = null;
			if (tmp_geo_constraint_spec != null && tmp_geo_constraint_spec.toSexpList().size() > 0) {
				geoObj = SexpOSpecHelper.getGeocodeInfo(tmp_geo_constraint_spec);
			}

			if (tmp_demo_constraint_spec!=null && tmp_demo_constraint_spec.toSexpList().size() > 0) {
				log.warn("Received request to add demo mapping, this is not currently supported in Joz");
			}
			
			String tSpecName = tmp_t_spec.toStringValue();
			float weight = 1.0f;

			if (tmp_weight!=null){
				if (tmp_weight.isSexpReal()) {
					String weightStr = tmp_weight.toStringValue();
					weightStr = weightStr.replaceAll("f","E");
					weight = new Float(weightStr);
				}
				else {
					weight = (float) tmp_weight.toSexpInteger().toNativeInteger32();
				}
			}

			//TODO: Set modified time into the mapping object(s)
			String modTime = tmp_modified.toStringValue();
			MappingsParams mapType = SexpOSpecHelper.mappingParams.get(opLookupDataType.toLowerCase());
            if (":add".equals(opType)) {
                switch(mapType) {
                case REALM:
                    //url value is in tmp_site_constraint
                    TransientDataManager.getInstance().addUrlMapping(tmp_site_spec, tSpecName, weight, geoObj);
                    break;

                case STOREID:
                    TransientDataManager.getInstance().addLocationMapping(tmp_site_spec, tSpecName, weight, geoObj, null);
                    break;

                }

            }
            else if (":delete".equals(opType)) {
                switch(mapType) {
                case REALM:
                    //url value is in tmp_site_constraint
                    TransientDataManager.getInstance().deleteUrlMapping(tmp_site_spec, tSpecName, weight, geoObj);
                    break;

                case STOREID:
                    TransientDataManager.getInstance().deleteLocationMapping(tmp_site_spec, tSpecName, weight, geoObj, null);
                    break;

                }
            }
		}
	}

	/**
	 * Returns the sorted set of included products if the oSpec has included products
	 * @param ospec
	 * @return
	 */
	public static ArrayList<Handle> getIncludedProducts(OSpec ospec) {
		ArrayList<Handle> prodsAL = new ArrayList<Handle>();
		List<TSpec> tspeclist = ospec.getTspecs();
		for (TSpec tspec : tspeclist) {
			List<ProductInfo> prodInfoList = tspec.getIncludedProducts();
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
		}
		
		return prodsAL;
	}
}

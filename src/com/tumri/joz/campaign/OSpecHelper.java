package com.tumri.joz.campaign;

import java.util.Iterator;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.Geocode;
import com.tumri.cma.domain.Location;
import com.tumri.cma.domain.LocationAdPodMapping;
import com.tumri.cma.domain.OSpec;
import com.tumri.cma.domain.Theme;
import com.tumri.cma.domain.ThemeAdPodMapping;
import com.tumri.cma.domain.Url;
import com.tumri.cma.domain.UrlAdPodMapping;
import com.tumri.cma.misc.SexpOSpecHelper;
import com.tumri.cma.misc.SexpOSpecHelper.MappingsParams;
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
	public static String doTSpecAdd(SexpList tSpecAddSpec) {
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
			CampaignDB.getInstance().addOSpec(theOSpec);
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
		CampaignDB.getInstance().deleteOSpec(tSpecName);
		OSpecQueryCache.getInstance().removeQuery(tSpecName);
		return tSpecName;
	}

	/**
	 * Update the TSpec mapping on the fly
	 * @param updtMappingCommands list of mapping commands
	 */
	public static void doUpdateTSpecMapping(SexpList updtMappingCommands){
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
			Sexp tmp_site_spec = null;
			SexpList siteSpecList = tmp_site_constraint_spec.toSexpList();
			if (siteSpecList.size() == 2) {
				tmp_site_constraint = siteSpecList.get(0).toSexpKeyword();
				tmp_site_spec = siteSpecList.get(1).toSexpString();
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
			OSpec aOSpec = CampaignDB.getInstance().getOspec(tSpecName);
			if (aOSpec != null) {
				if (":add".equals(opType)) {
					switch(mapType) {
					case REALM:
						//TODO Get the Url mapping ( if it exists ), or create a new one
						//url value is in tmp_site_constraint
						Url url = null;
						Integer urlID = null;
						UrlAdPodMapping urlMapping = new UrlAdPodMapping();
						urlMapping.setWeight((int)weight);
//						urlMapping.setUrlId(urlID);
//						urlMapping.setAdPodId(aOSpec.getId());
						//TODO: Commit the URL mapping back into the CampaignDB
						break;

					case STOREID:
						//TDP Get the Location or if the location does not exist
						Location theLocation = null;
						int locId = -1;
						LocationAdPodMapping locMapping = new LocationAdPodMapping();
						locMapping.setLocationId(locId);
						break;

					case THEME:
						Theme theTheme = null;
						Integer themeId = null;
						ThemeAdPodMapping themeMapping = new ThemeAdPodMapping();
//						themeMapping.setThemeId(themeId);
						break;
					}
				} else if (":delete".equals(opType)) {
					if (":realm".equals(opLookupDataType)) {
						//Delete the realm mapping to the TSpec
					} else if (":store-ID".endsWith(opLookupDataType)) {
						//Delete the storeid mapping to the TSpec
					}
				}
			} else {
				log.error("Could not add the mapping since the OSpec is not there in cache");
			}
		}
	}

}

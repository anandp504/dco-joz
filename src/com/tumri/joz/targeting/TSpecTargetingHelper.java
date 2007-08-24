package com.tumri.joz.targeting;

import java.util.List;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.joz.jozMain.JozData;
import com.tumri.joz.jozMain.JozURI;
import com.tumri.joz.jozMain.MappingObj;
import com.tumri.joz.jozMain.MappingObjList;

/**
 * Helper class that will help to choose a TSpec given a Ad Request
 * Note that this will be replaced by the actual targeting logic when implemented
 * 
 * This was refactored from teh CmdGetData.java -written by Doug
 * @author nipun
 *
 */
public class TSpecTargetingHelper {

	/**
	 * Returns the TSpec Name given the request
	 * Note that this needs to be refactored to use the actual targeting logic once it is created
	 * @return
	 */
	public static String doTargeting(AdDataRequest request) {
		//TODO: Replace with actual targeting logic
		String targetedTSpecName = null;
		String storeId = null;
		String theme = null;
		JozURI uri; // not initialized on purpose
		String tSpecParam = request.get_t_spec();

		if (tSpecParam != null) {
			targetedTSpecName = tSpecParam;
		} else if ((storeId = request.get_store_id()) != null) {
			targetedTSpecName = chooseTSpecForStoreId(storeId);
		} else if ((theme = request.get_theme()) != null) {
			targetedTSpecName = chooseTSpecForTheme(theme);
		} else if (request.get_url() != null
				&& (uri = JozURI.build_lax_uri(request.get_url())) != null) {
			targetedTSpecName = chooseTSpecForUri(uri);
		} 
		
		if ((targetedTSpecName==null) && (theme != null && (uri = JozURI.build_lax_uri(theme)) != null)) {
			targetedTSpecName = chooseTSpecForUri(uri);
		}  
		
		if (targetedTSpecName == null) {
			String default_realm_url = "http://default-realm";
			JozURI default_realm_uri = JozURI.build_lax_uri(default_realm_url);
			targetedTSpecName = chooseTSpecForRealm(default_realm_uri);
		}

		return targetedTSpecName;
	}

	private static String chooseTSpecForStoreId(String store_id) {
		MappingObjList mol = JozData.mapping_db.get_store_id_t_specs(store_id);
		if (mol == null || mol.size() == 0)
			return null;
		List<MappingObj> lmo = mol.get_list();
		MappingObj mo = lmo.get(0);
		return mo.get_t_spec();
	}

	private static String chooseTSpecForTheme(String theme) {
		MappingObjList mol = JozData.mapping_db.get_theme_t_specs(theme);
		if (mol == null || mol.size() == 0)
			return null;
		List<MappingObj> lmo = mol.get_list();
		MappingObj mo = lmo.get(0);
		return mo.get_t_spec();
	}

	private static String chooseTSpecForUri(JozURI uri) {
		MappingObjList mol = JozData.mapping_db.get_url_t_specs(uri);
		if (mol == null || mol.size() == 0)
			return null;
		List<MappingObj> lmo = mol.get_list();
		MappingObj mo = lmo.get(0);
		return mo.get_t_spec();
	}

	private static String chooseTSpecForRealm(JozURI realm_uri) {
		MappingObjList mol = JozData.mapping_db.get_url_t_specs(realm_uri);
		if (mol == null || mol.size() == 0)
			return null;
		List<MappingObj> lmo = mol.get_list();
		MappingObj mo = lmo.get(0);
		return mo.get_t_spec();
	}

}

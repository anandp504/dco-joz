package com.tumri.joz.campaign;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.tumri.cma.domain.AdPod;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.CampaignTheme;
import com.tumri.cma.domain.CampaignURL;
import com.tumri.cma.domain.Location;
import com.tumri.cma.domain.OSpec;
import com.tumri.joz.jozMain.BadMappingDataException;
import com.tumri.utils.sexp.BadSexpException;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpKeyword;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;

/**
 * Class to read the "mappings.lisp" file, and create the Campaign data. This is done after the TSpecs file has already been populated
 * @author nipun
 *
 */
public class MappingsLispFileParser {

	private static Logger log = Logger.getLogger(MappingsLispFileParser.class);
    
	private enum MappingsParams {
		REALM,
		THEME,
		STOREID,
	}
	
	private static HashMap<String, MappingsParams> mappingParams = new HashMap<String, MappingsParams>();

	static {
		mappingParams.put(":realm", MappingsParams.REALM);
		mappingParams.put(":theme", MappingsParams.THEME);
		mappingParams.put(":store-id", MappingsParams.STOREID);
	}
	
    /**
     * Read the mappings file and create the campaign objects, using the REALM (url) and the TSpec(OSpec) name.
     * @param mapping_path
     * @throws FileNotFoundException
     * @throws IOException
     * @throws BadMappingDataException
     */
	public static Iterator<Campaign> loadMappings(String mapping_path) throws FileNotFoundException, IOException, BadMappingDataException {
		ArrayList<Campaign> campArrayList = new ArrayList<Campaign>(500);
		Sexp file_sexp = null;
		try {
			file_sexp = SexpReader.readFromFile(mapping_path);
		} catch (BadSexpException e) {
			throw new BadMappingDataException("error reading sexp");
		}
		if (!file_sexp.isSexpList())
			throw new BadMappingDataException("expected list");
		SexpList file_list = file_sexp.toSexpList();
		Iterator<Sexp> iter = file_list.iterator();

		while (iter.hasNext()) {
			Sexp e = iter.next();
			Campaign theCampaign = new Campaign();
			if (!e.isSexpList()) {
				log.error("Bad mapping entry: " + e.toString());
				continue;
			}
			SexpList l = e.toSexpList();
			if (l.size() != 5) {
				log.error("Bad mapping entry: " + e.toString());
				continue;
			}

			Sexp tmp_kind = l.get(0);
			Sexp tmp_domain = l.get(1);
			Sexp tmp_t_spec = l.get(2);
			Sexp tmp_weight = l.get(3);
			Sexp tmp_modified = l.get(4);
			if (!tmp_kind.isSexpKeyword()
					|| !tmp_domain.isSexpString()
					|| !tmp_t_spec.isSexpSymbol()
					|| (!tmp_weight.isSexpInteger() && !tmp_weight.isSexpReal())
					|| (!tmp_modified.isSexpString() && !tmp_modified
							.isSexpInteger())) {
				log.error("Bad mapping entry: " + e.toString());
				continue;
			}
			SexpKeyword kind = tmp_kind.toSexpKeyword();
			String domain = tmp_domain.toStringValue();
			String t_spec = tmp_t_spec.toStringValue();
			float weight;
			
			//TODO: Where does weight figure in the CDB ?

			if (tmp_weight.isSexpReal())
				weight = new Float(tmp_weight.toSexpReal().toNativeReal64())
			.floatValue();
			else
				weight = (float) tmp_weight.toSexpInteger().toNativeInteger32();
			
			MappingsParams mapType = mappingParams.get(kind.toStringValue().toLowerCase());
			switch(mapType) {
			case REALM:
				CampaignURL url= new CampaignURL();
				url.setName(domain);
				theCampaign.setCampaignUrl(url);
				break;
			
			case STOREID:
				Location theLocation = new Location();
				theLocation.setName(domain);
				List<Location> locArray = theCampaign.getLocations();
				if (locArray == null) {
					locArray = new ArrayList<Location>();
				}
				locArray.add(theLocation);
				theCampaign.setLocations(locArray);
				break;
				
			case THEME:
				CampaignTheme theme = new CampaignTheme();
				theme.setName(domain);
				theCampaign.setCampaignTheme(theme);
				break;
			
			}
			
			//TODO Add the TSpec details to the Campaign
			//Get the oSpec information
			OSpec theOSpec = TSpecLispFileParser.g_OSpecMap.get(t_spec);
			if (theOSpec != null) {
				AdPod adPod = new AdPod();
				adPod.setName(t_spec); //TODO verify this
				adPod.setOspec(theOSpec);
				theCampaign.addAdPod(adPod);
			}
			campArrayList.add(theCampaign);
		}
		
		return campArrayList.iterator();
	}
}

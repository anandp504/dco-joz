package com.tumri.joz.campaign;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.tumri.cma.CMAException;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.CampaignTheme;
import com.tumri.cma.domain.CampaignURL;
import com.tumri.cma.domain.Location;
import com.tumri.cma.service.CampaignDeltaProvider;
import com.tumri.joz.jozMain.BadMappingDataException;
import com.tumri.joz.jozMain.BadTSpecException;
import com.tumri.joz.utils.AppProperties;

/**
 * Implementation to provide the Campaign information from the Lisp file
 * User: nipun
 * Date: Aug 16, 2007
 * Time: 3:06:15 PM
 */
public class CampaignLispDataProvider implements CampaignDeltaProvider {

    private static Logger log = Logger.getLogger (CampaignLispDataProvider.class);
    private static Iterator<Campaign> g_campaignIter = null;
    private static CampaignLispDataProvider g_campaignProvider;
    private static String g_tspecLispFileName = "t-specs.lisp";
    private static String g_mappingsLispFileName = "mapping.lisp";
    private static String g_lispFilePath = "..";
    
    /**
     * Default constructor
     *
     */
    public CampaignLispDataProvider(){
    	AppProperties props = AppProperties.getInstance();
    	if (props!= null) {
        	String tmpLispFilePath = props.getProperty("TSPEC_MAPPING_FILE_DIR");
        	if (tmpLispFilePath != null) {
        		g_lispFilePath = tmpLispFilePath;
        	}
        	String tmpTSpecLispFileName = props.getProperty("TSPEC_LISP_FILE_NAME");
        	if (tmpTSpecLispFileName != null) {
        		g_tspecLispFileName = tmpTSpecLispFileName;
        	}
        	String tmpMappingsFileName = props.getProperty("MAPPING_LISP_FILE_NAME");
        	if (tmpMappingsFileName != null) {
        		g_mappingsLispFileName = tmpMappingsFileName;
        	}
        }
    	
    }
    
    /**
     * Reads the lisp file and creates the campaign Iterator
     * @param lispFilePath
     */
    private void init() {
    	if (g_campaignIter == null) {
    		//Read OSpec data
    		try {
    			TSpecLispFileParser.loadTspecsFromFile(g_lispFilePath + "/" + g_tspecLispFileName);
    			g_campaignIter = MappingsLispFileParser.loadMappings(g_lispFilePath + "/" + g_mappingsLispFileName);
    		} catch (FileNotFoundException e) {
    			log.error("Could not find the lisp file to load");
    			e.printStackTrace();
    		} catch (IOException e) {
    			log.error("Could not read the lisp file");
    			e.printStackTrace();
    		} catch (BadTSpecException e) {
    			log.error("TSpec load failed - information incorrect ");
    			e.printStackTrace();
    		}
    		catch (BadMappingDataException e) {
    			log.error("Mapping load failed - bad mapping info");
    			e.printStackTrace();
    		}
    	}
    }
    
    public static CampaignLispDataProvider getInstance() {
    	if (g_campaignProvider == null) {
    		synchronized (CampaignLispDataProvider.class) {
    			if (g_campaignProvider == null) {
    				g_campaignProvider = new CampaignLispDataProvider();
    				g_campaignProvider.init();
    			}
    		}
    	}
    	return g_campaignProvider;
      }
    
	public Iterator<Campaign> getDeletedDeltas() throws CMAException {
		throw new UnsupportedOperationException("Feature not yet implemented");
	}

	/**
	 * Return all the campaigns from the lisp file
	 */
	public Iterator<Campaign> getNewDeltas() throws CMAException {
		return g_campaignIter;
	}

	public Iterator<Campaign> getUpdatedDeltas() throws CMAException {
		throw new UnsupportedOperationException("Feature not yet implemented");
	}
	
	@Test
	public void testNewDeltas(){

		CampaignLispDataProvider.g_lispFilePath = "/Users/nipun/Documents/Tumri/JoZ";
		CampaignLispDataProvider campProvider = CampaignLispDataProvider.getInstance();
		try {
			Iterator<Campaign> campIter = campProvider.getNewDeltas();
			int count = 0;
			while (campIter.hasNext()) {
				count++;
				Campaign campaign = campIter.next();
				CampaignURL campUrl = campaign.getCampaignUrl();
				CampaignTheme campTheme = campaign.getCampaignTheme();
				if (campUrl != null) {
					log.info("The Campaign URL is : " + campaign.getCampaignUrl().getName());
				} else if (campTheme != null) {
					log.info("The Campaign Theme is : " + campaign.getCampaignTheme().getName());
				} else if (campaign.getLocations() != null) {
					List locList = campaign.getLocations();
					Iterator locIter = locList.iterator();
					while (locIter.hasNext()) {
						Location theLoc = (Location)locIter.next();
						log.info("The Campaign Location is : " + theLoc.getName());
					}
				}
			}
			log.info("Total campaigns loaded = " + count);
		} catch (CMAException e) {
			log.error("Exception caught when getting the campaign informaion");
			e.printStackTrace();
		}
	}

}

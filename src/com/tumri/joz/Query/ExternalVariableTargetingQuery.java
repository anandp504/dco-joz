package com.tumri.joz.Query;

import com.tumri.cma.persistence.xml.CampaignXMLConstants;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.data.MultiSortedSet;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Targeting Query for external variables.
 *
 * @author raghu
 */
public class ExternalVariableTargetingQuery extends TargetingQuery {
	private static HashMap<Integer, String> extVars = null;
	private HashMap<Integer,String> extVarsMap = null;
    private static ExternalVariableTargetingQuery extTgtQuery = null;
    private static Logger log = Logger.getLogger (ExternalVariableTargetingQuery.class);
    private static String fieldSeparator= null;

    
    public static ExternalVariableTargetingQuery getInstance() {
        if (extTgtQuery == null) {
          synchronized (ExternalVariableTargetingQuery.class) {
            if (extTgtQuery == null) {
            	extTgtQuery = new ExternalVariableTargetingQuery();
            	extTgtQuery.init();
            }
          }
        }
        return extTgtQuery;
    }
    public ExternalVariableTargetingQuery() {
    	
    }

    private void init(){
    	if(extVars == null){
    		extVars = new HashMap<Integer,String>();
    		String externalTargetingVariables = AppProperties.getInstance().getProperty("externalTargetingVariables");
        	if (externalTargetingVariables==null || externalTargetingVariables.equals("")){
        		log.error("joz.property externalTargetingVariables not set");
        		externalTargetingVariables="";
        	}
        	StringTokenizer tokenizer = new StringTokenizer(externalTargetingVariables,",");
        	int i =0;
        	while(tokenizer.hasMoreTokens()){
        		String extVar = tokenizer.nextToken();
        		extVars.put(i,extVar);
        		i++;
        	}
    	}
        fieldSeparator= AppProperties.getInstance().getProperty("com.tumri.joz.multivalue.delimiter");
    }

    public void setExternalVars(HashMap<Integer,String> extVarsMap) {
    	this.extVarsMap = extVarsMap;
    }

    public Type getType() {
        return Type.kExtTarget;
    }

    public SortedSet<Handle> exec() {
        SortedSet<Handle> externalVariableTargetResults    = execExternalVariableQuery();
        SortedSet<Handle> nonExternalVariableTargetResults = execNonExternalVariableQuery();


        MultiSortedSet<Handle> results = new MultiSortedSet<Handle>();
        if(externalVariableTargetResults != null) {
            results.add(externalVariableTargetResults);
        }
        if(nonExternalVariableTargetResults != null) {
            results.add(nonExternalVariableTargetResults);
        }

        return results;
    }

    @SuppressWarnings({"unchecked"})
    private SortedSet<Handle> execExternalVariableQuery() {
        MultiSortedSet<Handle> extVariableResults = new MultiSortedSet<Handle>();
        SortedSet<Handle> results;
        AtomicAdpodIndex index = CampaignDB.getInstance().getExternalVariableAdPodMappingIndex();

    	// External key value is of the form x2_t1-value1,x2_t1-value2
        int numVarsSpecified = extVarsMap.size();
    	for(int cnt=0; cnt<numVarsSpecified;cnt++){
    		String externalField = extVarsMap.get(cnt);
    		// for each external field, find all the possible unique key values as these are comma separated
    		if(!"".equals(externalField)){
    			List<String> extKeyValues = getKeyValues(cnt,externalField);
    			for(String extKeyValue:extKeyValues){
    				// get all the key values for x2_t1-value1 and x2_t1-value2... x2_t1-valuen
    				results = index.get(extKeyValue);
                	if(results != null) {
                        extVariableResults.add(results);
                    }
    			}
    		}
    	}
        return extVariableResults;
    }

    private SortedSet<Handle> execNonExternalVariableQuery() {
        return CampaignDB.getInstance().getNonExternalVariableAdPodMappingIndex().get(AdpodIndex.EXTERNAL_VARIABLE_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }

    private List<String> getKeyValues(int cnt,String externalField){
    	ArrayList<String> extKeyValues= new ArrayList<String>();
    	// The specified field value maybe multivalued separated by  
		// for example value1,value2
    	String varType = extVars.get(cnt);
    	StringTokenizer tokenizer = new StringTokenizer(externalField,fieldSeparator);
		while(tokenizer.hasMoreTokens()){
			String field = tokenizer.nextToken();
			// key formed for ex: x2_t1-value1
			extKeyValues.add(varType+CampaignXMLConstants.EXT_VAR_APPENDER+field);
		}
		return extKeyValues;
    }
}
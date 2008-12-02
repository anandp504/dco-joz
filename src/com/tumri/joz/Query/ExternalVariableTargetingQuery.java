package com.tumri.joz.Query;

import com.tumri.cma.persistence.xml.CampaignXMLConstants;
import com.tumri.cma.persistence.xml.CampaignXMLDataProviderSAXParserImpl;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.joz.utils.AppProperties;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.data.SortedArraySet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

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
    public void init(){
    	if(extVars == null){
    		extVars = new HashMap<Integer,String>();
    		String externalTargetingVariables = AppProperties.getInstance().getProperty("externalTargetingVariables");
        	if(externalTargetingVariables.equals("") || (externalTargetingVariables==null)){
        		log.error("joz.property externalTargetingVariables not set");
        		externalTargetingVariables="";
        	}
        	StringTokenizer tokenizer = new StringTokenizer(externalTargetingVariables,",");
        	int i =0;
        	while(tokenizer.hasMoreTokens()){
        		String extVar = (String)tokenizer.nextToken();
        		extVars.put(i,extVar);
        		i++;
        	}
    	}
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
        double extVarScore = TargetingScoreHelper.getInstance().getTargetingVariableScore();

    	// External key value is of the form x2_t1-value1,x2_t1-value2
        int numVarsSpecified = extVarsMap.size();
    	for(int cnt=0; cnt<numVarsSpecified;cnt++){
    		String externalField = (String)extVarsMap.get(cnt);
    		// for each external field, find all the possible unique key values as these are comma separated
    		if(!"".equals(externalField)){
    			List<String> extKeyValues = getKeyValues(cnt,externalField);
    			for(String extKeyValue:extKeyValues){
    				// get all the key values for x2_t1-value1 and x2_t1-value2... x2_t1-valuen
    				results = index.get(extKeyValue);
                	//SortedSet<Handle> clonedResults = null;
                	if(results != null) {
                		/*
                        clonedResults = cloneResults(results, extVarScore);
                        if(clonedResults != null) {
                        	extVariableResults.add(clonedResults);
                        }
                        */
                        extVariableResults.add(results);
                    }
    			}
    		}
    	}
        return extVariableResults;
    }

    private SortedSet<Handle> cloneResults(SortedSet<Handle> results, double extVarScore) {
        SortedArraySet<Handle> sortedArraySet = null;
        ArrayList<Handle> list;
        if(results != null) {
            if(results instanceof RWLocked) {
                ((RWLocked)results).readerLock();
            }
            try {
                Iterator<Handle> iterator = results.iterator();
                if(iterator != null) {
                    list = new ArrayList<Handle>();
                    while(iterator.hasNext()) {
                        Handle handle = iterator.next();
                        handle = handle.createHandle(extVarScore);
                        list.add(handle);
                    }
                    sortedArraySet = new SortedArraySet<Handle>(list);
                }
            }
            finally {
                if(results instanceof RWLocked) {
                    ((RWLocked)results).readerUnlock();
                }
            }

        }
        return sortedArraySet;
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
    	String varType = (String)extVars.get(cnt);
    	String fieldSeparator=AppProperties.getInstance().getProperty("com.tumri.joz.multivalue.delimiter");
    	StringTokenizer tokenizer = new StringTokenizer(externalField,fieldSeparator);
		while(tokenizer.hasMoreTokens()){
			String field = (String)tokenizer.nextToken();
			// key formed for ex: x2_t1-value1
			extKeyValues.add(varType+CampaignXMLConstants.EXT_VAR_APPENDER+field);
		}
		return extKeyValues;
    }
}
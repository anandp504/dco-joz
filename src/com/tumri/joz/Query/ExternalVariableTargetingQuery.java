package com.tumri.joz.Query;

import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.campaign.UrlNormalizer;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.RWLocked;
import com.tumri.utils.data.SortedArraySet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;

/**
 * Targeting Query for external variables.
 *
 * @author raghu
 */
public class ExternalVariableTargetingQuery extends TargetingQuery {
	private static final String FIELD_SEPARATOR = ",";
	private static final String EXT_VAR_APPENDER ="-";
	private static int NUM_VARS = 5;
	private String[] extVars={"x2_t1","x2_t2","x2_t3","x2_t4","x2_t5"};
	private ArrayList<String> externalFieldList;

    public ExternalVariableTargetingQuery(String extField1,String extField2,String extField3,String extField4,String extField5) {
    	externalFieldList= new ArrayList<String>();
    	if(extField1 == null)
    		extField1="";
    	externalFieldList.add(0, extField1);
    	
    	if(extField2 == null)
    		extField2="";
    	externalFieldList.add(1, extField2);
    	
    	if(extField3 == null)
    		extField3="";
    	externalFieldList.add(2, extField3);
    	
    	if(extField4 == null)
    		extField4="";
    	externalFieldList.add(3, extField4);
    	
    	if(extField5 == null)
    		extField5="";
    	externalFieldList.add(4, extField5);   	
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
    	for(int cnt=0; cnt<NUM_VARS;cnt++){
    		String externalField = externalFieldList.get(cnt);
    		// for each external field, find all the possible unique key values as these are comma separated
    		if(!"".equals(externalField)){
    			List<String> extKeyValues = getKeyValues(cnt,externalField);
    			for(String extKeyValue:extKeyValues){
    				// get all the key values for x2_t1-value1 and x2_t1-value2... x2_t1-valuen
    				results = index.get(extKeyValue);
                	SortedSet<Handle> clonedResults = null;
                	if(results != null) {
                        clonedResults = cloneResults(results, extVarScore);
                        if(clonedResults != null) {
                        	extVariableResults.add(clonedResults);
                        }
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
    	String varType = extVars[cnt];
    	StringTokenizer tokenizer = new StringTokenizer(externalField,FIELD_SEPARATOR);
		while(tokenizer.hasMoreTokens()){
			String field = (String)tokenizer.nextToken();
			// key formed for ex: x2_t1-value1
			extKeyValues.add(varType+EXT_VAR_APPENDER+field);
		}
		return extKeyValues;
    }
}
package com.tumri.joz.Query;

import com.tumri.cma.persistence.xml.CampaignXMLConstants;
import com.tumri.joz.campaign.CampaignDB;
import com.tumri.joz.index.AdpodIndex;
import com.tumri.joz.index.AtomicAdpodIndex;
import com.tumri.joz.products.Handle;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.targeting.TargetingScoreHelper;
import com.tumri.utils.data.MultiSortedSet;
import com.tumri.utils.data.SortedArraySet;
import org.apache.log4j.Logger;

import java.util.*;

/**
 * Targeting Query for external variables.
 *
 * @author raghu
 */
public class ExternalVariableTargetingQuery extends TargetingQuery {
    private static Logger log = Logger.getLogger (ExternalVariableTargetingQuery.class);
    private static final String fieldSeparator= AppProperties.getInstance().getProperty("com.tumri.joz.multivalue.delimiter");
    private String values = null;
    private String extVarName = null;

    public ExternalVariableTargetingQuery(String extVarName, String queryStr) {
        values = queryStr;
        this.extVarName = extVarName;
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
        SortedSet<Handle> results = null;
        AtomicAdpodIndex index = CampaignDB.getInstance().getExternalVariableAdPodMappingIndex(extVarName);
        StringTokenizer tokenizer = new StringTokenizer(values,fieldSeparator);
        while(tokenizer.hasMoreTokens()){
            String field = tokenizer.nextToken();
            results = index.get(field);
        }
        return results;
    }

    private SortedSet<Handle> execNonExternalVariableQuery() {
        return CampaignDB.getInstance().getNonExternalVariableAdPodMappingIndex(extVarName).get(AdpodIndex.EXTERNAL_VARIABLE_NONE);
    }

    public boolean accept(Handle v) {
        return false;
    }

}
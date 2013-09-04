<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page import="com.tumri.joz.campaign.wm.*" %>
<%@ page import="com.tumri.cma.rules.CreativeSet" %>
<%@ page import="com.tumri.utils.Pair" %>
<%@ page import="com.tumri.utils.data.SortedBag" %>
<%@ page import="com.tumri.joz.rules.ListingClause" %>
<%@ page import="com.tumri.utils.data.RWLocked" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : Recipe Weight Matrix</title>
    <script type="text/javascript">
    </script>
</head>
<style type="text/css">
    .table {
        font-family: Verdana, Arial, Helvetica, sans-serif;
        border-collapse: collapse;
    }

    .table_row {
        background-color: #ffffff;
        font-family: verdana, arial, helvetica, sans-serif;
        font-size: 80%;
        white-space: nowrap;
    }

    .table_header {
        background-color: #A52A2A;
        padding: 1px;
        color: #FFFFFF;
        font-family: verdana, arial, helvetica, sans-serif;
        font-weight: bold;
        padding: 1px;
        text-decoration: none;
    }

    .table_column_header {
        background-color: #FF7F24;
        text-align: center;
        padding: 1px;
        font-family: verdana, arial, helvetica, sans-serif;
        font-weight: bold;
        color: #000000;
        text-align: center;
        padding: 1px;
        text-decoration: none;
        font-size: 90%;
    }

</style>
<body>

<jsp:include page="header.jsp"/>
<div id="homelink" style="text-align: right">
    <a href="/joz/console">home</a>
</div>
<br>

<div id="links">
    <strong>Weight Matrix</strong>
</div>
<br>
<%
    CampaignDB campaignDB = CampaignDB.getInstance();
    VectorDB db = VectorDB.getInstance();
    Iterator<VectorHandle> defHandles = db.getAllDefHandles();
    Iterator<VectorHandle> persHandles = db.getAllPersHandles();
    Iterator<VectorHandle> optHandles = db.getAllOptHandles();

    ExperienceVectorDB edb = ExperienceVectorDB.getInstance();
    Iterator<VectorHandle> eOptHandles = edb.getAllOptHandles();
%>
<table class="table" border="1">
<tr class="table_header">
    <th>AdPod</th>
    <th>Request Context</th>
    <th>Rule Set</th>
</tr>
<tr class="table_column_header">
    <th>Id, Name</th>
    <th>Type, Value(s)</th>
    <th>Creative Set (Default), RecipeId, Weight</th>
</tr>
<%
    while (defHandles.hasNext()) {
        VectorHandleImpl handle = (VectorHandleImpl) defHandles.next();
        int adpodId = handle.getExpId();
        int vectorId = handle.getVectorId();

        AdPod adPod = campaignDB.getAdPod(adpodId);
%>
<tr valign="middle">
    <td align="left" rowspan="2">
        <%if (adPod != null) {%>
        <%=adPod.getId()%>, <a href="/joz/jsp/adPodSelection.jsp?selAdPod=<%=adPod.getId()%>"><%=adPod.getName()%>
    </a>
        <% } else {
            Experience exp = campaignDB.getExperience(adpodId);

            if (exp != null) {%>
        <%=exp.getId()%>, <a href="/joz/jsp/expSelection.jsp?selExp=<%=exp.getId()%>"><%=exp.getName()%>
    </a>
        <% } else { %>
        Adpod/Experience not found in campaignDB: <%=adpodId%>
        <%}%>
        <%}%>
    </td>
    <td align="center">
        Id, Type, Value
    </td>
    <td align="center">
        Id, Name, Weight
    </td>
</tr>

<%
    Map<VectorAttribute, List<Integer>> contextMap = handle.getContextMap();
    Set<VectorAttribute> keys = contextMap.keySet();
%>
<tr>
    <td>
        <ul><%
            for (VectorAttribute k : keys) {
                List<Integer> idList = contextMap.get(k);
                String val = "";
                for (Integer id : idList) {
                    val = val + "," + VectorUtils.getDictValue(k, id);
                }
        %>
            <li><%=vectorId%>=<%=k.name()%><%=val%>
            </li>
            <%
                }
            %></ul>
    </td>
    <%
        SortedBag<Pair<CreativeSet, Double>> ruleList = db.getRules(handle.getOid());
        SortedBag<Pair<ListingClause, Double>> clauses = db.getClauses(handle.getOid());
    %>
    <td>
        <ul><%
            if (ruleList != null) {
                try {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerLock();
                    }
                    for (Pair<CreativeSet, Double> rulePair : ruleList) {
                        CreativeSet cs = rulePair.getFirst();
                        CAM theCAM = cs.getCAM();
                        CAMDimension[] dims = theCAM.getCamDimensions();
                        StringBuilder sbuild = new StringBuilder();
                        for (int i = 0; i < dims.length; i++) {
                            CAMDimension currDim = dims[i];
                            sbuild.append(currDim.getName() + "=");
                            List<String> values = cs.getAttributes(i);
                            if (currDim.getName().equals("RECIPEID")) {
                                for (String s : values) {
                                        Recipe r = CampaignDB.getInstance().getRecipe(Long.parseLong(s));
                                        String url = "<a href=\"/joz/jsp/recipeSelection.jsp?selRecipe=" + r.getId() + "\">" + r.getName() + "</a>";
                                        sbuild.append(s + " " + url + ",");
                                }
                            } else {
                                sbuild.append(cs + ",");
                            }
                        }
                        Double wt = rulePair.getSecond();
        %>
            <li><%=sbuild.toString()%> <%=wt%>
            </li>
            <%
                    }
                } finally {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerUnlock();
                    }
                }
            } else {
            %>
            No Rules Found!
            <%
                }
            %></ul>
    </td>
</tr>
<%
    } // End of def handles loop
%>
<tr class="table_column_header">
    <th>Id, Name</th>
    <th>Type, Value(s)</th>
    <th>Creative Set (Opt), RecipeId, Weight</th>
</tr>
<%
    while (optHandles.hasNext()) {
        VectorHandleImpl handle = (VectorHandleImpl) optHandles.next();

        int adpodId = handle.getExpId();
        int vectorId = handle.getVectorId();

        AdPod adPod = campaignDB.getAdPod(adpodId);
%>
<tr valign="middle">
    <td align="left" rowspan="2">
        <%if (adPod != null) {%>
        <%=adPod.getId()%>, <a href="/joz/jsp/adPodSelection.jsp?selAdPod=<%=adPod.getId()%>"><%=adPod.getName()%>
    </a>
        <% } else {
            Experience exp = campaignDB.getExperience(adpodId);

            if (exp != null) {%>
        <%=exp.getId()%>, <a href="/joz/jsp/expSelection.jsp?selExp=<%=exp.getId()%>"><%=exp.getName()%>
    </a>
        <% } else { %>
        Adpod/Experience not found in campaignDB: <%=adpodId%>
        <%}%>
        <%}%>
    </td>
    <td align="center">
        Id, Type, Value
    </td>
    <td align="center">
        Id, Name, Weight
    </td>
</tr>

<%
    Map<VectorAttribute, List<Integer>> contextMap = handle.getContextMap();
    Set<VectorAttribute> keys = contextMap.keySet();
%>
<tr>
    <td>
        <ul><%
            for (VectorAttribute k : keys) {
                List<Integer> idList = contextMap.get(k);
                String val = "";
                for (Integer id : idList) {
                    val = val + "," + VectorUtils.getDictValue(k, id);
                }
        %>
            <li><%=vectorId%>=<%=k.name()%><%=val%>
            </li>
            <%
                }
            %></ul>
    </td>
    <%
        SortedBag<Pair<CreativeSet, Double>> ruleList = db.getRules(handle.getOid());
        SortedBag<Pair<ListingClause, Double>> clauses = db.getClauses(handle.getOid());
    %>
    <td>
        <ul><%
            if (ruleList != null) {
                try {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerLock();
                    }
                    boolean first = true;
                    for (Pair<CreativeSet, Double> rulePair : ruleList) {
                        CreativeSet cs = rulePair.getFirst();
                        CAM theCAM = cs.getCAM();
                        CAMDimension[] dims = theCAM.getCamDimensions();
                        StringBuilder sbuild = new StringBuilder();
                        StringBuilder headerBuilder = null;
                        if(first){
                            headerBuilder = new StringBuilder();
                            for (int i = 0; i < dims.length; i++) {
                                CAMDimension currDim = dims[i];
                                headerBuilder.append(currDim.getName() + ";");
                            }
                            first = false;
                        }
                        Double wt = rulePair.getSecond();
                        sbuild.append(wt + " : ");
                        sbuild.append(cs);
         if(headerBuilder != null){
        %>
            <li><%=headerBuilder.toString()%></li>
            <%}%>
            <li><%=sbuild.toString()%> <%=wt%>
            </li>
            <%
                    }
                } finally {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerUnlock();
                    }
                }
            } else {
            %>
            No Rules Found!
            <%
                }
            %></ul>
        <ul><%
            if (clauses != null) {
                try {
                    if (clauses instanceof RWLocked) {
                        ((RWLocked) clauses).readerLock();
                    }
                    for (Pair<ListingClause, Double> rulePair : clauses) {
                        ListingClause lc = rulePair.getFirst();
                        Double wt = rulePair.getSecond();
        %>
            <li><%=lc.toString()%> <%=wt%>
            </li>
            <%
                    }
                } finally {
                    if (clauses instanceof RWLocked) {
                        ((RWLocked) clauses).readerUnlock();
                    }
                }
            } else {
            %>
            No Rules Found!
            <%
                }
            %></ul>
    </td>
</tr>
<%
    } // End of opt handles loop
%>
<tr class="table_column_header">
    <th>Id, Name</th>
    <th>Type, Value(s)</th>
    <th>Creative Set (Pers), RecipeId, Weight</th>
</tr>
<%
    while (persHandles.hasNext()) {
        VectorHandleImpl handle = (VectorHandleImpl) persHandles.next();
        int adpodId = handle.getExpId();
        int vectorId = handle.getVectorId();

        AdPod adPod = campaignDB.getAdPod(adpodId);
%>
<tr valign="middle">
    <td align="left" rowspan="2">
        <%if (adPod != null) {%>
        <%=adPod.getId()%>, <a href="/joz/jsp/adPodSelection.jsp?selAdPod=<%=adPod.getId()%>"><%=adPod.getName()%>
    </a>
        <% } else {
            Experience exp = campaignDB.getExperience(adpodId);
            ;
            if (exp != null) {%>
        <%=exp.getId()%>, <a href="/joz/jsp/expSelection.jsp?selExp=<%=exp.getId()%>"><%=exp.getName()%>
    </a>
        <% } else { %>
        Adpod/Experience not found in campaignDB: <%=adpodId%>
        <%}%>
        <%}%>
    </td>
    <td align="center">
        Id, Type, Value
    </td>
    <td align="center">
        Id, Name, Weight
    </td>
</tr>

<%
    Map<VectorAttribute, List<Integer>> contextMap = handle.getContextMap();
    Set<VectorAttribute> keys = contextMap.keySet();
%>
<tr>
    <td>
        <ul><%
            for (VectorAttribute k : keys) {
                List<Integer> idList = contextMap.get(k);
                String val = "";
                for (Integer id : idList) {
                    val = val + "," + VectorUtils.getDictValue(k, id);
                }
        %>
            <li><%=vectorId %>=<%=k.name()%><%=val%>
            </li>
            <%
                }
            %></ul>
    </td>
    <%
        SortedBag<Pair<CreativeSet, Double>> ruleList = db.getRules(handle.getOid());
    %>
    <td>
        <ul><%
            try {
                if (ruleList instanceof RWLocked) {
                    ((RWLocked) ruleList).readerLock();
                }
                for (Pair<CreativeSet, Double> rulePair : ruleList) {
                    CreativeSet cs = rulePair.getFirst();
                    List<String> valueList = cs.getAttributes(0); //For recipe
                    Double wt = rulePair.getSecond();
        %>
            <li><%=valueList.get(0)%> <%=wt%>
            </li>
            <%
                    }
                } finally {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerUnlock();
                    }
                }

            %></ul>
    </td>
</tr>
<%
    } // End of pers handles loop
%>

<tr class="table_column_header">
    <th>ApodId, Name</th>
    <th>Type, Value(s)</th>
    <th>Expience (Opt), Weight</th>
</tr>
<%
    while (eOptHandles.hasNext()) {
        VectorHandleImpl handle = (VectorHandleImpl) eOptHandles.next();

        int adpodId = handle.getExpId();
        int vectorId = handle.getVectorId();

        AdPod adPod = campaignDB.getAdPod(adpodId);
%>

<tr valign="middle">
    <td align="left" rowspan="2">
        <%if (adPod != null) {%>
        <%=adPod.getId()%>, <a href="/joz/jsp/adPodSelection.jsp?selAdPod=<%=adPod.getId()%>"><%=adPod.getName()%>
    </a>
        <% } else {
            Experience exp = campaignDB.getExperience(adpodId);

            if (exp != null) {%>
        <%=exp.getId()%>, <a href="/joz/jsp/expSelection.jsp?selExp=<%=exp.getId()%>"><%=exp.getName()%>
    </a>
        <% } else { %>
        Adpod/Experience not found in campaignDB: <%=adpodId%>
        <%}%>
        <%}%>
    </td>
    <td align="center">
        Id, Type, Value
    </td>
    <td align="center">
        Id, Name, Weight
    </td>
</tr>

<%
    Map<VectorAttribute, List<Integer>> contextMap = handle.getContextMap();
    Set<VectorAttribute> keys = contextMap.keySet();
%>
<tr>
    <td>
        <ul><%
            for (VectorAttribute k : keys) {
                List<Integer> idList = contextMap.get(k);
                String val = "";
                for (Integer id : idList) {
                    val = val + "," + VectorUtils.getDictValue(k, id);
                }
        %>
            <li><%=vectorId%>=<%=k.name()%><%=val%>
            </li>
            <%
                }
            %></ul>
    </td>
    <%
        SortedBag<Pair<Integer, Double>> ruleList = edb.getRules(handle.getOid());
    %>
    <td>
        <ul><%
            if (ruleList != null) {
                try {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerLock();
                    }
                    for (Pair<Integer, Double> rulePair : ruleList) {
                        Integer expId = rulePair.getFirst();
                        StringBuilder sbuild = new StringBuilder();
                        Experience exp = CampaignDB.getInstance().getExperience(expId);
                        String url = "<a href=\"/joz/jsp/expSelection.jsp?selExp=" + (exp!=null?exp.getId():expId) + "\">" + exp!=null?exp.getName():"Experience Not Found" + "</a>";
                        sbuild.append(" " + url + ",");

                        Double wt = rulePair.getSecond();
        %>
            <li><%=sbuild.toString()%> <%=wt%>
            </li>
            <%
                    }
                } finally {
                    if (ruleList instanceof RWLocked) {
                        ((RWLocked) ruleList).readerUnlock();
                    }
                }
            } else {
            %>
            No Rules Found!
            <%
                }
            %></ul>
    </td>
</tr>
<%
    } // End of opt handles loop
%>


</table>


</body>
</html>
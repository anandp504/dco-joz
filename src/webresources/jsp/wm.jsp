<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.CampaignDB" %>
<%@ page language="java" import="com.tumri.utils.strings.StringTokenizer" %>
<%@ page import="com.tumri.joz.campaign.wm.*" %>
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
    WMDB db = WMDB.getInstance();
    Set<Integer> adPodIds = db.getAdpodIds();
%>
<table class="table" border="1">
    <tr class="table_header">
        <th>AdPod</th>
        <th>Request Context</th>
        <th>Recipe/Weight</th>
    </tr>
    <tr class="table_column_header">
        <th>Id, Name</th>
        <th>Type, Value(s)</th>
        <th>Id, Name, Weight</th>
    </tr>
    <%
        for (Integer adPodId : adPodIds) {

            if (null == adPodId) continue;
            AdPod adPod = campaignDB.getAdPod(adPodId);
            if (adPod == null) continue;

            //Now print the data into html.
            int size = db.getWeightDB(adPodId).getNumHandles();
    %>
    <tr valign="middle">
        <td align="left" rowspan="<%=size +1%>">
            <%=adPod.getId()%>, <a href="/joz/jsp/adPodSelection.jsp?selAdPod=<%=adPod.getId()%>"><%=adPod.getName()%>
        </a>
        </td>
        <td align="center">
            Id, Type, Value
        </td>
        <td align="center">
            Id, Name, Weight
        </td>
    </tr>

    <%
        Iterator<WMHandle> iter = db.getWeightDB(adPodId).getAllHandles();

        if (iter != null) {
            while (iter.hasNext()) {
                WMHandle h = iter.next();
                long contextId = h.getOid();
                Map<WMIndex.Attribute, Integer> contextMap = h.getContextMap();
                Set<WMIndex.Attribute> keys = contextMap.keySet();
    %>
    <tr>
        <td>
            <ul><%
                for (WMIndex.Attribute k : keys) {
                    String val = WMUtils.getDictValue(k, contextMap.get(k));
            %>
                <li><%=contextId %>,<%=k.name()%>,<%=val%>
                </li>
                <%
                    }
                %></ul>
        </td>
        <%
            List<RecipeWeight> rwList = h.getRecipeList();
        %>
        <td>
            <ul><%
                for (RecipeWeight rw : rwList) {
                    Recipe r = campaignDB.getRecipe(rw.getRecipeId());
                    if (r != null) {
            %>
                <li><%=r.getId()%>,<a href="/joz/jsp/recipeSelection.jsp?selRecipe=<%=r.getId()%>"><%=r.getName()%>
                </a>,<%=rw.getWeight()%>
                </li>
                <%
                } else {
                %>
                <li>Recipe not found in campaignDB: id: <%=rw.getRecipeId()%> weight: <%=rw.getWeight()%>
                </li>
                <%
                        }
                    }
                %></ul>
        </td>
    </tr>
    <%
            }
        }
    %>

    <%
        }
    %>
</table>


</body>
</html>
<%@ page import="com.tumri.content.data.dictionary.DictionaryManager" %>
<%@ page import="com.tumri.joz.index.ProductAttributeIndex" %>
<%@ page import="com.tumri.joz.products.Handle" %>
<%@ page import="com.tumri.joz.products.IProduct" %>
<%@ page import="com.tumri.joz.products.ProductDB" %>
<%@ page import="com.tumri.joz.utils.IndexUtils" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.SortedSet" %>
<%@ page import="com.tumri.content.data.Product" %>
<%@ page import="com.tumri.utils.dictionary.Dictionary" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <script type="text/javascript">
        function submitQueryForm() {
            var form = document.getElementById("ProdSelForm");
            form.submit();
        }
        ;
    </script>

    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : DictionaryManager Insights content</title>
</head>
<body>
<%
    String indexName = request.getParameter("IndexName");
    java.util.List<Object> results = null;
    if (indexName != null) {
        com.tumri.utils.dictionary.Dictionary d = (Dictionary) com.tumri.content.data.dictionary.DictionaryManager.getInstance().getDictionary(Product.Attribute.valueOf(indexName));
        if (d != null) {
            results = d.getValues();
        }
    }
    Product.Attribute[] caaAttrs = Product.Attribute.values();
%>
<jsp:include page="header.jsp"/>
<div id="homelink" style="text-align: right">
    <a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/jsp/console.jsp">home</a>
</div>
<br>

<div id="links">
    <strong>DictionaryManager</strong>
</div>
<br>

<form id="ProdSelForm" action="/joz/jsp/caaDictionaries.jsp" method="post">
    <table cellpadding="5">
        <tr align="center">
            <th>Select Dictionary to View:</th>
        </tr>
        <tr align="center">
            <td>
                <select id="IndexName" name="IndexName">
                    <%
                        for (Product.Attribute attr : caaAttrs) {
                            String attrName = attr.name();
                            if (attrName == null) {
                                continue;
                            }
                            String selected = "";
                            if (attrName.equalsIgnoreCase(indexName)) {
                                selected = "selected";
                            }
                            com.tumri.utils.dictionary.Dictionary d = DictionaryManager.getDictionary(Product.Attribute.valueOf(attrName));
                            if (d != null) {
                                List l = d.getValues();
                                if (l != null) {
                                    out.print("<option value=\"" + attrName + "\" " + selected + ">" + attrName + " " + l.size() + "</option>");

                                }
                            }
                        }
                    %>
                </select>

            </td>
        </tr>
    </table>
    <input type="button" value="Submit Query" onClick="javascript:submitQueryForm()"/>
</form>
<br>
<!-- Query Results -->
<%
    if (indexName != null && results == null) {
%>
<strong>0 results for <%=indexName%>
</strong> :

<%
} else if (indexName != null && results != null) {
%>
<div>
    <strong>Query results for <%=indexName%> index lookup </strong> (<%=results.size()%> products found)
</div>
<%
    for (Object h : results) {
%>
<%=h.toString()%>,
<% } // End for
} // End if
%>

</body>
</html>

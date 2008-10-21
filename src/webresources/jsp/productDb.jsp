<%@ page import="com.tumri.content.data.dictionary.DictionaryManager" %>
<%@ page import="com.tumri.joz.index.ProductAttributeIndex" %>
<%@ page import="com.tumri.joz.products.Handle" %>
<%@ page import="com.tumri.joz.products.IProduct" %>
<%@ page import="com.tumri.joz.products.ProductDB" %>
<%@ page import="com.tumri.joz.utils.IndexUtils" %>
<%@ page import="java.util.Enumeration" %>
<%@ page import="java.util.SortedSet" %>
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
    <title>Joz Console : Product DB</title>
</head>
<body>
<%
        Enumeration<IProduct.Attribute> indices = ProductDB.getInstance().getIndices();
        String indexName = request.getParameter("IndexName");
        String indexKey = request.getParameter("IndexKey");

        SortedSet<Handle> results = null;
        if (indexName!=null && indexKey!=null) {
            //Do a lookup
            IProduct.Attribute kAttr = IndexUtils.getAttribute(indexName);
            ProductAttributeIndex theIndex=ProductDB.getInstance().getIndex(IndexUtils.getAttribute(indexName));
            Integer keyId = DictionaryManager.getInstance().getId (kAttr, indexKey);
            if (theIndex!= null && keyId!=null) {
                results = theIndex.get(keyId);
            }
        }
    %>
  	<jsp:include page="header.jsp"/>
	<div id="homelink" style="text-align: right">
		<a href="http://<%=request.getServerName()%>:<%=request.getServerPort()%>/joz/jsp/console.jsp">home</a>
	</div>
	<br>
    <div id="links">
		  <strong>Product DB Status</strong>
        <table>
            <tr>
                <td>Number of products : </td>
                <td><%=ProductDB.getInstance().getSize()%></td>
            </tr>
         </table>

	</div>
	<br>
        <form id="ProdSelForm" action="/joz/jsp/productDb.jsp" method="post">
            <table>
                <tr>
                    <td>
                        <div>
                            <strong>Select Index to Query</strong>
                        </div>
                        <select id="IndexName" name="IndexName">
                            <%
                                while(indices.hasMoreElements()) {
                                    IProduct.Attribute attr = indices.nextElement();
                                    String attrName = IndexUtils.getIndexName(attr);
                                    if (attrName==null) {
                                        continue;
                                    }
                                    String selected = "";
                                    if (attrName.equalsIgnoreCase(indexName)) {
                                      selected = "selected";
                                    }
                                    out.print("<option value=\""+attrName+"\" " + selected + ">"+attrName + "</option>");
                                }
                            %>
                        </select>

                    </td>
                    <td>
                        <div>
                            <strong>Enter value of the index</strong>
                        </div>
                        <input type="text"  name="IndexKey"  id="indexKey" value="<%=indexKey!=null?indexKey:""%>"/>
                    </td>
                </tr>
            </table>
            <input type="button" value="Submit Query" onClick="javascript:submitQueryForm()"/>
        </form>
        <!-- Query Results -->
        <%
            if (indexName!=null && indexKey!=null && results==null) {
        %>
        <strong>0 results for <%=indexName%> index lookup for key <%=indexKey%></strong> :

        <%
            } else if (indexName!=null && indexKey!=null && results!=null) {
        %>
         <div>
                <strong>Query results for <%=indexName%> index lookup for key <%=indexKey%></strong> (<%=results.size()%> products found)%>:
         </div>
        <%
                for (Handle h : results) {
        %>
                    <%=h.getOid()%>,
        <%      } // End for
            } // End if
        %>

  </body>
</html>

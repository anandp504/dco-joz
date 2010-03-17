<%@ page language="java" import="java.util.*" %>
<%@ page language="java" import="com.tumri.joz.monitor.*" %>
<%@ page language="java" import="com.tumri.joz.campaign.*" %>
<%@ page language="java" import="com.tumri.joz.products.JOZTaxonomy" %>
<%@ page language="java" import="com.tumri.content.data.Category" %>
<%@ page language="java" import="com.tumri.cma.domain.*" %>
<%@ page language="java" import="com.tumri.joz.utils.*" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : TSpec Information</title>

    <style>
        .floatingDiv {
            position: absolute;
            border: solid 1px blue;
            display: none;
            width: 325px;
            height: 300px;
        }
    </style>

    <script type="text/javascript">
        function submitProductForm(prodInfo2) {
            var selProducts = document.getElementById("selProducts");
            selProducts.value = prodInfo2;
            var form = document.getElementById("ProductsSelForm");
            form.submit();
        }
        ;
        function submitOSpecForm(oSpecId) {
            var selOSpec = document.getElementById("selOSpec");
            selOSpec.value = oSpecId;
            var form = document.getElementById("OSpecSelForm");
            form.submit();
        }
        ;
        function saveTSpec() {
            var perfForm = document.getElementById("SaveTSpec");
            perfForm.submit();
        }
    </script>
</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console?mode=ad">get-ad-data</a>
    <a href="/joz/console">home</a>
</div>

<%
    //get TSpec from provided TSpecId
    CampaignDB campaignDB = CampaignDB.getInstance();
    String TSpecName = request.getParameter("selTSpec");
    if (TSpecName == null) {
        TSpecName = (String) session.getAttribute("selTSpec");
    } else {
        session.setAttribute("selTSpec", TSpecName);
    }
    TSpec myTSpec = campaignDB.getTspec(Integer.parseInt(TSpecName));

%>
<div>
    TSpec Information
</div>
<br>
<%if (myTSpec == null) {%>
No TSpec Found in Campaign DB for ID: <%=TSpecName%>
<%} else {%>
<form id="SaveTSpec" action="/joz/console?mode=dl&option=tspec&id=<%=myTSpec.getId()%>" method="post">
    <input type="button" value="Save TSpec" onClick="javascript:saveTSpec()"/>
</form>
<br>
<table border="1">
    <tr>
        <td>
            <div>
                <form id="ProductsSelForm" action="/joz/jsp/products.jsp" method="post">
                    <%
                        String prodInfo = TSpecName;
                    %>
                    <input type="hidden" name="selProducts" id="selProducts" value=""/>
                    <input type="button" value="View Products" onClick="submitProductForm('<%=prodInfo%>')"/>
                </form>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Id</strong> = " + myTSpec.getId() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Name</strong> = " + myTSpec.getName() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Low Price</strong> = " + myTSpec.getLowPrice() + "<br>");
                %>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: OSpec Id</strong> = " + myTSpec.getOspecId() + " ");
                %>
                <form id="OSpecSelForm" action="/joz/jsp/oSpecSelection.jsp" method="post">
                    <input type="hidden" name="selOSpec" id="selOSpec" value=""/>
                    <input type="button" value="View OSpec" onClick="submitOSpecForm('<%=myTSpec.getOspecId()%>')"/>
                </form>
            </div>
            <br>
        </td>
        <td>
            <div>
                <%
                    Date creationDate = myTSpec.getCreationDate();
                    if (creationDate == null) {
                        out.print("<strong>TSpec: Creation Date</strong> = " + creationDate + "<br>");
                    } else {
                        out.print("<strong>TSpec: Creation Date</strong> = " + creationDate.toString() + "<br>");
                    }
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: SpecType = </strong>" + myTSpec.getSpecType() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: High Price = </strong>" + myTSpec.getHighPrice() + "<br>");
                %>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Radius</strong> = " + myTSpec.getRadius() + " ");
                %>
            </div>
            <br>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Use Radius Query</strong> = " + myTSpec.isUseRadiusQuery() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Geo Enable Flag = </strong>" + myTSpec.isGeoEnabledFlag() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Apply Geo Filter = </strong>" + myTSpec.isApplyGeoFilter() + "<br>");
                %>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <ul>
                <li>Apply External Filter1: <%=myTSpec.isUseListingFilter1()%>
                </li>
                <li>Apply External Filter2: <%=myTSpec.isUseListingFilter2()%>
                </li>
                <li>Apply External Filter3: <%=myTSpec.isUseListingFilter3()%>
                </li>
                <li>Apply External Filter4: <%=myTSpec.isUseListingFilter4()%>
                </li>
                <li>Apply External Filter5: <%=myTSpec.isUseListingFilter5()%>
                </li>
            </ul>
            <br>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Keyword Type</strong> = " + myTSpec.getKeywordSource() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Apply URL Filter = </strong>" + myTSpec.isApplyUrlFilter() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Mine Pub URL = </strong>" + myTSpec.isMinePubUrl() + "<br>");
                %>
            </div>
        </td>
    </tr>
    <tr>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Publish URL Keywords within OSpec</strong> = " + myTSpec.isPublishUrlKeywordsWithinOSpec() + " ");
                %>
            </div>
            <br>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Script Keywords within OSpec</strong> = " + myTSpec.isScriptKeywordsWithinOSpec() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Too Small Leaf Categories = </strong>" + myTSpec.isTooSmallLeafCategories() + "<br>");
                %>
            </div>
        </td>
        <td>
            <div>
                <%
                    out.print("<strong>TSpec: Backfill Enabled = </strong>" + myTSpec.isEnableBackFill() + "<br>");
                %>
            </div>
        </td>
    </tr>
</table>

<%
    List<BrandInfo> excludedBrands = myTSpec.getExcludedBrands();
    List<BrandInfo> includedBrands = myTSpec.getIncludedBrands();
    List<MerchantInfo> excludedMerchants = myTSpec.getExcludedMerchants();
    List<MerchantInfo> includedMerchants = myTSpec.getIncludedMerchants();
    List<CategoryInfo> excludedCategories = myTSpec.getExcludedCategories();
    List<CategoryInfo> includedCategories = myTSpec.getIncludedCategories();
    List<ProviderInfo> excludedProviders = myTSpec.getExcludedProviders();
    List<ProviderInfo> includedProviders = myTSpec.getIncludedProviders();
    List<ProductInfo> excludedProducts = myTSpec.getExcludedProducts();
    List<ProductInfo> includedProducts = myTSpec.getIncludedProducts();
    String includedKeywords = myTSpec.getLoadTimeKeywordExpression();
    out.print("<table border = \"1\">");

    out.print("<tr>");
    out.print("<td>");

    out.print("<strong>TSpec: Included Brands:</strong>");
    out.print("<br>");
    if (includedBrands != null) {
        for (int i = 0; i < includedBrands.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + includedBrands.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");

    out.print("<td>");

    out.print("<strong>TSpec: Excluded Brands:</strong>");
    out.print("<br>");
    if (excludedBrands != null) {

        for (int i = 0; i < excludedBrands.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + excludedBrands.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");

    out.print("<strong>TSpec: Included Merchants:</strong>");
    out.print("<br>");
    if (includedMerchants != null) {
        for (int i = 0; i < includedMerchants.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + includedMerchants.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");
    out.print("<strong>TSpec: Excluded Merchants:</strong>");
    out.print("<br>");
    if (excludedMerchants != null) {
        for (int i = 0; i < excludedMerchants.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + excludedMerchants.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("</tr>");
    out.print("<tr>");
    out.print("<td>");
    out.print("<strong>TSpec: Included Categories:</strong>");
    out.print("<br>");
    if (includedCategories != null) {
        for (int i = 0; i < includedCategories.size(); i++) {
            Category cat = null;
            String displayName = includedCategories.get(i).getDisplayName();
            try {
                cat = JOZTaxonomy.getInstance().getTaxonomy().getCategory(displayName);
                displayName = cat.getName();
            }
            catch (NullPointerException npe) {
                // do nothing
            }
            out.print("&nbsp;&nbsp;&nbsp;" + displayName);
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");
    out.print("<strong>TSpec: Excluded Categories:</strong>");
    out.print("<br>");
    if (excludedCategories != null) {
        for (int i = 0; i < excludedCategories.size(); i++) {
            Category cat = null;
            String displayName = excludedCategories.get(i).getDisplayName();
            try {
                cat = JOZTaxonomy.getInstance().getTaxonomy().getCategory(displayName);
                displayName = cat.getName();
            }
            catch (NullPointerException npe) {
                // do nothing
            }
            out.print("&nbsp;&nbsp;&nbsp;" + displayName);
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");
    out.print("<strong>TSpec: Included Providers:</strong>");
    out.print("<br>");
    if (includedProviders != null) {
        for (int i = 0; i < includedProviders.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + includedProviders.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");
    out.print("<strong>TSpec: Excluded Providers:</strong>");
    out.print("<br>");
    if (excludedProviders != null) {
        for (int i = 0; i < excludedProviders.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + excludedProviders.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("</tr>");
    out.print("<tr>");
    out.print("<td>");
    out.print("<strong>TSpec: Included Products:</strong>");
    out.print("<br>");
    if (includedProducts != null) {
        for (int i = 0; i < includedProducts.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + includedProducts.get(i).getName());
            out.print("<br>");
        }
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");
    out.print("<strong>TSpec: Excluded Products:</strong>");
    out.print("<br>");
    if (excludedProducts != null) {
        for (int i = 0; i < excludedProducts.size(); i++) {
            out.print("&nbsp;&nbsp;&nbsp;" + excludedProducts.get(i).getName());
            out.print("<br>");
        }
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("<td>");
    out.print("<strong>TSpec: Keyword:</strong>");
    out.print("<br>");
    if (includedKeywords != null) {
        out.print("&nbsp;&nbsp;&nbsp;" + includedKeywords);
        out.print("<br>");
    } else {
        out.print("&nbsp;&nbsp;&nbsp; Null <br><br>");
    }
    out.print("</td>");
    out.print("</tr>");
    out.print("</table>");
%>
<br>
<%}%>
</body>
</html>
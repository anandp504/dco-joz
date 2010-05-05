<%@ page import="com.tumri.content.ContentProvider" %>
<%@ page import="com.tumri.content.ContentProviderFactory" %>
<%@ page import="com.tumri.content.data.ContentProviderStatus" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="com.tumri.joz.jozMain.ListingProviderFactory" %>
<%@ page import="com.tumri.joz.products.JOZTaxonomy" %>
<%@ page import="com.tumri.joz.jozMain.MerchantDB" %>
<%@ page import="com.tumri.joz.products.ProductDB" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
  <head>
          <meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
          <title>Joz Console : Refresh-Data</title>
  </head>
  <body>
         <%
             String success = null;
             String datetime = null;
             String requestValue = request.getQueryString();
             if ((requestValue == null) || requestValue.equals("qac")) {
                 //Clear the product db when refreshing for qac
                 ProductDB.getInstance().clearProductDB();
                 ContentProviderFactory f = ContentProviderFactory.getDefaultInitializedInstance();
                 ContentProvider cp = f.getContentProvider();
                 cp.refresh(null);
                 ContentProviderStatus status = cp.getStatus();
                 //Invoke the content refresh on Listings Data client
                 ListingProviderFactory.refreshData(JOZTaxonomy.getInstance().getTaxonomy(),
                         MerchantDB.getInstance().getMerchantData());

                 success = (status.lastRunStatus == true ? "successful" : "failed");
                 datetime = (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss,SSS")).format(status.lastRefreshTime);
             }
         %>
        <%
                if (requestValue == null) {
        %>
        <jsp:include page="header.jsp"/>
        <div id="homelink" style="text-align: right">
                <a href="/joz/console">home</a>
        </div>
        <br>
        <div>
                <p>Refresh status:&nbsp;&nbsp; <%=success%> </p>
                <p>Refresh time: &nbsp;&nbsp; <%=datetime%> </p>
        </div>
        <br>
        <% }
                 else if (requestValue.equals("qac")) {
                        out.print(success);
                 }
        %>
  </body>
</html>

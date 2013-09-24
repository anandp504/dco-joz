<%@ page language="java" import="com.tumri.joz.server.domain.JozAdRequest" %>
<%@ page import="com.tumri.joz.server.domain.JozAdResponse" %>
<%@ page import="com.tumri.cma.domain.Advertiser" %>
<%@ page import="com.tumri.utils.Pair" %>
<%@ page import="java.util.*" %>
<%@ page import="com.tumri.joz.monitor.AdRequestMonitor" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<%@ page language="java" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
    <title>Joz Console : View Joz Request and Response</title>

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
        function displayAdvertisers() {
            if (document.ReqResForm.advertiserorcampaign[0].checked) {
                var advertiserVal = document.ReqResForm.advertiserorcampaign[0].value;
                if (advertiserVal != null) {
                    document.ReqResForm.advertiserorcampaign[0].disabled = false;
                    document.getElementById("advButton").disabled = false;
                    document.getElementById("advButton").style.visibility = "visible";
                    document.getElementById("AdvertiserList").disabled = false;
                    document.getElementById("AdvertiserList").style.visibility = "visible";
                    document.getElementById("AdvertiserList").style.display = "list-item";

                    document.ReqResForm.advertiserorcampaign[1].disabled = false;
                    document.getElementById("camButton").disabled = true;
                    document.getElementById("camButton").style.visibility = "hidden";
                    document.getElementById("CampaignList").style.visibility = "hidden";

                }
            }

        }
        ;
        function displayCampaigns() {
            if (document.ReqResForm.advertiserorcampaign[1].checked) {
                var campaignVal = document.ReqResForm.advertiserorcampaign[1].value;
                if (campaignVal != null) {
                    document.ReqResForm.advertiserorcampaign[1].disabled = false;
                    document.getElementById("camButton").style.visibility = "visible";
                    document.getElementById("camButton").disabled = false;
                    document.getElementById("CampaignList").disabled = false;
                    document.getElementById("CampaignList").style.visibility = "visible";
                    document.getElementById("CampaignList").style.display = "list-item"

                    document.ReqResForm.advertiserorcampaign[0].disabled = false;
                    document.getElementById("advButton").disabled = true;
                    document.getElementById("advButton").style.visibility = "hidden";
                    document.getElementById("AdvertiserList").style.visibility = "hidden";
                }
            }
        }
        ;
        function displayRequestResponseForAdvertiser() {
            var obj = document.getElementById("AdvertiserList");
            var selAdvertiser = document.getElementById("selAdvertiser");
            selAdvertiser.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("ReqResForm");
            document.getElementById("requestType").value = "Advertiser";
            form.submit();
        }
        ;
        function displayRequestResponseForCampaign() {
            var obj = document.getElementById("CampaignList");
            var selCampaign = document.getElementById("selCampaign");
            selCampaign.value = obj.options[obj.selectedIndex].value;
            var form = document.getElementById("ReqResForm");
            document.getElementById("requestType").value = "Campaign";
            form.submit();
        }
        ;

    </script>
</head>
<body>
<jsp:include page="header.jsp"/>
<div id="links" style="text-align: right">
    <a href="/joz/console">home</a>
</div>
<div>
    <form id="ReqResForm" name="ReqResForm" action="/joz/jsp/adRequest.jsp" method="get">
     <div>
        <input type="radio" name="advertiserorcampaign" value="advertiser"
               onclick="javascript:displayAdvertisers()">Advertiser<br>

        <select id="AdvertiserList" name="advertiser" style="display: none">
            <option value="advertiser" selected>- -Select - -</option>
            <%
                List<String> advertisers = AdRequestMonitor.getInstance().getAllAdvertiser();

                for (int i = 0; i < advertisers.size(); i++) {
                    if (advertisers.get(i) != null) {
                        out.print("<option value=\"" + advertisers.get(i) + "\">" + advertisers.get(i) + "</option>");
                    }
                }
            %>
        </select>
        <input type="hidden" id="requestType" name="requestType" value="">
        <input type="hidden" name="selAdvertiser" id="selAdvertiser" value=""/>
        <input type="button" value="Get AdRequestResponse" id="advButton" style="visibility: hidden"
               onClick="javascript:displayRequestResponseForAdvertiser()">
     </div>
        <div>
        <input type="radio" name="advertiserorcampaign" value="campaign" onclick="javascript:displayCampaigns()">Campaign<br>
        <select id="CampaignList" name="campaign" style="display: none">
            <option value="campaign" selected>- -Select - -</option>
            <%
                List<String> campaigns = AdRequestMonitor.getInstance().getAllCampaign();

                for (int i = 0; i < campaigns.size(); i++) {
                    if (campaigns.get(i) != null) {
                        out.print("<option value=\"" + campaigns.get(i) + "\">" + campaigns.get(i) + "</option>");
                    }
                }
            %>
        </select>
        <input type="hidden" name="selCampaign" id="selCampaign" value=""/>
        <input type="button" value="Get CamRequestResponse" id="camButton" style="visibility: hidden;"
               onClick="javascript:displayRequestResponseForCampaign()">
        </div>
    </form>
</div>
<%
    JozAdRequest adReq = null;
    JozAdResponse adResp = null;

    String adv = request.getParameter("selAdvertiser");
    String cam = request.getParameter("selCampaign");
    String requestType = request.getParameter("requestType");
    if (requestType == null) {
        requestType = "";
    }
    AdRequestMonitor requestResponseCache = AdRequestMonitor.getInstance();

    if ((requestType.equals("Advertiser")) && (!requestResponseCache.isRequestResponseCacheEmpty())) {

            if(adv.equals("advertiser")){
                out.println("<p><font color=red> Advertiser not selected, please do it !</font></p>");
            }else{
            //Latest RequestResponse for advertiser :adv
            Pair<JozAdRequest, JozAdResponse> reqResPair = requestResponseCache.getRequestResponsePairForAdvertiser(adv);
            adReq = reqResPair.getFirst();
            adResp = reqResPair.getSecond();
            }
    }else if ((requestType.equals("Campaign")) && (!requestResponseCache.isRequestResponseCacheEmpty())) {

        if(cam.equals("campaign")){
            out.println("<p><font color=red> Campaign not selected, please do it !</font></p>");
        }else{
            //Latest RequestResponse for campaign :cam
            Pair<JozAdRequest, JozAdResponse> reqResPair = requestResponseCache.getRequestResponsePairForCampaign(cam);
            adReq = reqResPair.getFirst();
            adResp = reqResPair.getSecond();
        }
    }else{
           if(requestResponseCache.isRequestResponseCacheEmpty())
            out.println("<p><font color=red> RequestResponse cache is empty !</font></p>");

     adReq = (JozAdRequest) request.getAttribute("adReq");
     adResp = (JozAdResponse) request.getAttribute("adResp");
    }
%>

<br>
<b>JOZ AD REQUEST:</b>
<br>
<textarea id="text_eval_expr" name="text_eval_expr" style="width:100%;height:5cm"><%
    if (adReq != null) {
        HashMap<String, String> requestMap = adReq.getRequestMap();
        Iterator<String> requestIter = requestMap.keySet().iterator();
        while (requestIter.hasNext()) {
            String resultKey = requestIter.next();
            String resultVal = requestMap.get(resultKey);

            if (resultKey.equals("externalfilterf1")) {
                resultKey = "f1";
            } else if (resultKey.equals("externalfilterf2")) {
                resultKey = "f2";
            } else if (resultKey.equals("externalfilterf3")) {
                resultKey = "f3";
            } else if (resultKey.equals("externalfilterf4")) {
                resultKey = "f4";
            } else if (resultKey.equals("externalfilterf5")) {
                resultKey = "f5";
            } else if (resultKey.equals("externaltargett1")) {
                resultKey = "t1";
            } else if (resultKey.equals("externaltargett2")) {
                resultKey = "t2";
            } else if (resultKey.equals("externaltargett3")) {
                resultKey = "t3";
            } else if (resultKey.equals("externaltargett4")) {
                resultKey = "t4";
            } else if (resultKey.equals("externaltargett5")) {
                resultKey = "t5";
            }
            if (resultVal != null) {
%>:<%=resultKey%> <%=resultVal%> <%
            }//end if
        }//end while
    }//end if

%>
</textarea>
<br>
<br>
<b>JOZ AD RESPONSE:</b>
<br>
<textarea id="text_eval_expr" name="text_eval_expr" style="width:100%;height:17cm">
    <%

        if (adResp != null) {
            HashMap<String, String> resultMap = adResp.getResultMap();
            Iterator<String> resultIter = resultMap.keySet().iterator();
            while (resultIter.hasNext()) {
                String resultKey = resultIter.next();
                String resultVal = resultMap.get(resultKey);
    %>
    <%=resultKey%> = <%=resultVal%>
    <%
            }//end while
        }//end if
    %>

</textarea>
</body>
</html>
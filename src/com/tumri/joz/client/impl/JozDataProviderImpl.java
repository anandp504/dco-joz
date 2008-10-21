package com.tumri.joz.client.impl;

import com.thoughtworks.xstream.XStream;
import com.tumri.cma.domain.Campaign;
import com.tumri.cma.domain.TSpec;
import com.tumri.joz.client.JoZClientException;
import com.tumri.joz.client.JozDataProvider;
import com.tumri.joz.client.helper.*;
import com.tumri.joz.server.domain.*;
import com.tumri.utils.tcp.client.TcpSocketConnectionPool;
import org.apache.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Properties;
/**
 * Implements the JozDataProvider interface and provides a single entry point to all interactions
 * in JoZ. Usage is as follows:
 * <p>
 * <code>
 * JozMerchantRequest aquery = new JozMerchantRequest(); <br>
 * JozDataProvider provider = new JozDataProviderImpl(host, port,poolSize,numRetries);<br>
 * List<JozMerchant> merchants = provider.getMerchants(aquery).getMerchants();<br>
 * </code>
 * <p>
 */
public class JozDataProviderImpl implements JozDataProvider {
    private static Logger log = Logger.getLogger(JozDataProviderImpl.class);
    private static String g_AppPropertyFile = "joz_client.properties";
    private static String JOZ_SERVER_HOSTNAME="joz_server_host";
    private static String JOZ_SERVER_PORT="joz_server_port";
    private static String JOZ_POOLSIZE="joz_poolsize";
    private static String JOZ_NUM_RETRIES="joz_num_retries";
    private Properties m_properties;
    private static boolean bInit = false;

    /**
     * Instantiate the data provider by looking for a properties file called joz_client.properties in the classpath
     */
    public JozDataProviderImpl() throws JoZClientException {
        try{
            InputStream is = getInputStream();
            if (is != null) {
                try {
                    Properties p = new Properties();
                    BufferedInputStream bis = new BufferedInputStream(is);
                    p.load(bis);
                    m_properties = p; 
                } finally {
                    is.close();
                }
            } else if (m_properties == null) {
                m_properties = new Properties();
            }
            // Read the properties for
            String host = getProperty(JOZ_SERVER_HOSTNAME);
            int port = Integer.parseInt(getProperty(JOZ_SERVER_PORT));
            int poolSize = Integer.parseInt(getProperty(JOZ_POOLSIZE));
            int numRetries = Integer.parseInt(getProperty(JOZ_NUM_RETRIES));
            init(host, port, poolSize, numRetries);
        } catch(IOException ioEx){
            log.error("joz client properties file "+g_AppPropertyFile+" not found");
            throw new JoZClientException(ioEx);
        }
    }


    public JozDataProviderImpl(String host,int port,int poolSize, int numRetries) {
       this.init(host, port, poolSize, numRetries);
    }


    public String getProperty(String attr) {
        return m_properties.getProperty(attr);
    }

    public Properties getProperties() {
        return m_properties;
    }

    /**
     * Instantiate the jozclient
     * @throws IOException
     */
    public void init(String host,int port,int poolSize,int numRetries){
        if (bInit) {
            log.warn("JozDataProvider already initialized");
            return;
        }
        TcpSocketConnectionPool.getInstance().init(host, port, poolSize, numRetries);
        bInit = true;
    }

    /**
     * Release all the resources needed
     */
    public void shutdown() {
        TcpSocketConnectionPool.getInstance().tearDown();
    }

    private static InputStream getInputStream() {
        InputStream is =  JozDataProviderImpl.class.getClassLoader().getResourceAsStream(g_AppPropertyFile);
        if (is == null) {
            log.debug("Could not locate the resource file "+g_AppPropertyFile + ". Will try using catalina.base property if its tomcat");
            String catalinaBase = System.getProperty("catalina.base");
            if (catalinaBase != null) {
                String confFile = catalinaBase + File.separator + "conf" + File.separator + g_AppPropertyFile;
                try {
                    is = new FileInputStream(confFile);
                } catch (FileNotFoundException ex) {
                    log.debug("Could not locate the resource file "+g_AppPropertyFile + "in tomcat conf directory. Will try ../conf");
                }
            } else {
                log.debug("Could not locate the resource file "+g_AppPropertyFile + " in tomcat as catalina.base is not defined. Will try ../conf");
            }
            if (is == null) {
                try {
                    is =  new FileInputStream("../conf/" + g_AppPropertyFile);
                } catch (FileNotFoundException ex) {
                    log.debug("Couldn't find file " + g_AppPropertyFile + " in ../conf directory. Failing.");
                }
            }
            if (is == null) {
                String message = "Couldn't locate resource file " + g_AppPropertyFile + " in classpath, catalina.base/conf or ../conf directory.";
                log.error(message);
            }
        }
        return is;
    }
    /**
     * Process the Ad Request and give back a response.
     * The JozAdRequest internally has a HashMap which is set by calling various accessor methods
     * The JozAdResponse internally has a HashMap which has accessor method to get the the various results.
     *
     * The response will contain information about the products as a JSON String, similar to what iCS gets
     * which will contain the meta information embedded.
     *
     * There are 2 flavours of the Joz Ad Request, one for ICS which will not specify a Listing Query,Recipe and pagination
     * parameters and another that will. The latter will be used by the TCM portal.
     * @param adRequest the JozAdRequest object 
     * @return JozAdResponse that encapsulates the ad data
     * @throws JoZClientException
     */
    public JozAdResponse getAdData(JozAdRequest adRequest) throws JoZClientException {
        JozAdResponse resp;
        try {
            JozAdDataProvider dataProvider = new JozAdDataProvider();
            resp = dataProvider.processRequest(adRequest);
        } catch (Throwable e) {
            log.error("Exception in fetching ad data", e);
            throw new JoZClientException("Exception in fetching ad data",e);
        }
        return resp;
    }
    /**
     * Get all the merchants loaded into Joz. At present Joz does not handle merchants that are specific
     * to providers.
     * The request is a object that internally has a HashMap with accessor methods to set any inputs.
     * Currently this input is none.
     * Response is an JozResponse that encapsulates the merchant data
     * @param merchantRequest the JozMerchantRequest object
     * @return JozResponse that encapsulates the merchant data
     * @throws JoZClientException
     */
    public JozResponse getMerchants(JozMerchantRequest merchantRequest) throws JoZClientException {
        JozResponse merchants = null;
        try {

            JozMerchantDataProvider dataProvider = new JozMerchantDataProvider();
            JozMerchantResponse res = dataProvider.processRequest(merchantRequest);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozMerchantResponse.KEY_MERCHANTS)) {
                    XStream xstream = new XStream();
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("getmerchant", ArrayList.class);
                    xstream.alias("merchant", JozMerchant.class);
                    xstream.useAttributeFor("id", String.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("count", String.class);
                    xstream.useAttributeFor("logourl", String.class);
                    xstream.useAttributeFor("catalogfilename", String.class);
                    xstream.useAttributeFor("catalogproductcount", String.class);
                    xstream.useAttributeFor("collectstax", String.class);
                    xstream.useAttributeFor("contactinfo", String.class);
                    xstream.useAttributeFor("hascatalogname", String.class);
                    xstream.useAttributeFor("homepageurl", String.class);
                    xstream.useAttributeFor("merchant", String.class);
                    xstream.useAttributeFor("merchantrating", String.class);
                    xstream.useAttributeFor("returnpolicytext", String.class);
                    xstream.useAttributeFor("reviewinfo", String.class);
                    xstream.useAttributeFor("shippingpromotext", String.class);
                    xstream.useAttributeFor("suppliescategory", String.class);

                    String xml = resultMap.get(s);
                    merchants = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }

        } catch (Throwable e) {
            log.error("Exception in fetching merchant data", e);
            throw new JoZClientException("Exception in fetching merchant data",e);
        }
        return merchants;
    }
    /**
     * Get all the providers loaded into Joz.
     * Response is an JozResponse that encapsulates the provider data
     * @param providerRequest the JozProviderRequest object
     * @return JozResponse that encapsulates the provider data
     * @throws JoZClientException
     */
    public JozResponse getProviders(JozProviderRequest providerRequest) throws JoZClientException {
        JozResponse providers = null;
        try {
            JozProviderDataProvider dataProvider = new JozProviderDataProvider();

            JozProviderResponse res = dataProvider.processRequest(providerRequest);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozProviderResponse.KEY_PROVIDERS)) {
                    XStream xstream = new XStream();
                    // set the aliases
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("getprovider", ArrayList.class);
                    xstream.alias("provider", JozProvider.class);
                    xstream.useAttributeFor("id", String.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("logourl", String.class);

                    String xml = resultMap.get(s);
                    providers = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in fetching provider data", e);
            throw new JoZClientException("Exception in fetching provider data",e);
        }
        return providers;
    }
    /**
     * Get the global taxonomy that is loaded into Joz. This includes the Tumri and the provider taxonomies.
     * The request can contain additional details of which node of Taxonomy that will be returned, but at this point
     * the Taxonomy request will return the entire tree.
     * The response is an JozResponse that encapsulates the taxonomy data
     * @param taxonomyRequest the JozTaxonomyRequest object
     * @return JozResponse that encapsulates the taxonomy data
     * @throws JoZClientException
     */
    public JozResponse getTaxonomy(JozTaxonomyRequest taxonomyRequest) throws JoZClientException {
        JozResponse tax = null;
        try {
            JozTaxonomyDataProvider dataProvider = new JozTaxonomyDataProvider();
            JozTaxonomyResponse res = dataProvider.processRequest(taxonomyRequest);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTaxonomyResponse.KEY_TAXONOMY)) {

                    XStream xstream = new XStream();
                    // set the aliases
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("gettaxonomy", JozTaxonomy.class);
                    xstream.alias("category", JozCategory.class);
                    xstream.useAttributeFor("id", String.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("count", String.class);
                    xstream.useAttributeFor("glassIdStr", String.class);

                    String xml = resultMap.get(s);
                    tax = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in fetching taxonomy data", e);
            throw new JoZClientException("Exception in fetching taxonomy data",e);
        }
        return tax;
    }
    /**
     * Get the category , taxonomy and merchant counts data
     * The response is an JozResponse that encapsulates the counts data
     * @param countRequest the JozCountRequest object 
     * @return JozResponse that encapsulates the counts data
     * @throws JoZClientException
     */
    public JozResponse getCountData(JozCountRequest countRequest) throws JoZClientException {
        JozResponse counts = null;
        try {
            JozCountDataProvider dataProvider = new JozCountDataProvider();
            JozCountResponse res = dataProvider.processRequest(countRequest);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozCountResponse.KEY_COUNTS)) {
                    XStream xstream = new XStream();
                    // set the alises
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("getcounts", JozCounts.class);
                    xstream.alias("category", JozCategoryCount.class);
                    xstream.alias("brand", JozBrandCount.class);
                    xstream.alias("provider", JozProviderCount.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("count", String.class);

                    String xml = resultMap.get(s);
                    counts = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in fetching counts data", e);
            throw new JoZClientException("Exception in fetching counts data",e);
        }
        return counts;
    }

    /**
     * Add a campaign to JoZ
     * The response is an JozResponse 
     * @param campaign the CMA Campaign object 
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse addCampaign(Campaign campaign) throws JoZClientException {
        JozResponse response = null;
        try {
            JozCampaignRequest campaignQuery = new JozCampaignRequest();
            // convert the Campaign object to XML using the XStream api's
            XStream xstream = new XStream();
            String xmlCampaign = xstream.toXML(campaign);
            log.debug("XML Campaign request \n"+xmlCampaign);
            campaignQuery.setValue(JozCampaignRequest.KEY_CAMPAIGN, xmlCampaign);
            campaignQuery.setValue(JozCampaignRequest.KEY_COMMAND, JozCampaignRequest.COMMAND_ADD);
            JozCampaignDataProvider dataProvider = new JozCampaignDataProvider();
            JozCampaignResponse res = dataProvider.processRequest(campaignQuery);

            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozCampaignResponse.KEY_CAMPAIGN)) {
                    xstream = new XStream();
                    // set the alises
                    String xml = resultMap.get(s);
                    log.debug("XML Campaign response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in add Campaign", e);
            throw new JoZClientException("Exception in add Campaign",e);
        }
        return response;
    }

    /**
     * Deletes  campaign to JoZ
     * The response is an JozResponse 
     * @param campaign the CMA Campaign object 
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse deleteCampaign(Campaign campaign)throws JoZClientException {
        if (campaign==null) {
            throw new JoZClientException("Empty campaign object passed in");
        }
        JozResponse response = null;
        try {
            JozCampaignRequest campaignQuery = new JozCampaignRequest();
            // convert the Campaign object to XML using the XStream api's
            Integer cid = campaign.getId();
            log.debug("Going to delete the Campaign \n"+cid);
            campaignQuery.setValue(JozCampaignRequest.KEY_CAMPAIGN_ID, cid.toString());
            campaignQuery.setValue(JozCampaignRequest.KEY_COMMAND, JozCampaignRequest.COMMAND_DELETE);
            JozCampaignDataProvider dataProvider = new JozCampaignDataProvider();
            JozCampaignResponse res = dataProvider.processRequest(campaignQuery);

            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozCampaignResponse.KEY_CAMPAIGN)) {
                    XStream xstream = new XStream();
                    // set the alises
                    String xml = resultMap.get(s);
                    log.debug("XML Campaign response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in delete Campaign", e);
            throw new JoZClientException("Exception in delete Campaign",e);
        }

        return response;
    }
    /**
     * Gets tspec results. This API is used during the listing query authoring process within TCM.
     * @param  tSpec  the TSpec for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse getTSpecDetails(TSpec tSpec,int pageSize, int pageNum) throws JoZClientException {
        JozResponse response = null;
        try {
            JozTSpecRequest tSpecQuery = new JozTSpecRequest();
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_NUM, ""+pageNum);
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_SIZE, ""+pageSize);
            XStream xstream = new XStream();
            String xmlTSpec = xstream.toXML(tSpec);
            tSpecQuery.setValue(JozTSpecRequest.KEY_TSPEC, xmlTSpec);
            tSpecQuery.setValue(JozTSpecRequest.KEY_COMMAND, JozTSpecRequest.KEY_GET_PRODUCTS_TSPEC);
            JozTSpecDataProvider dataProvider = new JozTSpecDataProvider();
            JozTSpecResponse res = dataProvider.processRequest(tSpecQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTSpecResponse.KEY_RESPONSE)) {
                    xstream = new XStream();
                    // set the alises
                    String xml = resultMap.get(s);
                    log.debug("XML getTSpecDetails response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getTSpecDetails", e);
            throw new JoZClientException("Exception in agetTSpecDetails",e);
        }
        return response;
    }
    /**
     * Gets tspec results. This API is used during the listing query authoring process within TCM.
     * @param  tSpecId the TSpec id for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse getTSpecDetails(int tSpecId,int pageSize, int pageNum) throws JoZClientException {
        JozResponse response = null;
        try {
            JozTSpecRequest tSpecQuery = new JozTSpecRequest();
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_NUM, ""+pageNum);
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_SIZE, ""+pageSize);
            tSpecQuery.setValue(JozTSpecRequest.KEY_TSPEC_ID, ""+tSpecId);
            tSpecQuery.setValue(JozTSpecRequest.KEY_COMMAND, JozTSpecRequest.KEY_GET_PRODUCTS_TSPEC_ID);
            JozTSpecDataProvider dataProvider = new JozTSpecDataProvider();
            JozTSpecResponse res = dataProvider.processRequest(tSpecQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTSpecResponse.KEY_RESPONSE)) {
                    XStream xstream = new XStream();
                    // set the alises
                    String xml = resultMap.get(s);
                    log.debug("XML getTSpecDetails response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getTSpecDetails", e);
            throw new JoZClientException("Exception in agetTSpecDetails",e);
        }
        return response;
    }
    /**
     * Gets tspec counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpec the TSpec object
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse getTSpecCounts(TSpec tSpec) throws JoZClientException {
        JozResponse response = null;
        try {
            JozTSpecRequest tSpecQuery = new JozTSpecRequest();

            XStream xstream = new XStream();

            String xmlTSpec = xstream.toXML(tSpec);
            tSpecQuery.setValue(JozTSpecRequest.KEY_TSPEC, xmlTSpec);
            tSpecQuery.setValue(JozTSpecRequest.KEY_COMMAND, JozTSpecRequest.KEY_GET_COUNTS_TSPEC);
            JozTSpecDataProvider dataProvider = new JozTSpecDataProvider();
            JozTSpecResponse res = dataProvider.processRequest(tSpecQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTSpecResponse.KEY_RESPONSE)) {
                    xstream = new XStream();
                    // set the alises
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("getcounts", JozCounts.class);
                    xstream.alias("category", JozCategoryCount.class);
                    xstream.alias("brand", JozBrandCount.class);
                    xstream.alias("provider", JozProviderCount.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("count", String.class);
                    String xml = resultMap.get(s);
                    log.debug("XML getTSpecCounts response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getTSpecCounts", e);
            throw new JoZClientException("Exception in agetTSpecCounts",e);
        }

        return response;
    }
    /**
     * Gets tspec counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpecId  the TSpec id for which the query is run
     * @return JozResponse which provides the status message
     * @throws JoZClientException
     */
    public JozResponse getTSpecCounts(int tSpecId) throws JoZClientException {
        JozResponse response = null;
        try {
            JozTSpecRequest tSpecQuery = new JozTSpecRequest();
            tSpecQuery.setValue(JozTSpecRequest.KEY_TSPEC_ID, ""+tSpecId);
            tSpecQuery.setValue(JozTSpecRequest.KEY_COMMAND, JozTSpecRequest.KEY_GET_COUNTS_TSPEC_ID);
            JozTSpecDataProvider dataProvider = new JozTSpecDataProvider();
            JozTSpecResponse res = dataProvider.processRequest(tSpecQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTSpecResponse.KEY_RESPONSE)) {
                    XStream xstream = new XStream();
                    // set the alises
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("getcounts", JozCounts.class);
                    xstream.alias("category", JozCategoryCount.class);
                    xstream.alias("brand", JozBrandCount.class);
                    xstream.alias("provider", JozProviderCount.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("count", String.class);
                    String xml = resultMap.get(s);
                    log.debug("XML getTSpecCounts response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getTSpecCounts", e);
            throw new JoZClientException("Exception in agetTSpecCounts",e);
        }
        return response;
    }
    /**
     * Gets tspec results and counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpec  the TSpec for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which contains the details and count data
     * @throws JoZClientException 
     */
    public JozResponse getTSpecDetailsAndCounts(TSpec tSpec, int pageSize, int pageNum) throws JoZClientException {
        JozResponse response = null;
        try {
            JozTSpecRequest tSpecQuery = new JozTSpecRequest();
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_NUM, ""+pageNum);
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_SIZE, ""+pageSize);
            XStream xstream = new XStream();
            String xmlTSpec = xstream.toXML(tSpec);
            tSpecQuery.setValue(JozTSpecRequest.KEY_TSPEC, xmlTSpec);
            tSpecQuery.setValue(JozTSpecRequest.KEY_COMMAND, JozTSpecRequest.KEY_GET_PRODUCTS_COUNTS_TSPEC);
            JozTSpecDataProvider dataProvider = new JozTSpecDataProvider();
            JozTSpecResponse res = dataProvider.processRequest(tSpecQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTSpecResponse.KEY_RESPONSE)) {
                    xstream = new XStream();
                    // set the alises
                    xstream.alias("jozresponse", JozResponse.class);
                    xstream.alias("getcounts", JozCounts.class);
                    xstream.alias("category", JozCategoryCount.class);
                    xstream.alias("brand", JozBrandCount.class);
                    xstream.alias("provider", JozProviderCount.class);
                    xstream.useAttributeFor("name", String.class);
                    xstream.useAttributeFor("count", String.class);
                    String xml = resultMap.get(s);
                    log.debug("XML getTSpecDetails response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getTSpecDetailsAndCounts", e);
            throw new JoZClientException("Exception in getTSpecDetailsAndCounts",e);
        }
        return response;
    }
    /**
     * Gets tspec results and counts. This API is used during the listing query authoring process within TCM.
     * @param  tSpecId  the TSpec id for which the query is run
     * @param  pageNum the page number of the result set
     * @return JozResponse which contains the details and count data
     * @throws JoZClientException 
     */
    public JozResponse getTSpecDetailsAndCounts(int tSpecId, int pageSize, int pageNum) throws JoZClientException {
        JozResponse response = null;
        try {
            JozTSpecRequest tSpecQuery = new JozTSpecRequest();
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_NUM, ""+pageNum);
            tSpecQuery.setValue(JozTSpecRequest.KEY_PAGE_SIZE, ""+pageSize);
            tSpecQuery.setValue(JozTSpecRequest.KEY_TSPEC_ID, ""+tSpecId);
            tSpecQuery.setValue(JozTSpecRequest.KEY_COMMAND, JozTSpecRequest.KEY_GET_PRODUCTS_COUNTS_TSPEC_ID);
            JozTSpecDataProvider dataProvider = new JozTSpecDataProvider();
            JozTSpecResponse res = dataProvider.processRequest(tSpecQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozTSpecResponse.KEY_RESPONSE)) {
                    XStream xstream = new XStream();
                    // set the alises
                    String xml = resultMap.get(s);
                    log.debug("XML getTSpecDetails response " + xml);
                    response = (JozResponse) xstream.fromXML(xml);
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getTSpecDetailsAndCounts", e);
            throw new JoZClientException("Exception in getTSpecDetailsAndCounts",e);
        }
        return response;
    }

    /**
     * Perform a health check with Joz
     *
     * @return
     * @throws com.tumri.joz.client.JoZClientException
     *
     */
    public boolean doHealthCheck() throws JoZClientException {
        boolean bStatus;
        try {
            JozHCRequest request = new JozHCRequest();
            JozHCDataProvider hcQuery = new JozHCDataProvider();
            JozHCResponse hcresponse = hcQuery.processRequest(request);
            HashMap<String, String> resultMap = hcresponse.getResultMap();
            String error = resultMap.get(JozHCResponse.KEY_ERROR);
            if (error!=null) {
                bStatus = false;
                return bStatus;
            }
            String status = resultMap.get(JozHCResponse.KEY_STATUS);
            bStatus = (JozHCResponse.SUCCESS.equals(status));
        } catch (Throwable e) {
            log.error("Exception in healthcheck", e);
            bStatus = false;
        }
        return bStatus;  
    }

    /**
     *  Gets campaign data for advertiser
     * @param advertiserId 
     * @return  String xml response as string
     * @throws JoZClientException
     */
	public String getAdvertiserCampaignData(int advertiserId) throws JoZClientException {
		String response = null;

        try {
            JozICSCampaignRequest campaignQuery = new JozICSCampaignRequest();

            campaignQuery.setValue(JozICSCampaignRequest.KEY_ADVERTISER_ID,""+advertiserId);
            campaignQuery.setValue(JozICSCampaignRequest.KEY_COMMAND, JozICSCampaignRequest.COMMAND_CAMPAIGN_FOR_ADVERTISER);
            JozICSCampaignDataProvider dataProvider = new JozICSCampaignDataProvider();
            JozICSCampaignResponse res = dataProvider.processRequest(campaignQuery);

            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozICSCampaignResponse.KEY_CAMPAIGN)) {
                    // set the alises
                    String xml = resultMap.get(s);
                    log.debug("XML Campaign response " + xml);
                    response = xml;
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in Campaign data for advertiser "+advertiserId, e);
            throw new JoZClientException("Exception in Campaign data for advertiser "+advertiserId,e);
        }
        return response;

	}

	/**
     *  Gets all advertisers. 
     * @return  String xml response as string
     * @throws JoZClientException
     */
	public String getAllAdvertisers() throws JoZClientException {
		String response = null;
        try {
            JozICSCampaignRequest campaignQuery = new JozICSCampaignRequest();

            campaignQuery.setValue(JozICSCampaignRequest.KEY_COMMAND, JozICSCampaignRequest.COMMAND_GET_ALL_ADVERTISERS);
            JozICSCampaignDataProvider dataProvider = new JozICSCampaignDataProvider();
            JozICSCampaignResponse res = dataProvider.processRequest(campaignQuery);
            HashMap<String, String> resultMap = res.getResultMap();
            for (String s : resultMap.keySet()) {
                if (s.equalsIgnoreCase(JozICSCampaignResponse.KEY_CAMPAIGN)) {
                    String xml = resultMap.get(s);
                    log.debug("XML Campaign response " + xml);
                    response = xml;
                    break;
                }
            }
        } catch (Throwable e) {
            log.error("Exception in getAdvertisers in Campaigns", e);
            throw new JoZClientException("Exception in getAdvertisers in Campaigns",e);
        }
        return response;
	}

	/**
	 * @param req: JozQARequest containing a list of advertisers
	 * @return JozQAResponse containing QA Report for failed recipes
	 * @throws JoZClientException
	 * Use:
	 * Construct a JozQARequest with desired Advertisers. Create a new JozDataProviderImpl.
	 * Process the request using JozDataProviderImpl.getQAReport(JozQARequest) which returns a JozQAResponse object containing the QA information.
	 */
	public JozQAResponse getQAReport(JozQARequest req) throws JoZClientException {
		JozQAResponse resp;
        try {
            JozQADataProvider dataProvider = new JozQADataProvider();
            JozQAResponseWrapper responseWrapper = dataProvider.processRequest(req);
	        String xml = responseWrapper.getResultMap().get(JozQAResponseWrapper.KEY_QAREPORTDETAIL);
			XStream xstream = new XStream();
			resp = (JozQAResponse)xstream.fromXML(xml);
        } catch (Throwable e) {
            log.error("Exception in fetching QA Report", e);
            throw new JoZClientException("Exception in fetching QA Report",e);
        }

        return resp;
	}

	/**
     *  Gets all campaigns. 
     * @return  String xml response  as string
     * @throws JoZClientException
     */
    public String getAllAdvertisersCampaignData()throws JoZClientException{
    	String response = getAdvertiserCampaignData(-1);
    	return response;
    }

}

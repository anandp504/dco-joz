package com.tumri.joz.jozMain;

import com.tumri.content.MerchantDataProvider;
import com.tumri.content.data.Taxonomy;
import com.tumri.joz.utils.AppProperties;
import com.tumri.joz.utils.LogUtils;
import com.tumri.lls.client.main.LLCClientException;
import com.tumri.lls.client.main.ListingProvider;
import org.apache.log4j.Logger;

/**
 * Factory class to provide access to all the Listing providers.
 * <P>
 * The factory class can be accessed in following two ways:
 * <P>
 * 1. If the configuration is done declaratively in the cma_user.properties file, use the following method. <BR/><BR/>
 *     <code>CMAFactory factory = CMAFactory.getInstance();</code> <BR/><BR/>
 *
 * <P>
 * 2. If the configuration is to be done programmatically use the following methods below: <BR/><BR/>
 * <code>
 *      Properties properties = new Properties(); <BR/>
 *      properties.setProperty(JDBCConfigProperties.JDBC_DRIVER_CLASS_KEY, "com.mysql.jdbc.Driver"); <BR/>
 *      properties.setProperty(JDBCConfigProperties.JDBC_URL_KEY, "jdbc:mysql://HOST:PORT/CAMPAIGN_DB"); <BR/>
 *      properties.setProperty(JDBCConfigProperties.JDBC_USERNAME_KEY, "YOUR_USER_NAME"); <BR/>
 *      properties.setProperty(JDBCConfigProperties.JDBC_PASSWORD_KEY, "YOUR_DB_PASSWORD");  <BR/><BR/>
 *
 *      CMAFactory factory = CMAFactory.getInstance(properties); <BR/>
 * </code>
 *
 * @author  nipun
 * @since   1.0.0
 * @version 1.0.0
 */
public class ListingProviderFactory {

    private static ListingProvider listingProvider;
    private static Logger log = Logger.getLogger(ListingProviderFactory.class);
    private static final String PROVIDER_CLASS_NAME = "com.tumri.joz.listing.provider.impl";
    private static boolean initialized;

    /**
     * @return ListingProvider Implementation
     * @throws RuntimeException
     */
    public static ListingProvider getProviderInstance(Taxonomy tax, MerchantDataProvider m) {
        if(initialized) {
            return listingProvider;
        }
        else {
            synchronized(ListingProviderFactory.class) {
                if(!initialized) {
                    instantiateFactory();
                    initialized = true;
                }
                try {
                    listingProvider.init(AppProperties.getInstance().getProperties(), tax, m);
                } catch (LLCClientException e) {
                   LogUtils.getFatalLog().fatal("Exception caught on initializing content provider");
//                    //Kick off thread that will retry and re do the content refresh
                    LlcReconnectPoller.getInstance(listingProvider,tax,m).init();

                }
            }
        }
        return listingProvider;
    }

    /**
     * Handle the content refresh
     * @param tax
     * @param m
     */
    public static void refreshData(Taxonomy tax, MerchantDataProvider m){
        if (!initialized) {
            throw new RuntimeException("Cannot refresh Listing Provider without initializing");
        }
        try {
            listingProvider.doContentRefresh(tax, m);
        } catch(LLCClientException e) {
            log.error("Content Refresh for listing provider failed.", e);
        }
    }

    /**
     * Clear the cache on the listing provider
     */
    public static void clearListingCache() {
        try {
            listingProvider.clearCache();
        } catch(LLCClientException e) {
            log.error("Clear cache on listing provider failed.", e);
        }

    }

    private static void instantiateFactory() throws RuntimeException {
        String factoryClassName = AppProperties.getInstance().getProperty(PROVIDER_CLASS_NAME);

        try {
            ClassLoader loader = Thread.currentThread().getContextClassLoader();
            Class t = (loader != null) ? loader.loadClass(factoryClassName) :
            Class.forName(factoryClassName);
            listingProvider = (ListingProvider) t.newInstance();
        }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("Unable to load the ListingProvider Implementation class. Check joz.properties for right class name", e);
        }
        catch (IllegalAccessException e) {
            throw new RuntimeException("Unable to access the ListingProvider Implementation class", e);
        }
        catch(InstantiationException e) {
            throw new RuntimeException("Unable to instantiate the ListingProvider Implementation class. Check joz.properties for right class name", e);
        }
    }

    /**
     * Clears up the resources
     */
    public static void shutdown() {
        if (listingProvider != null) {
            listingProvider.shutdown();
        }
        LlcReconnectPoller.getInstance(null,null,null).shutdown();
    }

}

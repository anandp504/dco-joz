// Repository of various Joz data.

package com.tumri.joz.jozMain;

import org.apache.log4j.Logger;

import com.tumri.joz.keywordServer.LuceneDB;

class JozData
{
    static public void
    init ()
	throws Exception
    {
	String data_path = "../data/joz";

	try
	{
	    String merchant_path = data_path + "/MD";
	    merchant_db = new MerchantDB (merchant_path + "/attributes-and-metadata.lisp",
					  merchant_path + "/tabulated-search-results.lisp");
	}
	catch (Exception e)
	{
	    log.error ("Unable to initialize Merchant DB: "
		       + e.toString ());
	}

	try
	{
	    //String mup_prefix = data_path + "/MUP/MUP-US0031-US-DEFAULT_";
	    String mup_prefix = data_path + "/MUP/MUP-USpub0017_MUP_US0071-US-DEFAULT_";

	    mup_db = new TmpMUPDB (mup_prefix + ".ifasl",
				   mup_prefix + ".strings",
				   mup_prefix + ".taxonomy",
				   "../data/default-realm.lisp");
	}
	catch (Exception e)
	{
	    log.error ("Unable to initialize MUP DB: "
		       + e.toString ());
	}

	try
	{
	    tspec_db = new TmpTSpecDB (data_path + "/t-specs/t-specs.lisp");
	}
	catch (Exception e)
	{
	    log.error ("Unable to initialize TSpec DB: "
		       + e.toString ());
	}

	try
	{
	    String lucene_path = data_path + "/lucene";
	    lucene_db = new LuceneDB (lucene_path);
	}
	catch (Exception e)
	{
	    log.error ("Unable to initialize Lucene DB: "
		       + e.toString ());
	}
    }

    // Merchant administrivia.
    public static MerchantDB merchant_db = null;

    // The MUP.
    public static MUPDB mup_db = null;

    // T-Specs.
    public static TSpecDB tspec_db = null;

    // The Lucene database.
    public static LuceneDB lucene_db = null;

    // implementation details -------------------------------------------------

    private static Logger log = Logger.getLogger (JozData.class);
}

package com.tumri.joz.productselection;

import java.io.Reader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.net.URLDecoder;
import java.net.URLEncoder;

import org.junit.Assert;
import org.junit.Test;

import com.tumri.joz.jozMain.AdDataRequest;
import com.tumri.utils.sexp.Sexp;
import com.tumri.utils.sexp.SexpList;
import com.tumri.utils.sexp.SexpReader;
import com.tumri.utils.sexp.SexpSymbol;

public class UrlScavengerTest {

	@Test
	public void testURLScavenging() {
        String url = "http://www.photography.com/ni11,pun/camera/nikon?q=xyz";
        try {
            String keywords = doURLScavenge(url, "", "");
            Assert.assertTrue(keywords!=null);
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
	public void testnullURL() {
        String url = "";
        try {
            String keywords = doURLScavenge(url, "", "");
            Assert.assertTrue((keywords.equals("")));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}

	}

        @Test
	public void testCrazyURL() {
        String url = "~!@#$&*()%^{}[]_+|\\';:<>,./?`";
        try {
            String keywords = doURLScavenge(url, "", "");
            Assert.assertTrue((keywords.equals("")));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}

	}

    @Test
	public void testRequestStopWords() {
        String url = "http://www.photography.com/camera/canon/nikon/tes%t";
        try {
            String keywords = doURLScavenge(url, "camera,canon", "");
            Assert.assertTrue((keywords!=null) && keywords.indexOf("canon") ==-1 && keywords.indexOf("camera") ==-1);
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
	public void testExciteUKIssue() {
        String url = "http://www.excite.co.uk/shopping/categories/briefcases_and_attache_cases?cid=8533&mid=&bid=&";
        try {
			String stopwords = "excite,co,uk,shopping,cid,categories,bid,mid";
            String keywords = doURLScavenge(url, stopwords,"" );
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
	public void testExciteUKStopWordIssue() {
        String url = "http://www.excite.co.uk/shopping/productresults/?from_search=1&query=Apple+iPod&cid=";
        try {
			String stopwords = "excite,co,uk,shopping,cid,mid,bid,categories,productresults,from_search,from,search";
            String keywords = doURLScavenge(url, stopwords,"query" );
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords1() {
        String url = "http://shop.internet.com/index.php?/SF-7/BEFID-6/keyword-/dnatrs-hewlett_packard";
        try {
			String stopWords = "shop, internet, sf-6, sf-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords, "");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords2() {
        String url = "http://shop.internet.com/index.php?/SF-6/BEFID-9006/keyword-Flat+Panel+LCD";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords,"");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords3() {
        String url = "http://shop.internet.com/index.php?/BEFID-96252/SF-6/keyword=plasma";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords, "");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords4() {
        String url = "http://shop.internet.com/index.php?IC_ic=1&IC_query_search=1&IC_QueryText=Sharp+Aguus&SUBMIT.x=0&SUBMIT.y=0&SUBMIT=Find";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords,"");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords5() {
        String url = "http://shop.internet.com/index.php?/SF-7/BEFID-96252/keyword-Sharp%20Aquos/dnatrs-price_range_1300_1650";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords,"");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords6() {
        String url = "http://alatest.com/Digital_SLR_Cameras/248/?v1=brand%7EPentax%7EL";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords,"");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords7() {
        String url = "http://alatest.com/Global_Positioning_Systems_GPS/15/?v1=brand~Magellan+Navigation~";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords,"");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    @Test
    public void testShopInternetStopWords8() {
        String url = "http://shop.internet.com/index.php?/SF-6/BEFID-96394/keyword-software/dnatrs-internet_and_communication-antivirus";
        try {
			String stopWords = "shop, internet, SF-6, SF-7, index.php, keyword, dnatrs, IC_ic, IC_query_Search, " +
                                           "IC_Query_text, SUBMIT, befid, price_range, brand";
            String keywords = doURLScavenge(url, stopWords,"");
            Assert.assertTrue((keywords!=null));
			System.out.println("The mined keywords are : " + keywords);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }


    @Test
	public void testRequestExciteQueryNames1() {
        String url = "http://www.excite.co.uk/shopping/productresults/?from_search=1&query=Apple+iPod&awesome=nice&query1=test&cid=";
        try {
			String requestQueryNames = "query, query1";
            String stopWords = "excite,co,uk,shopping,cid,mid,bid,categories,productresults,from_search,from,search";
            String keywords =  doURLScavenge(url, stopWords, requestQueryNames);
			System.out.println("The mined keywords using query names are : " + keywords);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("camera")==-1));
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
    public void testRequestExciteQueryNames2() {
        String url = "http://excite.co.uk/search?q=camera";
        try {
            String requestQueryNames = "q";
            String stopWords = "excite,co,uk,shopping,cid,mid,bid,categories,productresults,from_search,from,search";
            String keywords =  doURLScavenge(url, stopWords, requestQueryNames);
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue((keywords!=null)&&(keywords.indexOf("camera")!=-1));
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
	public void testRequestQueryNamesBasic() {
		String url = "http://www.photography.com/camera/canon/nikon/test?nipun=test&camera=nikon&testquery=blah";
		try {
            StringBuilder requestQueryNames = new StringBuilder();
            requestQueryNames.append("camera ");
            requestQueryNames.append("testquery,");
            requestQueryNames.append("nipun");
			String keywords = doURLScavenge(url, "",requestQueryNames.toString());
			System.out.println("The mined keywords using query names are : " + keywords);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("nikon")!=-1));
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
	public void testRequestQueryNamesPhotoBucket() {
		String url = "http://photobucket.com/mediadetail/?media=http%3A%2F%2Fi176.photobucket.com%2Falbums%2Fw174%2Fkain_020%2Fjessica-biel-picture-1.jpg&searchTerm=jessica%20biel&pageOffset=0";
		try {
			String keywords = doURLScavenge(url,"","searchTerm");
			System.out.println("The mined keywords using query names are : " + keywords);
			Assert.assertTrue(keywords!=null && keywords.indexOf("jessica")!=-1);
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
	}

    @Test
    public void testRequestQueryNamesGoogle() {
        String url = "http://www.google.com/search?as_q=game+xbox&hl=en&num=10&btnG=Google+Search&as_epq=project+g" +
                "otham&as_oq=car&as_eq=porsche&lr=&cr=&as_ft=i&as_filetype=&as_qdr=all&as_nlo=&as_nhi=&as_occt=" +
                "any&as_dt=i&as_sitesearch=&as_rights=&safe=images";
        try {
            String keywords = doURLScavenge(url,"","epq q oq eq");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("porsche")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testRequestQueryNamesGoogleBasic() {
        String url = "http://www.google.com/search?hl=en&lr=&as_qdr=all&q=Angelina+Jolie&btnG=Search";
        try {
            String keywords = doURLScavenge(url,"","q");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("angelina")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testRequestQueryNamesYahooBasic() {
        String url = "http://search.yahoo.com/search?p=Jessica+Biel&fr=yfp-t-501&toggle=1&cop=mss&ei=UTF-8";
        try {
            String keywords = doURLScavenge(url,"","p");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("jessica")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testRequestQueryNamesYahooAdvanced() {
        String url = "http://search.yahoo.com/search?n=10&ei=UTF-8&va_vt=any&vo_vt=any&" +
                "ve_vt=any&vp_vt=any&vd=all&vst=0&vf=all&vm=p&fl=0&p=xbox+games+spiderman+%22project+gotham%22+-pong";
        try {
            String keywords = doURLScavenge(url,"","p");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("xbox")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testMSNLiveBasic(){
        String url = "http://search.live.com/results.aspx?q=Jessica+Biel&go=Search&mkt=en-us&scope=&FORM=LIVSOP";
        try {
            String keywords = doURLScavenge(url,"","q");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("jessica")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

        @Test
    public void testMSNLiveAdvanced(){
        String url = "http://search.live.com/results.aspx?q=project+gotham+%28Xbox+OR+games%29+-%28spiderman%29&go=Search&form=QBRE";
        try {
            String keywords = doURLScavenge(url,"OR","q");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("gotham")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testAskSearchBasic(){
        String url = "http://www.ask.com/web?q=Tumri+Ads&search=search&qsrc=0&o=0&l=dir";
        try {
            String keywords = doURLScavenge(url,"","q");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("tumri")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testAskSearchAdvanced(){
        String url = "http://www.ask.com/web?q=Project+Gotham+%22Xbox+" +
                "games%22+-Pong+Max+OR+Project+Gotham+%22Xbox+games%22+-Pong+Payne&sm=adv&qsrc=196&o=0&l=dir";
        try {
            String keywords = doURLScavenge(url,"","q");
            System.out.println("The mined keywords using query names are : " + keywords);
            Assert.assertTrue(keywords!=null && keywords.indexOf("gotham")!=-1);
        } catch(Exception e) {
            System.out.println("Could not parse the request and mine the url");
            e.printStackTrace();
        }
    }

    @Test
    public void testQueryNamesAndStopWords() {
		String url = "http://www.photography.com/camera/canon/nikon/test?nipun=test,xyzw,UTF888&camera=nikon&testquery=blah";
		try {
            StringBuilder requestQueryNames = new StringBuilder();
            requestQueryNames.append("camera ");
            requestQueryNames.append("testquery,");
            requestQueryNames.append("nipun");
			String keywords = doURLScavenge(url, "blah,xyzw,UTF888",requestQueryNames.toString());
			System.out.println("The mined keywords using query names are : " + keywords);
			Assert.assertTrue((keywords!=null)&&(keywords.indexOf("nikon")!=-1));
		} catch(Exception e) {
			System.out.println("Could not parse the request and mine the url");
			e.printStackTrace();
		}
    }

    private String doURLScavenge(String url, String stopWords, String queryNames) {
        try {
            url = URLEncoder.encode(url, "utf-8");
        } catch(UnsupportedEncodingException e) {
            System.out.println("Unsuppported encoding excpetion caught");
            e.printStackTrace();
        }
        String queryStr = "(get-ad-data :keywords \"bagsakan\" :url \"" + url + "\")";
        AdDataRequest rqst = createRequestFromCommandString(queryStr);
        String keywords = URLScavenger.mineKeywords(rqst, stopWords, queryNames);
        return keywords;
    }

    /**
	 * Returns the constructed Ad Data request - used for test methods
	 * @param adDataCmdStr
	 * @return
	 */
	private AdDataRequest createRequestFromCommandString(String adDataCmdStr) {
		Reader r = new StringReader (adDataCmdStr);
		SexpReader lr = new SexpReader (r);
		AdDataRequest rqst = null;
		try {
			Sexp e = lr.read ();
			SexpList l = e.toSexpList ();
		 	Sexp cmd_expr = l.getFirst ();
			if (! cmd_expr.isSexpSymbol ())
				System.out.println("command name not a symbol: " + cmd_expr.toString ());

			SexpSymbol sym = cmd_expr.toSexpSymbol ();
			String cmd_name = sym.toString ();

			// Return the right Cmd* class to handle this request.

			if (cmd_name.equals ("get-ad-data")) {
				rqst = new AdDataRequest (e);
			} else {
				System.out.println("The request could not be parsed correctly");
				Assert.assertTrue(false);
			}
		} catch(Exception e) {
			System.out.println("Could not parse the request");
			e.printStackTrace();
		}
		return rqst;
	}
}

/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C)  TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (@tumri.com)
 * @version 1.0     
 *
 */
package com.tumri.joz.server.handlers;

import com.tumri.joz.server.domain.JozAdResponse;

import java.util.HashMap;
import java.util.Iterator;

/**
 * @author: nipun
 * Date: Sep 11, 2008
 * Time: 10:44:11 PM
 */
public class TestClass {

    public static void main(String[] args){
        String str1 = "THIS IS A TEST";
        String str2 = "THIS IS A TEST";

        JozAdResponse ad = new JozAdResponse();
        ad.addDetails(str1, str2);
        ad.addDetails("Nipun", "Testing");
        ad.addDetails("Nipun1", "Testing2");
        ad.addDetails("Nipun2", "Testing4");

        HashMap<String, String> testMap = ad.getResultMap();
        Iterator<String> myiter = testMap.keySet().iterator();
        while (myiter.hasNext()) {
            String s = myiter.next();
            System.out.println(s + " = " + testMap.get(s));
        }
       
    }

    /**
     * Get the bytes for a string
     * @param str
     * @return
     */
    public static byte[] getBytes(String str) {
        char cin[] = new char[str.length()];
        str.getChars(0,cin.length,cin,0);
        byte x[] = new byte[cin.length];
//        byte x[] = new byte[cin.length*2];
        for(int i=0;i<cin.length;i++) {
            int k = cin[i];
            x[i*2] = (byte)k;
            //x[i*2+1] = (byte)(k>>8);
        }
        return x;
    }

    /**
     * Reads a string from the given byte[]
     * @param bin
     * @return
     */
    public static String readString(byte[] bin) {
        char[] cout = new char[bin.length/2];
        for(int i=0;i<bin.length;i+=2) {
            int k = ((int)bin[i+1]<<8) | (int)bin[i];
            cout[i/2] = (char)k;
        }
        String s = new String(cout);

        return s;
    }

}

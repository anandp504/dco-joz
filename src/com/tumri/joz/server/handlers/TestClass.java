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

import java.util.ArrayList;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;

/**
 * @author: nipun
 * Date: Sep 11, 2008
 * Time: 10:44:11 PM
 */
public class TestClass {

    private static byte TAB_CHAR;
    private static byte[] REG;

    static {
        try {
            // The byte array has to be of length 1.
            TAB_CHAR = (new String("\t")).getBytes("UTF-8")[0];
            char regtm = (char)174;
            Character regtmO = new Character(regtm);
            REG = (regtmO.toString()).getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            // Doesn't happen.
        }
    }


        /**
     * Replace comma with string for external filters.
     * @param str
     * @return
     */
    private static String cleanseKeywords(String str) {
        if (str != null) {
            str = str.replaceAll(","," ");
        }
        return str;
    }

    public static void main(String[] args){

          System.out.println(cleanseKeywords(",Nipun,CA, SAN Francisco"));
//        Timestamp ts = new Timestamp(33523475189L);
//        System.out.println(ts.toString());

//            JozHCResponse response = null;
//            try {
//                NioClient client = NioClient.getInstance();
//                client.init("localhost", 2544, 10, 3);
//
//                NioResponseHandler handler = new NioResponseHandler();
//                ByteArrayOutputStream bos1 = new ByteArrayOutputStream(4096);
//                ObjectOutputStream q1 = new ObjectOutputStream(bos1);
//                q1.writeUnshared(new JozHCRequest());
//                NioClient.getInstance().send(bos1.toByteArray(), handler);
//                response = (JozHCResponse)handler.waitForResponse();
//            } catch (IOException e) {
//                System.err.println("Exception caught during process request");
//                e.printStackTrace();
//            } catch (Throwable t) {
//                System.err.println("Exception caught during process request");
//                t.printStackTrace();
//            }


//        JozAdResponse ad = new JozAdResponse();
//        ad.addDetails(str1, str2);
//        ad.addDetails("Nipun", "Testing");
//        ad.addDetails("Nipun1", "Testing2");
//        ad.addDetails("Nipun2", "Testing4");
//
//        HashMap<String, String> testMap = ad.getResultMap();
//        Iterator<String> myiter = testMap.keySet().iterator();
//        while (myiter.hasNext()) {
//            String s = myiter.next();
//            System.out.println(s + " = " + testMap.get(s));
//        }
       
    }


        protected static char[][] getColumnInfo(byte[] listingInfo) {
        if (listingInfo == null) {
            return null;
        }
        ArrayList<char[]> listingDetails = new ArrayList<char[]>();
        int colStartIdx = 0;

        for (int i=0; i < listingInfo.length; i++) {
            if (listingInfo[i] == TAB_CHAR) {
                listingDetails.add(subArrayCopy(listingInfo, colStartIdx, (i-colStartIdx)));
                colStartIdx = i + 1;
            }
        }
        //Get the last col value
        if (colStartIdx > 0) {
            listingDetails.add(subArrayCopy(listingInfo, colStartIdx, (listingInfo.length-colStartIdx)));
        }
        return listingDetails.toArray(new char[0][]);
    }

    /**
     * Copy given amount of chars from sourceArr from start index pos
     * @param sourceArr
     * @param start
     * @param size
     * @return
     */
    private static char[] subArrayCopy(byte[] sourceArr, int start, int size) {
        char[] cArr = new char[size];
        int k=0;
        for (int j=start; j< (start+size); j++) {
            int val = sourceArr[j];
            if (val < 0 || val > 127){
                byte[] singleArr = new byte[2];
                singleArr[0] = sourceArr[j];
                singleArr[1] = sourceArr[j++];
                try {
                    String str  = new String(singleArr, "UTF-8");
                    System.out.println(str);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                }
            }
            cArr[k++] = (char)sourceArr[j];
        }
        return cArr;
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

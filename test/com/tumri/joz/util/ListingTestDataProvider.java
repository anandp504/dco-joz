/*
 * ListingsQueryHandler.java
 *
 * COPYRIGHT (C) ${year} TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY,
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR
 * WRITTEN PERMISSION OF TUMRI INC.
 *
 * @author Nipun Nair (${user}@tumri.com)
 * @version 1.0     ${date}
 *
 */
package com.tumri.joz.util;

import com.tumri.content.data.impl.ProductImpl;
import com.tumri.content.data.Product;
import com.tumri.joz.products.IProduct;

import java.util.*;
import java.io.*;

/**
 * Produce synthetic listing data for testing purposes. The resulting data is not a valid mup, it is just to be used to
 * generate mup of a given size.
 * @author: nipun
 * Date: Apr 18, 2008
 * Time: 2:21:06 PM
 */
public class ListingTestDataProvider {

    private static String baseDir = ".";
    private static final String categoryIdFile = "catids.txt";
    private static final String brandIdFile = "brand.txt";
    private static final String supplierIdFile = "supplier.txt";
    private static final String providerIdFile = "provider.txt";
    private static final String productNameFile = "prodName.txt";
    private static final String productDescFile = "prodDesc.txt";
    private static final String pThumbNailFile = "thumbnail.txt";
    private static final String pPurchaseUrlFile = "purchaseUrl.txt";
    private static final String pImageUrlFile = "imageUrl.txt";
    private static final String pPriceFile = "prices.txt";

    //Product Type Values
    private static long prodId = 0;

    protected HashMap<IProduct.Attribute, List<String>> testDataMap = new HashMap<IProduct.Attribute, List<String>>();
    Random r;

    private ListingTestDataProvider(){
        init();
    }
    /**
     * Load up the sample data into testDataMap
     */
    private void init() {
        r = new Random(Calendar.getInstance().getTimeInMillis());
        testDataMap.put(IProduct.Attribute.kCategory, readFile(categoryIdFile));
        testDataMap.put(IProduct.Attribute.kBrand, readFile(brandIdFile));
        testDataMap.put(IProduct.Attribute.kProvider, readFile(providerIdFile));
        testDataMap.put(IProduct.Attribute.kSupplier, readFile(supplierIdFile));
        testDataMap.put(IProduct.Attribute.kProductName, readFile(productNameFile));
        testDataMap.put(IProduct.Attribute.kDescription, readFile(productDescFile));
        testDataMap.put(IProduct.Attribute.kThumbnail, readFile(pThumbNailFile));
        testDataMap.put(IProduct.Attribute.kPurchaseUrl, readFile(pPurchaseUrlFile));
        testDataMap.put(IProduct.Attribute.kImageUrl, readFile(pImageUrlFile));
        ArrayList<String> prodTypeList = new ArrayList<String>();
        prodTypeList.add("Product");
        prodTypeList.add("LEADGEN");
        testDataMap.put(IProduct.Attribute.kProductType, prodTypeList);
        testDataMap.put(IProduct.Attribute.kPrice, readFile(pPriceFile));
        System.out.println("Finished loading seed data");
    }
    /**
     * Reads the given file and populate arraylist
     * @param infile
     * @return
     */
    private ArrayList<String> readFile(String infile) {
        ArrayList<String> ids = new ArrayList<String>();
        try {
            FileInputStream inFile = new FileInputStream(baseDir + infile);
            InputStreamReader ir = new InputStreamReader(inFile,"UTF-8");
            BufferedReader br = new BufferedReader(ir);

            String line;
            boolean eof = false;
            while (!eof) {
                line = br.readLine();
                if (line == null) {
                    eof = true;
                    continue;
                }
                ids.add(line);
            }
            br.close();
            ir.close();
            inFile.close();

        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return ids;
    }

    private String getAttrValue(IProduct.Attribute kAttr) {
        String attrVal = "";
        List<String> currList = testDataMap.get(kAttr);
        if (currList!= null) {
            int index = Math.abs(r.nextInt()%currList.size());
            attrVal = currList.get(index);
            if (kAttr == IProduct.Attribute.kProductName ||
                    kAttr == IProduct.Attribute.kDescription) {
                attrVal = attrVal + prodId;
            }
        }

        return attrVal;
    }

    private ProductImpl getNewListing(boolean bNewMup) {
        ProductImpl retVal = new ProductImpl();
        prodId++;
        retVal.setGId("US" + prodId); // GId
        retVal.setCatalogStr("CAT.TEST"); //Catalog
        retVal.setIdSymbol("_3065.US" + prodId ); //Product ID
        retVal.setCategoryStr(getAttrValue(IProduct.Attribute.kCategory)); //product category
        retVal.setPriceStr(getAttrValue(IProduct.Attribute.kPrice)); //price
        retVal.setDiscountPriceStr(getAttrValue(IProduct.Attribute.kPrice)); //discount price
        retVal.setBrandStr(getAttrValue(IProduct.Attribute.kBrand)); //brand
        retVal.setSupplierStr(getAttrValue(IProduct.Attribute.kSupplier)); //supplier
        retVal.setProviderStr(getAttrValue(IProduct.Attribute.kProvider)); //provider
        retVal.setProductName(getAttrValue(IProduct.Attribute.kProductName)); //product name
        retVal.setDescription(getAttrValue(IProduct.Attribute.kDescription)); //description
        retVal.setRankStr("0"); //rank
        retVal.setThumbnail(getAttrValue(IProduct.Attribute.kThumbnail)); //thumbnail
        retVal.setPurchaseUrl(getAttrValue(IProduct.Attribute.kPurchaseUrl)); //purchase_url
        retVal.setImageUrl(getAttrValue(IProduct.Attribute.kImageUrl)); //image_url
        retVal.setImageWidthStr("100"); //image_width
        retVal.setImageHeightStr("500"); //image_height
        retVal.setCPCStr(getAttrValue(IProduct.Attribute.kPrice)); //CPC
        retVal.setCurrencyStr("USD"); //Currency
        retVal.setDiscountPriceCurrencyStr("USD"); //Discount price currency
        retVal.setBlackWhiteListStatusStr("0"); //Black/White List Status
        retVal.setProductTypeStr(getAttrValue(IProduct.Attribute.kProductType)); //ProductTYpe
        retVal.setCPOStr(getAttrValue(IProduct.Attribute.kPrice)); //CPO/A
        retVal.setBaseProductNumber("US" + prodId); //Base product Number

        if (bNewMup) {
            retVal.setCountryStr(getAttrValue(IProduct.Attribute.kCountry)); //Country
            retVal.setStateStr(getAttrValue(IProduct.Attribute.kState)); //State
            retVal.setCityStr(getAttrValue(IProduct.Attribute.kCity)); //City
            retVal.setZipStr(getAttrValue(IProduct.Attribute.kZip)); //Zip
            retVal.setDmaCodeStr(getAttrValue(IProduct.Attribute.kDMA)); //DMA
            retVal.setAreaCodeStr(getAttrValue(IProduct.Attribute.kArea)); //Area Code
            retVal.setProviderCategoryStr(getAttrValue(IProduct.Attribute.kProviderCategory)); //Provider Category
            retVal.setGlobalIdStr("UNSPSC" + prodId); //Global ID
            retVal.setCategoryField1Str(""); //Category Field 1
            retVal.setCategoryField2Str(""); //Category Field 2
            retVal.setCategoryField3Str(""); //Category Field 3
            retVal.setCategoryField4Str(""); //Category Field 4
            retVal.setCategoryField5Str(""); //Category Field 5
            retVal.setPassThroughField11Str("PassThru" + prodId); //Pass Thru 1
            retVal.setPassThroughField12Str("PassThru" + prodId); //Pass Thru 2
            retVal.setPassThroughField13Str("PassThru" + prodId); //Pass Thru 3
            retVal.setPassThroughField14Str("PassThru" + prodId); //Pass Thru 4
            retVal.setPassThroughField15Str("PassThru" + prodId); //Pass Thru 5
        }
       return retVal;
    }

    /**
     * Creates a tab delimited string from a given product
     * @param prod
     * @return
     */
    private String toListingString(ProductImpl prod, boolean bNewMup) {
        StringBuilder sBuilder = new StringBuilder();
        sBuilder.append(prod.getGId() + "\t");
        sBuilder.append(prod.getCatalogStr() + "\t");
        sBuilder.append(prod.getIdSymbol() + "\t");
        sBuilder.append(prod.getCategoryStr() + "\t");
        sBuilder.append(prod.getPrice() + "\t");
        sBuilder.append(prod.getDiscountPrice() + "\t");
        sBuilder.append(prod.getBrandStr() + "\t");
        sBuilder.append(prod.getSupplierStr() + "\t");
        sBuilder.append(prod.getProviderStr() + "\t");
        sBuilder.append(prod.getProductName() + "\t");
        sBuilder.append(prod.getDescription() + "\t");
        sBuilder.append(prod.getRank() + "\t");
        sBuilder.append(prod.getThumbnail() + "\t");
        sBuilder.append(prod.getPurchaseUrl() + "\t");
        sBuilder.append(prod.getImageUrl() + "\t");
        sBuilder.append(prod.getImageWidth() + "\t");
        sBuilder.append(prod.getImageHeight() + "\t");
        sBuilder.append(prod.getCPC() + "\t");
        sBuilder.append(prod.getCurrencyStr() + "\t");
        sBuilder.append(prod.getDiscountPriceCurrencyStr() + "\t");
        sBuilder.append(prod.getBlackWhiteListStatus() + "\t");
        sBuilder.append(prod.getProductTypeStr() + "\t");
        sBuilder.append(prod.getCPO() + "\t");
        sBuilder.append(prod.getBaseProductNumber());
        if (bNewMup) {
            sBuilder.append("\t" + prod.getCountryStr() + "\t");
            sBuilder.append(prod.getStateStr() + "\t");
            sBuilder.append(prod.getCityStr() + "\t");
            sBuilder.append(prod.getZipStr() + "\t");
            sBuilder.append(prod.getDmaCodeStr() + "\t");
            sBuilder.append(prod.getAreaCodeStr() + "\t");
            sBuilder.append(prod.getProviderCategoryStr() + "\t");
            sBuilder.append(prod.getGlobalIdStr() + "\t");
            sBuilder.append(prod.getCategoryField1Str() + "\t");
            sBuilder.append(prod.getCategoryField2Str() + "\t");
            sBuilder.append(prod.getCategoryField3Str() + "\t");
            sBuilder.append(prod.getCategoryField4Str() + "\t");
            sBuilder.append(prod.getCategoryField5Str() + "\t");
            sBuilder.append(prod.getPassThrough1Str() + "\t");
            sBuilder.append(prod.getPassThrough2Str() + "\t");
            sBuilder.append(prod.getPassThrough3Str() + "\t");
            sBuilder.append(prod.getPassThrough4Str() + "\t");
            sBuilder.append(prod.getPassThrough5Str());
        }
        sBuilder.append("\n");
        return sBuilder.toString();
    }

    /**
     * Generate the listings file
     * @param size
     * @param outFileName
     * @param bNewMup
     */
    private void generateListingFile(int size, String outFileName, boolean bNewMup) {
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(outFileName));
            for (int i=0;i<size;i++) {
                ProductImpl prod = getNewListing(bNewMup);
                out.write(toListingString(prod, bNewMup));
            }
            out.close();
            System.out.println("Done");
        } catch (IOException e) {
           System.out.println("IOException caught during generation of listing file");
            e.printStackTrace();
        }
    }

    /**
     * Entry point
     * @param args
     */
    public static void main(String[] args) {
        int size = 100000;
        String outFileName = "/opt/Tumri/joz/data/caa/US_JOZ01_100K_provider-content_1.0_.utf8";
        boolean bNewMup = false;
        String tbaseDir = "/Users/nipun/ws/depot/Tumri/tas/joz/test/data/";
        switch(args.length) {
            case 4: {
                tbaseDir = args[3];
            }
            case 3: {
                bNewMup = ("Y".equalsIgnoreCase(args[2]))?true:false;
            }
            case 2: {
                outFileName = args[1];
            }
            case 1: {
                size = Integer.parseInt(args[0]);
            }
            default: {

            }
        }
        if (tbaseDir!=null) {
            baseDir = tbaseDir;
        }
        System.out.println("Going to generate listing of size: " + size + " into outfile: " + outFileName + " , Is New Format: " + bNewMup);
        (new ListingTestDataProvider()).generateListingFile(size, outFileName, bNewMup);
    }
}

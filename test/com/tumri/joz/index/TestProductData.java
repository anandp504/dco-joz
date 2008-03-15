package com.tumri.joz.index;

import java.util.ArrayList;
import java.util.StringTokenizer;
import java.io.File;
import java.io.FileWriter;

/**
 * Data class for the Joz Index test
 * The format of the product string should be :
 * Gid,,CatalogStr,IdSymbol,CategoryStr,PriceStr,DiscountPriceStr,Brand,Supplier,Provider,ProductName,ProductDesc,Rank,
 * PurchaseURL,ImageURL,ImageWidth,ImageHeight,CPC,Currency,DiscountCurrency,BlackWhiteStatus,ProductType,CPO,BaseProductNumber
 * @author: nipun
 * Date: Feb 28, 2008
 * Time: 3:00:00 PM
 */
public class TestProductData {
    static ArrayList<String> newProducts = new ArrayList<String>(5);
    static ArrayList<String> oldProducts = new ArrayList<String>(5);
    static ArrayList<String> taxonomy = new ArrayList<String>();
    static String _provider = "APPLE";


    static {
        //Add the product data
        //SUMMARY : 1. Deleted product 10000000
        //SUMMARY : 2. Added product 10000004
        //SUMMARY : 3. No change for product 10000001
        //SUMMARY : 4. Updated the category and CPC for product 10000002
        //SUMMARY : 5. Updated the CPO for product 10000003

        //newProducts.add("US1000000,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,BRAND,APPLEUS,APPLEUS,P1 NAME, P1 DESC,0,tnurl1,purl1,iurl1,400,100,0,USD,USD,1,Product,0,US1000000");
        newProducts.add("US1000001,Catalog,ID,GLASSVIEW.TUMRI_14165,100,0,Brand1,APPLEUS,APPLEUS,P2 NAME, P2 DESC,0,tnurl2,purl2,iurl2,400,100,0,USD,USD,1,Product,0,US1000001");
        newProducts.add("US1000002,Catalog,ID,GLASSVIEW.TUMRI_14165,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl3,purl3,iurl2,400,100,0,USD,USD,1,Product,0,US1000002");
        newProducts.add("US1000003,Catalog,ID,GLASSVIEW.TUMRI_14165,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl4,purl4,iurl2,400,100,0,USD,USD,1,Product,0,US1000003");
        newProducts.add("US1000004,Catalog,ID,GLASSVIEW.TUMRI_14165,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl5,purl5,iurl2,400,100,0,USD,USD,1,Product,0,US1000004");

        oldProducts.add("US1000000,Catalog,ID,GLASSVIEW.TUMRI_14165,100,0,Brand1,APPLEUS,APPLEUS,P1 NAME, P1 DESC,0,tnurl1,purl1,iurl1,400,100,0,USD,USD,1,Product,0,US1000000");
        oldProducts.add("US1000001,Catalog,ID,GLASSVIEW.TUMRI_14165,100,0,Brand1,APPLEUS,APPLEUS,P2 NAME, P2 DESC,0,tnurl2,purl2,iurl2,400,100,0,USD,USD,1,Product,0,US1000001");
        oldProducts.add("US1000002,Catalog,ID,GLASSVIEW.TUMRI_14166,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl3,purl3,iurl2,400,100,5,USD,USD,1,Product,0,US1000002");
        oldProducts.add("US1000003,Catalog,ID,GLASSVIEW.TUMRI_14166,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl4,purl4,iurl2,400,100,0,USD,USD,1,Product,20,US1000003");
        //oldProducts.add("US1000004,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl5,purl5,iurl2,400,100,0,USD,USD,1,Product,0,US1000004");

//        oldProducts.add("US1000001,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P2 NAME, P2 DESC,0,tnurl2,purl2,iurl2,400,100,0,USD,USD,1,Product,0,US1000001");
//        oldProducts.add("US1000002,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl3,purl3,iurl2,400,100,0,USD,USD,1,Product,0,US1000002");
//        oldProducts.add("US1000003,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl4,purl4,iurl2,400,100,0,USD,USD,1,Product,0,US1000003");
//        oldProducts.add("US1000004,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl5,purl5,iurl2,400,100,0,USD,USD,1,Product,0,US1000004");
//
//        //oldProducts.add("US1000000,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P1 NAME, P1 DESC,0,tnurl1,purl1,iurl1,400,100,0,USD,USD,1,Product,0,US1000000");
//        //oldProducts.add("US1000001,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P2 NAME, P2 DESC,0,tnurl2,purl2,iurl2,400,100,0,USD,USD,1,Product,0,US1000001");
//        newProducts.add("US1000002,Catalog,ID,GLASSVIEW.TUMRI_14162,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl3,purl3,iurl2,400,100,5,USD,USD,1,Product,0,US1000002");
//        //oldProducts.add("US1000003,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,Brand1,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl4,purl4,iurl2,400,100,0,USD,USD,1,Product,20,US1000003");
//        //oldProducts.add("US1000004,Catalog,ID,GLASSVIEW.TUMRI_14164,100,0,APPLEUS,APPLEUS,P3 NAME, P4 DESC,0,tnurl5,purl5,iurl2,400,100,0,USD,USD,1,Product,0,US1000004");


        taxonomy.add("TUMRI_14165,Game Software,TUMRI_14164,Software");
        taxonomy.add("TUMRI_14166,Office Applications,TUMRI_14164,Software");
        taxonomy.add("TUMRI_14167,Learning Software,TUMRI_14164,Software");
        taxonomy.add("TUMRI_14168,Music & Entertainment Software,TUMRI_14164,Software");
        taxonomy.add("TUMRI_14169,Photography Software,TUMRI_14164,Software");
        taxonomy.add("TUMRI_14164,Software,TUMRI_14111,tumri");
    }


    public static void writeTaxonomy(File baseDir) {
        File newDataDir = new File(baseDir.getAbsolutePath() + "/new/data");
        newDataDir.mkdirs();
        writeTaxonomyFile(newDataDir);
        
        File oldDataDir = new File(baseDir.getAbsolutePath() + "/old/data");
        oldDataDir.mkdirs();
        writeTaxonomyFile(oldDataDir);

    }

    private static void writeTaxonomyFile(File baseDir) {
        FileWriter fw = null;
        try {
            File newMupFile = new File(baseDir + "/US_Tumri-Taxonomy_4-1_Taxonomy_1.0_.utf8");
            fw = new FileWriter(newMupFile);
            for (String tStr: taxonomy) {
                tStr = tStr.replaceAll("[,]","\t");
                fw.write(tStr + "\n");
            }
        } catch (Exception e) {
            System.err.println("Exception in writing taxonomy File");
        } finally {
            try {
                fw.close();
            } catch(Exception e) {
                //
            }
        }
    }

    public static void writeMupFiles(File baseDir) {
        File newDataDir = new File(baseDir.getAbsolutePath() + "/new/data");
        newDataDir.mkdirs();
        writeMup(newDataDir, newProducts, "US_" + _provider + "_002_provider-content_1.0_.utf8");

        File oldDataDir = new File(baseDir.getAbsolutePath() + "/old/data");
        oldDataDir.mkdirs();
        writeMup(oldDataDir, oldProducts, "US_" + _provider + "_001_provider-content_1.0_.utf8");
    }

    /**
     * Write the mup files to the given directory
     * @param baseDir
     */
    private static void writeMup(File baseDir, ArrayList<String> prodDetails, String fileName) {
        FileWriter fw = null;
        try {
            File newMupFile = new File(baseDir + "/" + fileName);
            fw = new FileWriter(newMupFile);
            for (String pStr: prodDetails) {
                pStr = pStr.replaceAll("[,]","\t");
                fw.write(pStr + "\n");
            }
        } catch (Exception e) {
            System.err.println("Exception in writing MUP File");
        } finally {
            try {
                fw.close();
            } catch(Exception e) {
                //
            }
        }
    }
}

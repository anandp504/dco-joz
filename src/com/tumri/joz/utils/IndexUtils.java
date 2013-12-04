package com.tumri.joz.utils;

import com.tumri.content.data.*;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.products.IProduct;
import org.apache.log4j.Logger;

/**
 * Utilities class used by the Joz Index operations
 * @author: nipun
 * Date: Mar 10, 2008
 * Time: 9:20:55 PM
 */
public class IndexUtils {

    private static Logger log = Logger.getLogger(IndexUtils.class);
    private static int getAttributeFieldPos(Product.Attribute kAttr) {
        int result = 0;

        switch(kAttr) {
            case kExternalFilterField1:
                result = 1;
                break;
            case kExternalFilterField2:
                result = 2;
                break;
            case kExternalFilterField3:
                result = 3;
                break;
            case kExternalFilterField4:
                result = 4;
                break;
            case kExternalFilterField5:
                result = 5;
                break;
            case kUT1:
                result = 6;
                break;
            case kUT2:
                result = 7;
                break;
            case kUT3:
                result = 8;
                break;
            case kUT4:
                result = 9;
                break;
            case kUT5:
                result = 10;
                break;
        }

        return result;
    }

    /**
     * Pack the values into a single long value.
     * The field pos is expected to be a integer between 1 and 7 since only 3 bits are used.
     * <field pos: 3 bits><Cat id : 29 bits><val : 32>
     * @param catId
     * @param kAttr
     * @param valId
     * @return
     */
    public static long createIndexKeyForCategoryAttribute(long catId, Product.Attribute kAttr, long valId) {
        long fieldPos = getAttributeFieldPos(kAttr);
        long l1 = (catId << (64-(29+1))) & 0x0FFFFFFF00000000L;
        long l2 = (fieldPos << (64-(3+1))) & 0xF000000000000000L;
        long l3 = l2 | l1;
        long l4 = l3 | (valId & 0x00000000FFFFFFFFL);
        return l4;
    }

    /**
     * Returns a int array of unpacked values.
     * 0 --> ValId
     * 1 --> Cat Id
     * 2 --> Field Pos.
     * @param key
     * @return
     */
    public static int[] getValuesFromCategoryAttrKey(long key) {
        int[] results = new int[3];
        long rVal = key & 0x00000000FFFFFFFFL;
        long cVal = key & 0x0FFFFFFF00000000L;
        cVal = cVal >> (64-(29+1));
        long fVal = key & 0xF000000000000000L;
        fVal = fVal >> (64-(3+1));
        results[0] = (int)rVal;
        results[1] = (int)cVal;
        results[2] = (int)fVal;
        return results;
    }

    /**
     * Pack the values into a single long value.
     * The field pos is expected to be a integer between 1 and 7 since only 3 bits are used.
     * <field pos: 3 bits><val : 32>
     * @param kAttr
     * @param valId
     * @return
     */
    public static long createLongIndexKey(Product.Attribute kAttr, long valId) {
        long fieldPos = getAttributeFieldPos(kAttr);
        long l2 = (fieldPos << (64-(3+1))) & 0xF000000000000000L;
        long l3 = l2 | (valId & 0x00000000FFFFFFFFL);
        return l3;
    }

    /**
     * Returns a int array of unpacked values.
     * 0 --> ValId
     * 1 --> Field Pos.
     * @param key
     * @return
     */
    public static int[] getValuesFromLongAttrKey(long key) {
        int[] results = new int[2];
        long rVal = key & 0x00000000FFFFFFFFL;
        long fVal = key & 0xF000000000000000L;
        fVal = fVal >> (64-(3+1));
        results[0] = (int)rVal;
        results[1] = (int)fVal;
        return results;
    }

    /**
     * Maps the given index name string to the equivalent dictionary id
     * @return
     */
    public static Integer getIndexIdFromDictionary(Product.Attribute attr, String indexVal) {
        Integer id = null;

        switch(attr) {
            case kCategory:
                id = DictionaryManager.getId(Product.Attribute.kCategory, indexVal);
                break;
            case kPrice:
                id = DictionaryManager.getId(Product.Attribute.kPrice, indexVal);
                break;
            case kBrand:
                id = DictionaryManager.getId(Product.Attribute.kBrand, indexVal);
                break;
            case kSupplier:
                id = DictionaryManager.getId(Product.Attribute.kSupplier, indexVal);
                break;
            case kProvider:
                id = DictionaryManager.getId(Product.Attribute.kProvider, indexVal);
                break;
            case kImageWidth:
                try {
                    id = Integer.parseInt(indexVal);
                } catch (NumberFormatException e) {
                    log.error("Invalid integer value passed in for imagewidth : " + indexVal);
                }
                break;
            case kImageHeight:
                try {
                    id = Integer.parseInt(indexVal);
                } catch (NumberFormatException e) {
                    log.error("Invalid integer value passed in for imagewidth : " + indexVal);
                }
                break;
            case kCPC:
                id = DictionaryManager.getId(Product.Attribute.kCPC, indexVal);
                break;
            case kProductType:
                id = DictionaryManager.getId(Product.Attribute.kProductType, indexVal);
                break;
            case kCPO:
                id = DictionaryManager.getId(Product.Attribute.kCPO, indexVal);
                break;
            case kCountry:
                id = DictionaryManager.getId(Product.Attribute.kCountry, indexVal);
                break;
            case kState:
                id = DictionaryManager.getId(Product.Attribute.kState, indexVal);
                break;
            case kCity:
                id = DictionaryManager.getId(Product.Attribute.kCity, indexVal);
                break;
            case kZip:
                id = DictionaryManager.getId(Product.Attribute.kZip, indexVal);
                break;
            case kDMA:
                id = DictionaryManager.getId(Product.Attribute.kDMA, indexVal);
                break;
            case kArea:
                id = DictionaryManager.getId(Product.Attribute.kArea, indexVal);
                break;
            case kGeoEnabledFlag:
                id = DictionaryManager.getId(Product.Attribute.kGeoEnabledFlag, indexVal);
                break;
            case kProviderCategory:
                id = DictionaryManager.getId(Product.Attribute.kProviderCategory, indexVal);
                break;
            case kGlobalId:
                id = DictionaryManager.getId(Product.Attribute.kGlobalId, indexVal);
                break;
            case kAge:
                id = DictionaryManager.getId(Product.Attribute.kAge, indexVal);
                break;
            case kHHI:
                id = DictionaryManager.getId(Product.Attribute.kHHI, indexVal);
                break;
            case kCC:
                id = DictionaryManager.getId(Product.Attribute.kCC, indexVal);
                break;
            case kBT:
                id = DictionaryManager.getId(Product.Attribute.kBT, indexVal);
                break;
            case kGender:
                id = DictionaryManager.getId(Product.Attribute.kGender, indexVal);
                break;
            case kExternalFilterField1:
                id = DictionaryManager.getId(Product.Attribute.kExternalFilterField1, indexVal);
                break;
            case kExternalFilterField2:
                id = DictionaryManager.getId(Product.Attribute.kExternalFilterField2, indexVal);
                break;
            case kExternalFilterField3:
                id = DictionaryManager.getId(Product.Attribute.kExternalFilterField3, indexVal);
                break;
            case kExternalFilterField4:
                id = DictionaryManager.getId(Product.Attribute.kExternalFilterField4, indexVal);
                break;
            case kExternalFilterField5:
                id = DictionaryManager.getId(Product.Attribute.kExternalFilterField5, indexVal);
                break;
            case kUT1:
                id = DictionaryManager.getId(Product.Attribute.kUT1, indexVal);
                break;
            case kUT2:
                id = DictionaryManager.getId(Product.Attribute.kUT2, indexVal);
                break;
            case kUT3:
                id = DictionaryManager.getId(Product.Attribute.kUT3, indexVal);
                break;
            case kUT4:
                id = DictionaryManager.getId(Product.Attribute.kUT4, indexVal);
                break;
            case kUT5:
                id = DictionaryManager.getId(Product.Attribute.kUT5, indexVal);
                break;
            case kRank:
                try {
                    id = Integer.parseInt(indexVal);
                } catch (NumberFormatException e) {
                    id = 0;
                }
                break;
            case kDiscount:
                try {
                    id = Integer.parseInt(indexVal);
                } catch (NumberFormatException e) {
                    id = 0;
                }
                break;
	        case kExperienceId:
	        case kExperienceIdF1:
	        case kExperienceIdF2:
	        case kExperienceIdF3:
	        case kExperienceIdF4:
	        case kExperienceIdF5:
	        case kExperienceIdUT1:
	        case kExperienceIdUT2:
	        case kExperienceIdUT3:
	        case kExperienceIdUT4:
	        case kExperienceIdUT5:
             id = DictionaryManager.getId(attr, indexVal);
             break;
        }
        return id;
    }


    /**
     * Maps the given index name string to the equivalent Product Attribute
     * @return
     */
    public static Product.Attribute getAttribute(String indexType) {
        Product.Attribute id = null;
        if (indexType.equals("category")) {
            id =Product.Attribute.kCategory;
        } else if (indexType.equals("price")) {
            id = Product.Attribute.kPrice;
        } else if (indexType.equals("brand")) {
            id = Product.Attribute.kBrand;
        } else if (indexType.equals("supplier")) {
            id =Product.Attribute.kSupplier;
        } else if (indexType.equals("provider")) {
            id = Product.Attribute.kProvider;
        } else if (indexType.equals("imagewidth")) {
            id = Product.Attribute.kImageWidth;
        } else if (indexType.equals("imageheight")) {
            id = Product.Attribute.kImageHeight;
        } else if (indexType.equals("cpc")) {
            id = Product.Attribute.kCPC;
        } else if (indexType.equals("producttype")) {
            id = Product.Attribute.kProductType;
        } else if (indexType.equals("cpo")) {
            id = Product.Attribute.kCPO;
        } else if (indexType.equals("country")) {
            id = Product.Attribute.kCountry;
        } else if (indexType.equals("state")) {
            id = Product.Attribute.kState;
        } else if (indexType.equals("city")) {
            id = Product.Attribute.kCity;
        } else if (indexType.equals("zip")) {
            id = Product.Attribute.kZip;
        } else if (indexType.equals("dma")) {
            id = Product.Attribute.kDMA;
        } else if (indexType.equals("area")) {
            id = Product.Attribute.kArea;
        } else if (indexType.equals("geoenabled")) {
            id = Product.Attribute.kGeoEnabledFlag;
        } else if (indexType.equals("providercategory")) {
            id = Product.Attribute.kProviderCategory;
        } else if (indexType.equals("globalid")) {
            id = Product.Attribute.kGlobalId;
        } else if (indexType.equals("age")) {
            id = Product.Attribute.kAge;
        } else if (indexType.equals("gender")) {
            id = Product.Attribute.kGender;
        } else if (indexType.equals("hhi")) {
            id = Product.Attribute.kHHI;
        } else if (indexType.equals("ms") || indexType.equals("cc")) {
            id = Product.Attribute.kCC;
        } else if (indexType.equals("bt")) {
            id = Product.Attribute.kBT;
        } else if (indexType.equals("externalfilterf1") || indexType.equalsIgnoreCase("f1")) {
            id = Product.Attribute.kExternalFilterField1;
        } else if (indexType.equals("externalfilterf2") || indexType.equalsIgnoreCase("f2")) {
            id = Product.Attribute.kExternalFilterField2;
        } else if (indexType.equals("externalfilterf3") || indexType.equalsIgnoreCase("f3")) {
            id = Product.Attribute.kExternalFilterField3;
        } else if (indexType.equals("externalfilterf4") || indexType.equalsIgnoreCase("f4")) {
            id = Product.Attribute.kExternalFilterField4;
        } else if (indexType.equals("externalfilterf5") || indexType.equalsIgnoreCase("f5")) {
            id = Product.Attribute.kExternalFilterField5;
        } else if (indexType.equalsIgnoreCase("ut1")) {
            id = Product.Attribute.kUT1;
        } else if (indexType.equalsIgnoreCase("ut2")) {
            id = Product.Attribute.kUT2;
        } else if (indexType.equalsIgnoreCase("ut3")) {
            id = Product.Attribute.kUT3;
        } else if (indexType.equalsIgnoreCase("ut4")) {
            id = Product.Attribute.kUT4;
        } else if (indexType.equalsIgnoreCase("ut5")) {
            id = Product.Attribute.kUT5;
        } else if (indexType.equals("rank")) {
            id = Product.Attribute.kRank;
        } else if (indexType.equals("discount")) {
            id = Product.Attribute.kDiscount;
        } else if (indexType.equals("optExp")){
	        id = Product.Attribute.kExperienceId;
        } else if (indexType.equals("optF1")){
	        id = Product.Attribute.kExperienceIdF1;
        } else if (indexType.equals("optF2")){
	        id = Product.Attribute.kExperienceIdF2;
        } else if (indexType.equals("optF3")){
	        id = Product.Attribute.kExperienceIdF3;
        } else if (indexType.equals("optF4")){
	        id = Product.Attribute.kExperienceIdF4;
        } else if (indexType.equals("optF5")){
	        id = Product.Attribute.kExperienceIdF5;
        } else if (indexType.equals("optUT1")){
	        id = Product.Attribute.kExperienceIdUT1;
        } else if (indexType.equals("optUT2")){
	        id = Product.Attribute.kExperienceIdUT2;
        } else if (indexType.equals("optUT3")){
	        id = Product.Attribute.kExperienceIdUT3;
        } else if (indexType.equals("optUT4")){
	        id = Product.Attribute.kExperienceIdUT4;
        } else if (indexType.equals("optUT5")){
	        id = Product.Attribute.kExperienceIdUT5;
        }
        return id;
    }

    public static String getIndexName(IProduct.Attribute id) {
        String name = null;
        if (id ==Product.Attribute.kCategory) {
            name = "category";
        } else if (id == Product.Attribute.kPrice) {
            name = "price";
        } else if (id == Product.Attribute.kBrand) {
            name = "brand";
        } else if (id ==Product.Attribute.kSupplier) {
            name = "supplier";
        } else if (id == Product.Attribute.kProvider) {
            name = "provider";
        } else if (id == Product.Attribute.kImageWidth) {
            name = "imagewidth";
        } else if (id == Product.Attribute.kImageHeight) {
            name = "imageheight";
        } else if (id == Product.Attribute.kCPO) {
            name = "cpo";
        } else if (id == Product.Attribute.kCPC) {
            name = "cpc";
        } else if (id == Product.Attribute.kProductType) {
            name = "producttype";
        } else if (id == Product.Attribute.kCountry) {
            name = "country";
        } else if (id == Product.Attribute.kState) {
            name = "state";
        } else if (id == Product.Attribute.kCity) {
            name = "city";
        } else if (id == Product.Attribute.kZip) {
            name = "zip";
        } else if (id == Product.Attribute.kDMA) {
            name = "dma";
        } else if (id == Product.Attribute.kArea) {
            name = "area";
        } else if (id == Product.Attribute.kGeoEnabledFlag) {
            name = "geoenabled";
        } else if (id == Product.Attribute.kProviderCategory) {
            name = "providercategory";
        } else if (id == Product.Attribute.kGlobalId) {
            name = "globalid";
        } else if (id == Product.Attribute.kAge) {
            name = "age";
        } else if (id == Product.Attribute.kGender) {
            name = "gender";
        } else if (id == Product.Attribute.kBT) {
            name = "bt";
        } else if (id == Product.Attribute.kCC) {
            name = "cc";
        } else if (id == Product.Attribute.kHHI) {
            name = "hhi";
        } else if (id == Product.Attribute.kExternalFilterField1) {
            name = "externalfilterf1";
        } else if (id == Product.Attribute.kExternalFilterField2) {
            name = "externalfilterf2";
        } else if (id == Product.Attribute.kExternalFilterField3) {
            name = "externalfilterf3";
        } else if (id == Product.Attribute.kExternalFilterField4) {
            name = "externalfilterf4";
        } else if (id == Product.Attribute.kExternalFilterField5) {
            name = "externalfilterf5";
        } else if (id == Product.Attribute.kUT1) {
            name = "ut1";
        } else if (id == Product.Attribute.kUT2) {
            name = "ut2";
        } else if (id == Product.Attribute.kUT3) {
            name = "ut3";
        } else if (id == Product.Attribute.kUT4) {
            name = "ut4";
        } else if (id == Product.Attribute.kUT5) {
            name = "ut5";
        } else if (id == Product.Attribute.kExperienceId){
	        name = "optExperienceId";
        } else if (id == Product.Attribute.kExperienceIdF1){
	        name = "optF1";
        } else if (id == Product.Attribute.kExperienceIdF2){
	        name = "optF2";
        } else if (id == Product.Attribute.kExperienceIdF3){
	        name = "optF3";
        } else if (id == Product.Attribute.kExperienceIdF4){
	        name = "optF4";
        } else if (id == Product.Attribute.kExperienceIdF5){
	        name = "optF5";
        } else if (id == Product.Attribute.kExperienceIdUT1){
	        name = "optUT1";
        } else if (id == Product.Attribute.kExperienceIdUT2){
	        name = "optUT2";
        } else if (id == Product.Attribute.kExperienceIdUT3){
	        name = "optUT3";
        } else if (id == Product.Attribute.kExperienceIdUT4){
	        name = "optUT4";
        } else if (id == Product.Attribute.kExperienceIdUT5){
	        name = "optUT5";
        }

        return name;
    }
}

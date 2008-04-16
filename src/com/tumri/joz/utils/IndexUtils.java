package com.tumri.joz.utils;

import com.tumri.content.data.*;
import com.tumri.content.data.dictionary.DictionaryManager;
import com.tumri.joz.products.JOZTaxonomy;
import org.apache.log4j.Logger;

/**
 * Utilities class used by the Joz Index operations
 * @author: nipun
 * Date: Mar 10, 2008
 * Time: 9:20:55 PM
 */
public class IndexUtils {

    private static Logger log = Logger.getLogger(IndexUtils.class);
    private static int getCategoryFieldPos(Product.Attribute kAttr) {
        int result = 0;

        switch(kAttr) {
            case kCategoryField1:
                result = 1;
                break;

            case kCategoryField2:
                result = 2;
                break;
            case kCategoryField3:
                result = 3;
                break;
            case kCategoryField4:
                result = 4;
                break;
            case kCategoryField5:
                result = 5;
                break;
        }

        return result;
    }

    /**
     * Get the cat details for the given category attr field.
     * @param catId
     * @param kAttr
     * @return
     */
    public static CategoryAttributeDetails getDetailsForCategoryField(int catId, Product.Attribute kAttr) {
        CategoryAttributeDetails result = null;
        JOZTaxonomy tax = JOZTaxonomy.getInstance();
        Taxonomy t = tax.getTaxonomy();
        CategorySpec cs = t.getCategorySpec(catId);
        if (cs != null) {
            result =  cs.getAttributeDetails(kAttr);
        } else {
            //Get the details from the parent if available
            Category parentCat = t.getParent(t.getCategory(catId));
            if (parentCat!=null && !parentCat.getIdStr().equals("TUMRI_00000")){
                return getDetailsForCategoryField(parentCat.getGlassId(), kAttr);
            }
        }
        return result;
    }

    /**
     * Get the cat details for the given category field name
     * @param catId
     * @param fieldName
     * @return
     */
    public static CategoryAttributeDetails getDetailsForCategoryFieldName(int catId, String fieldName) {
        CategoryAttributeDetails result = null;
        JOZTaxonomy tax = JOZTaxonomy.getInstance();
        Taxonomy t = tax.getTaxonomy();
        CategorySpec cs = t.getCategorySpec(catId);
        if (cs != null) {
            result =  cs.getAttributeForColumnName(fieldName);
        } else {
            //Get the details from the parent if available
            Category parentCat = t.getParent(t.getCategory(catId));
            if (parentCat!=null && !parentCat.getIdStr().equals("TUMRI_00000")){
                return getDetailsForCategoryFieldName(parentCat.getGlassId(), fieldName);
            }
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
    public static long createIndexKeyForCategory(long catId, Product.Attribute kAttr, long valId) {
        long fieldPos = getCategoryFieldPos(kAttr);
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
                id = DictionaryManager.getId(Product.Attribute.kImageWidth, indexVal);
                break;
            case kImageHeight:
                id = DictionaryManager.getId(Product.Attribute.kImageHeight, indexVal);
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
            case kCategoryField1:
                id = DictionaryManager.getId(Product.Attribute.kCategoryField1, indexVal);
                break;
            case kCategoryField2:
                id = DictionaryManager.getId(Product.Attribute.kCategoryField2, indexVal);
                break;
            case kCategoryField3:
                id = DictionaryManager.getId(Product.Attribute.kCategoryField3, indexVal);
                break;
            case kCategoryField4:
                id = DictionaryManager.getId(Product.Attribute.kCategoryField4, indexVal);
                break;
            case kCategoryField5:
                id = DictionaryManager.getId(Product.Attribute.kCategoryField5, indexVal);
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
        } else if (indexType.equals("categoryfield1")) {
            id = Product.Attribute.kCategoryField1;
        } else if (indexType.equals("categoryfield2")) {
            id = Product.Attribute.kCategoryField2;
        } else if (indexType.equals("categoryfield3")) {
            id = Product.Attribute.kCategoryField3;
        } else if (indexType.equals("categoryfield4")) {
            id = Product.Attribute.kCategoryField4;
        } else if (indexType.equals("categoryfield5")) {
            id = Product.Attribute.kCategoryField5;
        }
        return id;
    }


}

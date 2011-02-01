/* 
 * ProductWrapper.java
 * 
 * COPYRIGHT (C) 2007 TUMRI INC.  ALL RIGHTS RESERVED. TUMRI AND LOGO ARE 
 * EITHER TRADEMARKS OR REGISTERED TRADEMARKS OF TUMRI.  ALL OTHER COMPANY, 
 * PRODUCTS, AND BRAND NAMES ARE TRADEMARKS OF THEIR RESPECTIVE OWNERS. ALL MATERIAL
 * CONTAINED IN THIS FILE (INCLUDING, BUT NOT LIMITED TO, TEXT, IMAGES, GRAPHICS,
 * HTML, PROGRAMMING CODE AND SCRIPTS) CONSTITUTE PROPRIETARY AND CONFIDENTIAL 
 * INFORMATION PROTECTED BY COPYRIGHT LAWS, TRADE SECRET AND OTHER LAWS. NO PART 
 * OF THIS SOFTWARE MAY BE COPIED, REPRODUCED, MODIFIED OR DISTRIBUTED IN ANY FORM 
 * OR BY ANY MEANS, OR STORED IN A DATABASE OR RETRIEVAL SYSTEM WITHOUT THE PRIOR 
 * WRITTEN PERMISSION OF TUMRI INC.
 * 
 * @author Bhavin Doshi (bdoshi@tumri.com)
 * @version 1.0     Aug 30, 2007
 * 
 */
package com.tumri.joz.products;

import com.tumri.content.data.Product;

/**
 *
 * Created by Bhavin Doshi (bdoshi@tumri.com) on Aug 30, 2007
 * Company: Tumri Inc.
 */
public class ProductWrapper implements IProduct {
    
    protected Product prod = null;
    protected ProductHandle handle = null;
    private long id;
    
    public ProductWrapper(Product p) {
        super();
        if (p == null) {
            throw new NullPointerException("Product cannot be null for the wrapper");
        }
        prod = p;
        //handle = new ProductHandle(this, 1.0);
        id = p.getId();
        handle = new ProductHandle( new Double(1.0).doubleValue(), id);
    }

    public Product getProduct() {
      return prod;
    }

    public ProductHandle getHandle() {
          return handle;
    }
    
    public String getBaseProductNumber() {
        return prod.getBaseProductNumber();
    }

    public Object getValue(Attribute type) {
        return prod.getValue(type);
    }

    public Integer getCountry() {
        return prod.getCountry();
    }

    public String getCountryStr() {
        return prod.getCountryStr();
    }

    public Integer getState() {
        return prod.getState();
    }

    public String getStateStr() {
        return prod.getStateStr();
    }

    public Integer getCity() {
        return prod.getCity();
    }

    public String getCityStr() {
        return prod.getCityStr();
    }

    public Integer getZip() {
        return prod.getZip();
    }

    public String getZipStr() {
        return prod.getZipStr();
    }

    public Integer getDmaCode() {
        return prod.getDmaCode();
    }

    public String getDmaCodeStr() {
        return prod.getDmaCodeStr();
    }

    public Integer getAreaCode() {
        return prod.getAreaCode();
    }

    public String getAreaCodeStr() {
        return prod.getAreaCodeStr();  
    }

    public Integer getGeoEnabled() {
        return prod.getGeoEnabled();
    }

    public String getGeoEnabledStr() {
        return prod.getGeoEnabledStr();
    }

    public Integer getProviderCategory() {
        return prod.getProviderCategory();
    }

    public String getProviderCategoryStr() {
        return prod.getProviderCategoryStr();
    }

    public Integer getGlobalId() {
        return prod.getGlobalId();
    }

    public String getGlobalIdStr() {
        return prod.getGlobalIdStr();
    }

    public Integer getPassThrough1() {
        return prod.getPassThrough1();
    }

    public String getPassThrough1Str() {
        return  prod.getPassThrough1Str();
    }

    public Integer getPassThrough2() {
        return  prod.getPassThrough2();
    }

    public String getPassThrough2Str() {
        return  prod.getPassThrough2Str();
    }

    public Integer getPassThrough3() {
        return  prod.getPassThrough3();
    }

    public String getPassThrough3Str() {
        return  prod.getPassThrough3Str();
    }

    public Integer getPassThrough4() {
        return  prod.getPassThrough4();
    }

    public String getPassThrough4Str() {
        return  prod.getPassThrough4Str();
    }

    public Integer getPassThrough5() {
        return  prod.getPassThrough5();
    }

    public String getPassThrough5Str() {
        return  prod.getPassThrough5Str();
    }

    public Integer getBlackWhiteListStatus() {
        return prod.getBlackWhiteListStatus();
    }
    
    public Integer getBrand() {
        return prod.getBrand();
    }
    
    public String getBrandStr() {
        return prod.getBrandStr();
    }
    
    public Double getCPC() {
        return prod.getCPC();
    }
    
    public Double getCPO() {
        return prod.getCPO();
    }
    
    public Integer getCatalog() {
        return prod.getCatalog();
    }
    
    public String getCatalogStr() {
        return prod.getCatalogStr();
    }
    
    public Integer getCategory() {
        return prod.getCategory();
    }
    
    public String getCategoryStr() {
        return prod.getCategoryStr();
    }
    
    public Integer getCurrency() {
        return prod.getCurrency();
    }
    
    public String getCurrencyStr() {
        return prod.getCurrencyStr();
    }
    
    public String getDescription() {
        return prod.getDescription();
    }
    
    public Double getDiscountPrice() {
        return prod.getDiscountPrice();
    }
    
    public Integer getDiscountPriceCurrency() {
        return prod.getDiscountPriceCurrency();
    }
    
    public String getDiscountPriceCurrencyStr() {
        return prod.getDiscountPriceCurrencyStr();
    }
    
    public String getGId() {
        return prod.getGId();
    }
    
    public long getId() {
        return prod.getId();
    }
    
    public String getIdSymbol() {
        return prod.getIdSymbol();
    }
    
    public Integer getImageHeight() {
        return prod.getImageHeight();
    }
    
    public String getImageUrl() {
        return prod.getImageUrl();
    }
    
    public Integer getImageWidth() {
        return prod.getImageWidth();
    }
    
    public Double getPrice() {
        return prod.getPrice();
    }
    
    public String getProductName() {
        return prod.getProductName();
    }
    
    public Integer getProductType() {
        return prod.getProductType();
    }
    
    public String getProductTypeStr() {
        return prod.getProductTypeStr();
    }
    
    public Integer getProvider() {
        return prod.getProvider();
    }
    
    public String getProviderStr() {
        return prod.getProviderStr();
    }
    
    public String getPurchaseUrl() {
        return prod.getPurchaseUrl();
    }
    
    public Integer getRank() {
        return prod.getRank();
    }
    
    public Integer getSupplier() {
        return prod.getSupplier();
    }
    
    public String getSupplierStr() {
        return prod.getSupplierStr();
    }
    
    public String getThumbnail() {
        return prod.getThumbnail();
    }

    public Integer getExternalFilterField1() {
        return prod.getExternalFilterField1();
    }

    public String getExternalFilterField1Str() {
        return prod.getExternalFilterField1Str();
    }

    public Integer getExternalFilterField2() {
        return prod.getExternalFilterField2();
    }

    public String getExternalFilterField2Str() {
        return prod.getExternalFilterField2Str();
    }

    public Integer getExternalFilterField3() {
        return prod.getExternalFilterField3();
    }

    public String getExternalFilterField3Str() {
        return prod.getExternalFilterField3Str();
    }

    public Integer getExternalFilterField4() {
        return prod.getExternalFilterField4();
    }

    public String getExternalFilterField4Str() {
        return prod.getExternalFilterField4Str();
    }

    public Integer getExternalFilterField5() {
        return prod.getExternalFilterField5();
    }

    public String getExternalFilterField5Str() {
        return prod.getExternalFilterField5Str();
    }

    public int compareTo(Object ip) {
        ProductWrapper o = (ProductWrapper)ip;
        return (id < o.id ? -1 :
            id == o.id ? 0 : 1);
    }
    
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Product lProduct = (Product) o;

        if (getGId() != null ? !getGId().equals(lProduct.getGId()) : lProduct.getGId() != null) return false;

        return true;
      }

      public int hashCode() {
        return (getGId() != null ? getGId().hashCode() : 0);
      }

    public Integer getAge() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getAgeStr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getGender() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getGenderStr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getHHI() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getHHIStr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getBT() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getBTStr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getCC() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getCCStr() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getUT1() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUT1Str() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getUT2() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUT2Str() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getUT3() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUT3Str() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getUT4() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUT4Str() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getUT5() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public String getUT5Str() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Integer getDiscount() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}

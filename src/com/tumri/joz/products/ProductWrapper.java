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
public class ProductWrapper implements IProduct, Comparable<IProduct> {
    
    protected Product prod = null;
    protected ProductHandle handle = null;
    
    public ProductWrapper(Product p) {
        super();
        if (p == null) {
            throw new NullPointerException("Product cannot be null for the wrapper");
        }
        prod = p;
        handle = new ProductHandle(this, 1.0);
    }
    
    public ProductHandle getHandle() {
          return handle;
    }
    
    public String getBaseProductNumber() {
        return prod.getBaseProductNumber();
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
    
    public int getId() {
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
    
    public int compareTo(IProduct o) {
        return (getId() < o.getId() ? -1 :
            getId() == o.getId() ? 0 : 1);
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


    
}

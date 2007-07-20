package com.tumri.joz.products;

import com.tumri.joz.index.DictionaryManager;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class Product implements IProduct {
  private String m_Gid;
  private Integer m_Catalog;
  private Integer m_Category;
  private Double m_Price;
  private Double m_DiscountPrice;
  private Integer m_Brand;
  private Integer m_Supplier;
  private Integer m_Provider;
  private String m_ProductName;
  private String m_Description;
  private int    m_Rank;
  private String m_Thumbnail;
  private String m_PurchaseUrl;
  private String m_ImageUrl;
  private int m_ImageWidth;
  private int m_ImageHeight;
  private Double m_CPC;
  private Integer m_Currency;
  private Integer m_DiscountPriceCurrency;
  private Integer m_BlackWhiteListStatus;
  private Integer m_ProductType;
  private Double m_CPO;
  private String m_BaseProductNumber;

  // Handle object is constructed on the fly
  private ProductHandle m_handle;
  private int m_id = 0;

  public int getId() {
    return m_id;
  }

  public String getGId() {
    return m_Gid;
  }

  public void setGId(String aId) {
    m_Gid = aId;
    m_id = DictionaryManager.getInstance().getId(IProduct.Attribute.kId, m_Gid);
  }

  public final Integer getCatalog() {
    return m_Catalog;
  }

  public void setCatalog(String aCatalog) {
    m_Catalog = DictionaryManager.getInstance().getId(Attribute.kCatalog, aCatalog);
  }

  public Integer getCategory() {
    return m_Category;
  }

  public void setCategory(String aCategory) {
    m_Category = DictionaryManager.getInstance().getId(Attribute.kCategory, aCategory);
  }

  public Double getPrice() {
    return m_Price;
  }

  public void setPrice(String aPrice) {
    try {
      m_Price = new Double(aPrice);
    } catch (Exception e) {
      System.err.println("Error in parsing Price value");
      m_Price = new Double(0.0);
    }
  }

  public Double getDiscountPrice() {
    return m_DiscountPrice;
  }

  public void setDiscountPrice(String aDiscountPrice) {
    try {
      m_DiscountPrice = new Double(aDiscountPrice);
    } catch (NumberFormatException e) {
      System.err.println("Error in parsing DiscountPrice value");
    }
  }

  public Integer getBrand() {
    return m_Brand;
  }

  public void setBrand(String aBrand) {
    m_Brand = DictionaryManager.getInstance().getId(Attribute.kBrand, aBrand);
  }

  public Integer getSupplier() {
    return m_Supplier;
  }

  public void setSupplier(String aSupplier) {
    m_Supplier = DictionaryManager.getInstance().getId(Attribute.kSupplier, aSupplier);
  }

  public Integer getProvider() {
    return m_Provider;
  }

  public void setProvider(String aProvider) {
    m_Provider = DictionaryManager.getInstance().getId(Attribute.kProvider, aProvider);
  }

  public String getProductName() {
    return m_ProductName;
  }

  public void setProductName(String aProductName) {
    m_ProductName = aProductName;
  }

  public String getDescription() {
    return m_Description;
  }

  public void setDescription(String aDescription) {
    m_Description = aDescription;
  }

  public int getRank() {
    return m_Rank;
  }

  public void setRank(String aRank) {
    try {
      m_Rank = new Integer(aRank); // @todo explore this
    } catch (NumberFormatException e) {
      System.err.println("Error in parsing Rank value");
    }
  }

  public String getThumbnail() {
    return m_Thumbnail;
  }

  public void setThumbnail(String aThumbnail) {
    m_Thumbnail = aThumbnail;
  }

  public String getPurchaseUrl() {
    return m_PurchaseUrl;
  }

  public void setPurchaseUrl(String aPurchaseUrl) {
    m_PurchaseUrl = aPurchaseUrl;
  }

  public String getImageUrl() {
    return m_ImageUrl;
  }

  public void setImageUrl(String aImageUrl) {
    m_ImageUrl = aImageUrl;
  }

  public int getImageWidth() {
    return m_ImageWidth;
  }

  public void setImageWidth(String aImageWidth) {
    try {
      m_ImageWidth = new Integer(aImageWidth);
    } catch (NumberFormatException e) {
      System.err.println("Error in parsing ImageWidth value");
    }
  }

  public int getImageHeight() {
    return m_ImageHeight;
  }

  public void setImageHeight(String aImageHeight) {
    try {
      m_ImageHeight = new Integer(aImageHeight);
    } catch (NumberFormatException e) {
      System.err.println("Error in parsing ImageHeight value");
    }
  }

  public Double getCPC() {
    return m_CPC;
  }

  public void setCPC(String aCPC) {
    try {
      m_CPC = new Double(aCPC);
    } catch (NumberFormatException e) {
      System.err.println("Error in parsing CPC value");
      m_CPC = new Double(0.0);
    }
  }

  public Integer getCurrency() {
    return m_Currency;
  }

  public void setCurrency(String aCurrency) {
    m_Currency = DictionaryManager.getInstance().getId(Attribute.kCurrency, aCurrency);
  }

  public Integer getDiscountPriceCurrency() {
    return m_DiscountPriceCurrency;
  }

  public void setDiscountPriceCurrency(String aDiscountPriceCurrency) {
    m_DiscountPriceCurrency = DictionaryManager.getInstance().getId(Attribute.kDiscountPriceCurrency, aDiscountPriceCurrency);
  }

  public Integer getBlackWhiteListStatus() {
    return m_BlackWhiteListStatus;
  }

  public void setBlackWhiteListStatus(String aBlackWhiteListStatus) {
    m_BlackWhiteListStatus = DictionaryManager.getInstance().getId(Attribute.kBlackWhiteListStatus, aBlackWhiteListStatus);
  }

  public Integer getProductType() {
    return m_ProductType;
  }

  public void setProductType(String aProductType) {
    m_ProductType = DictionaryManager.getInstance().getId(Attribute.kProductType, aProductType);
  }

  public Double getCPO() {
    return m_CPO;
  }

  public void setCPO(String aCPO) {
    try {
      m_CPO = new Double(aCPO);
    } catch (NumberFormatException e) {
      System.err.println("Error in parsing CPO value");
      m_CPO = new Double(0.0);
    }
  }

  public String getBaseProductNumber() {
    return m_BaseProductNumber;
  }

  public void setBaseProductNumber(String aBaseProductNumber) {
    m_BaseProductNumber = aBaseProductNumber;
  }

  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Product lProduct = (Product) o;

    if (m_Gid != null ? !m_Gid.equals(lProduct.m_Gid) : lProduct.m_Gid != null) return false;

    return true;
  }

  public int hashCode() {
    return (m_Gid != null ? m_Gid.hashCode() : 0);
  }

  public ProductHandle getHandle() {
    if (m_handle == null) {
      m_handle = new ProductHandle(getId(),DictionaryManager.getInstance().getId(IProduct.Attribute.kRank,getRank()));
    }
    return m_handle;
  }
}


package com.tumri.joz.products;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */

/**
 * Defines the interface object called IProduct
 */
public interface IProduct {
  public ProductHandle getHandle();
  public String getGId();
  public int getId();
  public Integer getCatalog();
  public Integer getCategory();
  public Double getPrice();
  public Double getDiscountPrice();
  public Integer getBrand();
  public Integer getSupplier();
  public Integer getProvider();
  public String getProductName();
  public String getDescription();
  public int    getRank();
  public String getThumbnail();
  public String getPurchaseUrl();
  public String getImageUrl();
  public int getImageWidth();
  public int getImageHeight();
  public Double getCPC();
  public Integer getCurrency();
  public Integer getDiscountPriceCurrency();
  public Integer getBlackWhiteListStatus();
  public Integer getProductType();
  public Double getCPO();
  public String getBaseProductNumber();

  enum Attribute {
    kGId,
    kId,
    kCatalog,
    kCategory,
    kPrice,
    kDiscountPrice,
    kBrand,
    kSupplier,
    kProvider,
    kProductName,
    kDescription,
    kRank,
    kThumbnail,
    kPurchaseUrl,
    kImageUrl,
    kImageWidth,
    kImageHeight,
    kCPC,
    kCurrency,
    kDiscountPriceCurrency,
    kBlackWhiteListStatus,
    kProductType,
    kCPO,
    kBaseProductNumber,
    kKeywords,
    kNone
  };
}
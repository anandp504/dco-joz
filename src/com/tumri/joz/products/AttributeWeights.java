package com.tumri.joz.products;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class AttributeWeights {
  private static double kCategory = 1.2;
  private static double kPrice = 1.2;
  private static double kBrand = 1.2;
  private static double kSupplier = 1.2;
  private static double kProvider = 1.2;
  private static double kCPC = 1.2;
  private static double kCPO = 1.2;
  private static double kKeywords = 3.0;
  private static double kNone = 10.0;

  public static double getWeight(IProduct.Attribute attr) {
    switch(attr) {
      case kCategory: return kCategory;
      case kPrice: return kPrice;
      case kBrand: return kBrand;
      case kSupplier: return kSupplier;
      case kProvider: return kProvider;
      case kCPC: return kCPC;
      case kCPO: return kCPO;
      case kKeywords: return kKeywords;
      case kNone: return kNone;
      default:
    }
    return 1.0;
  }
}

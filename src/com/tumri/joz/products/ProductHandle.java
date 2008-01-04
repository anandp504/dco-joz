package com.tumri.joz.products;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductHandle implements Handle {
  private IProduct m_product;
  private double m_score;
  private int m_oid;


  public ProductHandle(IProduct aProduct, double aScore) {
    m_product = aProduct;
    m_score = aScore;
    m_oid = aProduct.getId();
  }


  protected ProductHandle(double aScore, int aOid) {
    m_score = aScore;
    m_oid = aOid;
  }

  private ProductHandle(ProductHandle handle, double aScore) {
    m_score = aScore;
    m_oid = handle.getOid();
  }

  public int getOid() {
    return m_oid;
  }

  public double getScore() {
    return m_score;
  }


  public IProduct getProduct() {
    return m_product;
  }

  public int compareTo(Object handle) {
    ProductHandle ph = (ProductHandle)handle;
    return (m_oid < ph.m_oid ? -1 :
            m_oid == ph.m_oid ? 0 : 1);
  }


  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ProductHandle that = (ProductHandle) o;

    if (m_oid != that.m_oid) return false;

    return true;
  }

  public int hashCode() {
    return m_oid;
  }


  public int compare(Object h1, Object h2) {
    ProductHandle handle1 = (ProductHandle)h1;
    ProductHandle handle2 = (ProductHandle)h2;
    if (handle1.m_score > handle2.m_score) return -1;
    if (handle1.m_score < handle2.m_score) return 1;
    if (handle1.m_oid < handle2.m_oid) return -1;
    if (handle1.m_oid > handle2.m_oid) return 1;
    return 0;
  }

  public Handle createHandle(double score) {
    return (score != m_score ? new ProductHandle(this,score) : this);
  }
}
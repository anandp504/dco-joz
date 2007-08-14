package com.tumri.joz.products;

import java.util.Comparator;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductHandle implements Handle<ProductHandle> {
  private IProduct m_product;
  private double m_score;
  private int m_oid;


  public ProductHandle(IProduct aProduct, double aScore) {
    m_product = aProduct;
    m_score = aScore;
    m_oid = m_product.getId();
  }

  public int getOid() {
    return m_oid;  }

  public double getScore() {
    return m_score;
  }


  public IProduct getProduct() {
    return m_product;
  }

  public int compareTo(ProductHandle handle) {
    return (m_oid < handle.m_oid ? -1 :
            m_oid == handle.m_oid ? 0 : 1);
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


  public int compare(ProductHandle handle1, ProductHandle handle2) {
    if (handle1.m_score > handle2.m_score) return -1;
    if (handle1.m_score < handle2.m_score) return 1;
    if (handle1.m_oid < handle2.m_oid) return -1;
    if (handle1.m_oid > handle2.m_oid) return 1;
    return 0;
  }

  public Handle createHandle(double score) {
    return (score != m_score ? new ProductHandle(m_product,score) : this);
  }
}

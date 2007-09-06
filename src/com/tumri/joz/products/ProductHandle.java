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
    m_oid = m_product.getId();
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

  public int compareTo(Handle handle) {
    return (getOid() < handle.getOid() ? -1 :
            getOid() == handle.getOid() ? 0 : 1);
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


  public int compare(Handle handle1, Handle handle2) {
    if (handle1.getScore() > handle2.getScore()) return -1;
    if (handle1.getScore() < handle2.getScore()) return 1;
    if (handle1.getOid() < handle2.getOid()) return -1;
    if (handle1.getOid() > handle2.getOid()) return 1;
    return 0;
  }

  public Handle createHandle(double score) {
    return (score != m_score ? new ProductHandle(m_product,score) : this);
  }
}
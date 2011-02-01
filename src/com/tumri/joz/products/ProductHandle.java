package com.tumri.joz.products;

import org.junit.Test;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public class ProductHandle implements Handle {
	private double m_score;
	private long m_oid;
	private long m_bitMap;
	//private byte m_type;//at least 3 buckets -- 15 bits
	// 1111 1111 1111 1110 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 -- FFFE000000000000
	// 0000 0000 0000 0001 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 -- 0001FFFFFFFFFFFF

	//private int m_age;//at least 10 buckets -- 20 bits
	// 0000 0000 0000 0001 1111 1111 1111 1111 1110 0000 0000 0000 0000 0000 0000 0000 -- 0001FFFFE0000000
	// 1111 1111 1111 1110 0000 0000 0000 0000 0001 1111 1111 1111 1111 1111 1111 1111 -- FFFE00001FFFFFFF

	//private int m_gender;//at least 3 buckets-- 5 bits
	// 0000 0000 0000 0000 0000 0000 0000 0000 0001 1111 0000 0000 0000 0000 0000 0000 -- 000000001F000000
	// 1111 1111 1111 1111 1111 1111 1111 1111 1110 0000 1111 1111 1111 1111 1111 1111 -- FFFFFFFFE0FFFFFF

	//private int m_hhi; //at least 3 buckets -- 5 bits
	// 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 1111 1000 0000 0000 0000 0000 -- 0000000000F80000
	// 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 0000 0111 1111 1111 1111 1111 -- FFFFFFFFFF07FFFF

    //private int m_rank; //at most 100 buckets -- 7 bits
    // 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0111 1111 0000 0000 0000 -- 000000000007F000
    // 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1000 0000 1111 1111 1111 -- FFFFFFFFFFF80FFF

	//private int m_ms; //at least 3 buckets -- 5 bits
	// 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 1111 1000 0000 -- 0000000000000F80
	// 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 0000 0111 1111 -- FFFFFFFFFFFFF07F

	//private int m_discountPrice; //at most 100 buckets -- 7 bits
	// 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0000 0111 1111 -- 000000000000007F
	// 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1111 1000 0000 -- FFFFFFFFFFFFFF80


	public ProductHandle(double aScore, long aOid) {
		m_score = aScore;
		m_oid = aOid;
	}

	private ProductHandle(ProductHandle handle, double aScore) {
		m_score = aScore;
		m_oid = handle.m_oid;
		m_bitMap = handle.m_bitMap;
	}

	public long getOid() {
		return m_oid;
	}

	public double getScore() {
		return m_score;
	}

	public void setProductType(Integer type) {
		m_bitMap = (m_bitMap & 0x0001FFFFFFFFFFFFL) | (((long)type.intValue()) << 49);
	}

	public Integer getProductType(){
		long retVal = (m_bitMap & 0xFFFE000000000000L)>>>49;
		return (int)retVal;
	}

	public void setAge(Integer age) {
		m_bitMap = (m_bitMap & 0xFFFE00001FFFFFFFL) | (((long)age.intValue()) << 29);
	}

	public Integer getAge(){
		long retVal = (m_bitMap & 0x0001FFFFE0000000L)>>>29;
		return (int)retVal;
	}

	public void setGender(Integer gender) {
		m_bitMap = (m_bitMap & 0xFFFFFFFFE0FFFFFFL) | (((long)gender.intValue()) << 24);
	}

	public Integer getGender(){
		long retVal = (m_bitMap & 0x000000001F000000L)>>>24;
		return (int)retVal;
	}

	public void setHouseHoldIncome(Integer income){
		 m_bitMap = (m_bitMap & 0xFFFFFFFFFF07FFFFL) | (((long)income.intValue()) << 19);
	}

	public Integer getHouseHoldIncome(){
		long retVal = (m_bitMap & 0x0000000000F80000L)>>>19;
		return (int)retVal;
	}
	public void setRank(Integer rank){
		 m_bitMap = (m_bitMap & 0xFFFFFFFFFFF80FFFL) | (((long) rank.intValue()) << 12);
	}

	public Integer getRank(){
		long retVal = (m_bitMap & 0x000000000007F000L)>>>12;
		return (int)retVal;
	}

	public void setChildCount(Integer status){
		 m_bitMap = (m_bitMap & 0xFFFFFFFFFFFFF07FL) | (((long) status.intValue()) << 7);
	}

	public Integer getChildCount(){
		long retVal = (m_bitMap & 0x0000000000000F80L)>>>7;
		return (int)retVal;
	}
    
	public void setDiscount(Integer disc){
		 m_bitMap = (m_bitMap & 0xFFFFFFFFFFFFFF80L) | (((long) disc.intValue()));
	}

	public Integer getDiscount(){
		long retVal = (m_bitMap & 0x000000000000007FL);
		return (int)retVal;
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
		return (int)m_oid;
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
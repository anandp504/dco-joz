/**
 * 
 */
package com.tumri.joz.productselection;

import junit.framework.Assert;

import org.junit.Test;

/**
 * @author omprakash
 * @date Jun 11, 2014
 * @time 4:08:36 PM
 */
public class TestKeywordLookup {

	@Test
	public void test(){
		System.out.println(KeywordAttributeLookup.lookup("x2_s1"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.S1, KeywordAttributeLookup.lookup("x2_s1"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.F1, KeywordAttributeLookup.lookup("x2_f1"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.F2, KeywordAttributeLookup.lookup("x2_f2"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.F3, KeywordAttributeLookup.lookup("x2_f3"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.F4, KeywordAttributeLookup.lookup("x2_f4"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.F5, KeywordAttributeLookup.lookup("x2_f5"));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.S1, KeywordAttributeLookup.lookup(null));
		Assert.assertEquals(KeywordAttributeLookup.KWAttribute.IGNORE, KeywordAttributeLookup.lookup("ignore"));
	}
}

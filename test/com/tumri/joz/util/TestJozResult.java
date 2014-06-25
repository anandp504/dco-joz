/**
 * 
 */
package com.tumri.joz.util;

import java.util.ArrayList;
import java.util.Collections;

import junit.framework.Assert;

import org.junit.Test;
import static org.junit.Assert.*;

import com.tumri.joz.utils.Result;

/**
 * @author omprakash
 * @date May 30, 2014
 * @time 2:54:08 PM
 */
public class TestJozResult {

	@Test
	public void test(){
		Result r1 = new Result(111111, 1.1);
		Result r2 = new Result(222222,2.2);
		Result r3 = new Result(111111, 1.1);
		Result r4 = new Result(666666, 1.1);
		Result r5 = new Result(222222,2.3);
		Result r6 = new Result(444444, 1.1);
		
		Assert.assertTrue(r1.equals(r3));
		Assert.assertFalse(r1.equals(r2));
		Assert.assertFalse(r2.equals(r3));
		
		ArrayList<Result> results = new ArrayList<Result>();
		results.add(r1);
		results.add(r2);
		results.add(r3);
		results.add(r4);
		results.add(r5);
		results.add(r6);
	
		for(Result result: results){
			System.out.println("Id: " + result.getOid() + " Score: " + result.getScore()) ;
		}
		Collections.sort(results);
		
		System.out.println("after sorting the Results:");
		for(Result result: results){
			System.out.println("Id: " + result.getOid() + " Score: " + result.getScore()) ;
		}
	}
}

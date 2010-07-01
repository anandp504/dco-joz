package com.tumri.joz.core;

import com.tumri.joz.products.ProductHandle;
import org.junit.Test;

/**
 * User: scbraun
 * Date: Jun 24, 2010
 */
public class TestProductHandle {

	@Test
	public void test(){
		int numProductTypes = 32768;
		int numAgeBuckets = 1048576;
		int numGenderBuckets = 32;
		int numHHIBuckets = 4096;
		int numMSBuckets = 4096;
		ProductHandle ph = new ProductHandle(1, 1);
		for(int i = 0; i < numProductTypes; i++){
			ph.setProductType(i);
			for(int j = 0; j< numAgeBuckets; j++){
				ph.setAge(j);
				for(int k=0;k<numGenderBuckets;k++){
					ph.setGender(k);
					for(int w=0; w<numHHIBuckets; w++){
						ph.setHouseHoldIncome(w);
						for(int x=0; x<numMSBuckets;x++){
							ph.setMaritalStatus(x);
							if(ph.getProductType() != i){
								System.out.println("Incorrect productType. PH: " + ph.getProductType() + " != " + i);
							}
							if(ph.getAge() != j){
								System.out.println("Incorrect age. PH: " + ph.getAge() + " != " + j);
							}
							if(ph.getGender() != k){
								System.out.println("Incorrect gender. PH: " + ph.getGender() + " != " + k);
							}
							if(ph.getHouseHoldIncome() != w){
								System.out.println("Incorrect hhi. PH: " + ph.getHouseHoldIncome() + " != " + w);
							}
							if(ph.getMaritalStatus() != x){
								System.out.println("Incorrect ms. PH: " + ph.getMaritalStatus() + " != " + x);
							}
						}
					}
				}
			}
		}
	}

}

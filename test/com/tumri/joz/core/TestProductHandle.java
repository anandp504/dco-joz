package com.tumri.joz.core;

import com.tumri.joz.products.ProductHandle;
import org.junit.Assert;
import org.junit.Test;

/**
 * User: scbraun
 * Date: Jun 24, 2010
 */
public class TestProductHandle {

    @Test
    public void testPH() {
       ProductHandle ph = new ProductHandle(1,1);
       ph.setAge(0);
       ph.setGender(0);
       ph.setHouseHoldIncome(0);
       ph.setDiscount(0);
       ph.setRank(0);
       ph.setProductType(0);
       ph.setMaritalStatus(2);
       Assert.assertTrue(ph.getAge()==0);
       Assert.assertTrue(ph.getGender()==0);
       Assert.assertTrue(ph.getHouseHoldIncome()==0);
       Assert.assertTrue(ph.getDiscount()==0);
       Assert.assertTrue(ph.getRank()==0);
       Assert.assertTrue(ph.getProductType()==0);
       Assert.assertTrue(ph.getMaritalStatus()==2);
    }

	@Test
	public void test(){
		int numProductTypes = 32768;
		int numAgeBuckets = 1048576;
		int numGenderBuckets = 4;
		int numHHIBuckets = 4;
		int numMSBuckets = 4;
		int numRankBuckets = 100;
		int numDiscountBuckets = 100;

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
                            for(int y=0; y<numRankBuckets;y++){
                                ph.setRank(y);
                                for(int z=0; z<numDiscountBuckets;z++){
                                    ph.setDiscount(z);
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
                                        System.out.println("Incorrect age. PH: " + ph.getAge() + " != " + j);
                                        System.out.println("Incorrect gender. PH: " + ph.getGender() + " != " + k);
                                        System.out.println("Incorrect hhi. PH: " + ph.getHouseHoldIncome() + " != " + w);
                                        System.out.println("Incorrect PT. PH: " + ph.getProductType() + " != " + i);
                                        System.out.println("Incorrect rank. PH: " + ph.getRank() + " != " + y);
                                        System.out.println("Incorrect discount. PH: " + ph.getDiscount() + " != " + z);
                                    }
                                    if(ph.getRank() != y){
                                        System.out.println("Incorrect rank. PH: " + ph.getRank() + " != " + y);
                                    }
                                    if(ph.getDiscount() != z){
                                        System.out.println("Incorrect discount. PH: " + ph.getDiscount() + " != " + z);
                                    }
                                }
                            }
						}
					}
				}
			}
		}
	}

}

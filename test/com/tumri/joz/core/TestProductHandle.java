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
        ph.setProductType(1);
       ph.setRank(0);
        ph.setChildCount(2);
        ph.setDiscount(0);

        ph.setHouseHoldIncome(0);
        ph.setAge(0);
        ph.setGender(0);



       Assert.assertTrue(ph.getAge()==0);
       Assert.assertTrue(ph.getGender()==0);
       Assert.assertTrue(ph.getHouseHoldIncome()==0);
       Assert.assertTrue(ph.getDiscount()==0);
       Assert.assertTrue(ph.getRank()==0);
       Assert.assertTrue(ph.getProductType()==1);
       Assert.assertTrue(ph.getChildCount()==2);
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
						for(int x=1; x<numMSBuckets;x++){
							ph.setChildCount(x);
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
                                    if(ph.getChildCount() != x){
                                        System.out.println("2. Incorrect ms. PH: " + ph.getChildCount() + " != " + x);
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

/**
 * 
 */
package com.tumri.joz.ranks;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.tumri.joz.products.ProductHandle;

/**
 * @author omprakash
 * @date Jun 10, 2014
 * @time 11:11:19 PM
 */
public class TestWeight {

	@Test
	public void test(){
		double score = 1.0;
		ProductHandle ph = new ProductHandle(score, 37319666L);
		double minWeight = 0.2;
		{
			AreaCodeWeight acWeight = AreaCodeWeight.getInstance();
			Assert.assertEquals(1.05, acWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.05, acWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.05, acWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(acWeight.mustMatch());
		}
		{
			BrandWeight brandWeight = BrandWeight.getInstance();
			Assert.assertEquals(1.2, brandWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, brandWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, brandWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(brandWeight.mustMatch());
		}
		{
			CategoryWeight catWeight = CategoryWeight.getInstance();
			Assert.assertEquals(1.2, catWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, catWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, catWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(catWeight.mustMatch());
		}
		{
			CityWeight cityWeight = CityWeight.getInstance();
			Assert.assertEquals(1.15, cityWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.15, cityWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.15, cityWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(cityWeight.mustMatch());
		}
		{
			CPCWeight cpcWeight = CPCWeight.getInstance();
			Assert.assertEquals(1.2, cpcWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, cpcWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, cpcWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(cpcWeight.mustMatch());
		}
		{
			CPOWeight cpoWeight = CPOWeight.getInstance();
			Assert.assertEquals(1.2, cpoWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, cpoWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, cpoWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(cpoWeight.mustMatch());
		}
		{
			ph.setDiscount(10);
			DiscountWeight discWeight = DiscountWeight.getInstance();
			Assert.assertTrue(discWeight.getWeight(ph, minWeight) > 1.0);
			Assert.assertEquals(1.0, discWeight.getMinWeight(), 0.001);
			Assert.assertEquals(2.0, discWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(discWeight.mustMatch());
		}
		{
			DmaCodeWeight dmaWeight = DmaCodeWeight.getInstance();
			Assert.assertEquals(1.20, dmaWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.20, dmaWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.20, dmaWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(dmaWeight.mustMatch());
		}
		{
			GenericIWeight<ProductHandle> dmaWeight = new GenericIWeight<ProductHandle>(minWeight, true);
			Assert.assertEquals(0.2, dmaWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(0.2, dmaWeight.getMinWeight(), 0.001);
			Assert.assertEquals(0.2, dmaWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(dmaWeight.mustMatch());
			Assert.assertTrue(dmaWeight.match(ph) == 1);
		}
		{
			GeoEnabledWeight geoEnabledWeight = GeoEnabledWeight.getInstance();
			Assert.assertEquals(1.0, geoEnabledWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.0, geoEnabledWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.0, geoEnabledWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(geoEnabledWeight.mustMatch());
		}
		{
			GlobalIdWeight globalIdWeight = GlobalIdWeight.getInstance();
			Assert.assertEquals(1.2, globalIdWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, globalIdWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, globalIdWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(globalIdWeight.mustMatch());
		}
		{
			ImageHeightWeight imgHWeight = ImageHeightWeight.getInstance();
			Assert.assertEquals(1.2, imgHWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, imgHWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, imgHWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(imgHWeight.mustMatch());
		}
		{
			ImageWidthWeight imgWWeight = ImageWidthWeight.getInstance();
			Assert.assertEquals(1.2, imgWWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, imgWWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, imgWWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(imgWWeight.mustMatch());
		}
		{
			KeywordsWeight keyWeight = KeywordsWeight.getInstance();
			Assert.assertEquals(score, keyWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(0.0, keyWeight.getMinWeight(), 0.001);
			Assert.assertEquals(10.0, keyWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(keyWeight.mustMatch());
		}
		{
			MultiValueTextFieldWeight multValWeight = MultiValueTextFieldWeight.getInstance();
			Assert.assertEquals(1.05, multValWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.05, multValWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.05, multValWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(multValWeight.mustMatch());
		}
		{
			NeutralWeight nonWeight = NeutralWeight.getInstance();
			Assert.assertEquals(1.0, nonWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.0, nonWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.0, nonWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(nonWeight.mustMatch());
			Assert.assertTrue(nonWeight.match(ph) == 0);
		}
		{
			OptimizedWeight optWeight = OptimizedWeight.getInstance();
			Assert.assertEquals(1.2, optWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, optWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, optWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(optWeight.mustMatch());
		}
		{
			OptTextFieldWeight optTxtWeight = OptTextFieldWeight.getInstance();
			Assert.assertEquals(1.001, optTxtWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.001, optTxtWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.001, optTxtWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(optTxtWeight.mustMatch());
		}
		{
			PriceWeight priceWeight = PriceWeight.getInstance();
			Assert.assertEquals(1.2, priceWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, priceWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, priceWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(priceWeight.mustMatch());
		}
		{
			ProductTypeWeight prodTypWeight = ProductTypeWeight.getInstance();
			Assert.assertEquals(1.2, prodTypWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, prodTypWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, prodTypWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(prodTypWeight.mustMatch());
		}
		{
			ProviderWeight provWeight = ProviderWeight.getInstance();
			Assert.assertEquals(1.2, provWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, provWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, provWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(provWeight.mustMatch());
		}
		{
			RadiusWeight radWeight = RadiusWeight.getInstance();
			Assert.assertEquals(1.10, radWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.10, radWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.10, radWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(radWeight.mustMatch());
		}
		{
			ph.setRank(20);
			RankWeight rankWeight = RankWeight.getInstance();
			Assert.assertTrue(rankWeight.getWeight(ph, minWeight) > 1.0);
			Assert.assertEquals(0.0, rankWeight.getMinWeight(), 0.001);
			Assert.assertEquals(2.0, rankWeight.getMaxWeight(), 0.001);
			Assert.assertFalse(rankWeight.mustMatch());
		}
		{
			StateWeight stateWeight = StateWeight.getInstance();
			Assert.assertEquals(1.10, stateWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.10, stateWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.10, stateWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(stateWeight.mustMatch());
		}
		{
			SupplierWeight suppWeight = SupplierWeight.getInstance();
			Assert.assertEquals(1.2, suppWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.2, suppWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.2, suppWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(suppWeight.mustMatch());
		}
		{
			ZipCodeWeight zipWeight = ZipCodeWeight.getInstance();
			Assert.assertEquals(1.40, zipWeight.getWeight(ph, minWeight), 0.001);
			Assert.assertEquals(1.40, zipWeight.getMinWeight(), 0.001);
			Assert.assertEquals(1.40, zipWeight.getMaxWeight(), 0.001);
			Assert.assertTrue(zipWeight.mustMatch());
		}
	}
}

package com.tumri.joz.rules;

import com.tumri.joz.campaign.wm.VectorAttribute;
import com.tumri.joz.campaign.wm.VectorHandleImpl;
import com.tumri.joz.campaign.wm.VectorUtils;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Unit test to cover the api of vector handle
 * User: nipun
 * Date: Jun 4, 2010
 * Time: 4:29:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class VectorHandleTest {

    /**
     * Test single attribute
     */
    @Test
    public void test0() {
        Map<VectorAttribute, List<Integer>> map = new HashMap<VectorAttribute, List<Integer>>();

        List details = new ArrayList<Integer>();
        details.add(VectorUtils.getDictId(VectorAttribute.kT1, "12345"));
        map.put(VectorAttribute.kT1, details);

        VectorHandleImpl h1 = new VectorHandleImpl(12, 10, 1, map, false );

        Map<VectorAttribute, List<Integer>> cmap = h1.getContextMap();

        compareMaps(map, cmap);
    }

    @Test
    public void test1() {
        Map<VectorAttribute, List<Integer>> map = new HashMap<VectorAttribute, List<Integer>>();

        List details = new ArrayList<Integer>();
        details.add(VectorUtils.getDictId(VectorAttribute.kT1, "12345"));
        details.add(VectorUtils.getDictId(VectorAttribute.kT1, "64465"));
        details.add(VectorUtils.getDictId(VectorAttribute.kT1, "122121"));
        map.put(VectorAttribute.kT1, details);

        VectorHandleImpl h1 = new VectorHandleImpl(12, 10, 1, map, true );
        Map<VectorAttribute, List<Integer>> cmap = h1.getContextMap();

        compareMaps(map, cmap);

    }

    @Test
    public void test2() {
        Map<VectorAttribute, List<Integer>> map = new HashMap<VectorAttribute, List<Integer>>();

        List ldetails = new ArrayList<Integer>();
        ldetails.add(VectorUtils.getDictId(VectorAttribute.kT1, "12345"));
        map.put(VectorAttribute.kT1, ldetails);

        List sdetails = new ArrayList<Integer>();
        sdetails.add(VectorUtils.getDictId(VectorAttribute.kState, "CA"));
        map.put(VectorAttribute.kState, sdetails);

        VectorHandleImpl h1 = new VectorHandleImpl(12, 10, 1, map, false );
        Map<VectorAttribute, List<Integer>> cmap = h1.getContextMap();

        compareMaps(map, cmap);

    }

    @Test
    public void test4() {
        Map<VectorAttribute, List<Integer>> map = new HashMap<VectorAttribute, List<Integer>>();

        List ldetails = new ArrayList<Integer>();
        ldetails.add(VectorUtils.getDictId(VectorAttribute.kT1, "12345"));
        ldetails.add(VectorUtils.getDictId(VectorAttribute.kT1, "343443"));
        map.put(VectorAttribute.kT1, ldetails);

        List sdetails = new ArrayList<Integer>();
        sdetails.add(VectorUtils.getDictId(VectorAttribute.kState, "CA"));
        sdetails.add(VectorUtils.getDictId(VectorAttribute.kState, "KY"));
        sdetails.add(VectorUtils.getDictId(VectorAttribute.kState, "WY"));
        map.put(VectorAttribute.kState, sdetails);

        List s2dets = new ArrayList<Integer>();
        s2dets.add(VectorUtils.getDictId(VectorAttribute.kT5, "2121"));
        s2dets.add(VectorUtils.getDictId(VectorAttribute.kT5, "KsdsY"));
        s2dets.add(VectorUtils.getDictId(VectorAttribute.kT5, "dsdss"));
        map.put(VectorAttribute.kT5, s2dets);

        VectorHandleImpl h1 = new VectorHandleImpl(12, 10, 1, map, true );
        Map<VectorAttribute, List<Integer>> cmap = h1.getContextMap();

        compareMaps(map, cmap);

    }

    @Test
    public void test3() {
        VectorHandleImpl h1 = new VectorHandleImpl(12, 10, 1, null, false );

        long id = h1.createId(12, 10);
        int[] arr = h1.getIdDetails(id);

        Assert.assertTrue(arr.length ==2);
        Assert.assertTrue(arr[1] == 12 && arr[0] == 10);
    }

    @Test
    public void test5() {
        Map<VectorAttribute, List<Integer>> map = new HashMap<VectorAttribute, List<Integer>>();
        List ldetails = new ArrayList<Integer>();
        ldetails.add(VectorUtils.getDictId(VectorAttribute.kT1, "12345"));
        ldetails.add(VectorUtils.getDictId(VectorAttribute.kT1, "343443"));
        map.put(VectorAttribute.kT1, ldetails);
        VectorHandleImpl h1 = new VectorHandleImpl(12, 10, 1, map, false );

        Map<VectorAttribute, List<Integer>> map2 = new HashMap<VectorAttribute, List<Integer>>();
        List det = new ArrayList<Integer>();
        det.add(VectorUtils.getDictId(VectorAttribute.kT1, "12345"));
        det.add(VectorUtils.getDictId(VectorAttribute.kT1, "343443"));
        map2.put(VectorAttribute.kT1, det);
        VectorHandleImpl h2 = new VectorHandleImpl(12, 10, 1, map2, false );
        
        //Assert.assertTrue(h1.isMatch(h2));
    }

    private void compareMaps(Map<VectorAttribute, List<Integer>> map, Map<VectorAttribute, List<Integer>> cmap) {
        Assert.assertTrue(cmap.size() == map.size());
        for (VectorAttribute k: map.keySet()) {
            List<Integer> list1 = map.get(k);
            List<Integer> list2 = cmap.get(k);

            Assert.assertTrue(list1.size() == list2.size());
            Assert.assertTrue(list1.containsAll(list2));
        }
    }



}

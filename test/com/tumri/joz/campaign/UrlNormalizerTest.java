package com.tumri.joz.campaign;

import static org.junit.Assert.*;

import org.junit.Test;

import java.util.List;

public class UrlNormalizerTest {

    @Test
    public void testGetNormalizedURLValid1() {
        String url = "http://www.yahoo.com/sports/baseball";
        String normalizedStr = "yahoo/sports/baseball";
        String actualString = UrlNormalizer.getNormalizedUrl(url);
        assertEquals(normalizedStr, actualString);
    }

    @Test
    public void getAllPossibleUrls() {
        long startTime = System.currentTimeMillis();
        List<String> list = UrlNormalizer.getAllPossibleNormalizedUrl("http://www.yahoo.com/sports/baseball");
        long endTime = System.currentTimeMillis();
        System.out.println("Start Time: " + startTime + " End Time: " + endTime + " total: " + (endTime - startTime));
        for(String url:list) {
            System.out.println(url);
        }
    }

    @Test
	public void testGetNormalizedURLValid2() {
		String url = "google.co.in/direction/home";
		String normalizedStr = "google/direction/home";
        long startTime = System.currentTimeMillis();
        String actualString = UrlNormalizer.getNormalizedUrl(url);
        long endTime = System.currentTimeMillis();
        System.out.println("Start Time: " + startTime + " End Time: " + endTime + " total: " + (endTime - startTime));
        assertEquals(normalizedStr, actualString);
	}

    @Test
	public void testGetNormalizedURLValid3() {
		String url = "http://sports.yahoo.com/cricket";
		String normalizedStr = "yahoo/sports/cricket";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLValid4() {
		String url = "https://www.google.com/finance/gainers";
		String normalizedStr = "google/finance/gainers";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLValid5() {
		String url = "maps.google.com/direction/home";
		String normalizedStr = "google/maps/direction/home";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLValid6() {
		String url = "google.co.in/direction/home";
		String normalizedStr = "google/direction/home";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLValid7() {
		String url = "google.com/direction/home";
		String normalizedStr = "google/direction/home";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLWithParameters() {
		String url = "google.co.in/direction/home?test1=one&test2=two";
		String normalizedStr = "google/direction/home";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLNull() {
		String url = null;
		String normalizedStr = null;
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLOneWord() {
		String url = "TestString";
		String normalizedStr = "TestString";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLTwoWords() {
		String url = "OneTestString TwoTestString";
		String normalizedStr = "OneTestString TwoTestString";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLDot() {
		String url = ".";
		String normalizedStr = ".";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLTwoDots() {
		String url = "..";
		String normalizedStr = "..";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLThreeDots() {
		String url = "...";
		String normalizedStr = "...";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLOneSlash() {
		String url = "/";
		String normalizedStr = "";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLDostAndSlash1() {
		String url = "./";
		String normalizedStr = ".";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLDostAndSlash2() {
		String url = "../";
		String normalizedStr = "..";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLDostAndSlash3() {
		String url = ".././";
		String normalizedStr = "../.";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLTwoSlash() {
		String url = "//";
		String normalizedStr = "";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}

	@Test
	public void testGetNormalizedURLJustProtocol() {
		String url = "http://";
		String normalizedStr = "";
		String actualString = UrlNormalizer.getNormalizedUrl(url);
		assertEquals(normalizedStr, actualString);
	}
}

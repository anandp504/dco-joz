package com.tumri.joz.campaign;

import java.util.ArrayList;
import java.util.List;

public class UrlNormalizer {

	public static String getNormalizedUrl(String url){
		if(url == null) {
			return null;
		}
		String protocol = null;
		if(url.indexOf("://") != -1 ) {
			protocol = url.substring(0, url.indexOf("://"));
			url = url.substring(url.indexOf("://") + 3);
		}

		String domainStr = null;
		if(url.indexOf("/") != -1) {
			domainStr = url.substring(0, url.indexOf("/"));
		}
		else {
			domainStr = url;
		}
		String postFixStr = "";
		String parameterStr = "";
		if(url.indexOf("?") != -1 && url.indexOf("/") != -1) {
			postFixStr = url.substring(url.indexOf("/"), url.indexOf("?"));
			parameterStr = url.substring(url.indexOf("?"), url.length());
		}
		else  if(url.indexOf("?") == -1 && url.indexOf("/") != -1){
			postFixStr = url.substring(url.indexOf("/"), url.length());
		}
		else  if(url.indexOf("?") != -1 && url.indexOf("/") == -1){
			postFixStr = "";
			parameterStr = url.substring(url.indexOf("?"), url.length());
		}
		String domain = getDomain(domainStr);

		List<String> domainPostfix = getDomainPostfix(domainStr, postFixStr);

		return getString(domain, domainPostfix);
	}

	public static List<String> getAllPossibleNormalizedUrl(String url){
		if(url == null) {
			return null;
		}
		String normalizedUrl = getNormalizedUrl(url);

		String[] tokens = normalizedUrl.split("[/]");

		List<String> list = new ArrayList<String>();

		for(int i = (tokens.length-1); i >= 0; i--)  {
			int j = 1;
			StringBuffer buf = new StringBuffer();
			buf.append(tokens[0]);
			while(j <= i) {
				buf.append("/");
				buf.append(tokens[j++]);
			}
			list.add(buf.toString());
		}
		return list;
	}

	private static String getString(String domain, List<String> urlPostfix) {
		StringBuffer buf = new StringBuffer();

		buf.append(domain);

		for(String s: urlPostfix) {
			buf.append("/");
			buf.append(s);
		}
		return buf.toString();
	}

	private static List<String> getDomainPostfix(String domainStr, String postFixStr) {
		ArrayList<String> list = new ArrayList<String>();

		if( !(domainStr.indexOf(".") != -1 && domainStr.length() == 1) && domainStr.indexOf("..") == -1) {
			String[] tokens = domainStr.split("[.]");
			if(tokens[tokens.length - 1].length() == 3) {
				if(tokens.length != 2) {
					for(int i = tokens.length - 3; i >= 0; i--) {
						if(!tokens[i].startsWith("www")) {
							list.add(tokens[i]);
						}
					}
				}
			}
			else {
				if(tokens.length != 3) {
					for(int i = tokens.length - 3; i >= 0; i--) {
						if(!tokens[i].startsWith("www")) {
							list.add(tokens[i]);
						}
					}
				}
			}
		}

		String[] tmp = postFixStr.split("[/]");

		for(String s: tmp) {
			list.add(s);
		}

		boolean blankPresent = true;

		while(blankPresent) {
			list.remove("");
			blankPresent = list.contains("");
		}
		return list;
	}

	private static String getDomain(String domainStr) {

		String domain;

		if(domainStr.indexOf(".") == -1) {
			return domainStr;
		}

		if(domainStr.indexOf(".") != -1 && domainStr.length() == 1) {
			return domainStr;
		}

		if(domainStr.indexOf("..") != -1) {
			return domainStr;
		}

		String[] tokens = domainStr.split("[.]");
		if(tokens[tokens.length - 1].length() == 3) {
			domain = tokens[tokens.length - 2];
		}
		else if(tokens[tokens.length - 1].length() == 2) {
            domain = tokens[tokens.length - 3];
        }
        else {
			domain = domainStr;
		}
		return domain;
	}
}

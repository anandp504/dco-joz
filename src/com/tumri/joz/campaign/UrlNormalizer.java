package com.tumri.joz.campaign;

import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

public class UrlNormalizer {
    private static Logger log = Logger.getLogger (UrlNormalizer.class);

    public static String getNormalizedUrl(String origUrl){
        String url = origUrl;
        if(url == null) {
			return null;
		}
        String result;
        //String protocol = null;

        //nuke the protocol if exists.
        // example: 1) http://xyz -> xyz , 2) https://xyz -> xyz , 3) xyz -> xyz
        if(url.indexOf("://") != -1 ) {
            //protocol = url.substring(0, url.indexOf("://"));
            url = url.substring(url.indexOf("://") + 3);
        }

        //parse the domain string.
        // example: 1) www.xyz.com/a/b/c/d -> www.xyz.com , 2) www.xyz.com -> www.xyz.com
        String domainStr;
        if(url.indexOf("/") != -1) {
            domainStr = url.substring(0, url.indexOf("/"));
        }
        else {
            domainStr = url;
        }

        String postFixStr = "";
        //String parameterStr = "";

        //nuke the parameter string, if present
        //example: www.xyz.com/a/b/c/d?param1=a1&param2=a2 -> postFixStr = a/b/c/d & parameterStr = param1=a1&param2=a2
        if(url.indexOf("?") != -1 && url.indexOf("/") != -1) {
            postFixStr = url.substring(url.indexOf("/"), url.indexOf("?"));
            //parameterStr = url.substring(url.indexOf("?"), url.length());
        }
        else  if(url.indexOf("?") == -1 && url.indexOf("/") != -1){
            postFixStr = url.substring(url.indexOf("/"), url.length());
        }
        else  if(url.indexOf("?") != -1 && url.indexOf("/") == -1){
            postFixStr = "";
            //parameterStr = url.substring(url.indexOf("?"), url.length());
        }
        String domain = getDomain(domainStr);

        List<String> domainPostfix = getDomainPostfix(domain, postFixStr);

        result = getString(domainPostfix);
        return result;
    }

	public static List<String> getAllPossibleNormalizedUrl(String url){
		if(url == null) {
			return null;
		}
		String normalizedUrl = getNormalizedUrl(url);
        List<String> list = null;
        if(normalizedUrl != null && !"".equals(normalizedUrl)) {
            String[] tokens = normalizedUrl.split("[/]");
            if(tokens != null && tokens.length > 0) {
                list = new ArrayList<String>();
                for(int i = (tokens.length-1); i >= 0; i--)  {
                    int j = 1;
                    StringBuilder buf = new StringBuilder();
                    buf.append(tokens[0]);
                    while(j <= i) {
                        buf.append("/");
                        buf.append(tokens[j++]);
                    }
                    list.add(buf.toString());
                }
            }
        }
        return list;
	}

	private static String getString(List<String> urlPostfix) {
        String result = null;
        if(urlPostfix != null && urlPostfix.size() > 0) {
            StringBuilder buf = new StringBuilder();
            boolean addPrefix = false;
            for(String s: urlPostfix) {
                if(addPrefix) {
                    buf.append("/");
                }
                addPrefix = true;
                buf.append(s);
            }
            result = buf.toString();
        }
        return result;
    }

	private static List<String> getDomainPostfix(String domainStr, String postFixStr) {
		ArrayList<String> list = new ArrayList<String>();

        if(domainStr != null && !domainStr.equals("")) {
            String[] tmp2 = domainStr.split("[/]");
            for(String s1: tmp2) {
                if(!s1.equals("")) {
                    list.add(s1);
                }
            }
        }

        if(postFixStr != null && !postFixStr.equals("")) {
            String[] tmp = postFixStr.split("[/]");
            for(String s: tmp) {
                if(!s.equals("")) {
                    list.add(s);
                }
            }
        }
		return list;
	}

	private static String getDomain(String domainStr) {
		String domain = "";
        if(domainStr == null || domainStr.length() == 0) {
            return domainStr;
        }

        String[] tokens = domainStr.split("[.]");

        if(tokens != null && tokens.length > 1) {
            int endIndex = 0;
            //Check if the domain begins with www, if yes ignore the first word of the domain
            // example: if the domainStr is www1.yahoo.com, then ignore www1.
            if(tokens[0].length() > 2) {
                String prefix = tokens[0].substring(0,3);
                if(prefix.equalsIgnoreCase("www")) {
                    endIndex = 1;
                }
            }
            //reverse all the words of the domain, so www.sports.yahoo.com becomes -> com/yahoo/sports, (www is ignored) 
            for(int i = (tokens.length - 1); i>=endIndex; i--) {
                if(tokens[i] != null && tokens[i].length() > 0) {
                    domain = domain + tokens[i];
                    if(i > endIndex) {
                        domain = domain + "/";
                    }
                }
            }
        }
        else if(tokens != null && tokens.length == 1) {
            domain = tokens[0];
        }
        else {
            domain = domainStr;
        }
        return domain;
	}

}

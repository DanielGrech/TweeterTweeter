package com.DGSD.TweeterTweeter.Utils;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringUtils {

	private static Pattern hyperLinksPattern = 
			Pattern.compile("\\(?\\b(http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[-A-Za-z0-9+&@#/%=~_()|]");
	
	/**
	 * 
	 * @param src The text to be parsed
	 * @return a list of all urls found in the src text
	 */
	public static LinkedList<String> getUrls(String src) {
		LinkedList<String> retval = new LinkedList<String>();
		
		Matcher m = hyperLinksPattern.matcher(src);

		while (m.find()){
			String s = src.substring(m.start(), m.end());
			
			if(s.startsWith("(") && s.endsWith(")")) {
				s = s.substring(1, s.length() - 2);
			}
				
			if(!s.startsWith("http://")) {
				retval.add("http://".concat(s));
			} else {
				retval.add(s);
			}
		}
		
		return retval;
	}
	
	/**
	 * 
	 * @param a valid url
	 * @return the website portion of the url. 
	 * E.g. http://www.example.com returns 'example'
	 * @throws MalformedURLException 
	 */
	public static String getWebsiteFromUrl(String url) throws MalformedURLException {
		String retval = new URL(url).getHost();
		
		if(retval.startsWith("http://www.")) {
			retval = retval.substring(11);
		} else if(retval.startsWith("http://")) {
			retval = retval.substring(7);
		} else if(retval.startsWith("www.")) {
			retval = retval.substring(4);
		}
		
		return retval;
	}
}

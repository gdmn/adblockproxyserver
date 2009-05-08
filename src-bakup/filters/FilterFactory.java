/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;

/**
 *
 * @author dmn
 */
public class FilterFactory {
	public static FilterInterface createFilter(String s) {
		if (s.startsWith("@") || s.startsWith("!") /* || s.startsWith("|") || s.endsWith("|") */ || s.contains("#") || s.contains("[Adblock")) {
			return null;
		}
		FilterInterface result = null;
		if (s.contains("\\") || (s.contains("|") && !s.startsWith("|") && !s.endsWith("|")) || s.contains("+")) {
			result = new FilterRegexp(s);
		} else if (s.contains("*") || s.contains("|")) {
			String s2 = s.replaceAll("\\.", "\\\\.").replaceAll("\\*", ".*");
			if (s2.indexOf("|") == 0) {
				s2 = "^" + s2.substring(1);
			}
			if (s2.indexOf("|") == s2.length()-1) {
				s2 = s2.substring(0, s2.length()-1) + "$";
			}
			result = new FilterRegexp(s2);
		}
		if (result != null) {
			return result;
		}
		return new FilterSimple(s);
	}
}

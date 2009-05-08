/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author dmn
 */
public class FilterRegexp implements FilterInterface {
	private String filterText;
	private Pattern pattern;

	// packet access
	FilterRegexp(String s) {
		filterText = s;
		pattern = Pattern.compile(filterText, Pattern.CASE_INSENSITIVE);
	}

	@Override
	public boolean match(String textToMatch) {
		Matcher matcher = pattern.matcher(textToMatch);
		if (matcher.find()) {
			return true;
		}
		return false;
	}

	@Override
	public String toString() {
		return FilterRegexp.class.getName() + ": " + filterText;
	}
}

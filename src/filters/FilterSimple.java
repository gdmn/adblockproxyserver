/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package filters;

/**
 *
 * @author dmn
 */
public class FilterSimple implements FilterInterface {

	private String filterText;

	// packet access
	FilterSimple(String s) {
		filterText = s.toLowerCase();
	}

	@Override
	public boolean match(String textToMatch) {
		return textToMatch.toLowerCase().contains(filterText);
	}

	@Override
	public String toString() {
		return FilterSimple.class.getName() + ": " + filterText;
	}
}

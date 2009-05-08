/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package filters;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dmn
 */
public class Filters implements FilterInterface {
	private ArrayList<FilterInterface> blackList = new ArrayList<FilterInterface>();
	private ArrayList<FilterInterface> whiteList = new ArrayList<FilterInterface>();
	private static Filters filters;
	private static String[] filtersURL = new String[]{
		//"http://chewey.de/mozilla/data/adblock.txt",
		//"file:///home/dmn/adblockchewey.txt",
		"http://www.niecko.pl/adblock/adblock.txt",
		"http://jurek6.republika.pl/adblockplus.txt",};

	private Filters() {
		//readFromURL(filtersURL);
	}

	public void readFromURL(String... url) {
		for (String url1 : url) {
				int countWhite = 0, countBlack = 0;
			try {
				URL yahoo = new URL(url1);
				BufferedReader in = new BufferedReader(new InputStreamReader(yahoo.openStream()));
				String inputLine;
				while ((inputLine = in.readLine()) != null) {
					//System.out.println("--- Try "+inputLine);
					if (inputLine.trim().equals("")) {
						continue;
					}
					boolean white = false;
					if (inputLine.indexOf("@@") == 0) {
						inputLine = inputLine.substring(2);
						white = true;
					}
					FilterInterface f = FilterFactory.createFilter(inputLine);
					if (f == null) {
						//System.out.println("--- Dropped " + inputLine);
					} else {
						if (white) {
							whiteList.add(f);
							countWhite++;
						} else {
							blackList.add(f);
							countBlack++;
						}
					//System.out.println("Added to " + (white ? "whitelist" : "blacklist") + " " + f.toString());
					}
				}
				in.close();
			} catch (Exception ex) {
				Logger.getLogger(Filters.class.getName()).log(Level.SEVERE, null, ex);
			} finally {
				System.out.println("Read " + countWhite + "+" + countBlack + " filter(s) from " + url1);
			}
		}
	}

	public static Filters getInstance() {
		if (filters == null) {
			filters = new Filters();
		}
		return filters;
	}

	@Override
	public boolean match(String textToMatch) {
		for (FilterInterface f : whiteList) {
			if (f.match(textToMatch)) {
//				System.out.println("! whitelist "+textToMatch+" "+f.toString());
				return false;
			}
		}
		for (FilterInterface f : blackList) {
			if (f.match(textToMatch)) {
//				System.out.println("! blacklist "+textToMatch+" "+f.toString());
				return true;
			}
		}
		return false;
	}
}

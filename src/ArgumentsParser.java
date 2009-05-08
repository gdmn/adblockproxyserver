/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author dmn
 */
public abstract class ArgumentsParser {
	
	public static HashMap<String, List<String>> parse(String[] args) throws IOException {
		int offset = 0;
		String arg;
		HashMap<String, List<String>> result = new HashMap<String, List<String>>(args.length);
		List<String> value = null;
		while (args.length > offset) {
			arg = args[offset];
			boolean foundSwitch = false;
			if (arg.startsWith("--")) {
				arg = arg.substring(2);
				foundSwitch = true;
			} else if (arg.startsWith("-")) {
				arg = arg.substring(1);
				foundSwitch = true;
			}
			if (foundSwitch && (arg == null || arg.equals(""))) {
				throw new IOException("Empty parameter");
			}
			if (foundSwitch) {
				if (result.containsKey(arg)) {
					value = result.get(arg);
				} else {
					value = new LinkedList<String>();
					result.put(arg, value);
				}
			} else {
				if (value == null) {
					throw new IOException("There is no key to put a value " + arg);
				}
				value.add(arg);
			}
			offset++;
		}
		return result;
	}

}

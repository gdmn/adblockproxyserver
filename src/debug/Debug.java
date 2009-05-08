/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package debug;

import java.io.PrintStream;
import java.util.Calendar;

/**
 *
 * @author gorladam
 */
public class Debug {

    private static int level = 10000;
    private static PrintStream debugOut = System.out;
    private static Debug instance = null;

    private Debug() {
        //instance = this;
    }

    public static PrintStream getDebugOut() {
        return Debug.debugOut;
    }

    public static void setDebugOut(PrintStream debugOut) {
        Debug.debugOut = debugOut;
    }

    public static int getLevel() {
        return Debug.level;
    }

    public static void setLevel(int level) {
        Debug.level = level;
    }

    public static Debug getInstance() {
        if (instance == null) {
            instance = new Debug();
        }
        return instance;
    }

    public static void println(int levelOfDebug, String debugMessage) {
        if (levelOfDebug <= Debug.level) {
            Debug.debugOut.println(Calendar.getInstance().getTime() + " " + debugMessage);
        }
    }
}

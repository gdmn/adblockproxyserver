/*
 * This is a simple multi-threaded Java proxy server
 * for HTTP requests (HTTPS doesn't seem to work, because
 * the CONNECT requests aren't always handled properly).
 * I implemented the class as a thread so you can call it
 * from other programs and kill it, if necessary (by using
 * the closeSocket() method).
 *
 * We'll call this the 1.1 version of this class. All I
 * changed was to separate the HTTP header elements with
 * \r\n instead of just \n, to comply with the official
 * HTTP specification.
 *
 * This can be used either as a direct proxy to other
 * servers, or as a forwarding proxy to another proxy
 * server. This makes it useful if you want to monitor
 * traffic going to and from a proxy server (for example,
 * you can run this on your local machine and set the
 * fwdServer and fwdPort to a real proxy server, and then
 * tell your browser to use "localhost" as the proxy, and
 * you can watch the browser traffic going in and out).
 *
 * One limitation of this implementation is that it doesn't
 * close the ProxyThread socket if the client disconnects
 * or the server never responds, so you could end up with
 * a bunch of loose threads running amuck and waiting for
 * connections. As a band-aid, you can set the server socket
 * to timeout after a certain amount of time (use the
 * setTimeout() method in the ProxyThread class), although
 * this can cause false timeouts if a remote server is simply
 * slow to respond.
 *
 * Another thing is that it doesn't limit the number of
 * socket threads it will create, so if you use this on a
 * really busy machine that processed a bunch of requests,
 * you may have problems. You should use thread pools if
 * you're going to try something like this in a "real"
 * application.
 *
 * Note that if you're using the "main" method to run this
 * by itself and you don't need the debug output, it will
 * run a bit faster if you pipe the std output to 'nul'.
 *
 * You may use this code as you wish, just don't pretend
 * that you wrote it yourself, and don't hold me liable for
 * anything that it does or doesn't do. If you're feeling
 * especially honest, please include a link to nsftools.com
 * along with the code. Thanks, and good luck.
 *
 * Julian Robichaux -- http://www.nsftools.com
 */

import filters.Filters;
import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import debug.Debug;

public class JProxy extends Thread {

    public static final int DEFAULT_PORT = 8080;
    private ServerSocket server = null;
    private int thisPort = DEFAULT_PORT;
    private String fwdServer = "";
    private int fwdPort = 0;
    private int ptTimeout = ProxyThread.DEFAULT_TIMEOUT;
    private final ExecutorService pool;
    private static int poolSize = 24;

    /* here's a main method, in case you want to run this by itself */
    public static void main(String args[]) throws IOException {
        Integer port = null;
        String fwdProxyServer = "";
        int fwdProxyPort = 0;

        HashMap<String, List<String>> arguments = ArgumentsParser.parse(args);
        // get the command-line parameters
        List<String> parameter;
        boolean errorInParameters = false;


        int debugL = 1;
        parameter = arguments.get("debug");
        if (parameter != null && parameter.size() == 1) {
            debugL = Integer.parseInt(parameter.get(0));
        }
        Debug.setLevel(debugL);

        parameter = arguments.get("port");
        if (parameter != null && parameter.size() == 1) {
            port = Integer.parseInt(parameter.get(0));
        } else {
            errorInParameters = true;
        }
        parameter = arguments.get("fwdproxy");
        if (parameter != null && parameter.size() == 1) {
            fwdProxyServer = args[1];
        }
        parameter = arguments.get("fwdport");
        if (parameter != null && parameter.size() == 1) {
            fwdProxyPort = Integer.parseInt(parameter.get(0));
        }

        parameter = arguments.get("block");
        if (parameter != null && parameter.size() > 0) {
            for (String u : parameter) {
                Filters.getInstance().readFromURL(u);
            }
        }

        parameter = arguments.get("threads");
        if (parameter != null && parameter.size() == 1) {
            for (String u : parameter) {
                int k = Integer.parseInt(parameter.get(0));
                if (k > 0) {
                    poolSize = k;
                } else {
                    errorInParameters = true;
                }
            }
        }


        if (args.length == 0 || errorInParameters) {
            System.err.println("USAGE: java jProxy --port <port number> [--fwdproxy <fwd proxy>] [--fwdport <fwd port>] [--block <url>] [--debug <level>] [--threads <threads count>]");
            System.err.println("  <port number>   the port this service listens on");
            System.err.println("  <fwd proxy>     optional proxy server to forward requests to");
            System.err.println("  <fwd port>      the port that the optional proxy server is on");
            System.err.println("  <url>           url of adblock/adblock plus filter (http:// or file://)");
            System.err.println("  <level>         level of debug, default is " + debugL + ", max is 3");
            System.err.println("  <threads count> size of thread pool (default " + poolSize + ")");
            System.err.println("\nHINT: if you don't want to see all the debug information flying by,");
            System.err.println("you can pipe the output to a file or to 'nul' using \">\". For example:");
            System.err.println("  to send output to the file prox.txt: java jProxy 8080 > prox.txt");
            System.err.println("  to make the output go away: java jProxy 8080 > nul");
            return;
        }


        // create and start the jProxy thread, using a 20 second timeout
        // value to keep the threads from piling up too much
        System.err.println("  **  Starting jProxy on port " + port + ". Press CTRL-C to end.  **\n");
        JProxy jp = new JProxy(port, fwdProxyServer, fwdProxyPort, 20);
        Debug.setDebugOut(System.out);
        Debug.setLevel(debugL);		// or set the debug level to 2 for tons of output
        jp.start();

        // run forever; if you were calling this class from another
        // program and you wanted to stop the jProxy thread at some
        // point, you could write a loop that waits for a certain
        // condition and then calls jProxy.closeSocket() to kill
        // the running jProxy thread
        while (true) {
            try {
                if (!jp.isAlive()) break;
                Thread.sleep(3000);
            } catch (Exception e) {
            }
        }

    // if we ever had a condition that stopped the loop above,
    // we'd want to do this to kill the running thread
    //jp.closeSocket();
    //return;
    }


    /* the proxy server just listens for connections and creates
     * a new thread for each connection attempt (the ProxyThread
     * class really does all the work)
     */
    public JProxy(int port) {
        thisPort = port;
        pool = Executors.newFixedThreadPool(poolSize);
    }

    public JProxy(int port, String proxyServer, int proxyPort) {
        this(port);
        fwdServer = proxyServer;
        fwdPort = proxyPort;
    }

    public JProxy(int port, String proxyServer, int proxyPort, int timeout) {
        this(port, proxyServer, proxyPort);
        ptTimeout = timeout;
    }

    /* get the port that we're supposed to be listening on
     */
    public int getPort() {
        return thisPort;
    }


    /* return whether or not the socket is currently open
     */
    public boolean isRunning() {
        if (server == null) {
            return false;
        } else {
            return true;
        }
    }


    /* closeSocket will close the open ServerSocket; use this
     * to halt a running jProxy thread
     */
    public void closeSocket() {
        try {
            // close the open server socket
            server.close();
        // send it a message to make it stop waiting immediately
        // (not really necessary)
			/*Socket s = new Socket("localhost", thisPort);
        OutputStream os = s.getOutputStream();
        os.write((byte)0);
        os.close();
        s.close();*/
        } catch (Exception e) {
            Debug.println(1, e.toString());
        }

        server = null;
    }

    @Override
    public void run() {
        try {
            // create a server socket, and loop forever listening for
            // client connections

            server = new ServerSocket(thisPort);
            Debug.println(0, "Started jProxy on port " + thisPort + ", threads pool size " + poolSize);

            while (true) {
                //pool.execute(new Handler(serverSocket.accept()));

                Socket client = server.accept();
                ProxyThread t = new ProxyThread(client, fwdServer, fwdPort);
                t.setTimeout(ptTimeout);
                //t.start();
                pool.execute(t);
            }
        } catch (Exception e) {
            Debug.println(1, "jProxy Thread error: " + e);
        } finally {
            closeSocket();
            Debug.println(1, "Socket closed");
        }
    }
}


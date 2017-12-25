package rpc;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

//import org.apache.commons.lang.exception.ExceptionUtils;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

/**
 * Proxy class for JSON-RPC function calls (http://json-rpc.org) using a TCP socket streaming the data.
 * 
 * One TCP connection per simulation is used. This is a optimized, high performance implementation.
 * 
 * This implementation is not JSON-RPC 2.0 standard compatible as the communication is not over HTTP.
 * 
 * Simple custom RPC streaming protocol sending JSON-RPC 2.0 string lines encoded as UTF-8:
 * <ol>
 * <li>Open TCP socket communication connection
 * <li>Send JSON-RPC 2.0 request terminated by EOL.
 * <li>Read JSON-RPC 2.0 response terminated by EOL.
 * <li>Repeat 2 until simulation has ended.
 * <li>Send empty line terminated by EOL to terminate the connection.
 * </ol>
 * 
 * EOL = End of line (i.e. \n in Python/Java or Unicode \u000a)
 * 
 * This client proxy uses parts of http://software.dzhuvinov.com/json-rpc-2.0-base.html.
 * 
 * The {@link JsonRpcProxy} is similar, but creates for each request a new HTTP (TCP) connection.
 * Therefore, {@link JsonRpcStreamProxy} is much slower, but JSON-RPC standard conform.
 * 
 * @author rkurmann
 * @since Polysun 9.2
 * 
 * @see JsonRpcProxy
 *
 */
public class JsonRpcStreamProxy extends AbstractJsonRpcProxy {

	/** TCP socket for streaming the function JSON-RPC requests and JSON-RPC responses. */
	private Socket socket;
	/** PrintWriter using TCP socket for sending the function JSON-RPC requests. */
	private PrintWriter socketOut;
	/** BufferedReader using TCP socket for reading the function JSON-RPC responses. */
	private BufferedReader socketIn;
		
	/**
	 * Constructor.
	 * 
	 * @param rpcServerURL URL of the RPC server for the function calls, e.g. http://localhost:2102/control
	 * @param rpcFunction Name of the RPC function, e.g. controlFlowrate
	 * @param connectionTimeout Connection timeout [ms] 0 may mean wait forever.
	 * @param readTimeout Read timeout [ms] 0 may mean wait forever.
	 * @param verboseLevel Level of verbosity
	 */
	protected JsonRpcStreamProxy(URL rpcServerURL, String rpcFunction, int connectionTimeout, int readTimeout, int verboseLevel) {
		super(rpcServerURL, rpcFunction, connectionTimeout, readTimeout, verboseLevel);
	}

	@Override
	public void disconnectProxy() {
		if (socket != null) {
			if (RpcProxy.sLog.isLoggable(Level.INFO))
				RpcProxy.sLog.info("Disconnect proxy");
			try {
				socketIn.close();
			} catch (IOException e) {
//				sLog.warning(ExceptionUtils.getFullStackTrace(e));
				RpcProxy.sLog.warning(e.getMessage());
			}
			socketOut.print("\n"); // Send empty line as signal to close connection
			socketOut.close(); // flush() is called before the writer is closed
			try {
				socket.close();
			} catch (IOException e) {
//				sLog.warning(ExceptionUtils.getFullStackTrace(e));
				RpcProxy.sLog.warning(e.getMessage());
			}
			socketIn = null;
			socketOut = null;
			socket = null;
			rpcServerURL = null;
		}
	}

	@Override
	public JSONRPC2Response invoke(JSONRPC2Request request) throws JSONRPC2ParseException, IOException {
		getProxy();
		socketOut.print(request + "\n"); // Do not use println() since EOL is platform specific
		socketOut.flush(); // Flush request since println() is not used
		long startWait = System.currentTimeMillis();
		while (!socketIn.ready() && startWait + readTimeout > System.currentTimeMillis());
		String rawResponse = socketIn.readLine();
		return JSONRPC2Response.parse(rawResponse, false, true, false);
	}
	
	/**
	 * Returns a proxy to communicate with the JSON-RPC server.
	 * 
	 * @return the proxy
	 * @throws IOException for IO problems
	 * @throws UnknownHostException for host resolving problems
	 */
	protected Socket getProxy() throws UnknownHostException, IOException {
		if (socket == null) {
			if (verboseLevel >= VERBOSE_LEVEL_DEBUG) {
				RpcProxy.sLog.fine("Create JsonRpcStreamProxy");
			}
			
			socket = new Socket(rpcServerURL.getHost(), rpcServerURL.getPort());
			socket.setPerformancePreferences(0, 2, 1);
			socket.setTcpNoDelay(true); // Not actually useful here, since our protocol is a ping pong
			socket.setSoTimeout(readTimeout);
			socketOut = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), "UTF-8"), true); // new BufferedWriter() is not useful in our case since we send one line in one command, thus no buffering is required
			socketIn = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		}
		return socket;
	}

	/**
	 * Test call.
	 * 
	 * @param args Program arguments
	 * 
	 * @throws Exception
	 *             For any problems
	 */
	public static void main(String... args) throws Exception {
		System.out.println("Start");
		URL serverURL = new URL("jsonrpc2://localhost:2102");

		int requestID = 0;
		
		JsonRpcStreamProxy proxy = new JsonRpcStreamProxy(serverURL, "keras_test_numpy", 15000, 15000, 3);

		JSONRPC2Request request = new JSONRPC2Request("ping", requestID++);
		JSONRPC2Response response = proxy.invoke(request);
		if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}

//		request = new JSONRPC2Request("ping", requestID++);
//		response = proxy.invoke(request);
		/*if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}*/
		
		request = new JSONRPC2Request("print", Arrays.asList(new Object[] { "WOW" }), requestID++);
		response = proxy.invoke(request);
		if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}

		int num = 100000;
		long start = System.nanoTime();
		for (int i = 0; i < num; i++) {
			response = proxy.invoke(new JSONRPC2Request("ping", requestID++));
		}
		long stop = System.nanoTime();
		System.out.println("@ ping " + num + " times cost time: " + ((stop - start) / 10000 / num) / 100f + "ms");
		if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}

		System.out.println("End");
//		System.exit(0);
	}

}

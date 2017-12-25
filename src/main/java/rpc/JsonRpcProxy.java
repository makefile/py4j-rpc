package rpc;

import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2Session;
import com.thetransactioncompany.jsonrpc2.client.JSONRPC2SessionOptions;

/**
 * Proxy class for JSON-RPC function calls (http://json-rpc.org) over HTTP.
 * 
 * One HTTP connection (TCP connection) per request is created.
 * 
 * JSON-RPC 2.0 standard compatible.
 * 
 * This client proxy uses http://software.dzhuvinov.com/json-rpc-2.0-base.html.
 * 
 * Could run into "java.net.BindException: Address already in use: connect" due to ephemeral TCP ports exhaustion, see PSA-4571.
 * 
 * The {@link JsonRpcStreamProxy} is similar, but uses only once TCP connection for the whole
 * simulation. Therefore, {@link JsonRpcStreamProxy} is much faster, but not JSON-RPC standard conform.
 * 
 * @author rkurmann
 * @since Polysun 9.1
 * 
 * @see JsonRpcStreamProxy
 *
 */
public class JsonRpcProxy extends AbstractJsonRpcProxy {

	/** The JSON-RPC proxy. */
	private JSONRPC2Session proxy;
	
	/**
	 * Constructor.
	 * 
	 * @param rpcServerURL URL of the RPC server for the function calls, e.g. http://localhost:2102/control
	 * @param rpcFunction Name of the RPC function, e.g. controlFlowrate
	 * @param connectionTimeout Connection timeout [ms] 0 may mean wait forever.
	 * @param readTimeout Read timeout [ms] 0 may mean wait forever.
	 * @param verboseLevel Level of verbosity
	 */
	protected JsonRpcProxy(URL rpcServerURL, String rpcFunction, int connectionTimeout, int readTimeout, int verboseLevel) {
		super(rpcServerURL, rpcFunction, connectionTimeout, readTimeout, verboseLevel);
	}

	@Override
	public JSONRPC2Response invoke(JSONRPC2Request request) throws Exception {
		return getProxy().send(request);
	}

	@Override
	public void disconnectProxy() {
		if (proxy != null) {
			if (RpcProxy.sLog.isLoggable(Level.INFO))
				RpcProxy.sLog.info("Disconnect proxy");
			proxy = null;
			rpcServerURL = null;
		}
	}

	/**
	 * Returns a proxy to communicate with the JSON-RPC server.
	 * 
	 * @return the proxy
	 */
	protected JSONRPC2Session getProxy() {
		if (proxy == null) {
			if (verboseLevel >= VERBOSE_LEVEL_DEBUG) {
				RpcProxy.sLog.fine("Create rpc.JsonRpcProxy");
			}
			proxy = new JSONRPC2Session(rpcServerURL);
			JSONRPC2SessionOptions sessionOptions = new JSONRPC2SessionOptions();
			sessionOptions.ignoreVersion(true);
			sessionOptions.setReadTimeout(readTimeout);
			sessionOptions.setConnectTimeout(connectionTimeout);
			sessionOptions.enableCompression(false);
			sessionOptions.parseNonStdAttributes(false);
			proxy.setOptions(sessionOptions);
		}
		return proxy;
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
		URL serverURL = new URL("http://localhost:2102/control");

		// Create new JSON-RPC 2.0 client session
		JSONRPC2Session mySession = new JSONRPC2Session(serverURL);
		// Construct new request
		int requestID = 0;

		JSONRPC2Request request = new JSONRPC2Request("ping", requestID);
		JSONRPC2Response response = mySession.send(request);
		if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}

		request = new JSONRPC2Request("print", Arrays.asList(new Object[] { "WOW" }), requestID);
		response = mySession.send(request);
		if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}

		request = new JSONRPC2Request("ping", requestID);
		int num = 1000;
		long start = System.nanoTime();
		for (int i = 0; i < num; i++) {
			response = mySession.send(request);
		}
		long stop = System.nanoTime();
		System.out.println("@ ping: " + ((stop - start) / 10000 / num) / 100f + "ms");
		if (response.indicatesSuccess()) {
			System.out.println(response.getResult());
		} else {
			System.out.println(response.getError().getMessage());
		}

		System.out.println("End");
		System.exit(0);
	}

}

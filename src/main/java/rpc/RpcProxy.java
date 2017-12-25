package rpc;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Base RPC proxy class encapsulating RPC communication.
 * 
 * @author rkurmann
 * @since Polysun 9.1
 *
 */
public abstract class RpcProxy {

	/** Static instance of the Logger for this class */
	protected static Logger sLog = Logger.getLogger(RpcProxy.class.getName());

	/** Name of the RPC function to call. Comes from the controller element GUI. */
	protected String rpcFunction;
	
	/** URL of the webservice having the RPC control functions. */
	protected URL rpcServerURL = null;
	
	/** Connection timeout [ms]. 0 may mean wait forever. */
	protected int connectionTimeout = 10000;
	
	/** Read timeout [ms]. 0 may mean wait forever. */
	protected int readTimeout = 10000;
	
	/**
	 * Verbose level.
	 * 0 = default
	 * 1 = verbose
	 * 2 = debug
	 * Comes from the controller element GUI.
	 */
	protected int verboseLevel;

	/** Empty paramters object. */
	final protected Map<String, Object> emptyParamters = new HashMap<String, Object>();

	/**
	 * Constructor.
	 * 
	 * @param rpcServerURL URL of the RPC server for the function calls, e.g. http://localhost:2102/control
	 * @param rpcFunction Name of the RPC function, e.g. controlFlowrate
	 * @param connectionTimeout Connection timeout [ms] 0 may mean wait forever.
	 * @param readTimeout Read timeout [ms] 0 may mean wait forever.
	 * @param verboseLevel Level of verbosity
	 */
	protected RpcProxy(URL rpcServerURL, String rpcFunction, int connectionTimeout, int readTimeout, int verboseLevel) {
		this.rpcServerURL = rpcServerURL;
		this.rpcFunction = rpcFunction;
		this.connectionTimeout = connectionTimeout;
		this.readTimeout = readTimeout;
	}

	/**
	 * Calls the remote function.
	 * 
	 * @param parameters
	 *            Map&lt;String, Object&gt;: Generic parameters
	 * @return
	 * 
	 * @throws Throwable
	 *             For any problems
	 */
	public abstract ControlFunctionResponse callRemoteFunction(
			Map<String, Object> parameters, int request_id) throws Throwable;

	public abstract JSONRPC2Response invoke(JSONRPC2Request request) throws Exception;
	/**
	 * Converts float[] to a Double[].
	 * 
	 * @param input input array
	 * @return output array
	 */
	protected Double[] convertFloatsToDoubles(float[] input) {
		if (input == null) {
			return null;
		}
		Double[] output = new Double[input.length];
		for (int i = 0; i < input.length; i++) {
			output[i] = (double) input[i];
		}
		return output;
	}

	/**
	 * Disconnects the RPC proxy.
	 */
	public abstract void disconnectProxy();

	/**
	 * Returns the verbose level.
	 * 
	 * @return the verboseLevel
	 */
	public int getVerboseLevel() {
		return verboseLevel;
	}

	/**
	 * Sets up the RPC proxy.
	 * @param parameters Generic parameters
	 * @throws Throwable For any problems
	 */
	public abstract void setupRpc(Map<String, Object> parameters) throws Throwable;

	/**
	 * Sets the verbose level
	 * @param verboseLevel
	 *            the verboseLevel to set
	 */
	public void setVerboseLevel(int verboseLevel) {
		this.verboseLevel = verboseLevel;
	}

	/**
	 * Writes a message to the RPC Server console.
	 * @param str messate to write to the server
	 * 
	 * @throws Throwable  For any problems
	 */
	public abstract void writeMsgToServer(String str) throws Throwable;


}

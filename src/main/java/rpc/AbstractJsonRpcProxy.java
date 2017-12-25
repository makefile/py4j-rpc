package rpc;//import static rpc.ControlFunctionResponse.EMPTY_FLOAT_ARRAY;
//import static com.velasolaris.plugin.controller.rpc.rpc.ControlFunctionResponse.EMPTY_INT_ARRAY;
//import static com.velasolaris.plugin.controller.rpc.rpc.ControlFunctionResponse.convertObjectArrayToFloats;
//import static rpc.ControlFunctionResponse.convertObjectToInts;

import java.net.URL;
import java.util.Arrays;
import java.util.Map;

//import org.apache.commons.lang.ArrayUtils;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
//import com.velasolaris.plugin.controller.spi.PluginControllerException;

import net.minidev.json.JSONArray;

/**
 * Common base class for JSON-RPC proxies.
 * 
 * @author rkurmann
 * @since 9.2
 *
 */
public abstract class AbstractJsonRpcProxy extends RpcProxy {

	/**
	 * Constructor.
	 * 
	 * @param rpcServerURL URL of the RPC server for the function calls, e.g. http://localhost:2102/control
	 * @param rpcFunction Name of the RPC function, e.g. controlFlowrate
	 * @param connectionTimeout Connection timeout [ms] 0 may mean wait forever.
	 * @param readTimeout Read timeout [ms] 0 may mean wait forever.
	 * @param verboseLevel Level of verbosity
	 */
	protected AbstractJsonRpcProxy(URL rpcServerURL, String rpcFunction, int connectionTimeout, int readTimeout,
			int verboseLevel) {
		super(rpcServerURL, rpcFunction, connectionTimeout, readTimeout, verboseLevel);
	}
	
	/**
	 * Invoke a RPC function via JSON-RPC over the TCP socket.
	 * 
	 * @param request the JSON-RPC request
	 * @return the JSON-RPC response
	 * @throws Exception a for problems
	 */
	public abstract JSONRPC2Response invoke(JSONRPC2Request request) throws Exception;

	@Override
	public ControlFunctionResponse callRemoteFunction(Map<String, Object> parameters,int request_id)
			throws Exception {
				// def control(simulationTime, status, sensors, sensorsUsed, properties,
				// propertiesStr, preRun, controlSignalsUsed, numLogValues, stage,
				// fixedTimestep, verboseLevel, parameters)
				JSONRPC2Request request = new JSONRPC2Request(rpcFunction,
						Arrays.asList(new Object[] {parameters}),
//								ArrayUtils.toObject(sensorsUsed), convertFloatsToDoubles(properties), propertiesStr, preRun,
//								ArrayUtils.toObject(controlSignalsUsed), logValues.length, stage, fixedTimestep, verboseLevel,
//								/*parameters != null && false ? parameters :*/ emptyParamters }),
						request_id);
				JSONRPC2Response response = invoke(request);
				ControlFunctionResponse result;
				if (response.indicatesSuccess()) {
					if (response.getResult() instanceof JSONArray) {
						Object[] resultArray = ((JSONArray) response.getResult()).toArray();
						result = new ControlFunctionResponse(ControlFunctionResponse.convertObjectArrayToFloats(resultArray[0]));
//						result = new ControlFunctionResponse(
//								resultArray.length > 0 ? ControlFunctionResponse.convertObjectArrayToFloats(resultArray[0]) : ControlFunctionResponse.EMPTY_FLOAT_ARRAY,
//								resultArray.length > 1 ? ControlFunctionResponse.convertObjectArrayToFloats(resultArray[1]) : ControlFunctionResponse.EMPTY_FLOAT_ARRAY,
//								resultArray.length > 2 ? ControlFunctionResponse.convertObjectToInts(resultArray[2]) : ControlFunctionResponse.EMPTY_INT_ARRAY);
					} else {
						result = new ControlFunctionResponse(ControlFunctionResponse.EMPTY_FLOAT_ARRAY);
					}
				} else {
//					throw new PluginControllerException(response.getError());
					throw new Exception(response.getError());
				}
				return result;
			}

	@Override
	public void setupRpc(Map<String, Object> parameters) throws Exception {
		writeMsgToServer("hello: Java side connected to Server.");
		sLog.info("Connected to Server");
	}

	/** Debug verbose level. */
	public static final int VERBOSE_LEVEL_DEBUG = 2;
	/** Standard verbose level. */
	public static final int VERBOSE_LEVEL_STANDARD = 0;
	/** Verbose level: Verbose (more thans standard output). */
	public static final int VERBOSE_LEVEL_VERBOSE = 1;

	@Override
	public void writeMsgToServer(String str) throws Exception {
//		if (verboseLevel >= SimpleRpcPluginController.VERBOSE_LEVEL_DEBUG) {
		if (verboseLevel >= VERBOSE_LEVEL_DEBUG) {
			sLog.fine("Write Message to Server: " + str);
		}
		invoke(new JSONRPC2Request("print", Arrays.asList(new Object[] { str }), 0));
	}

}
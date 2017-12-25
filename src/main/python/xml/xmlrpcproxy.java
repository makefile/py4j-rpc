package com.velasolaris.plugin.controller.rpc;

import static com.velasolaris.plugin.controller.rpc.ControlFunctionResponse.EMPTY_FLOAT_ARRAY;
import static com.velasolaris.plugin.controller.rpc.ControlFunctionResponse.EMPTY_INT_ARRAY;
import static com.velasolaris.plugin.controller.rpc.ControlFunctionResponse.convertObjectArrayToFloats;
import static com.velasolaris.plugin.controller.rpc.ControlFunctionResponse.convertObjectToInts;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.ArrayUtils;
import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

/**
 * Proxy class for XML-RPC function calls using HTTP.
 * 
 * XML-RPC standard compatible.
 * 
 * One HTTP connection (TCP connection) per request is created.
 * 
 * This implementation uses
 * https://ws.apache.org/xmlrpc/client.html
 * where the library is at
 * http://archive.apache.org/dist/ws/xmlrpc/
 * 
 * @author rkurmann
 * @since Polysun 9.1
 *
 */
public class XmlRpcProxy extends RpcProxy {

	/** RPC proxy. */
	private XmlRpcClient proxy;
	/** RPC proxy config. */
	private XmlRpcClientConfigImpl config;

	/**
	 * Constructor.
	 * 
	 * @param rpcServerURL URL of the RPC server for the function calls, e.g. http://localhost:2102/control
	 * @param rpcFunction Name of the RPC function, e.g. controlFlowrate
	 * @param connectionTimeout Connection timeout [ms] 0 may mean wait forever.
	 * @param readTimeout Read timeout [ms] 0 may mean wait forever.
	 * @param verboseLevel Level of verbosity
	 */
	protected XmlRpcProxy(URL rpcServerURL, String rpcFunction, int connectionTimeout, int readTimeout, int verboseLevel) {
		super(rpcServerURL, rpcFunction, connectionTimeout, readTimeout, verboseLevel);
	}

	@Override
	public ControlFunctionResponse callRemoteFunction(int simulationTime, boolean status, float[] sensors, boolean[] sensorsUsed,
			float[] properties, String[] propertiesStr, boolean preRun, boolean[] controlSignalsUsed, float[] logValues,
			int stage, int fixedTimestep, int verboseLevel, Map<String, Object> parameters) throws XmlRpcException {
		// def control(simulationTime, status, sensors, sensorsUsed, properties,
		// propertiesStr, preRun, controlSignalsUsed, numLogValues, stage,
		// fixedTimestep, verboseLevel, parameters)
		Object response = getProxy().execute(rpcFunction,
				new Object[] { simulationTime, status, convertFloatsToDoubles(sensors),
						ArrayUtils.toObject(sensorsUsed), convertFloatsToDoubles(properties), propertiesStr, preRun,
						ArrayUtils.toObject(controlSignalsUsed), logValues.length, stage, fixedTimestep, verboseLevel,
						/*parameters != null && false ? parameters :*/ emptyParamters });
		ControlFunctionResponse result;
		if (response instanceof Object[]) {
			Object[] resultArray = (Object[]) response;
			result = new ControlFunctionResponse(
					resultArray.length > 0 ? convertObjectArrayToFloats(resultArray[0]) : EMPTY_FLOAT_ARRAY,
					resultArray.length > 1 ? convertObjectArrayToFloats(resultArray[1]) : EMPTY_FLOAT_ARRAY,
					resultArray.length > 2 ? convertObjectToInts(resultArray[2]) : EMPTY_INT_ARRAY);
		} else {
			result = new ControlFunctionResponse();
		}
		return result;
	}

	@Override
	public void setupRpc(Map<String, Object> parameters) throws Exception {
		sLog.info("Start connecting to Server...");
		if (config == null) {
			config = new XmlRpcClientConfigImpl();
			config.setServerURL(rpcServerURL);
			config.setEnabledForExtensions(true);
			config.setGzipCompressing(false);
			config.setGzipRequesting(false);
			config.setContentLengthOptional(true);
			config.setConnectionTimeout(connectionTimeout);
			config.setReplyTimeout(readTimeout);
		}

		writeMsgToServer("Polysun connected to Server.");
		sLog.info("Connected to Server");
		parameters.put("Plugin.PrintMessage", "Connected to Server");
	}

	/**
	 * Returns a proxy to communicate with XML-RPC server.
	 * 
	 * @return the proxy
	 */
	protected XmlRpcClient getProxy() {
		if (proxy == null) {
			if (verboseLevel >= SimpleRpcPluginController.VERBOSE_LEVEL_DEBUG) {
				sLog.fine("Create XmlRpcClient");
			}
			proxy = new XmlRpcClient();
			proxy.setConfig(config);
		}
		return proxy;
	}

	@Override
	public void writeMsgToServer(String str) throws XmlRpcException {
		if (verboseLevel >= SimpleRpcPluginController.VERBOSE_LEVEL_DEBUG) {
			sLog.fine("Write Message to Server: " + str);
		}
		getProxy().execute("print", new Object[] { str });
	}

	@Override
	public void disconnectProxy() {
		if (proxy != null) {
			if (sLog.isLoggable(Level.INFO))
				sLog.info("Disconnect proxy");
			proxy = null;
			config = null;
		}
	}

	/**
	 * Test call.
	 * 
	 * @param args program arguments
	 * 
	 * @throws Exception
	 *             For any problems
	 */
	public static void main(String... args) throws Exception {
		System.out.println("Start");
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL("http://localhost:2101/control"));
		config.setEnabledForExtensions(true);
		config.setEnabledForExceptions(true);
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		String result = (String) client.execute("ping", new Object[] {});
		System.out.println(result);

		int num = 1000;
		long start = System.nanoTime();
		for (int i = 0; i < num; i++) {
			result = (String) client.execute("ping", new Object[] {});
		}
		long stop = System.nanoTime();
		System.out.println("Avg. ping: " + ((stop - start) / 10000 / num) / 100f + "ms");

		client.execute("print", new Object[] { "Polysun connected to Server." });
		// def controlFlowrate(simulationTime, status, sensors, sensorsUsed,
		// properties, propertiesStr, preRun, controlSignalsUsed, numLogValues,
		// stage, fixedTimestep, verboseLevel, parameters):
		Object[] ret = (Object[]) client.execute("controlTest", new Object[] { 1, true, new Double[] { 1d } });
		System.out.println("Response length: " + ret.length);
		// ret = (Object[]) client.execute("controlFlowrate", new Object[] {1,
		// true, new Float[] {1f, 1f}, new Float[] {1f, 1f}, new Float[] {1f},
		// new String[] {"X"}, true, new Float[] {1f}, 1, FUNCTION_STAGE_INIT,
		// 120, 3, new Object[0]});
		Map<String, Object> parameters = new HashMap<>();
		parameters.put("Polysun.UserLevel", 1);
//		parameters.put("Polysun.Version", 1l);
		parameters.put("Polysun.DataPath", "~/dev/java/polysun/polysun/production/commons");

		ret = (Object[]) client.execute("controlFlowrate",
				new Object[] { 1, true, new Double[] { 1d, 1d }, new Double[] { 1d, 1d }, new Double[] { 1d },
						new String[] { "X" }, true, new Double[] { 1d }, 1,
						SimpleRpcPluginController.FUNCTION_STAGE_INIT, 120, 3, parameters });
		long start2 = System.nanoTime() / 1000000;
		ret = (Object[]) client.execute("controlFlowrate",
				new Object[] { 1, true, new Double[] { 1d, 1d, 1d }, new Double[] { 1d, 1d, 1d },
						new Double[] { 1d, 1d, 1d }, new String[] { "X", "Y", "Z" }, true, new Double[] { 1d, 1d, 1d },
						1, SimpleRpcPluginController.FUNCTION_STAGE_SIMULATION, 120, 3, new Object[0] });
		long stop2 = System.nanoTime() / 1000000;
		System.out.println("controlTest: " + (stop2 - start2) + "ms");
		System.out.println("End");
		System.exit(0);
	}

}

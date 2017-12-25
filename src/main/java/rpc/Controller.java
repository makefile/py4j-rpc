package rpc;

import com.thetransactioncompany.jsonrpc2.JSONRPC2ParseException;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.net.URLStreamHandlerFactory;
import java.util.Map;

public class Controller {
    /** RPC Type */
    public enum RpcType {
        /**
         * JSON-RPC as stream using one TCP socket. Non standard behaviour.
         * Very fast and avoiding "java.net.BindException: Address already in use: connect" due to ephemeral TCP ports exhaustion on Windows PSA-4571.
         */
        JSON_STREAM,
        /**
         * JSON-RPC over HTTP as defined by standard.
         * Could run into "java.net.BindException: Address already in use: connect" due to ephemeral TCP ports exhaustion, see PSA-4571.
         */
        JSON,
        /** XML-RPC over HTTP as defined by standard. */
        XML
    }
    /** Name of the RPC function to call. */
    protected String rpcFunction;
    /** The RPC proxy that calls the RPC server. */
    private RpcProxy rpcProxy = null;
    /** Type of RPC, JSON or XML. */
    private RpcType rpcType;
    /** URL of the webservice having the RPC control functions. */
    protected String serverURL = null;

    /** Connection timeout [ms]. 0 may mean wait forever. */
    protected int connectionTimeout = 60000;

    /** Read timeout [ms]. 0 may mean wait forever. */
    protected int readTimeout = 0;
    /** Verbose level. 0 = default 1 = verbose 2 = debug */
    protected int verboseLevel;
    static {
        // Add the jsonrpc2 protocol to the URL stream handler
        // URL URL.setURLStreamHandlerFactory must not be called twice in the whole application
        URL.setURLStreamHandlerFactory(new URLStreamHandlerFactory() {
            //            @Override
            public URLStreamHandler createURLStreamHandler(String protocol) {
                return "jsonrpc2".equals(protocol) ? new URLStreamHandler() {
                    @Override
                    protected URLConnection openConnection(URL url) throws IOException {
                        return new URLConnection(url) {
                            @Override
                            public void connect() throws IOException {
                                // Do nothing
                            }
                        };
                    }
                } : null;
            }
        });
    }
    public void disconnectProxy() {
        if (rpcProxy != null) {
            rpcProxy.disconnectProxy();
        }
    }
    public void closeResources() {
        disconnectProxy();
    }
    /**
     * Sets up the RPC proxy.
     *
     * @throws Throwable
     *             For any problems
     * @see rpc.RpcProxy#setupRpc(java.util.Map)
     */
    public void setupRpc(RpcType rpcType,String serverURL,String rpcFunction, int connectionTimeout, int readTimeout,int verboseLevel) throws Throwable {
        if (rpcProxy != null) {
            rpcProxy.disconnectProxy();
        }
        if (rpcType == RpcType.JSON) {
            rpcProxy = new JsonRpcProxy(new URL(serverURL), rpcFunction, connectionTimeout, readTimeout, verboseLevel);
        } else if (rpcType == RpcType.JSON_STREAM) {
            rpcProxy = new JsonRpcStreamProxy(new URL(serverURL), rpcFunction, connectionTimeout, readTimeout, verboseLevel);
        } /*else {
            rpcProxy = new XmlRpcProxy(new URL(serverURL), rpcFunction, connectionTimeout, readTimeout, verboseLevel);
        }*/
        rpcProxy.setupRpc(null);
    }
    public ControlFunctionResponse callRemoteFunction(Map<String, Object> parameters,int request_id)
            throws Throwable {
        return rpcProxy.callRemoteFunction(parameters,request_id);
    }

    public RpcProxy getRpcProxy() {
        return rpcProxy;
    }
}

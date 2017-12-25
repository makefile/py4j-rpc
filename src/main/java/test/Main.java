package test;

import com.thetransactioncompany.jsonrpc2.JSONRPC2Request;
import com.thetransactioncompany.jsonrpc2.JSONRPC2Response;
import org.python.core.PyList;
import org.python.core.PyString;
import org.python.util.PythonInterpreter;
import rpc.ControlFunctionResponse;
import rpc.Controller;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class Main {

    public static void naive_test(String[] args){
        Properties props = new Properties();
        props.put("python.console.encoding", "UTF-8"); // Used to prevent: console: Failed to install '': java.nio.charset.UnsupportedCharsetException: cp0.
        props.put("python.security.respectJavaAccessibility", "false"); //don't respect java accessibility, so that we can access protected members on subclasses
        props.put("python.import.site","false");

        Properties preprops = System.getProperties();

        PythonInterpreter.initialize(preprops, props, new String[0]);
        PythonInterpreter pi = new PythonInterpreter();
        pi.execfile("src/main/python/my_func.py");
        pi.exec("print(ping())");
        pi.exec("result = ping()");
        PyString result = (PyString)pi.get("result");
        System.out.println(result);
//        long start = System.nanoTime() / 1000000;
//        pi.set("simulationTime", 1);
//        pi.set("status", true);
//        pi.set("sensors", new Double[] {1d, 2d, 3d});
//        pi.exec("result = controlTest(simulationTime, status, sensors)");
//        PyList resultList = (PyList)pi.get("result");
//        System.out.println(resultList);
//        long stop = System.nanoTime() / 1000000;
//        System.out.println("controlTest: " + (stop - start) + "ms");
    }
    public static void main(String[] args) throws Throwable {
        Controller controller = new Controller();
        String serverURL = "jsonrpc2://localhost:2102";
        String rpcFunction = "keras_test_numpy";
        controller.setupRpc(Controller.RpcType.JSON_STREAM, serverURL, rpcFunction, 15000, 15000, 3);
//        JsonRpcStreamProxy.main(args);
        int requestID = 0;

        JSONRPC2Request request = new JSONRPC2Request("ping", requestID++);
        JSONRPC2Response response = controller.getRpcProxy().invoke(request);
        if (response.indicatesSuccess()) {
            System.out.println(response.getResult());
        } else {
            System.out.println(response.getError().getMessage());
        }
        final float[][] feature = new float[3][4];
        feature[0][3] = 1;feature[1][3] = 2;feature[2][3] = 3;
        Map<String, Object> paramters = new HashMap<String, Object>();
        paramters.put("feature", feature);
        paramters.put("rows", feature.length);
        paramters.put("cols", feature[0].length);
        ControlFunctionResponse result = controller.callRemoteFunction(paramters, requestID++);
        System.out.println(Arrays.toString(result.getPredictResult()));
        controller.closeResources();
    }
}

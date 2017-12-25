## Py4j-RPC

Py4j-RPC is a simple bridge that enables Java VM call Python codes through TCP connection between Jython side and python side. And it uses protocal of JSON-RPC.

It can be used as a simple way to run Python services for Java, such as Keras, PyTorch libraries for deep learning. The thing you need to notice is that the params to send is basic type. There is a walkaround if you need numpy array in python, put the data in a 1-dim array as Python function's param,then reshape and copy to numpy array.

This is a maven project that depend on [Jython](http://www.jython.org) and [JSON-RPC-2.0](http://www.jsonrpc.org/specification) java library.

## Usage

- `python side`: run `python JsonRPC-stream-server.py`
  this will listen on host and serve some functions which will be called in Java side.
- `java side`: see Main.java

**Other repo that might be useful:**
- [invesdwin-context-python](https://github.com/subes/invesdwin-context-python)

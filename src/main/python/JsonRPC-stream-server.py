#!/usr/bin/env python
#encoding=utf-8
"""
JSON-RPC Server for Polysun plugin controller functions of SimpleRpcPluginController (RPC-Type: JSON-RPC stream).

Functions from controlFunctions.py starting with "control" are provided as JSON-RPC stream.

One TCP connection per simulation is used.
This server is not JSON-RPC 2.0 compatible as the communication is not over HTTP.

Simple custom RPC streaming protocol sending JSON-RPC 2.0 string lines encoded as UTF-8:
1. Open TCP socket communication connection
2. Send JSON-RPC 2.0 request terminated by EOL.
3. Read JSON-RPC 2.0 response terminated by EOL.
4. Repeat 2 until simulation has ended.
5. Send empty line terminated by EOL to terminate the connection.

EOL = End of line (i.e. \n in Python/Java or Unicode \u000a)

This server avoids "java.net.BindException: Address already in use: connect"
due to ephemeral TCP ports exhaustion.

See controlJsonRpcServer.py for a JSON-RPC 2.0 compliant server using HTTP as communication protocol.

Call: python controlJsonRpcStreamServer.py
or:   pypy controlJsonRpcStreamServer.py

This script works with Python 2.7 and Python 3.4.
Supports pypy (http://pypy.org/) with JIT compliation

Performance:
JSON-RPC stream (one TCP connection per simulation) is much faster than standard JSON-RPC over HTTP.
JSON-RPC is faster than XML-RPC.
Python 2.7 is faster then Python 3.4.
PyPy 5.4.1 (Python 2.7) is faster than CPython 2.7.
However, a Java implementation is much faster. The time per function call is not measurable.
If performance is an important, the controller logic should directly implemented in Java,
see FlowratePluginController.

Measured average time per function call for control_flowrate() in Python and FlowratePluginController in Java
on a Intel Core i7-4500U CPU 1.80GHz (dual core) are about:
                                 JSON-RPC stream        JSON-RPC         XML-RPC            Java          Matlab
RPC type:                                 custom        standard        standard          native         library
Communication protocol:               TCP socket            HTTP            HTTP          native             RMI
TCP connection per                    simulation        timestep        timestep             N/A      simulation
PyPy 5.4.1 and Polysun compiled:          0.06ms           0.3ms           0.6ms             0ms           0.6ms
PyPy 5.4.1 and Eclipse debug:             0.06ms           0.5ms           0.9ms             0ms           0.6ms
Python 2.7 and Eclipse debug:             0.09ms           0.7ms           1.3ms             0ms           0.6ms
Python 3.4 and Eclipse debug:             0.08ms           0.9ms           1.4ms             0ms           0.6ms

PyPy is an Python interpreter using Just in Time (JIT) compilation.
PyPy is the best way to start the RPC Server.

Jython (http://www.jython.org) is package running Python in a JVM. It works, see below, but it is quite slow.
Functions calls are about 20ms. The Jython.jar is about 27MB.
"""

from __future__ import division, unicode_literals, print_function, absolute_import, with_statement  # Ensure compatibility with Python 3
import argparse
import inspect
import sys
import json
import threading
import request_jsonrpc
import importlib
from utils import indent

PY3 = sys.version_info[0] == 3
PY2 = sys.version_info[0] == 2

if PY3:
    from socketserver import StreamRequestHandler
    from socketserver import TCPServer
elif PY2:
    from SocketServer import StreamRequestHandler
    from SocketServer import TCPServer

__author__ = 'Roland Kurmann'
__email__ = 'roland dot kurmann at velasolaris dot com'
__url__ = 'http://velasolaris.com'
__license__ = 'MIT'
__version__ = '9.2'

parser = argparse.ArgumentParser(description='Polysun control function (JSON-RPC stream server using TCP).')
parser.add_argument('-p', '--port', default='2102', type=int, help='Port of the JSON-RPC stream server (TCP socket). Default 2102')
parser.add_argument('-s', '--host', default='127.0.0.1', help='Host address of the stream server. Default 127.0.0.1')
parser.add_argument('-f', '--functions', default='controlfunctions', help='Python module with control functions. Default controlFunctions')
parser.add_argument('-d', '--debug', action='store_true', help='Enable debug mode with debug output')

args = parser.parse_args()

# instead of 'import controlFunctions', load dynamically using arguments
importlib.import_module(args.functions)

class JsonRpcStreamServerHandler(StreamRequestHandler):
    """
    The request handler class for our TCP server.

    It is instantiated once per connection to the server, and must
    override the handle() method to implement communication to the
    client.

    See https://docs.python.org/2/library/socketserver.html
    """
    def handle(self):
        try:
            print("Connection opened from {} and listening...".format(self.client_address[0]))
            ok = True
            while ok:
                # self.rfile is a file-like object created by the handler;
                # we can now use e.g. readline() instead of raw recv() calls
                self.data = self.rfile.readline().strip()
                # print "{} wrote:".format(self.client_address[0])
                if args.debug:
                    print("'" + self.data + "'")
                # Likewise, self.wfile is a file-like object used to write back
                # to the client
                # self.wfile.write(self.data.upper())
                jsonResponse = self.data.decode('UTF-8')
                # An empty line means stopping the connection
                if jsonResponse != None and jsonResponse.strip() != "":
                    request = json.loads(jsonResponse)
                    response = jsonrpc.handle_rpc(request)
                    jsonResponse = json.dumps(response) + "\n"
                    self.wfile.write(jsonResponse.encode('UTF-8'))
                    self.wfile.flush()
                    if args.debug:
                        print("handled: " + jsonResponse)
                else:
                    ok = False
            print("Connection  stopped")
        except ConnectionResetError:
            print("\nconnection closed.")
        except KeyboardInterrupt:
            print("\nKeyboard interrupt received in request, exiting.")
            server_shutdown()

jsonrpc = request_jsonrpc.register(args.debug)

# echo '{ "jsonrpc": "2.0", "method": "ping", "params": [], "id": 1}' | nc 127.0.0.1 2102
@jsonrpc
def ping():
    """Returns "pong"."""
    return "pong"

# echo '{ "jsonrpc": "2.0", "method": "print", "params": ["Wow"], "id": 1}' | nc 127.0.0.1 2102
def print_msg(msg):
    """Prints a message on the server."""
    print(msg)

jsonrpc.methods['print'] = print_msg

def server_shutdown():
    """Shutdown TCP server.

    server.shutdown() must not be called in the main thread and sys.exit(0) does not work,
    and  os._exit(0) does not clean up server which leads to 'error: [Errno 98] Address already in use'
    """
    print("Initiate server shutdown...")
    threading.Thread(target=server.shutdown).start()

jsonrpc.methods['stop'] = server_shutdown

# http://stackoverflow.com/questions/4040620/is-it-possible-to-list-all-functions-in-a-module
functions = inspect.getmembers(sys.modules[args.functions], inspect.isfunction)
for function in functions:
    if function[0].startswith("keras_"):
        jsonrpc.methods[function[0]] = function[1]

print("\nStart JSON-RPC stream server (TCP)...")
print("jsonrpc2://" + args.host + ":" + str(args.port))
li = "\n    - "
print("Functions:" + li + li.join(str(x) for x in sorted(jsonrpc.methods)))
print("Ctrl-C to stop")

server = None
try:
    # Create the server
    server = TCPServer((args.host, args.port), JsonRpcStreamServerHandler)
    server.allow_reuse_address = True

    # Activate the server; this will keep running until you interrupt the program with Ctrl-C
    server.serve_forever()
except KeyboardInterrupt:
    print("\nKeyboard interrupt received, exiting.")
    server.shutdown()
finally:
    print("Terminate server")
    if server != None:  # Check if server exists, since in case of server creation errors, server variable may not be set
        server.server_close()
    sys.exit(0)

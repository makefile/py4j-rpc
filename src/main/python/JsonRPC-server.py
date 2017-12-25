#!/usr/bin/env python
"""
JSON-RPC Server for Polysun plugin controller functions of SimpleRpcPluginController (RPC-Type: JSON-RPC).

Functions from controlFunctions.py starting with "control" are provided as JSON-RPC.

Call: python controlJsonRpcServer.py
or:   pypy controlJsonRpcServer.py

This server could run into "java.net.BindException: Address already in use: connect"
due to ephemeral TCP ports exhaustion.

See controlJsonRpcStreamServer.py for faster RPC server.
See controlXmlRpcStreamServer.py for a XML-RPC server.

This script works with Python 2.7 and Python 3.4.
Supports pypy (http://pypy.org/) with JIT compliation

Used modules:
- Bottle (https://bottlepy.org/) for webservice functionality
- bottle_jsonrpc.py (https://github.com/olemb/bottle_jsonrpc) for JSON-RPC

The module files have been added for easier use.

Alternative JSON-RPC libraries:
- https://falconframework.org/ (https://github.com/falconry/falcon)
- http://json-rpc.org/wiki/python-json-rpc
- https://wiki.python.org/moin/WebServices

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

JSON-RPC of Bottle can be started with an WSGI server:
uwsgi: https://www.toptal.com/bottle/building-a-rest-api-with-bottle-framework
Some WSGI servers don't have Bottle adapters. These can be started with the server's own run commands. On uWSGI, for instance, all you'd have to do would be to call uwsgi like this:
$ uwsgi --http :8000 --wsgi-file main.py
"""

from __future__ import division, unicode_literals, print_function, absolute_import, with_statement  # Ensure compatibility with Python 3
import argparse
import inspect
import sys
import importlib
from utils import indent

PY3 = sys.version_info[0] == 3
PY2 = sys.version_info[0] == 2

from bottle import route, run, request, response, template, server_names
import bottle_jsonrpc

__author__ = 'Roland Kurmann'
__email__ = 'roland dot kurmann at velasolaris dot com'
__url__ = 'http://velasolaris.com'
__license__ = 'MIT'
__version__ = '9.2'

parser = argparse.ArgumentParser(description='Polysun control function (JSON-RPC HTTP Server).')
parser.add_argument('-p', '--port', default='2102', type=int, help='Port of the REST-Server. Default 2102')
parser.add_argument('-s', '--host', default='127.0.0.1', help='Host address of the REST-Server. Default 127.0.0.1')
parser.add_argument('--path', default='/control', help='Path of the JSON-RPC-Server (namespace). Default /control')
parser.add_argument('-f', '--functions', default='controlFunctions', help='Python module with control functions. Default controlFunctions')
parser.add_argument('--noreload', action='store_true', help='Do not automatically reload server after script changes')
parser.add_argument('-d', '--debug', action='store_true', help='Enable debug mode with debug output')

args = parser.parse_args()

# instead of 'import controlFunctions', load dynamically using arguments
importlib.import_module(args.functions)

jsonrpc = bottle_jsonrpc.register(args.path)

# curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:2102/control -d '{ "jsonrpc": "2.0", "method": "ping", "params": [], "id": 1}'
@jsonrpc
def ping():
    """Returns "pong"."""
    return "pong"

# curl -X POST -i -H "Content-type: application/json" -X POST http://localhost:2102/control -d '{ "jsonrpc": "2.0", "method": "print", "params": ["Wow"], "id": 1}'
def print_msg(msg):
    """Prints a message on the server."""
    print(msg)

jsonrpc.methods['print'] = print_msg

jsonrpc.methods['stop'] = quit

# http://stackoverflow.com/questions/4040620/is-it-possible-to-list-all-functions-in-a-module
functions = inspect.getmembers(sys.modules[args.functions], inspect.isfunction)
for function in functions:
    if function[0].startswith("control_"):
        jsonrpc.methods[function[0]] = function[1]

print("\nStart JSON-RPC server...")
print("http://" + args.host + ":" + str(args.port) + args.path)
li = "\n    - "
print("Functions:" + li + li.join(str(x) for x in sorted(jsonrpc.methods)))
print("Ctrl-C to stop")

try:
    # Run the server's main loop
    run(server='wsgiref', host=args.host, port=args.port, quiet=not args.debug, reloader=not args.noreload, debug=args.debug)
except KeyboardInterrupt:
    print("\nKeyboard interrupt received, exiting.")
    sys.exit(0)

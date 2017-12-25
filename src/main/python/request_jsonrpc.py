# -*- coding: utf-8 -*-
"""
Very minimal implementation of JSON-RPC requests.

Adapted from bottle_jsonrpc.py (https://github.com/olemb/bottle_jsonrpc).
Based on bottle_jsonrpc version 0.2.1 of Ole Martin Bjorndalen.

"""

from __future__ import division, unicode_literals, print_function, absolute_import, with_statement  # Ensure compatibility with Python 3
import sys
import traceback

__author__ = 'Roland Kurmann'
__email__ = 'roland dot kurmann at velasolaris dot com'
__url__ = 'http://velasolaris.com'
__license__ = 'MIT'
__version__ = '9.2'

def get_public_methods(obj):
    """Return a dictionary of all public callables in a namespace.

    This can be used for objects, classes and modules.
    """
    methods = {}

    for name in dir(obj):
        method = getattr(obj, name)
        if not name.startswith('_') and callable(method):
            methods[name] = method

    return methods


class NameSpace:
    def __init__(self, debug=False, obj=None, catchall=True):
        self.debug = debug
        self.methods = {}
        self.catchall = catchall

        if obj is not None:
            self.add_object(obj)

    def add_object(self, obj):
        """Adds all public methods of the object."""
        self.methods.update(get_public_methods(obj))

    def handle_rpc(self, request):
        try:
            name = request['method']
            func = self.methods[name]
            params = request.get('params', {})
            if params != None:
                result = func(*params)
            else:
                result = func()

            return {
                'jsonrpc': '2.0',  # Added by rkurmann for JSON-RPC 2.0 compliancy
                'id': request['id'],
                'result': result,
                # 'error': None,  # Removed by rkurmann for JSON-RPC 2.0 compliancy
            }
        except:
            if not self.catchall:
                raise
            traceback.print_exc(file=sys.stderr)
            response = {
                'id': request['id'],
                # 'result': None,  # Removed by rkurmann for JSON-RPC 2.0 compliancy
                'error': 'Internal server error',
            }
            if self.debug:
                response['traceback'] = traceback.format_exc()

            return response

    def __call__(self, func):
        """This is called when the mapper is used as a decorator."""
        self.methods[func.__name__] = func
        return func

register = NameSpace

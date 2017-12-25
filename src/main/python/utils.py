"""
Utils package
"""

from __future__ import division, unicode_literals, print_function, absolute_import, with_statement  # Ensure compatibility with Python 3

__author__ = 'Roland Kurmann'
__email__ = 'roland dot kurmann at velasolaris dot com'
__url__ = 'http://velasolaris.com'
__license__ = 'MIT'
__version__ = '9.1'

# Example:
# @static_vars(counter=0)
# def foo():
#     foo.counter += 1
#     print "Counter is %d" % foo.counter
# http://stackoverflow.com/questions/279561/what-is-the-python-equivalent-of-static-variables-inside-a-function
def static_vars(**kwargs):
    '''Annotation for static variables in functions. Similar to static in Java or persistent in Matlab.

    Works only for a single thread.
    '''
    def decorate(func):
        for k in kwargs:
            setattr(func, k, kwargs[k])
        return func
    return decorate

def indent(lines, padding="\t"):
    "Indent each line with padding"
    padding = padding
    return padding + ('\n' + padding).join(lines.split('\n'))

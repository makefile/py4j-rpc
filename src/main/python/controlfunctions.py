#encoding=utf-8
"""
Polysun plugin controller functions for SimpleRpcPluginController.

The function control() describes the interface.
The function control_flowrate() is a flowrate controller, see Example in Polysun.

The Polysun user manual describes the usage of plugin controllers.

"""

from __future__ import division, unicode_literals, print_function, absolute_import, with_statement  # Ensure compatibility with Python 3
from utils import static_vars

import numpy as np

__author__ = 'fyk'
__url__ = 'github.com/makefile'

def keras_test_numpy(param_dict):
    """
    py_arr, rows, cols
    参数是Jython传过来的一个一维数组，
    TODO 这里将其转成shape为(rows,cols)的numpy数组，交给keras处理
    打印numpy数组值
    """
    py_arr = param_dict['feature']
    rows = param_dict['rows']
    cols = param_dict['cols']
    #np_arr = np.zeros([rows,cols]) 
    np_arr = np.array(py_arr).reshape([rows,cols])  # dtype=np.float64

    # predict 每行预测一个结果
    pred = np_arr[:,-1] # 这里模拟成取最后一列，这会得到一个行向量
    # 返回预测的结果数组和个数
    return pred.tolist(), pred.shape[0]

@static_vars(lastDay=0)  # static_vars work only for a single thread
def control_flowrate(simulationTime, status, sensors, sensorsUsed, properties, propertiesStr, preRun, controlSignalsUsed, numLogValues, stage, fixedTimestep, verboseLevel, parameters):
    """ """
    pass

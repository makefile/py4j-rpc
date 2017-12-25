package rpc;

import net.minidev.json.JSONArray;

/**
 * Response object from remote function calls. Data transfer object (DTO).
 * 
 * @author rkurmann
 * @since Polysun 9.2
 *
 */
public class ControlFunctionResponse {
	
	public static final float[] EMPTY_FLOAT_ARRAY = new float[0];
	public static final int[] EMPTY_INT_ARRAY = new int[0];
	
	private float[] predictResult;

	public ControlFunctionResponse(float[] predictResult) {
		this.predictResult = predictResult;
	}

	public float[] getPredictResult() {
		return predictResult;
	}
	//	private int result_num;
	
	/**
	 * Constructor.
	 * @param controlSignals control signals array
	 * @param logValues log values array
	 * @param timepoints timepoints array
	 */
	/*ControlFunctionResponse(float[] controlSignals, float[] logValues, int[] timepoints) {
		this.controlSignals = controlSignals;
		this.logValues = logValues;
		this.timepoints = timepoints;
	}*/

	/**
	 * Default constructor creating empty arrays.
	 */
	/*ControlFunctionResponse() {
		this.controlSignals = EMPTY_FLOAT_ARRAY;
		this.logValues = EMPTY_FLOAT_ARRAY;
		this.timepoints = EMPTY_INT_ARRAY;
	}*/
	
	/**
	 * Returns the control signals array.
	 * @return Returns the control signals array
	 */
//	public float[] getControlSignals() {
//		return this.controlSignals;
//	}
	
	/**
	 * Sets the control signals array.
	 * @param controlSignals control signals array
	 */
//	public void setControlSignals(float[] controlSignals) {
//		this.controlSignals = controlSignals;
//	}
	/**
	 * Returns the log values array.
	 * @return Returns the log values array
	 */
//	public float[] getLogValues() {
//		return this.logValues;
//	}
	/**
	 * Sets the log values array.
	 * @param logValues log values array
	 */
//	public void setLogValues(float[] logValues) {
//		this.logValues = logValues;
//	}
	/**
	 * Returns the timepoints array.
	 * @return Returns the timepoints array
	 */
//	public int[] getTimepoints() {
//		return this.timepoints;
//	}
	/**
	 * Sets the timpoints array.
	 * @param timepoints timpoints array
	 */
//	public void setTimepoints(int[] timepoints) {
//		this.timepoints = timepoints;
//	}

	/**
	 * Converts Object[] or JSONArray to float[]. Java cannot do that on its
	 * own.
	 * 
	 * @param input
	 *            Object[] or JSONArray
	 * @return float array
	 */
	public static float[] convertObjectArrayToFloats(Object input) {
		if (input == null) {
			return null;
		}
		Object[] inputArray = null;
		if (input instanceof Object[]) {
			inputArray = (Object[]) input;
		} else if (input instanceof JSONArray) {
			inputArray = ((JSONArray) input).toArray();
		}
		float[] output = new float[inputArray.length];
		for (int i = 0; i < inputArray.length; i++) {
			Object val = inputArray[i];
			if (val instanceof Boolean) {
				output[i] = ((Boolean) val) ? 1 : 0;
			} else if (val instanceof Number) {
				output[i] = ((Number) val).floatValue();
			} else {
				output[i] = Float.NaN;
			}
		}
		return output;
	}

	/**
	 * Converts Object[] or JSONArray to int[]. Java cannot do that on its own.
	 * 
	 * @param input
	 *            Object[] or JSONArray
	 * @return int array
	 */
	public static int[] convertObjectToInts(Object input) {
		if (input == null) {
			return null;
		}
		Object[] inputArray = null;
		if (input instanceof Object[]) {
			inputArray = (Object[]) input;
		} else if (input instanceof JSONArray) {
			inputArray = ((JSONArray) input).toArray();
		}
		int[] output = new int[inputArray.length];
		for (int i = 0; i < inputArray.length; i++) {
			Object val = inputArray[i];
			if (val instanceof Boolean) {
				output[i] = ((Boolean) val) ? 1 : 0;
			} else if (val instanceof Number) {
				output[i] = ((Number) val).intValue();
			} else {
				output[i] = 0;
			}
		}
		return output;
	}
}

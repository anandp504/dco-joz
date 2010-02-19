package com.tumri.joz.index;

/**
 * The type of object that can be put into a range
 *
 * @param <Value>
 */
public interface IRangeValue<Value> {

	/**
	 * Returns the next in the series to this value
	 * Needed to make sure that the indexes can be built without overlapping
	 *
	 * @return
	 */
	public IRangeValue<Value> getNext();

	/**
	 * Get the previous in the series to this value
	 * Neened to make sure tha the indexes can be built without overlapping
	 *
	 * @return
	 */
	public IRangeValue<Value> getPrevious();

	/**
	 * Comparison of 2 range boundary condition
	 *
	 * @param k
	 * @return
	 */
	public boolean lessThanEqualTo(IRangeValue k);

	/**
	 * Comparison of 2 range boundary condition
	 *
	 * @param k
	 * @return
	 */
	public boolean greaterThanEqualTo(IRangeValue k);

	public Value getValue();

}

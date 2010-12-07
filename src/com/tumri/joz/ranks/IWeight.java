package com.tumri.joz.ranks;

/**
 * Created by IntelliJ IDEA.
 * User: snawathe
 * To change this template use File | Settings | File Templates.
 */
public interface IWeight<Value> {
	public double getWeight(Value v, double minWeight);

	public double getMinWeight();

	public int match(Value v);

	/**
	 * @return true if the match is mandatory, false otherwise
	 */
	public boolean mustMatch();

	public double getMaxWeight();

}
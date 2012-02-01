package fr.insarennes.fafdti;

public interface Criterion {

	/**
	 * Returns true if value1 is a better value than value 2 for this criterion
	 */
	public boolean better(double value1, double value2);

	/**
	 * Compute the value of the criterion for the given distribution
	 * vector.
	 * @param distributionVector The distribution vector for which
	 * we want to compute the criterion
	 * @return the value of the criterion for the given distribution
	 * vector
	 */
	public double compute(int[] distributionVector);
}

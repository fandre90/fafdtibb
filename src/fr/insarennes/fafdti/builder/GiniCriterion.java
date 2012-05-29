package fr.insarennes.fafdti.builder;

public class GiniCriterion extends Criterion {

	@Override
	public boolean better(double value1, double value2) {
		return value1 < value2;
	}

	@Override
	public double compute(int[] distributionVector) {
		double criterionValue = 0;
		//calcul de la somme des ni²
		double nbExamples = 0;
		for (int i = 0; i < distributionVector.length; i++)
			nbExamples += distributionVector[i];
		// calcul de la somme des ni²
		double N = 0;
		for (int i = 0; i < distributionVector.length; i++)
			N += Math.pow(distributionVector[i]/nbExamples, 2);
		criterionValue = 1 - N;
		return criterionValue;
	}

}

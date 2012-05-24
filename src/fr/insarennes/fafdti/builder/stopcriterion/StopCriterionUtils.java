package fr.insarennes.fafdti.builder.stopcriterion;
/**
 * Interface que doit implémenter toute classe souhaitant se servir de {@link StoppingCriterion}
 */
public interface StopCriterionUtils {
	public double getCurrentGain();
	public int getDepth();
	public int getMinExamples();
}

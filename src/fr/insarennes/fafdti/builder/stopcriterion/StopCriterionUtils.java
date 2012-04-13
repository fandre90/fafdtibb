/**
 * Interface que doit implémenter toute classe souhaitant se servir de {@link StoppingCriterion}
 */

package fr.insarennes.fafdti.builder.stopcriterion;

public interface StopCriterionUtils {
	public double getCurrentGain();
	public int getDepth();
	public int getMinExamples();
}

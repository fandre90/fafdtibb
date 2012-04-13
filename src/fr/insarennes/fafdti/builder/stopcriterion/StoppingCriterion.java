/**
 * Interface des critères d'arrêt
 */

package fr.insarennes.fafdti.builder.stopcriterion;

public interface StoppingCriterion {
	public boolean mustStop(StopCriterionUtils node);
}

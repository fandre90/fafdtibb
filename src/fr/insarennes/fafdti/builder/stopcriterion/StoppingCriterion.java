package fr.insarennes.fafdti.builder.stopcriterion;
/**
 * Interface des critères d'arrêt
 */
public interface StoppingCriterion {
	public boolean mustStop(StopCriterionUtils node);
}

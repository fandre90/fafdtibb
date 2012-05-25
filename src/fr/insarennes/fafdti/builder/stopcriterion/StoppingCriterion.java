package fr.insarennes.fafdti.builder.stopcriterion;
import fr.insarennes.fafdti.IHadoopConfStockable;

/**
 * Interface des critères d'arrêt
 */
public interface StoppingCriterion extends IHadoopConfStockable{
	public boolean mustStop(StopCriterionUtils node);
}
